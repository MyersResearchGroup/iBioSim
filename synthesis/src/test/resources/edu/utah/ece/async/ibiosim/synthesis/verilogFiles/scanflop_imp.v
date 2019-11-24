
// ----------------------------
// This module impelements a scanflop.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module scanflop_imp (in1_0, in1_1, in2_0, in2_1, sel0, sel1, q0, q1, req, ack);

  input wire in1_0, in1_1, in2_0, in2_1, sel0, sel1, req;
  output reg q1, q0, ack;
  reg state;

	initial begin
		q0 = 1'b0;
    q1 = 1'b0;
		ack = 1'b0;
		state = 1'b0;
	end

	always begin
		wait (req == 1'b1) #5;

		// input to output

    if(sel0 == 1'b1 && in1_0 == 1'b1) begin
      #5 q0 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel0 == 1'b1 && in1_1 == 1'b1) begin
      #5 q1 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel0 == 1'b1 && in1_0 == 1'b0 && in1_1 == 1'b0 && state == 1'b0) begin
      #5 q0 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel0 == 1'b1 && in1_0 == 1'b0 && in1_1 == 1'b0 && state == 1'b1) begin
      #5 q1 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel1 == 1'b1 && in2_0 == 1'b1) begin
      #5 q0 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel1 == 1'b1 && in2_1 == 1'b1) begin
      #5 q1 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel1 == 1'b1 && in2_0 == 1'b0 && in2_1 == 1'b0 && state == 1'b0) begin
      #5 q0 = 1'b1;
      #5 ack = 1'b1;
    end else if(sel1 == 1'b1 && in2_0 == 1'b0 && in2_1 == 1'b0 && state == 1'b1) begin
      #5 q1 = 1'b1;
      #5 ack = 1'b1;
    end

		// output to next state
		if(q0 == 1'b1 && state == 1'b1) begin
			state = 1'b0;
		end
		else if(q1 == 1'b1 && state == 1'b0) begin
			state = 1'b1;
		end

		//stabilize the signals
		wait((q0 == 1'b1 && state != 1'b1) || (q1 == 1'b1 && state == 1'b1)) #5;

		//reset
		wait(req != 1'b1) #5;
		if (q0 == 1'b1) begin
      #5 q0 = 1'b0;
    end
    if(q1 == 1'b1) begin
      #5 q1 = 1'b0;
    end
    #5 ack = 1'b0;
	end


endmodule
