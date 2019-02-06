// ----------------------------
// An verilog module containing an always block
// with different boolean assignments.
//
// author: Tramy Nguyen
// ----------------------------

module always_block(out0);
	
	output reg out0;
	reg state; 
	
  always begin
    out0 = state && 1'b1 || (state && ~out0);
  end

endmodule