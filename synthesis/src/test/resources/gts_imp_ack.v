

module gts_ack_imp (IPTG, aTc, GFP, ack);

	output reg GFP, ack;
	input wire IPTG, aTc;

	initial begin
		GFP = 1'b0;
		ack = 1'b0;
	end

	always begin
		wait(IPTG == 1'b1 || aTc == 1'b1) #5;

		if(IPTG == 1'b1) begin
			#5 GFP = 1'b1;
			#5 ack = 1'b1;
		end else begin
			#5 GFP = 1'b0;
			#5 ack = 1'b1;
		end

		wait(IPTG != 1'b1 && aTc != 1'b1) #5;

		#5 ack = 1'b0;
	end

endmodule
