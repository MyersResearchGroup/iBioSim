// ----------------------------
// This module contains the multiple of three design
// that will output a 1 when the design receives a
// multiple number of 3 input bits. Otherwise, a 0 is
// outputted from the design.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module multthree_imp (in0, in1, parity0, parity1);
  input wire in0, in1;
  output reg parity0, parity1;
  reg state0, state1, temp;

  initial begin
    parity0 = 1'b0;
    parity1 = 1'b0;
    state0 = 1'b0;
    state1 = 1'b0;
    temp = 1'b0;
  end

  always begin
    wait (in0 == 1'b1 || in1 == 1'b1 ) #5;

    // input to output
    if((in0 == 1'b1 && state1 != 1'b1 && state0 != 1'b1) ||
    (in1 == 1'b1 && state1 != 1'b1 && state0 == 1'b1))begin
        #5 parity1 = 1'b1;
    end
    else begin
      if (state1 == 1'b1) begin
        temp = 1'b1;
      end
      else begin
        temp = 1'b0;
      end
      #5 parity0 = 1'b1;
    end

    // output to next state
    if(parity0 == 1'b1) begin
      if (state1 != 1'b1 && state0 != 1'b1) begin
        state0 = 1'b1;
      end
      else if (state1 != 1'b1 && state0 == 1'b1) begin
        state1 = 1'b1;
      end
      else if (state1 == 1'b1 && state0 == 1'b1 && in0 == 1'b1) begin
        state1 = 1'b0;
      end
    end
    else if(parity1 == 1'b1 && state1 != 1'b1 && state0 == 1'b1) begin
      state0 = 1'b0;
    end

    wait((parity1 == 1'b1 && state0 != 1'b1  && state1 != 1'b1 ) || (parity0 == 1'b1 && state1 != 1'b1  && state0 == 1'b1) || (parity0 == 1'b1 && state1 == 1'b1 && state0 == 1'b1)) #5;

    //reset
    wait (in0 != 1'b1 && in1 != 1'b1) #5;
    if (parity0 == 1'b1) begin
       #5 parity0 = 1'b0;
    end
    else begin
       #5 parity1 = 1'b0;
    end

  end
endmodule
