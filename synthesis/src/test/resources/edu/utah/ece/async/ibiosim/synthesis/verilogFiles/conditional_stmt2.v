// ----------------------------
// if else statement
//
// author: Tramy Nguyen
// ----------------------------

module conditional_stmt2(out0, out1);
	
	output reg out0, out1;
	
  always begin
    out0 = 1'b1;
    out1 = 1'b1;
    if (out0 == 1'b1 && out1 == 1'b0) begin
    	out0 = 1'b1;
    	out1 = 1'b1;
    end else if(out0 == 1'b0 && out1 == 1'b1) begin
    	out0 = ~out1;
    end else begin
    	out0 = 1'b0;
    	out1 = 1'b0;
    end
  end

endmodule