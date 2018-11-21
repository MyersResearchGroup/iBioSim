// ----------------------------
// This module contains the three general c-element pipedlined design.
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
// - async. design follows dual rail encoding that maps
//   input to output and output to internal states
//
// author: Tramy Nguyen
// ----------------------------



module gC_imp(sensor_0, sensor_1, startSignal_0, startSignal_1, actuator_0, actuator_1);

  input sensor_0, sensor_1, startSignal_0, startSignal_1;
	output reg actuator_0, actuator_1;
  reg cell1, cell2, cell3;
  reg QS1_0, QS1_1, QS2_0, QS2_1;


  initial begin
    actuator_0 = 1'b0;
    actuator_1 = 1'b0;
    cell1 = 1'b0;
    cell2 = 1'b0;
    cell3 = 1'b0;
    QS1_0 = 1'b0;
    QS1_1 = 1'b0;
    QS2_0 = 1'b0;
    QS2_1 = 1'b0;
  end

	always begin
    //wait for input signal(s)
    wait ((sensor_0 == 1'b1 || sensor_1 == 1'b1) &&
    (startSignal_0 == 1'b1 || startSignal_1 == 1'b1))#5;

    //set output base on input signal and current state
    if(sensor_0 == 1'b1 && startSignal_0 == 1'b1) begin
      #5 QS1_0 = 1'b1;
    end
    else if(sensor_1 == 1'b1 && startSignal_1 == 1'b1) begin
      #5 QS1_1 = 1'b1;
    end
    else if(cell1 == 1'b1) begin
      #5 QS1_1 = 1'b1;
    end
    else begin
      #5 QS1_0 = 1'b1;
    end

    if(sensor_0 == 1'b1 && QS1_0 == 1'b1) begin
      #5 QS2_0 = 1'b1;
    end
    else if(sensor_1 == 1'b1 && QS1_1 == 1'b1) begin
      #5 QS2_1 = 1'b1;
    end
    else if(cell2 == 1'b1) begin
      #5 QS2_1 = 1'b1;
    end
    else begin
      #5 QS2_0 = 1'b1;
    end

    if(sensor_0 == 1'b1 && QS2_0 == 1'b1) begin
      #5 actuator_0 = 1'b1;
    end
    else if(sensor_1 == 1'b1 && QS2_1 == 1'b1) begin
      #5 actuator_1 = 1'b1;
    end
    else if(cell3 == 1'b1) begin
      #5 actuator_1 = 1'b1;
    end
    else begin
      #5 actuator_0 = 1'b1;
    end


    //set next state(s) base on output signal(s)
    if(QS1_0 == 1'b1 && cell1 == 1'b1) begin
      cell1 = 1'b0;
    end
    else if(QS1_1 == 1'b1 && cell1 == 1'b0) begin
      cell1 = 1'b1;
    end

    if(QS2_0 == 1'b1 && cell2 == 1'b1) begin
      cell2 = 1'b0;
    end
    else if(QS2_1 == 1'b1 && cell2 == 1'b0) begin
      cell2 = 1'b1;
    end

    if(actuator_0 == 1'b1 && cell3 == 1'b1) begin
      cell3 = 1'b0;
    end
    else if(actuator_1 == 1'b1 && cell3 == 1'b0) begin
      cell3 = 1'b1;
    end


    //stabilize the current state(s)
    wait(((cell1 == 1'b1 && QS1_1 == 1'b1) || (cell1 == 1'b0 && QS1_0 == 1'b1)) &&
    ((cell2 == 1'b1 && QS2_1 == 1'b1) || (cell2 == 1'b0 && QS2_0 == 1'b1)) &&
    ((cell3 == 1'b1 && actuator_1 == 1'b1) || (cell3 == 1'b0 && actuator_0 == 1'b1))) #5;

    //confirm input signal has been reset
    wait(sensor_0 == 1'b0 && sensor_1 == 1'b0 && startSignal_0 == 1'b0 && startSignal_1 == 1'b0) #5;

    //reset output signal(s)
    if(QS1_0 == 1'b1) begin
      #5 QS1_0 = 1'b0;
    end

    if(QS1_1 == 1'b1) begin
      #5 QS1_1 = 1'b0;
    end

    if(QS2_0 == 1'b1) begin
      #5 QS2_0 = 1'b0;
    end

    if(QS2_1 == 1'b1) begin
      #5 QS2_1 = 1'b0;
    end

    if(actuator_0 == 1'b1) begin
      #5 actuator_0 = 1'b0;
    end

    if(actuator_1 == 1'b1) begin
      #5 actuator_1 = 1'b0;
    end

  end
endmodule
