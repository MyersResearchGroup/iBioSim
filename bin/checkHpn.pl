#!/usr/bin/perl
#*******************************************************************************
#  
# This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
# for the latest version of iBioSim.
#
# Copyright (C) 2017 University of Utah
#
# This library is free software; you can redistribute it and/or modify it
# under the terms of the Apache License. A copy of the license agreement is provided
# in the file named "LICENSE.txt" included with this software distribution
# and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
#  
#*******************************************************************************

###############################################################################
##
## File Name     : checkHpn.pl
## Author        : 
## E-mail        : little@cs.utah.edu
## Date Created  : 05/12/2004
##
## Description : A simple script to check results for the ATACS THPN
##               analysis engine.
##
## Assumptions   :
##
## ToDo          :
##
###############################################################################
## CVS HOOKS
##
## $Source: /home/ming/cvsroot/BioSim/bin/Attic/checkHpn.pl,v $
###############################################################################
## $Id: checkHpn.pl,v 1.1 2010/02/16 18:11:26 myers Exp $
###############################################################################

use strict;

#the following 2 hashes give the current "correct" values for states
#and zones on the examples.
my %stateH =
(
 "water" => 2,
 "water2x" => 2,
 "waterAMS" => 10,
 "billiardsAMS" => 66,
 "temperatureAMS" => 39,
 "diode_noosc" => 14,
 "diode_osc" => 17,
 "2x2diode" => 4,
 "3x3diode" => 6,
 "4x4diode" => 16,
 "naive_4x4noosc" => 12,
 "naive_4x4osc" => 13,
 "our_4x4noosc" => 15,
 "our_4x4nooscprop" => 41,
# "our_4x4nooscprop2" => 37,
 "our_4x4nooscprop4" => 14,
 "our_4x4osc" => 14,
 "our_4x4oscprop" => 22,
# "our_4x4oscprop2" => 17,
 "our_4x4oscprop4" => 17,
 "our_noosc" => 9,
 "our_osc" => 13,
 "pll2" => 27,
 "pll3" => 20,
 "pll_lite" => 5,
 "vco" => 2,
 "waterEI" => 11,
 "billiardsEI" => 28,
 "temperatureEI" => 23
 );

my %zoneH =
(
 "water" => 11,
 "water2x" => 11,
 "waterAMS" => 10,
 "billiardsAMS" => 134,
 "temperatureAMS" => 48,
 "diode_noosc" => 3612,
 "diode_osc" => 3343,
 "2x2diode" => 57,
 "3x3diode" => 522,
 "4x4diode" => 13334,
 "naive_4x4noosc" => 68,
 "naive_4x4osc" => 101,
 "our_4x4noosc" => 1473,
 "our_4x4nooscprop" => 8479,
# "our_4x4nooscprop2" => 3829,
 "our_4x4nooscprop4" => 3109,
 "our_4x4osc" => 698,
 "our_4x4oscprop" => 802,
# "our_4x4oscprop2" => 296,
 "our_4x4oscprop4" => 2830,
 "our_noosc" => 271,
# "our_osc" => 359,
 "pll2" => 1497,
 "pll3" => 615,
 "pll_lite" => 11,
 "vco" => 7,
 "waterEI" => 14,
 "billiardsEI" => 48,
 "temperatureEI" => 37
 );

my $file;

#grab the size of @ARGV
my $arg_size = scalar(@ARGV);

if($arg_size != 1) {
  print "Wrong number of arguments to the script.\n";
  exit(-1);
}
else {
  $file = $ARGV[0];
}
  
my $foundWarning = 0;
my $foundZoneCnt = 0;
my $foundStateCnt = 0;
my $status = 0;

open(LOG, "$file.log");
print "$file: ";
my $ok = 1;
while(<LOG>) {
  chomp;
  my $line = $_;
  if($line =~ /Warning:/ && !$foundWarning) {
    if($line !~ /^Warning: Petri net contains/) {
      print "Failed!  A warning was given.\n";
      $ok = 0;
      $foundWarning = 1;
      $status = 1;
    }
  }
  if($line =~ /^States:/) {
    $foundStateCnt = 1;
    my @stateA = split / /, $line;
    if($stateA[1] != $stateH{$file}) {
      print "Failed!  Wrong state count -- given $stateA[1] expected $stateH{$file}\n";
      $ok = 0;
      $status = 2;
    }
  }
  if($line =~ /^Zones: \d+$/) {
    $foundZoneCnt = 1;
    my @zoneA = split / /, $line;
    if($zoneA[1] != $zoneH{$file}) {
      print "Failed!  Wrong zone count -- given $zoneA[1] expected $zoneH{$file}\n";
      $ok = 0;
      $status = 3;
    }
  }
}
close LOG;
if($ok && $foundZoneCnt && $foundStateCnt) {
  print "Ok\n";
}

if(!$foundZoneCnt) {
  print "No zone count found!\n";
  $status = 4;
}

if(!$foundStateCnt) {
  print "No state count found!\n";
  $status = 5;
}
exit($status);
