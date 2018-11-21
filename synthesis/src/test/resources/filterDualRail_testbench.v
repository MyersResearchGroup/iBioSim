
// ----------------------------
// This module contains both the instantiation and the testbench for the
// gC_imp.v circuit.
//
// Note:
// - This module compiles and simulates with Verilog synthesizer
// - async. design follows bundled-data encoding that maps
//   input to output and output to internal states
//
// author: Chris Myers
// author: Tramy Nguyen
// ----------------------------

module filter_testbench();

  wire Actuator;
  reg Start, Sensor;

  initial begin
    Start = 1'b0;
    Sensor = 1'b0;
  end

  filter_imp filter_instance(
    .Start(Start),
    .Sensor(Sensor),
    .Actuator(Actuator)
  );

  always begin
    #5 Start = 1'b1;
    #5 Sensor = 1'b1;

    wait (Actuator == 1'b1) #5;

    #5 Start = 1'b0;
    #5 Sensor = 1'b0;

    wait (Actuator == 1'b0) #5;
  end


endmodule
