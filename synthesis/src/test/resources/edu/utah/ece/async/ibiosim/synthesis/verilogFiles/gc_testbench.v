
module gc_testbench ();

	wire Actuator;
        reg Start, Sensor;


	initial begin
		Start = 1'b1;
		Sensor = 1'b1;
	end

	gc_imp gc_instance(
	.Start(Start),
	.Sensor(Sensor),
	.Actuator(Actuator)
	);

always begin
   #5 Sensor = 1'b0;
   #5 Start = 1'b0;
   wait (Actuator == 1'b1);
   #5 Sensor = 1'b1;
   wait (Actuator == 1'b0);
   #5 Sensor = 1'b0;
   wait (Actuator == 1'b1);
   #5 Start = 1'b1;
   #5 Sensor = 1'b1;
   wait (Actuator == 1'b0);
end

endmodule
