# `checkSkolemization` — design & implementation plan

A `skolemize` step (`Step.Skolemization(name, formula, status, newSymbols, binding, parents)`,
dispatched when role is `plain` and rule is `skolemize`) records an
**equisatisfiability** transformation: an existentially quantified variable is
replaced by a fresh Skolem term applied to the universals it depends on.

The soundness-critical direction for a refutation is **parent SAT ⟹ child SAT**
(equivalently child UNSAT ⟹ parent UNSAT). That is satisfiability-*preservation*,
which a refutation prover cannot certify, so — unlike `checkPlainInferenceStep` —
**this check uses no prover**. Soundness rests entirely on syntactic side
conditions: a *fresh* Skolem symbol, *correct dependencies*, and a *correct
capture-avoiding substitution*.

## Properties checked

### A. Well-formedness
1. `status == esa` (we **reject** `status(thm)`, the choice-axiom variant).
2. Exactly one parent (skolemization is unary), which resolves in `byName`.
3. `binding` present (the `skolemize(V, T)` record).
4. `T = f(x1,…,xn)` with every argument a variable; `f` (its head) ∈ `newSymbols`.

### B. Freshness of the Skolem symbol `f` — ⚠ soundness-critical
Two conditions, and these two suffice:
5a. `f` does not occur in the **problem** (axioms + conjecture).
5b. `f` is introduced by **no other** skolemization step (`new_symbols` uniqueness).

**Why these two are enough.** Classify every occurrence of `f`. By 5a it is not in
a leaf (so not in `negated_conjecture = ¬conjecture` either). By 5b it is minted
by exactly this step. By the *Lemma* below, every other occurrence is either
downstream of this step (legitimately consuming the Skolem definition) or — by
each `plain` step's own consequence check — *valid for all interpretations of `f`*.

> **Lemma.** Any step mentioning `f` that is not downstream of this step holds in
> all interpretations of `f`. (Induction on topological order: a leaf would
> violate 5a; a second introduction would violate 5b; a `plain` step `g` has
> `parents ⊨ g`, and every `f`-mentioning parent is either downstream — making `g`
> downstream — or, by induction, valid-for-all-`f`, so `parents ⊨ ∀f. g`.)

So a model is built by walking the proof in topological order while leaving `f`
**uninterpreted** until this step: every earlier `f`-occurrence holds regardless,
and here `f` is set to the witness (the parent's existential has one). Hence
parent SAT ⟹ child SAT. An *ancestor* clause is therefore not only redundant but
would wrongly reject a valid-for-all-`f` use placed upstream.

### C. The transformation is a correct skolemization — ⚠ soundness-critical
Locate in the parent the **effectively-existential** binder of `V`, tracking
polarity (`Pos`/`Neg`/`Both`) and the governing (effectively-universal) variables
in scope:

| node | polarity / scope rule |
| --- | --- |
| `Not(g)` | flip polarity |
| `And/Or(a,b)` | same polarity, both |
| `Implies(a,b)` | flip on `a`, keep on `b` |
| `Iff(a,b)` | both subformulas become `Both` |
| `Forall(w,b)` | `Pos`⇒ governing universal; `Neg`⇒ effectively existential; `Both`⇒ ambiguous |
| `Exists(w,b)` | dual of `Forall` |

6. The binder of `V` must be effectively existential and of **well-defined
   polarity** (not under `<=>`). A positive `∀V` / negative `∃V` / `Both` is rejected.
7. **Dependency** (let `gov` = governing universals at the cut, `ψ` = the binder's
   body): every `arg` of `T` must be in `gov` (no out-of-scope/foreign variable),
   and every `u ∈ gov` with `u ∈ freeVars(ψ)` must appear in `T`'s arguments
   (no dropped dependency). Mini-scoping (omitting a `u` that `ψ` does not mention)
   is allowed.
8. **Substitution**: the parent with `Q V. ψ` replaced by `subst(ψ, V, T)`
   (capture-avoiding) must be α-equivalent to the step's `formula`.

Why no prover: the prover-checkable direction (`child ⊨ parent`) is the "free"
one; the critical direction is satisfiability-preservation, certified only by
B + C above.

## Implementation

Pure helpers in `object Verifier` (beside `alphaEquivalent`/`deBruijn`):
- `freeVars(Formula)`, `termVars(Term)` — dependency check.
- `symbolsOf(Formula)` — all function + predicate names, for freshness.
- `subst(Formula, v, Term)` — capture-avoiding: stops at binders re-binding `v`,
  α-renames inner binders whose name ∈ `termVars(t)` before descending.
- `enum Pol { Pos, Neg, Both }` with `flip`.
- `skolemReplace(parent, v, t): Either[String, (Formula, Set[String], Set[String])]`
  — the locate-and-rewrite descent; on success returns the rebuilt child, the
  governing set `gov`, and `freeVars(ψ)` at the cut.

In `Run`:
- `skolemIntroducers: Map[String, List[String]]` — symbol → introducing steps,
  for the uniqueness part of freshness; computed once and passed into the loop.
- `isFresh(symbol, stepName, introducers)` — the two-condition check (5a + 5b).
- `checkSkolemization(step, byName, introducers)` — matches `Step.Skolemization`,
  runs A → B → C in order, first failure reports an `error(...)` and returns.

Wired into the per-step loop in `run` after `checkPlainInferenceStep`.

## Test corpus (`tests/skolemization/{correct,wrong}/`)

Auto-run by `ProofCorpusTest` (`correct` ⇒ Verified, `wrong` ⇒ FailedVerified,
optional `.expected` substrings).

**correct/**
- `prenex` — `![X]:?[Y]:p(X,Y)` ⟶ `![X]:p(X,sK0(X))`
- `chain` — two nested skolems (example3 bride+groom)
- `under_conjunction` — `![X]:(q(X) & ?[Y]:p(X,Y))` ⟶ `![X]:(q(X) & p(X,sK0(X)))`
- `negative_universal` — `~![Y]:p(Y)` ⟶ `~p(sK0)` (∀ in negative position; nullary)
- `independent_existential` — `![X]:?[Y]:p(Y)` ⟶ `![X]:p(sK0)` (mini-scope, `X` omitted)
- `parallel_reuse` — the Skolem constant is reused (valid-for-all-`f`) in a
  *parallel* branch (`instantiate` an axiom at `sK0`), outside the introducer's
  descendants; accepted under the two-condition freshness

**wrong/**
- `reused_symbol` — `sK0` already used (freshness)
- `missing_dependency` — `![X]:?[Y]:p(X,Y)` ⟶ `![X]:p(X,sK0)` (drops `X`)
- `altered_body` — substitution changes a predicate
- `status_thm` — valid shape but `status(thm)`
- `positive_universal` — `skolemize(X, sK0)` on a positive `![X]`
- `ambiguous_polarity` — existential under `<=>`
- `foreign_var` — `skolemize(Y, sK0(Z))`, `Z` out of scope
- `bad_new_symbols` — term head not declared in `new_symbols`
