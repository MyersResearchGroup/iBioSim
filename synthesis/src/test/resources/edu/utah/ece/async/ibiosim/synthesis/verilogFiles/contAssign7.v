module contAssign7(a, b, c, d);

  input a, b, c;
  output d;

assign d = ~(a | b | c);
endmodule
