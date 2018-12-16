/*
 * filter
*/
module Start_Sensor_Actuator_net(Start, Sensor, Actuator);

  input Start;
  input Sensor;
  output Actuator;

assign Actuator = (Start & Sensor) | (Sensor & Actuator);

// Initial state: 
// !Start !Sensor !Actuator 

endmodule


