module srlatch_imp (s, r, q);
	output reg q;
	input wire s, r;
	reg state;
	initial begin
		q = 1'b0;
		state = 1'b0;
	end
	always begin
		wait((s == 1'b1 && r == 1'b0)
			|| (s == 1'b0 && r == 1'b1)
			|| (s == 1'b0 && r == 1'b0));
		//input to output
		if(s == 1'b1 && r == 1'b0) begin
			#5 q = 1'b1;
		end else if(s == 1'b0 && r == 1'b1 ) begin
			#5 q = 1'b0;
		end else begin
			if(state == 1'b1) begin
				#5 q = 1'b1;
			end else begin
				#5 q = 1'b0;
			end
		end
		// output to current state
		if(q == 1'b1 && state == 1'b0) begin
			state = 1'b1;
		end
		else if(q == 1'b0 && state == 1'b1) begin
			state = 1'b0;
		end
		wait((q == 1'b0 && state == 1'b0)
			|| (q == 1'b1 && state == 1'b1));
	end
endmodule
