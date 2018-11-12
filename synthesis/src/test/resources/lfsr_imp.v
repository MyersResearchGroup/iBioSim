// ----------------------------
// This module impelements a linear feedback shift register (lfsr).
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module lfsr_imp (req, ack, a0, a1, b0, b1, c0, c1);
	input req;
	output reg ack, a0, a1, b0, b1, c0, c1;
	reg feedback, state0, state1;

	initial begin
		a0 = 1'b0;
		b0 = 1'b0;
		c0 = 1'b0;
		a1 = 1'b0;
		b1 = 1'b0;
		c1 = 1'b0;
		ack = 1'b0;
		feedback = 1'b0;
		state0 = 1'b0;
		state1 = 1'b0;
	end

	always begin
		// input to output
		wait(req == 1'b1) #5;

		if(state0 != 1'b1 && state1 != 1'b1 && feedback != 1'b1) begin
			#5 a0 = 1'b1;
			#5 b0 = 1'b1;
			#5 c0 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 != 1'b1 && state1 != 1'b1 && feedback == 1'b1) begin
			#5 a0 = 1'b1;
			#5 b0 = 1'b1;
			#5 c1 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 != 1'b1 && state1 == 1'b1 && feedback != 1'b1) begin
			#5 a0 = 1'b1;
			#5 b1 = 1'b1;
			#5 c0 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 != 1'b1 && state1 == 1'b1 && feedback == 1'b1) begin
			#5 a0 = 1'b1;
			#5 b1 = 1'b1;
			#5 c1 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 == 1'b1 && state1 != 1'b1 && feedback != 1'b1) begin
			#5 a1 = 1'b1;
			#5 b0 = 1'b1;
			#5 c0 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 == 1'b1 && state1 != 1'b1 && feedback == 1'b1) begin
			#5 a1 = 1'b1;
			#5 b0 = 1'b1;
			#5 c1 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 == 1'b1 && state1 == 1'b1 && feedback != 1'b1) begin
			#5 a1 = 1'b1;
			#5 b1 = 1'b1;
			#5 c0 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state0 == 1'b1 && state1 == 1'b1 && feedback == 1'b1) begin
			#5 a1 = 1'b1;
			#5 b1 = 1'b1;
			#5 c1 = 1'b1;
			#5 ack = 1'b1;
		end

		// output to states
		if(a0 == 1'b1 && b0 == 1'b1 && c0 == 1'b1) begin
			state0 = 1'b0;
			state1 = 1'b0;
			feedback = 1'b1;
		end
		else if(a0 == 1'b1 && b1 == 1'b1 && c0 == 1'b1) begin
			state0 = 1'b1;
			state1 = 1'b0;
			feedback = 1'b0;
		end
		else if(a1 == 1'b1 && b0 == 1'b1 && c0 == 1'b1) begin
			state0 = 1'b0;
			state1 = 1'b0;
			feedback = 1'b0;
		end
		else if(a1 == 1'b1 && b1 == 1'b1 && c0 == 1'b1) begin
			state0 = 1'b1;
			state1 = 1'b0;
			feedback = 1'b1;
		end
		else if(a0 == 1'b1 && b0 == 1'b1 && c1 == 1'b1) begin
			state0 = 1'b0;
			state1 = 1'b1;
			feedback = 1'b1;
		end
		else if(a0 == 1'b1 && b1 == 1'b1 && c1 == 1'b1) begin
			state0 = 1'b1;
			state1 = 1'b1;
			feedback = 1'b0;
		end
		else if(a1 == 1'b1 && b0 == 1'b1 && c1 == 1'b1) begin
			state0 = 1'b0;
			state1 = 1'b1;
			feedback = 1'b0;
		end
		else if(a1 == 1'b1 && b1 == 1'b1 && c1 == 1'b1) begin
			state0 = 1'b1;
			state1 = 1'b1;
			feedback = 1'b1;
		end
		//stabilize signals
		wait((a0 == 1'b1 && b0 == 1'b1 && c0 == 1'b1 && state0 != 1'b1 && state1 != 1'b1 && feedback == 1'b1) ||
				 (a0 == 1'b1 && b1 == 1'b1 && c0 == 1'b1 && state0 == 1'b1 && state1 != 1'b1 && feedback != 1'b1) ||
				 (a1 == 1'b1 && b0 == 1'b1 && c0 == 1'b1 && state0 != 1'b1 && state1 != 1'b1 && feedback != 1'b1) ||
				 (a1 == 1'b1 && b1 == 1'b1 && c0 == 1'b1 && state0 == 1'b1 && state1 != 1'b1 && feedback == 1'b1) ||
				 (a0 == 1'b1 && b0 == 1'b1 && c1 == 1'b1 && state0 != 1'b1 && state1 == 1'b1 && feedback == 1'b1) ||
				 (a0 == 1'b1 && b1 == 1'b1 && c1 == 1'b1 && state0 == 1'b1 && state1 == 1'b1 && feedback != 1'b1) ||
				 (a1 == 1'b1 && b0 == 1'b1 && c1 == 1'b1 && state0 != 1'b1 && state1 == 1'b1 && feedback != 1'b1) ||
				 (a1 == 1'b1 && b1 == 1'b1 && c1 == 1'b1 && state0 == 1'b1 && state1 == 1'b1 && feedback == 1'b1)) #5;

		// reset
		wait(req != 1'b1) #5;
		if(a0 == 1'b1) begin
			#5 a0 = 1'b0;
		end
		if(a1 == 1'b1) begin
			#5 a1 = 1'b0;
		end
		if(b0 == 1'b1) begin
			#5 b0 = 1'b0;
		end
		if(b1 == 1'b1) begin
			#5 b1 = 1'b0;
		end
		if(c0 == 1'b1) begin
			#5 c0 = 1'b0;
		end
		if(c1 == 1'b1) begin
			#5 c1 = 1'b0;
		end
		#5 ack = 1'b0;
	end

endmodule
