

// ----------------------------
// testing continuous assignments
//
// author: Tramy Nguyen
// ----------------------------

module system_functions();

  reg [31:0] addr1;
  reg [31:0] addr2;
  reg [64:0] addr3;
  reg [31:0] data;

  initial begin
    addr1 = $urandom_range(30,20);
    addr2 = $urandom_range(20); //takes max value as '0'
    addr3 = $urandom_range(20,30); //considers max value as '30' and min value as '20'
    //$display("addr1=%0d, addr2=%0d, addr3=%0d",addr1,addr2,addr3);
  end
endmodule
