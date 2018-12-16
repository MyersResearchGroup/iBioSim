`timescale 1ps/1fs


module unif_tb();

    reg bit0, bit1, next, d_val;

    initial begin
        bit0 = 1'b0;
        bit1 = 1'b0;
        next = 1'b0;
        d_val = 1'b0;
    end

    function uniform;
        input a, b;
        begin
            uniform = $urandom_range(a*1000, b*1000)/1000.0;
        end
    endfunction

    always begin
        #5 next = $random%2;

        if (next != 1'b1) begin
            d_val = $urandom_range(3, 5);
            bit0 = #d_val 1'b1;
        end
        else begin
            d_val = uniform(0,3);
            bit1 = #d_val 1'b1;
        end

    end

endmodule
