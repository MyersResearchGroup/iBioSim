// ----------------------------
// This module contains the evenzeroes design
// that will output a 1 when the design receives an
// even number of 0 input bits. Otherwise, a 0 is
// outputted from the design.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module evenzeroes_imp(bit0, bit1, parity0, parity1);

  input bit0, bit1;
	output reg parity0, parity1;
	reg state;

  initial begin
    parity0 = 1'b0;
    parity1 = 1'b0;
    state = 1'b0;
  end

	always begin
    wait (bit0 || bit1) #5;

    if ((!state && bit1) || (state && bit0))  begin
      #5 parity1 = 1'b1;
    end else begin //state is 1
      #5 parity0 = 1'b1;
    end

    if (parity1) begin
      state = 1'b0;
    end else begin
      state = 1'b1;
    end
    wait ((parity0 && state) || (parity1 && !state)) #5;

    wait (!bit0 && !bit1) #5; //reset
    if (parity0) begin
       #5 parity0 = 1'b0;
    end else begin
       #5 parity1 = 1'b0;
    end
  end
endmodule
