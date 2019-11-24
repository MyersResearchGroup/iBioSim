module srlatch_imp (s, r, q);
	output reg q;
	input wire s, r;

	initial begin
		q = 1'b0;
	end
	always begin
		wait (s == 1'b1);
		#5 q = 1'b1;
		wait (r == 1'b1);
		#5 q = 1'b0;
	end
endmodule
