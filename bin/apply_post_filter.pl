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

if ($#ARGV != 3){
    print "Usage: ./recheck_results.pl cutoff file_method_dot file_correct.dot file_output\n";
    exit(1);
}

my $cutoff = $ARGV[0];
my $outfile = $ARGV[3];

check_correctness($ARGV[1], $ARGV[2]);


sub check_correctness{
    my $filename = shift;
    my $dot_file = shift;

    open (IN1, "$dot_file") or die "I cannot check dot correctness for $dot_file\n";
    open (IN2, "$filename") or die "I cannot check correctness for $filename\/method.dot\n";
    open (OUT, ">$outfile") or die "I cannot write the checked file\n";

    my @in1 = <IN1>;
    my @in2 = <IN2>;
    close IN1;
    close IN2;

    my $in1 = join ("",@in1);
    my $in2 = join ("",@in2);

    $in1 =~ s/sp_//g;
    $in2 =~ s/sp_//g;

    print OUT "digraph G {\n";
    #generate the states in the corrected output
    for (my $i = 0; $i <= $#in2; $i++){
	if ($in2[$i] =~ m/shape=ellipse/){
	    print OUT $in2[$i];
	}
    }
    
    my $r_c = 0;
    my $r_t = 0;
    #check precision
    while ($in1 =~ m/s([0-9]+) -> s([0-9]+) .+arrowhead=((vee|tee))/g){
	$r_t++;
	my $state1 = $1;
	my $state2 = $2;
	my $arc = $3;
	#print "I matched $state1 $arc $state2\n";
	if ($in2 =~ m/s$state1 -> s$state2 .*arrowhead=$arc/){
	    $r_c++;
	}
    }
    print "\tRecall: $r_c/$r_t = '" . $r_c/$r_t . "'\n";

    my $p_c = 0;
    my $p_t = 0;
    #check precision
    while ($in2 =~ m/s([0-9]+) -> s([0-9]+) (.+), *arrowhead=((vee|tee))/g){
	$p_t++;
	my $state1 = $1;
	my $state2 = $2;
	my $mid = $3;
	my $arc = $4;

	my $remove_arc = 0;
	#print "I matched $state1 $arc $state2\n";
	if ($mid =~ m/label=\"[-]*([0-9]+[.]*[0-9]*)/){
	    my $num = $1;
	    $num = (int (10000 * $num)) / 10000;
#	    $mid =~ m/(label=\"[-]*)[0-9]+[.]*[0-9]*/$1$num/;
	    if ($num < $cutoff){
		$mid =~ s/color=\"[^\"]+/color=\"green/;
		$remove_arc = 1;
	    }
	}
	if (not $remove_arc){
#WE DO NOT NEED TO CHECK THIS AT THIS STAGE
#	    if ($in1 =~ m/s$state1 -> s$state2 .*arrowhead=$arc/){
#		$p_c++;
		print OUT "s$state1 -> s$state2 $mid,arrowhead=$arc]\n";
#	    }
#	    else{
#		print OUT "s$state1 -> s$state2 $mid,arrowhead=$arc,style=dashed]\n";
#	    }
	}
    }
    if ($p_t > 0){
	print "\tPrecision: $p_c/$p_t = '" . $p_c/$p_t . "'\n";
    }
    else{
	print "\tPrecision: $p_c/$p_t = '0'\n";
    }

    print OUT "\n}\n";

    close OUT;

    return ($r_c,$r_t,$p_c,$p_t);

}

