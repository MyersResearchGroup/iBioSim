#!/usr/bin/perl

#This file writes the checked dot file.

if ($#ARGV == 1){
    print "Normal Usage: ./check_dot.pl Unchecked_Dot, Master_Dot, Out_Dot\n";
    print "Assumed usage: ./check_dot.pl Unchecked_Dot, [find the masterdot ] Out_Dot\n";
    $assumed = $ARGV[1];
    $assumed =~ s/(.*)\/(.*)\/(.*)\/([^\/]*).dot/$1\/$2\/$2.dot/;
    if (not -e "$assumed"){
	print "ERROR: Unable to find a master dot file $assumed from $ARGV[1]\n";
	exit(1);
    }

    $unchecked = $ARGV[0];
    $master_dot = $assumed;
    $out = $ARGV[1];

}
elsif ($#ARGV != 2){
    print "Usage: ./check_dot.pl Unchecked_Dot, Master_Dot, Out_Dot\n";
    exit(1);
}
else{
    $unchecked = $ARGV[0];
    $master_dot = $ARGV[1];
    $out = $ARGV[2];
}



if (not -e "$master_dot"){
    print "ERROR: unable to check correctness for non exsistant? $master_dot\n";
    exit(1);
}
if (not -e "$unchecked"){
    print "ERROR: unable to check correctness for non exsistant? '$unchecked'\n";
    exit(1);
}
open (IN1, "$master_dot") or die "I cannot check dot correctness for $master_dot\n";
open (IN2, "$unchecked") or die "I cannot check correctness for $filename\/method.dot\n";
open (CHECKED, ">$out") or die "I cannot write the checked file\n";

my @in1 = <IN1>;
my @in2 = <IN2>;
close IN1;
close IN2;

my $in1 = join ("",@in1);
my $in2 = join ("",@in2);

$in1 =~ s/sp_//g;
$in2 =~ s/sp_//g;

print CHECKED "digraph G {\n";
#generate the states in the corrected output
for (my $i = 0; $i <= $#in2; $i++){
    if ($in2[$i] =~ m/shape=ellipse/){
	print CHECKED $in2[$i];
    }
}

#draw things acording to the following plan
#        \ True
#Reported \   a            r     n
#          \ ___________________________
#      a    | blue   | red    | black   |
#           | solid  | dashed | dashed  |
#           | normal | tee    | onormal |
#           |-----------------|---------|
#      r    | blue   | red    | black   |
#           | dashed | solid  | dashed  |
#           | normal | tee    | obox    |
#           |-----------------|---------|
#      n    | blue   | red    |         |
#           | dotted | dotted |         |
#           | normal | tee    |         |
#           -----------------------------
#
#Check the first 2 columns above
while ($in1 =~ m/s([0-9]+) -> s([0-9]+) (.+)arrowhead= *((vee|tee))/g){
    my $state1 = $1;
    my $state2 = $2;
    my $mid = $3;
    my $arc = $4;
#    if ($mid =~ m/label=\"[-]*([0-9]+[.]*[0-9]*)/){
#	my $num = $1;
#	$num = (int (10000 * $num)) / 10000;
#	if ($num < $green_level){
#	    $mid =~ s/color=\"[^\"]+/color=\"green/;
#	}
#    }
    if ($in2 =~ m/s$state1 -> s$state2 (.*)arrowhead=$arc/){
	$tmp = $1;
	$arc =~ s/vee/normal/;
	print CHECKED "s$state1 -> s$state2 $tmp arrowhead=$arc]\n";
    }
    elsif ($in2 =~ m/s$state1 -> s$state2 (.*)arrowhead=/){
	$tmp = $1;
	if ($tmp =~ m/blue/){
	    $tmp =~ s/blue/firebrick/;
	}
	else{
	    $tmp =~ s/firebrick/blue/;
	}
	$arc =~ s/vee/normal/;
	print CHECKED "s$state1 -> s$state2 $tmp style=dashed, arrowhead=$arc]\n";
    }
    else{
	$arc =~ s/vee/normal/;
	#$mid =~ s/color=\"[^\"]+/color=\"gray/;
	print CHECKED "s$state1 -> s$state2 $mid style=dotted, arrowhead=$arc]\n";
    }
}
#Check the third column
while ($in2 =~ m/s([0-9]+) -> s([0-9]+) (.+)arrowhead=((vee|tee))/g){
    my $state1 = $1;
    my $state2 = $2;
    my $mid = $3;
    my $arc = $4;
    if ($in1 =~ m/s$state1 -> s$state2 /){
	#do nothing as this was already taken care of above
    }
    else{
	$mid =~ s/color=\"[^\"]+/color=\"black/;
	$arc =~ s/(vee|normal)/onormal/;
	$arc =~ s/tee/obox/;
	print CHECKED "s$state1 -> s$state2 $mid style=dashed, arrowhead=$arc]\n";
    }
}




print CHECKED "\n}\n";

close CHECKED;

 
