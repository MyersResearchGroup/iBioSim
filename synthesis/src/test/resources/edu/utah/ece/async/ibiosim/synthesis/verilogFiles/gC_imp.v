// ----------------------------
// Genetic Sensor and Filter
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
//
// author: Chris Myers
// ----------------------------

module gC_imp(Start, Sensor, Actuator);

	input wire Start, Sensor;
	output reg Actuator;

	initial begin
		Actuator = 1'b0;
	end

   always begin
      wait (Start == 1'b1 && Sensor == 1'b1);
      #5 Actuator = 1'b1;
      wait (Sensor == 1'b0 && Start == 1'b0);
      #5 Actuator = 1'b0;
   end

endmodule
