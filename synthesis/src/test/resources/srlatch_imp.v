// ----------------------------
// This module impelements an srlatch.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows bundle data encoding
//
// author: Tramy Nguyen
// ----------------------------

module srlatch_imp (s, r, q, ack);

	output reg q, ack;
	input wire s, r;

	initial begin
		q = 1'b0;
		ack = 1'b0;
	end

	always begin
		wait(s == 1'b1 || r == 1'b1) #5;

		if(s == 1'b1) begin
			#5 q = 1'b1;
			#5 ack = 1'b1;
		end else begin
			#5 q = 1'b0;
			#5 ack = 1'b1;
		end

		wait(s != 1'b1 && r != 1'b1) #5;

		#5 ack = 1'b0;
	end

endmodule
