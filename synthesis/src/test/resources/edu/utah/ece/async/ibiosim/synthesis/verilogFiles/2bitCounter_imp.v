
module counter_imp (start, ack);
	input wire start;
	output reg ack;
  reg count0, count1;

	initial begin
		ack = 1'b0;
		count0 = 1'b0;
    count1 = 1'b0;
	end

	always begin
		wait(start == 1'b1) #5;
		#5 ack = 1'b1;
		if(count1 == 1'b0 && count0 == 1'b0) begin
      #5 count0 = 1'b1;
    end
    else if(count1 == 1'b0 && count0 == 1'b1) begin
      #5 count1 = 1'b1;
    end
    else if(count1 == 1'b1 && count0 == 1'b1) begin
      #5 count0 = 1'b0;
    end
    else if(count1 == 1'b1 && count0 == 1'b0) begin
      #5 count1 = 1'b0;
    end


		wait(start == 1'b0) #5;
		#5 ack = 1'b0;
	end

endmodule
