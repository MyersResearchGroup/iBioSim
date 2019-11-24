// ----------------------------
// testing continuous assignments
//
// author: Tramy Nguyen
// ----------------------------

module contAssign6(a, b, y);

  input a;
  input b;
  output y;

assign y = ~(a | b);
endmodule
