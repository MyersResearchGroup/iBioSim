// ----------------------------
// Genetic Sensor and Filter Testbench
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
//
// author: Chris Myers
// ----------------------------


module gc_testbench ();

	wire Actuator;
        reg Start, Sensor;


	initial begin
		Start = 1'b0;
		Sensor = 1'b0;
	end

	gc_imp gc_instance(
	.Start(Start),
	.Sensor(Sensor),
	.Actuator(Actuator)
	);

always begin
   #5 Sensor = 1'b1;
   #5 Start = 1'b1;
   wait (Actuator == 1'b1);
   #5 Sensor = 1'b0;
   wait (Actuator == 1'b0);
   #5 Sensor = 1'b1;
   wait (Actuator == 1'b1);
   #5 Start = 1'b0;
   #5 Sensor = 1'b0;
   wait (Actuator == 1'b0);
end

endmodule
