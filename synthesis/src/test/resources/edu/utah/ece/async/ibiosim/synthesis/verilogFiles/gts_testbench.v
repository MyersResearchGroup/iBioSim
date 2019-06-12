
module gts_testbench();

	wire GFP;
	reg IPTG, aTc;

	initial begin
		IPTG = 1'b0;
		aTc = 1'b0;
	end

	gts_imp gts_instance(
	.IPTG(IPTG),
	.aTc(aTc),
	.GFP(GFP)
	);

	always begin
		#5 IPTG = 1'b1;
		wait(GFP == 1'b1) #5;
		#5 IPTG = 1'b0;
		#5 aTc = 1'b1;
		wait(GFP != 1'b1) #5;
		#5 aTc = 1'b0;
	end

endmodule
