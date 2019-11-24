
module gc_imp (Start, Sensor, Actuator);

	input wire Start, Sensor;
	output reg Actuator;

	initial begin
		Actuator = 1'b0;
	end

   always begin
      wait (Start == 1'b0 && Sensor == 1'b0);
      #5 Actuator = 1'b1;
      wait (Sensor == 1'b1);
      #5 Actuator = 1'b0;
   end

endmodule
