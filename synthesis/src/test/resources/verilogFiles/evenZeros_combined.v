// ----------------------------
// This module contains both the design and testbench  
// that will output a 1 when the design receives an 
// even number of 0 input bits. Otherwise, a 0 is 
// outputted from the design. 
//
// Note: This version has:
// - Syntax works and compiles with Verilog2LPN compiler and ATACS
// - async. design follows dual rail encoding that maps 
//   input to internal states then internal states to output
// - 
//
// author: Tramy Nguyen
// ----------------------------


module evenzeros (bit0, bit1, parity0, parity1);
  output reg parity0, parity1;
  input wire next, bit0, bit1;

  reg state;
  
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
      #5 bit0 = 1'b1;
    end else begin
      #5 bit1 = 1'b1;
    
    end

    wait (parity0 == 1'b1 || parity1 == 1'b1) #5;

    if (bit0 == 1'b1) begin
      #5 bit0 = 1'b0;
    end else begin  
      #5 bit1 = 1'b0;
    end

    wait (parity0 == 1'b0 && parity1 == 1'b0) #5;
  end
  
  always begin
    wait (bit0 == 1'b1 || bit1 == 1'b1);
     
    if ((state == 1'b0 && bit1 == 1'b1) || (state == 1'b1 && bit0 == 1'b1))  begin
      #5 parity1 = 1'b1;
    end else begin //state is 1
      #5 parity0 = 1'b1;
    end
    
    if (parity1 == 1'b1) begin
      #5 state = 1'b0;
    end else begin
      #5 state = 1'b1;
    end
    wait ((parity0 == 1'b1 && state == 1'b1) || (parity1 == 1'b1 && state == 1'b0));
 
     
    wait (bit0 == 1'b0 && bit1 == 1'b0) #5; //reset
    if (parity0 == 1'b1) begin
       #5 parity0 = 1'b0;
    end else begin 
       #5 parity1 = 1'b0;
    end 
  end
endmodule

