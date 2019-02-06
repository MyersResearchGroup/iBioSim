// ----------------------------
// This module contains both the instantiation and the testbench for the
// scanflop_imp.v circuit.
//
// Note:
// - This module compiles and simulates with Verilog synthesizer
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------



module scanflop_testbench();

	wire q0, q1, ack;
	reg in1_0, in1_1, in2_0, in2_1, sel0, sel1, next1, next2, next3, req;

	initial begin
		next1 = 1'b0;
		next2 = 1'b0;
		next3 = 1'b0;
    in1_0 = 1'b0;
    in1_1 = 1'b0;
    in2_0 = 1'b0;
    in2_1 = 1'b0;
    sel0 = 1'b0;
    sel1 = 1'b0;
		req = 1'b0;
	end

	scanflop_imp sf_instance(
	.in1_0(in1_0),
	.in1_1(in1_1),
	.in2_0(in2_0),
	.in2_1(in2_1),
	.sel0(sel0),
	.sel1(sel1),
	.q0(q0),
	.q1(q1),
	.req(req),
	.ack(ack)
	);

	always begin
		#5 next1 = $random%2;
		#5 next2 = $random%2;
		#5 next3 = $random%2;

		if(next1 != 1'b1) begin
			#5 in1_0 = 1'b1;
		end
		else begin
		  #5 in1_1 = 1'b1;
		end

		if(next2 != 1'b1) begin
			#5 in2_0 = 1'b1;
		end
		else begin
			#5 in2_1 = 1'b1;
		end

		if(next3 != 1'b1) begin
			#5 sel0 = 1'b1;
		end
		else begin
			#5 sel1 = 1'b1;
		end

		#5 req = 1'b1;
		wait(ack == 1'b1) #5;
		#5 req = 1'b0;

		if(in1_0 == 1'b1) begin
			#5 in1_0 = 1'b0;
		end
		else begin
			#5 in1_1 = 1'b0;
		end

		if(in2_0 == 1'b1) begin
			#5 in2_0 = 1'b0;
		end
		else begin
			#5 in2_1 = 1'b0;
		end

		if(sel0 == 1'b1) begin
			#5 sel0 = 1'b0;
		end
		else begin
			#5 sel1 = 1'b0;
		end

		wait(ack != 1'b1) #5;
	end
endmodule
