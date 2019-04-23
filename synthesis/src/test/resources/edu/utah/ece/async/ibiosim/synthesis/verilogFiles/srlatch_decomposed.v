/*
 * /Users/tramyn/Desktop/vfiles/srlatch
*/
module r_s_q_sl_instance__state_net(r, s, q);

  input r;
  input s;
  output q;
  //output sl_instance__state;

assign q = (s) | (~r) & q;
//assign sl_instance__state = (q);

// Initial state:
// !r !s !q !sl_instance__state

endmodule
