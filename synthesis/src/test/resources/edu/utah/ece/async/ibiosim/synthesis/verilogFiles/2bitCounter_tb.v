
module counter_testbench ();

	reg start;
	wire ack;

	initial begin
		start = 1'b0;
	end

	counter_imp counter_instance(
	.start(start),
	.ack(ack)
	);

	always begin
		#5 start = 1'b1;
		wait(ack == 1'b1);
		#5 start = 1'b0;
		wait(ack == 1'b0);

	end

endmodule
