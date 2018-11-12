// ----------------------------
// Testbench for parity checker
// Zach Zundel
// CS 5750
// ----------------------------

module testbench_dr (bit0, bit1, parity0, parity1);
  input wire parity0, parity1;
  output reg bit0, bit1;

  reg next, state;
  
  initial begin
    bit0 = 1'b0;
    bit1 = 1'b0;
    parity0 = 1'b0;
    parity1 = 1'b0;
    state = 1'b0;
    next = 1'b0;
  end
  
  always begin
    next = $random%2;

    if (next == 1'b0) begin
      assign bit0 = 1'b1;
      wait (bit0 == 1'b1) #5;
    end else begin
      assign bit1 = 1'b1;
      wait (bit1 == 1'b1) #5;
    end

    wait (parity0 == 1'b1 || parity1 == 1'b1) #5;

    if (bit0 == 1'b1) begin
      assign bit0 = 1'b0;
      wait (bit0 == 1'b0) #5;
    end else begin  
      assign bit1 = 1'b0;
      wait (bit1 == 1'b0) #5;
    end

    wait (parity0 == 1'b0 && parity1 == 1'b0) #5;
  end
  
  always begin
    wait (bit0 == 1'b1 || bit1 == 1'b1) #5;

    if (bit0 == 1'b1)
      state = ~state;
    
    if (state == 1'b0)  begin
      assign parity0 = 1'b1;
      if (parity0 != 1'b1) wait (parity0 == 1'b1) #5;
    end else begin
      assign parity1 = 1'b1;
      if (parity1 != 1'b1) wait (parity1 == 1'b1) #5;
    end

    wait (bit0 == 1'b0 && bit1 == 1'b0) #5;

    assign parity0 = 1'b0;
    if (parity0 != 1'b0) wait (parity0 == 1'b0) #5;

    assign parity1 = 1'b0;
    if (parity1 != 1'b0) wait (parity1 == 1'b0) #5;
  end
endmodule