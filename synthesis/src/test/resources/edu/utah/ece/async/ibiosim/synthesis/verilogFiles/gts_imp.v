



module gts_imp(IPTG, aTc, GFP);
output reg GFP;
input wire IPTG, aTc;

initial begin
  GFP = 1'b0;
end

always begin
  wait (IPTG == 1'b1);
  #5 GFP = 1'b1;
  wait (aTc == 1'b1);
  #5 GFP = 1'b0;
end
endmodule
