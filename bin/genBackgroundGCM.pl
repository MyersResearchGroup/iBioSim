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

open (IN, "run-1.tsd");
@in = <IN>;
close IN;

open (OUT, ">background.gcm");
$in = join("",@in);
@in = split (/\),\(/,$in);
$in = $in[0];
$in =~ s/^(.*?)\),\(/$1/;
$in =~ s/\"//g;
$in =~ s/\(|\)//g;

print "got '$in'\n";

@in = split (/,/,$in);



print OUT "diagraph G {\n";

for ($i = 1; $i <= $#in; $i++){
    my $a = $in[$i];
    print OUT "$a [ID=$a,Name=\"$a\",Type=normal,shape=ellipse,label=\"$a\"]\n";

}
print OUT "}\nGlobal {\n}\nPromoters {\n}\nSBML file=\"\"\n"
