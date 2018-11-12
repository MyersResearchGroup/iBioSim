// ----------------------------
// This module impelements an srlatch.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
// - This design is not simulatable and is only used for testing
// author: Tramy Nguyen
// ----------------------------

module srlatch_imp (IPTG0, IPTG1, aTc0, aTc1, GFP0, GFP1);

	output reg GFP0, GFP1;
	input wire IPTG0, IPTG1, aTc0, aTc1;
	reg state;

	initial begin
		GFP0 = 1'b0;
		GFP1 = 1'b0;
		state = 1'b0;
	end

	always begin
		wait((IPTG0 == 1'b1 && aTc1 == 1'b1) || (IPTG1 == 1'b1 && aTc0 == 1'b1) || (IPTG0 == 1'b1 && aTc0 == 1'b1)) #5;

		// input to output
		if(IPTG1 == 1'b1 && aTc0 == 1'b1) begin
			#5 GFP1 = 1'b1;
		end
		else if(aTc1 == 1'b1 && IPTG0 == 1'b1 ) begin
			#5 GFP0 = 1'b1;
		end // else keep old state
		else if(aTc0 == 1'b1 && IPTG0 == 1'b1)begin
			if(state == 1'b1) begin
				#5 GFP1 = 1'b1;
			end else begin
				#5 GFP0 = 1'b1;
			end
		end

		// output to next state
		if(GFP1 == 1'b1 && state == 1'b0) begin
			state = 1'b1;
		end
		else if(GFP0 == 1'b1 && state == 1'b1) begin
			state = 1'b0;
		end
		wait((GFP0 == 1'b1 && state != 1'b1) || (GFP1 == 1'b1 && state == 1'b1)) #5;

		//reset
		wait(IPTG0 != 1'b1 && IPTG1 != 1'b1 && aTc0 != 1'b1 && aTc1 != 1'b1) #5;
		if(GFP0 == 1'b1) begin
			#5 GFP0 = 1'b0;
		end
		else begin
			#5 GFP1 = 1'b0;
		end
	end

endmodule
