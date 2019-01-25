`timescale 1ps/1fs

// ----------------------------
// testing $urandom_range with with varying parameter values
//
// author: Tramy Nguyen
// ----------------------------

module system_func3();

  reg delay1;
  reg delay2;
  reg delay3;
  reg delay4;

  initial begin
    delay1 = $urandom_range(30,20);
    delay2 = $urandom_range(20);
    delay3 = $urandom_range(20,30);
    delay4 = $urandom_range(20, 20);
  end
endmodule
