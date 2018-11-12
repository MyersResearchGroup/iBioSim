// ----------------------------
// This module contains both the instantiation and the testbench for the
// lfsr_imp.v circuit.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------


module lfsr_testbench ();

	wire ack, a0, a1, b0, b1, c0, c1;
	reg req;

	initial begin
		req = 1'b0;
	end

	lfsr_imp lfsr_instance(
	.req(req),
	.ack(ack),
	.a0(a0),
	.a1(a1),
	.b0(b0),
	.b1(b1),
	.c0(c0),
	.c1(c1)
	);

	always begin
		#5 req = 1'b1;
		wait(ack == 1'b1) #5;
		#5 req = 1'b0;
		wait(ack != 1'b1) #5;
	end

endmodule
