// ----------------------------
// Test port mapping with different levels of port name
//
// author: Tramy Nguyen
// ----------------------------

module portMapping();

  reg IPTG, aTc;
  wire GFP;

  initial begin
    IPTG = 1'b0;
    aTc = 1'b0;
  end

  srLatch_imp srLatch_instance(
  .r(IPTG),
  .s(aTc),
  .q(GFP)
  );


  endmodule
