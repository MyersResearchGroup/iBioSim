// ----------------------------
// Genetic Sensor and Filter Testbench
//
// Note:
// - Syntax works and compiles with Verilog2LPN compiler
//
// author: Chris Myers
// ----------------------------


module filter_testbench ();

	wire Actuator;
	wire QS1out, QS2out;
	reg QS1in, QS2in;
  reg Start, Sensor;

	initial begin
		Start = 1'b0;
		Sensor = 1'b0;
		QS1in = 1'b0;
		QS2in = 1'b0;
	end

	gC_imp cell1(
	.Start(Start),
	.Sensor(Sensor),
	.Actuator(QS1out)
	);

	gC_imp cell2(
	.Start(QS1in),
	.Sensor(Sensor),
	.Actuator(QS2out)
	);

	gC_imp cell3(
	.Start(QS2in),
	.Sensor(Sensor),
	.Actuator(Actuator)
	);

always begin
   #5 Sensor = 1'b1;
   #5 Start = 1'b1;
   wait(QS1out == 1'b1);
   #5 QS1in = QS1out;
   wait(QS2out == 1'b1);
   #5 QS2in = QS2out;
   wait(Actuator == 1'b1);
   #5 Start = 1'b0;
   #5 Sensor = 1'b0;
   #5 QS1in = 1'b0;
   #5 QS2in = 1'b0;
   wait (QS1out == 1'b0 && QS2out == 1'b0 && Actuator == 1'b0);
end

endmodule
