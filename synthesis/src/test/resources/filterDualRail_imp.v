// ----------------------------
// This module contains the three general c-element pipedlined design.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows bundled-data encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------


module filter_imp(Start, Sensor, Actuator);

  input Start, Sensor;
  output reg Actuator;
  reg QS1;
  reg QS2;

  initial begin
    Actuator = 1'b0;
    QS1 = 1'b0;
    QS2 = 1'b0;
  end


  always begin
    wait (Start == 1'b1 && Sensor == 1'b1) #5;
    #5 QS1 = 1'b1;
    wait (Start == 1'b0 && Sensor == 1'b0) #5;
    #5 QS1 = 1'b0;
  end

  always begin
    wait (QS1 == 1'b1 && Sensor == 1'b1) #5;
    #5 QS2 = 1'b1;
    wait (QS1 == 1'b0 && Sensor == 1'b0) #5;
    #5 QS2 = 1'b0;
  end

  always begin
    wait (QS2 == 1'b1 && Sensor == 1'b1) #5;
    #5 Actuator = 1'b1;
    wait (QS2 == 1'b0 && Sensor == 1'b0) #5;
    #5 Actuator = 1'b0;
  end


endmodule
