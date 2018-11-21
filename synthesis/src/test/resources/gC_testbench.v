
// ----------------------------
// This module contains both the instantiation and the testbench for the
// gC_imp.v circuit.
//
// Note:
// - This module compiles and simulates with Verilog synthesizer
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------

module gC_testbench();

  wire actuator_0, actuator_1;
  reg sensor_0, sensor_1, startSignal_0, startSignal_1, next1, next2;

  initial begin
    sensor_0 = 1'b0;
    sensor_1 = 1'b0;
    startSignal_0 = 1'b0;
    startSignal_1 = 1'b0;
    next1 = 1'b0;
    next2 = 1'b0;
  end

  gC_imp gc_instance(
  .sensor_0(sensor_0),
  .sensor_1(sensor_1),
  .startSignal_0(startSignal_0),
  .startSignal_1(startSignal_1),
  .actuator_0(actuator_0),
  .actuator_1(actuator_1)
  );

  always begin
    #5 next1 = $random%2;
    #5 next2 = $random%2;

    if (next1 != 1'b1) begin
      #5 sensor_0 = 1'b1;
    end else begin
      #5 sensor_1 = 1'b1;
    end

    if (next2 != 1'b1) begin
      #5 startSignal_0 = 1'b1;
    end else begin
      #5 startSignal_1 = 1'b1;
    end

    wait (actuator_0 == 1'b1 || actuator_1 == 1'b1) #5;

    if (sensor_0 == 1'b1) begin
      #5 sensor_0 = 1'b0;
    end else begin
      #5 sensor_1 = 1'b0;
    end

    if (startSignal_0 == 1'b1) begin
      #5 startSignal_0 = 1'b0;
    end else begin
      #5 startSignal_1 = 1'b0;
    end

    wait (actuator_0 != 1'b1 && actuator_1 != 1'b1) #5;
  end


endmodule
