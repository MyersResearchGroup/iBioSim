
// ----------------------------
// testing continuous assignments
//
// author: Tramy Nguyen
// ----------------------------

module contAssign(bit1, bit0, parity1, parity0);

  input bit1;
  input bit0;
  output parity1;
  output parity0;
  wire t;

assign t = ~parity0;
endmodule
