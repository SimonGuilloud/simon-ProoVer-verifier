% 11 pigeons in 10 holes must collide, so this step is genuinely valid; but propositional
% pigeonhole is exponentially hard for resolution, so Vampire cannot prove it within the
% 3s budget and the verifier rejects the (correct) step as too complicated (Timeout).
fof(map1, axiom, (f(p1) = h1 | f(p1) = h2 | f(p1) = h3 | f(p1) = h4 | f(p1) = h5 | f(p1) = h6 | f(p1) = h7 | f(p1) = h8 | f(p1) = h9 | f(p1) = h10), file('problem.p', map1)).
fof(map2, axiom, (f(p2) = h1 | f(p2) = h2 | f(p2) = h3 | f(p2) = h4 | f(p2) = h5 | f(p2) = h6 | f(p2) = h7 | f(p2) = h8 | f(p2) = h9 | f(p2) = h10), file('problem.p', map2)).
fof(map3, axiom, (f(p3) = h1 | f(p3) = h2 | f(p3) = h3 | f(p3) = h4 | f(p3) = h5 | f(p3) = h6 | f(p3) = h7 | f(p3) = h8 | f(p3) = h9 | f(p3) = h10), file('problem.p', map3)).
fof(map4, axiom, (f(p4) = h1 | f(p4) = h2 | f(p4) = h3 | f(p4) = h4 | f(p4) = h5 | f(p4) = h6 | f(p4) = h7 | f(p4) = h8 | f(p4) = h9 | f(p4) = h10), file('problem.p', map4)).
fof(map5, axiom, (f(p5) = h1 | f(p5) = h2 | f(p5) = h3 | f(p5) = h4 | f(p5) = h5 | f(p5) = h6 | f(p5) = h7 | f(p5) = h8 | f(p5) = h9 | f(p5) = h10), file('problem.p', map5)).
fof(map6, axiom, (f(p6) = h1 | f(p6) = h2 | f(p6) = h3 | f(p6) = h4 | f(p6) = h5 | f(p6) = h6 | f(p6) = h7 | f(p6) = h8 | f(p6) = h9 | f(p6) = h10), file('problem.p', map6)).
fof(map7, axiom, (f(p7) = h1 | f(p7) = h2 | f(p7) = h3 | f(p7) = h4 | f(p7) = h5 | f(p7) = h6 | f(p7) = h7 | f(p7) = h8 | f(p7) = h9 | f(p7) = h10), file('problem.p', map7)).
fof(map8, axiom, (f(p8) = h1 | f(p8) = h2 | f(p8) = h3 | f(p8) = h4 | f(p8) = h5 | f(p8) = h6 | f(p8) = h7 | f(p8) = h8 | f(p8) = h9 | f(p8) = h10), file('problem.p', map8)).
fof(map9, axiom, (f(p9) = h1 | f(p9) = h2 | f(p9) = h3 | f(p9) = h4 | f(p9) = h5 | f(p9) = h6 | f(p9) = h7 | f(p9) = h8 | f(p9) = h9 | f(p9) = h10), file('problem.p', map9)).
fof(map10, axiom, (f(p10) = h1 | f(p10) = h2 | f(p10) = h3 | f(p10) = h4 | f(p10) = h5 | f(p10) = h6 | f(p10) = h7 | f(p10) = h8 | f(p10) = h9 | f(p10) = h10), file('problem.p', map10)).
fof(map11, axiom, (f(p11) = h1 | f(p11) = h2 | f(p11) = h3 | f(p11) = h4 | f(p11) = h5 | f(p11) = h6 | f(p11) = h7 | f(p11) = h8 | f(p11) = h9 | f(p11) = h10), file('problem.p', map11)).
fof(hd1_2, axiom, h1 != h2, file('problem.p', hd1_2)).
fof(hd1_3, axiom, h1 != h3, file('problem.p', hd1_3)).
fof(hd1_4, axiom, h1 != h4, file('problem.p', hd1_4)).
fof(hd1_5, axiom, h1 != h5, file('problem.p', hd1_5)).
fof(hd1_6, axiom, h1 != h6, file('problem.p', hd1_6)).
fof(hd1_7, axiom, h1 != h7, file('problem.p', hd1_7)).
fof(hd1_8, axiom, h1 != h8, file('problem.p', hd1_8)).
fof(hd1_9, axiom, h1 != h9, file('problem.p', hd1_9)).
fof(hd1_10, axiom, h1 != h10, file('problem.p', hd1_10)).
fof(hd2_3, axiom, h2 != h3, file('problem.p', hd2_3)).
fof(hd2_4, axiom, h2 != h4, file('problem.p', hd2_4)).
fof(hd2_5, axiom, h2 != h5, file('problem.p', hd2_5)).
fof(hd2_6, axiom, h2 != h6, file('problem.p', hd2_6)).
fof(hd2_7, axiom, h2 != h7, file('problem.p', hd2_7)).
fof(hd2_8, axiom, h2 != h8, file('problem.p', hd2_8)).
fof(hd2_9, axiom, h2 != h9, file('problem.p', hd2_9)).
fof(hd2_10, axiom, h2 != h10, file('problem.p', hd2_10)).
fof(hd3_4, axiom, h3 != h4, file('problem.p', hd3_4)).
fof(hd3_5, axiom, h3 != h5, file('problem.p', hd3_5)).
fof(hd3_6, axiom, h3 != h6, file('problem.p', hd3_6)).
fof(hd3_7, axiom, h3 != h7, file('problem.p', hd3_7)).
fof(hd3_8, axiom, h3 != h8, file('problem.p', hd3_8)).
fof(hd3_9, axiom, h3 != h9, file('problem.p', hd3_9)).
fof(hd3_10, axiom, h3 != h10, file('problem.p', hd3_10)).
fof(hd4_5, axiom, h4 != h5, file('problem.p', hd4_5)).
fof(hd4_6, axiom, h4 != h6, file('problem.p', hd4_6)).
fof(hd4_7, axiom, h4 != h7, file('problem.p', hd4_7)).
fof(hd4_8, axiom, h4 != h8, file('problem.p', hd4_8)).
fof(hd4_9, axiom, h4 != h9, file('problem.p', hd4_9)).
fof(hd4_10, axiom, h4 != h10, file('problem.p', hd4_10)).
fof(hd5_6, axiom, h5 != h6, file('problem.p', hd5_6)).
fof(hd5_7, axiom, h5 != h7, file('problem.p', hd5_7)).
fof(hd5_8, axiom, h5 != h8, file('problem.p', hd5_8)).
fof(hd5_9, axiom, h5 != h9, file('problem.p', hd5_9)).
fof(hd5_10, axiom, h5 != h10, file('problem.p', hd5_10)).
fof(hd6_7, axiom, h6 != h7, file('problem.p', hd6_7)).
fof(hd6_8, axiom, h6 != h8, file('problem.p', hd6_8)).
fof(hd6_9, axiom, h6 != h9, file('problem.p', hd6_9)).
fof(hd6_10, axiom, h6 != h10, file('problem.p', hd6_10)).
fof(hd7_8, axiom, h7 != h8, file('problem.p', hd7_8)).
fof(hd7_9, axiom, h7 != h9, file('problem.p', hd7_9)).
fof(hd7_10, axiom, h7 != h10, file('problem.p', hd7_10)).
fof(hd8_9, axiom, h8 != h9, file('problem.p', hd8_9)).
fof(hd8_10, axiom, h8 != h10, file('problem.p', hd8_10)).
fof(hd9_10, axiom, h9 != h10, file('problem.p', hd9_10)).
fof(pd1_2, axiom, p1 != p2, file('problem.p', pd1_2)).
fof(pd1_3, axiom, p1 != p3, file('problem.p', pd1_3)).
fof(pd1_4, axiom, p1 != p4, file('problem.p', pd1_4)).
fof(pd1_5, axiom, p1 != p5, file('problem.p', pd1_5)).
fof(pd1_6, axiom, p1 != p6, file('problem.p', pd1_6)).
fof(pd1_7, axiom, p1 != p7, file('problem.p', pd1_7)).
fof(pd1_8, axiom, p1 != p8, file('problem.p', pd1_8)).
fof(pd1_9, axiom, p1 != p9, file('problem.p', pd1_9)).
fof(pd1_10, axiom, p1 != p10, file('problem.p', pd1_10)).
fof(pd1_11, axiom, p1 != p11, file('problem.p', pd1_11)).
fof(pd2_3, axiom, p2 != p3, file('problem.p', pd2_3)).
fof(pd2_4, axiom, p2 != p4, file('problem.p', pd2_4)).
fof(pd2_5, axiom, p2 != p5, file('problem.p', pd2_5)).
fof(pd2_6, axiom, p2 != p6, file('problem.p', pd2_6)).
fof(pd2_7, axiom, p2 != p7, file('problem.p', pd2_7)).
fof(pd2_8, axiom, p2 != p8, file('problem.p', pd2_8)).
fof(pd2_9, axiom, p2 != p9, file('problem.p', pd2_9)).
fof(pd2_10, axiom, p2 != p10, file('problem.p', pd2_10)).
fof(pd2_11, axiom, p2 != p11, file('problem.p', pd2_11)).
fof(pd3_4, axiom, p3 != p4, file('problem.p', pd3_4)).
fof(pd3_5, axiom, p3 != p5, file('problem.p', pd3_5)).
fof(pd3_6, axiom, p3 != p6, file('problem.p', pd3_6)).
fof(pd3_7, axiom, p3 != p7, file('problem.p', pd3_7)).
fof(pd3_8, axiom, p3 != p8, file('problem.p', pd3_8)).
fof(pd3_9, axiom, p3 != p9, file('problem.p', pd3_9)).
fof(pd3_10, axiom, p3 != p10, file('problem.p', pd3_10)).
fof(pd3_11, axiom, p3 != p11, file('problem.p', pd3_11)).
fof(pd4_5, axiom, p4 != p5, file('problem.p', pd4_5)).
fof(pd4_6, axiom, p4 != p6, file('problem.p', pd4_6)).
fof(pd4_7, axiom, p4 != p7, file('problem.p', pd4_7)).
fof(pd4_8, axiom, p4 != p8, file('problem.p', pd4_8)).
fof(pd4_9, axiom, p4 != p9, file('problem.p', pd4_9)).
fof(pd4_10, axiom, p4 != p10, file('problem.p', pd4_10)).
fof(pd4_11, axiom, p4 != p11, file('problem.p', pd4_11)).
fof(pd5_6, axiom, p5 != p6, file('problem.p', pd5_6)).
fof(pd5_7, axiom, p5 != p7, file('problem.p', pd5_7)).
fof(pd5_8, axiom, p5 != p8, file('problem.p', pd5_8)).
fof(pd5_9, axiom, p5 != p9, file('problem.p', pd5_9)).
fof(pd5_10, axiom, p5 != p10, file('problem.p', pd5_10)).
fof(pd5_11, axiom, p5 != p11, file('problem.p', pd5_11)).
fof(pd6_7, axiom, p6 != p7, file('problem.p', pd6_7)).
fof(pd6_8, axiom, p6 != p8, file('problem.p', pd6_8)).
fof(pd6_9, axiom, p6 != p9, file('problem.p', pd6_9)).
fof(pd6_10, axiom, p6 != p10, file('problem.p', pd6_10)).
fof(pd6_11, axiom, p6 != p11, file('problem.p', pd6_11)).
fof(pd7_8, axiom, p7 != p8, file('problem.p', pd7_8)).
fof(pd7_9, axiom, p7 != p9, file('problem.p', pd7_9)).
fof(pd7_10, axiom, p7 != p10, file('problem.p', pd7_10)).
fof(pd7_11, axiom, p7 != p11, file('problem.p', pd7_11)).
fof(pd8_9, axiom, p8 != p9, file('problem.p', pd8_9)).
fof(pd8_10, axiom, p8 != p10, file('problem.p', pd8_10)).
fof(pd8_11, axiom, p8 != p11, file('problem.p', pd8_11)).
fof(pd9_10, axiom, p9 != p10, file('problem.p', pd9_10)).
fof(pd9_11, axiom, p9 != p11, file('problem.p', pd9_11)).
fof(pd10_11, axiom, p10 != p11, file('problem.p', pd10_11)).
fof(c, conjecture, ?[X,Y]: (X != Y & f(X) = f(Y)), file('problem.p', c)).
fof(s1, plain, ?[X,Y]: (X != Y & f(X) = f(Y)), inference(php, [status(thm)], [map1, map2, map3, map4, map5, map6, map7, map8, map9, map10, map11, hd1_2, hd1_3, hd1_4, hd1_5, hd1_6, hd1_7, hd1_8, hd1_9, hd1_10, hd2_3, hd2_4, hd2_5, hd2_6, hd2_7, hd2_8, hd2_9, hd2_10, hd3_4, hd3_5, hd3_6, hd3_7, hd3_8, hd3_9, hd3_10, hd4_5, hd4_6, hd4_7, hd4_8, hd4_9, hd4_10, hd5_6, hd5_7, hd5_8, hd5_9, hd5_10, hd6_7, hd6_8, hd6_9, hd6_10, hd7_8, hd7_9, hd7_10, hd8_9, hd8_10, hd9_10, pd1_2, pd1_3, pd1_4, pd1_5, pd1_6, pd1_7, pd1_8, pd1_9, pd1_10, pd1_11, pd2_3, pd2_4, pd2_5, pd2_6, pd2_7, pd2_8, pd2_9, pd2_10, pd2_11, pd3_4, pd3_5, pd3_6, pd3_7, pd3_8, pd3_9, pd3_10, pd3_11, pd4_5, pd4_6, pd4_7, pd4_8, pd4_9, pd4_10, pd4_11, pd5_6, pd5_7, pd5_8, pd5_9, pd5_10, pd5_11, pd6_7, pd6_8, pd6_9, pd6_10, pd6_11, pd7_8, pd7_9, pd7_10, pd7_11, pd8_9, pd8_10, pd8_11, pd9_10, pd9_11, pd10_11])).
