module srlatch_testbench ();
	wire q;
	reg s, r;
	initial begin
		s = 1'b0;
		r = 1'b0;
	end
	srlatch_imp sl_instance(
	.s(s),
	.r(r),
	.q(q)
	);
	always begin
		#5 s = 1'b1;
		wait(q == 1'b1);
		#5 s = 1'b0;
		wait(q == 1'b1);
		#5 r = 1'b1;
		wait(q == 1'b0);
		#5 r = 1'b0;
		wait(q == 1'b0);
	end
endmodule
