
// ----------------------------
// This module impelements a counter.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module counter_imp (a0, a1, b0, b1, req, ack);
	input wire req;
	output reg a0, a1, b0, b1, ack;
	reg state0, state1;

	initial begin
		a0 = 1'b0;
		a1 = 1'b0;
		b0 = 1'b0;
		b1 = 1'b0;
		state0 = 1'b0;
		state1 = 1'b0;
		ack = 1'b0;
	end

	always begin
		wait(req == 1'b1) #5;

		// input to output
		if(state1 != 1'b1 && state0 != 1'b1) begin
			#5 a0 = 1'b1;
			#5 b0 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state1 != 1'b1 && state0 == 1'b1) begin
			#5 a0 = 1'b1;
			#5 b1 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state1 == 1'b1 &&  state0 == 1'b1) begin
			#5 a1 = 1'b1;
			#5 b0 = 1'b1;
			#5 ack = 1'b1;
		end
		else if(state1 == 1'b1 &&  state0 != 1'b1) begin
			#5 a1 = 1'b1;
			#5 b1 = 1'b1;
			#5 ack = 1'b1;
		end

		// output to next state
		if(a0 == 1'b1 && b0 == 1'b1 ) begin
			if(state1 != 1'b1 && state0 != 1'b1) begin
				state0 = 1'b1;
			end
		end
		else if(a0 == 1'b1 && b1 == 1'b1 ) begin
			if(state1 != 1'b1 && state0 == 1'b1) begin
				state1 = 1'b1;
			end
		end
		else if(a1 == 1'b1 && b0 == 1'b1) begin
			if(state1 == 1'b1 && state0 == 1'b1) begin
				state0 = 1'b0;
				end
		end
		else if(a1 == 1'b1 && b1 == 1'b1) begin
			if(state1 == 1'b1 && state0 != 1'b1) begin
				state1 = 1'b0;
			end
		end

		//stabilize the signals
		wait((a0 == 1'b1 && b0 == 1'b1 && state1 != 1'b1 && state0 == 1'b1) ||
		     (a0 == 1'b1 && b1 == 1'b1 && state1 == 1'b1 && state0 == 1'b1) ||
				 (a1 == 1'b1 && b0 == 1'b1 && state1 == 1'b1 && state0 != 1'b1) ||
				 (a1 == 1'b1 && b1 == 1'b1 && state1 != 1'b1 && state0 != 1'b1)) #5;

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
		#5 ack = 1'b0;

	end

endmodule
