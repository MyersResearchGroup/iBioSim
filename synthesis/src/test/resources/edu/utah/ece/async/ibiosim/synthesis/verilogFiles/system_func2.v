`timescale 1ps/1fs

// ----------------------------
// testing $urandom_range with two parameter
//
// author: Tramy Nguyen
// ----------------------------
module system_func2();

    reg bit0;

    initial begin
        bit0 = 1'b0;
    end

    always begin
        #($urandom_range(5,10)) bit0 = 1'b1;
    end

endmodule
