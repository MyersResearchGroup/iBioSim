// ----------------------------
// create NAND spec
//
// author: Tramy Nguyen
// ----------------------------

module Nand(a, b, y);

  input a;
  input b;
  output y;

assign y = ~(a & b);
endmodule
