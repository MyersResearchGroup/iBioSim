// ----------------------------
// This module contains both the instantiation and the testbench for the
// srlatch_imp.v circuit.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows bundle data encoding 
// author: Tramy Nguyen
// ----------------------------


module srlatch_testbench ();

	wire q, ack;
	reg s, r, next;

	initial begin
		s = 1'b0;
		r = 1'b0;
		next = 1'b0;
	end

	srlatch_imp sl_instance(
	.s(s),
	.r(r),
	.q(q),
	.ack(ack)
	);

	always begin
		#5 next = $random%2;

		if(next == 1'b0) begin
			#5 r = 1'b1;
		end else begin
			#5 s = 1'b1;
		end

		wait(ack == 1'b1) #5;

		if(s == 1'b1) begin
			#5 s = 1'b0;
		end else begin
			#5 r = 1'b0;
		end

		wait(ack != 1'b1) #5;
	end

endmodule
