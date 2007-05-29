#!/usr/bin/perl



#NOTE!!!!!!!!!!!!!!!!!!
#This file only writes the checked dot file.
#In REPORTS, a similar function is called to determine recall and precision called check_correctness"
#gen_current_method_dot_overview.pl contains this function.

if ($#ARGV == 1){
    print "Normal Usage: ./check_dot.pl Unchecked_Dot, Master_Dot, Out_Dot\n";
    print "Assumed usage: ./check_dot.pl Unchecked_Dot, [find the masterdot ] Out_Dot\n";
    $assumed = $ARGV[1];
    $assumed =~ s/(.*)\/(.*)\/(.*)\/([^\/]*).dot/$1\/$2\/$2.dot/;
    if (not -e "$assumed"){
	print "ERROR: Unable to find a master dot file at $assumed\n";
	exit(1);
    }
    $ARGV[2] = $ARGV[1];
    $ARGV[1] = $assumed;

}
elsif ($#ARGV != 2){
    print "Usage: ./check_dot.pl Unchecked_Dot, Master_Dot, Out_Dot\n";
    exit(1);
}

$unchecked = $ARGV[0];
$dot_file = $ARGV[1];
$out = $ARGV[2];


if (not -e "$dot_file"){
    print "ERROR: unable to check correctness for non exsistant? $dot_file\n";
    exit(1);
}
if (not -e "$unchecked"){
    print "ERROR: unable to check correctness for non exsistant? '$filename/method.dot'\n";
    exit(1);
}
open (IN1, "$dot_file") or die "I cannot check dot correctness for $dot_file\n";
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

while ($in2 =~ m/s([0-9]+) -> s([0-9]+) (.+)arrowhead=((vee|tee))/g){
    my $state1 = $1;
    my $state2 = $2;
    my $mid = $3;
    my $arc = $4;
    if ($mid =~ m/label=\"[-]*([0-9]+[.]*[0-9]*)/){
	my $num = $1;
	$num = (int (10000 * $num)) / 10000;
	if ($num < $green_level){
	    $mid =~ s/color=\"[^\"]+/color=\"green/;
	}
    }
    if ($in1 =~ m/s$state1 -> s$state2 .*arrowhead=$arc/){
	print CHECKED "s$state1 -> s$state2 $mid arrowhead=$arc]\n";
    }
    else{
	print CHECKED "s$state1 -> s$state2 $mid arrowhead=$arc,style=dashed]\n";
    }
}

print CHECKED "\n}\n";

close CHECKED;

