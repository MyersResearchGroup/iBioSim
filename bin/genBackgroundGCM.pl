#!/usr/bin/perl

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
