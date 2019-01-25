module srlatchDesign(s, r, q, qnot);

  input s, r;
  output q, qnot;

  assign q = ~(r | qnot);
  assign qnot = ~(s | q);

endmodule
