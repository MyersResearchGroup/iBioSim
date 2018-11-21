
module gts_ack_testbench ();

	wire GFP, ack;
	reg IPTG, aTc, next;

	initial begin
		IPTG = 1'b0;
		aTc = 1'b0;
		next = 1'b0;
	end

	gts_ack_imp gts_ack_instance(
	.IPTG(IPTG),
	.aTc(aTc),
	.GFP(GFP),
	.ack(ack)
	);

	always begin
		#5 next = $random%2;

		if(next == 1'b0) begin
			#5 aTc = 1'b1;
		end else begin
			#5 IPTG = 1'b1;
		end

		wait(ack == 1'b1) #5;

		if(IPTG == 1'b1) begin
			#5 IPTG = 1'b0;
		end else begin
			#5 aTc = 1'b0;
		end

		wait(ack != 1'b1) #5;
	end

endmodule
