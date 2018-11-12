// ----------------------------
// This module contains both the instantiation and the testbench for the
// srlatch_imp.v circuit.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
// - This design is not simulatable and is only used for testing
// author: Tramy Nguyen
// ----------------------------


module srlatch_testbench ();

	wire GFP0, GFP1;
	reg IPTG0, IPTG1, aTc0, aTc1, next;

	initial begin
		IPTG0 = 1'b0;
		IPTG1 = 1'b0;
		aTc0 = 1'b0;
		aTc1 = 1'b0;
	end

	srlatch_imp sl_instance(
	.IPTG0(IPTG0),
	.IPTG1(IPTG1),
	.aTc0(aTc0),
	.aTc1(aTc1),
	.GFP0(GFP0),
	.GFP1(GFP1)
	);

	always begin
		#5 next = $random%2;

		if(next != 1'b1) begin
			#5 IPTG0 = 1'b1;
			#5 aTc1 = 1'b1;
		end
		else begin
		  #5 IPTG1 = 1'b1;
		  #5 aTc0 = 1'b1;
		end

		wait (GFP0 == 1'b1 || GFP1 == 1'b1) #5;

		if(IPTG0 == 1'b1) begin
			#5 IPTG0 = 1'b0;
		end
		else begin
			#5 IPTG1 = 1'b0;
		end

		if(aTc0 == 1'b1) begin
			#5 aTc0 = 1'b0;
		end
		else begin
			#5 aTc1 = 1'b0;
		end

		wait (GFP0 != 1'b1 && GFP1 != 1'b1) #5;
	end

endmodule
