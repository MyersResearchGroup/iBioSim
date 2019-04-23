
module gts_testbench();

	wire GFP;
	reg IPTG, aTc, next;

	initial begin
		IPTG = 1'b0;
		aTc = 1'b0;
		next = 1'b0;
	end

	gts_imp gts_instance(
	.IPTG(IPTG),
	.aTc(aTc),
	.GFP(GFP)
	);

	always begin
		#5 next = $random%2;

		if(next == 1'b0) begin
			#5 IPTG = 1'b1;
		end else begin
			#5 aTc = 1'b1;
		end

		wait(GFP == 1'b1) #5;

		if(aTc == 1'b1) begin
			#5 aTc = 1'b0;
		end else begin
			#5 IPTG = 1'b0;
		end

		wait(GFP != 1'b1) #5;
	end

endmodule
