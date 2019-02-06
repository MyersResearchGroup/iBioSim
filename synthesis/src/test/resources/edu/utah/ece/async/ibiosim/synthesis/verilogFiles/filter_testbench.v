// ----------------------------
// Genetic Sensor and Filter Testbench
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
//
// author: Chris Myers
// ----------------------------


module filter_testbench ();

	wire Actuator, QS1, QS2;
  reg Start, Sensor;

	initial begin
		Start = 1'b0;
		Sensor = 1'b0;
	end

	filter_imp cell1(
	.Start(Start),
	.Sensor(Sensor),
	.Actuator(QS1)
	);

	filter_imp cell2(
	.Start(QS1),
	.Sensor(Sensor),
	.Actuator(QS2)
	);

	filter_imp cell3(
	.Start(QS2),
	.Sensor(Sensor),
	.Actuator(Actuator)
	);

always begin
   #5 Sensor = 1'b1;
   #5 Start = 1'b1;
   wait (QS1 == 1'b1);
	 #5 Start = 1'b0;
   #5 Sensor = 1'b0;
   wait (QS1 == 1'b0);

   #5 Sensor = 1'b1;
	 #5 QS1 = 1'b1;
   wait (QS2 == 1'b1);
   #5 QS1 = 1'b0;
   #5 Sensor = 1'b0;
   wait (QS2 == 1'b0);

	 #5 Sensor = 1'b1;
	 #5 QS2 = 1'b1;
   wait (Actuator == 1'b1);
   #5 QS2 = 1'b0;
   #5 Sensor = 1'b0;
   wait (Actuator == 1'b0);
end

endmodule
