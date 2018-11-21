/*
 * /Users/tramyn/Documents/publications/IEEE_2018/gts_compiler_output/gts_ack
*/
module aTc_IPTG_GFP_ack_net(aTc, IPTG, GFP, ack);

  input aTc;
  input IPTG;
  output GFP;
  output ack;

assign GFP = (IPTG) | (~aTc) & GFP;
assign ack = (aTc & ~GFP) | (IPTG & GFP);

// Initial state: 
// !aTc !IPTG !GFP !ack 

endmodule


