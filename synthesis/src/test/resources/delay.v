// ----------------------------
// setting delay before variable assignment
//
// author: Tramy Nguyen
// ----------------------------


module delay(out0, out1);
	
	output reg out0, out1;
	reg next; 

  always begin
    #5 next = $random%2;
    if (next == 1'b0) begin
      out0 = 1'b1;
      wait (out0 == 1'b1) #5;
    end else begin
      out1 = 1'b1;
      wait (out1 == 1'b1) #5;
    end
  end

endmodule