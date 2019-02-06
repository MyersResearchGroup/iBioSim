// ----------------------------
// This module contains both the instantiation and the testbench for the
// counter_imp.v circuit.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------


module counter_testbench ();

	wire a0, a1, b0, b1, ack;
	reg req;

	initial begin
		req = 1'b0;
	end

	counter_imp counter_instance(
	.a0(a0),
	.a1(a1),
	.b0(b0),
	.b1(b1),
	.req(req),
	.ack(ack)
	);

	always begin
		#5 req = 1'b1;
		wait(ack == 1'b1) #5;
		#5 req = 1'b0;
		wait(ack != 1'b1) #5;
	end

endmodule
