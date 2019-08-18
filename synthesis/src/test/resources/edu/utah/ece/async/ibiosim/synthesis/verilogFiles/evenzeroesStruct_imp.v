module evenzeroesStruct_imp (bit1, bit0, parity1, parity0);

  input bit1;
  input bit0;
  output reg parity1;
  output reg parity0;
  reg ez_instance__state;

  initial begin
    parity0 = 1'b0;
    parity1 = 1'b0;
    ez_instance__state = 1'b0;
  end

  always begin
    wait(bit0 == 1'b1 || bit1 == 1'b1);
    #1 parity1 = (bit1 & ~ez_instance__state) | (bit0 & ~parity0 & ez_instance__state) | (bit0 & parity1);
    parity0 = (bit1 & ez_instance__state) | (bit0 & parity0) | (bit0 & ~parity1 & ~ez_instance__state);
    ez_instance__state = (parity0) | (~parity1 & ez_instance__state);

  end

endmodule
