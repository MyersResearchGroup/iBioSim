// ----------------------------
// a simple wait statment
//
// author: Tramy Nguyen
// ----------------------------


module wait_stmt(out0);
	
	output reg out0;
	
  always begin
    out0 = 1'b1;
    wait (out0 == 1'b1) #5;
  end

endmodule