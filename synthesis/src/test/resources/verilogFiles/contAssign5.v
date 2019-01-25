// ----------------------------
// testing continuous assignments
//
// author: Tramy Nguyen
// ----------------------------

module contAssign5(s, r, q, qnot);

  input s, r;
  output q, qnot;

assign q = ~(r | qnot);
assign qnot = ~(s | q);
endmodule
