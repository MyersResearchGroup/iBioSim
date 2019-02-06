// ----------------------------
// Register assignments in an always block.
//
// author: Tramy Nguyen
// ----------------------------

module reg_assign();
	
	reg r1, r2, r3, r4, r; 

	always begin 
		r = r1 && (r2 || r3) && ~r4 && 1'b1;
		r1 = r2;
		r2 = 1'b0;
	end
	
endmodule