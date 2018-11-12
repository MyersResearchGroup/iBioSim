// ----------------------------
// if else statement
//
// author: Tramy Nguyen
// ----------------------------

module conditional_stmt1(in, out);
	
    input in;
	output reg out;

  always begin
    if(in) 
        out = 1'b0;
    else
        out = 1'b1;
  end

endmodule