# System Requirements

## Design and Requirements
* Systems must be fully automatic, i.e., all command line switches have to be the same for all problems in each division.
* Systems' performances must be reproducible by running the system again.
* Systems can use an internal trusted prover or check every sep --- but you have to figure ou the steps that can be used by yourself :)
* The check must check that the proof correspond to the problem file
* Executable must include external tools
* You can use an external ATP for verify non-specified proof steps, but it's your responsibility to trust it.

## Output
* Exactly one SZS status line per problem:
  * `%SZS status Verified`       --- the proof is valid
  * `%SZS status FailedVerified` --- the proof is invalid
  * `%SZS status NotVerified`    --- the checker could not decide
* Optional clarifications may follow on the same line after a colon.

## System Delivery

## System Execution
* Axiom and conjecture in a separated file
