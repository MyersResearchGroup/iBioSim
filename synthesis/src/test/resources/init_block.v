// ----------------------------
// A verilog module containing an initial block
//
// author: Tramy Nguyen
// ----------------------------

module init_block(in0, out0);
	
	input in0;
	output reg out0;
	reg state; 
	
  initial begin
    out0 = 1'b0;
    state = 1'b1;
  end

endmodule