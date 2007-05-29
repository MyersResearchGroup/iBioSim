#!/usr/bin/perl


$genenet_dir = $ARGV[0];
$reports_dir = $ARGV[1];
$file_name_to_use = $ARGV[2];
$out_name = $ARGV[3];

#remove the last / in dir
$reports_dir =~ s/\/$//;


sub main{
    my @files = `ls $genenet_dir*/*/*/$file_name_to_use`;
    
    print "Sorting dir\n";
    @files = dir_sort(\@files);
    print "Done sorting dir\n";
    if ($#files < 1){
	print "Error, no files found\n";
	exit(1);
    }

    open (OUT, ">$reports_dir/$out_name") or die "Cannot open out file '$reports_dir/$out_name'\n";
    print OUT ",,,,,GeneNet,,GeneNet,,,GeneNet Time,,,,,,,\n";
    #print OUT "Name,# Genes,# Experiments,Sample Interval,Exp Duration,R,P,R,P,R/R,P/P, # Correct (R P), Total Arcs (R), Reported Arcs (P), # Correct (R P), Total Arcs (R), Reported Arcs (P),\n";
    print OUT "Name,Genes,#Exp,S Size,Dur,R,P,# Correct (R P), Total Arcs (R), Reported Arcs (P),user,system,elapsed,CPU,major pagefaults,minor pagefaults, swaps\n";

    my $exp_name = $files[0];
    my @running_genenet;

    for (my $i = 0; $i <= $#files; $i++){
	$file = $files[$i];
	$file =~ s/[.]dot//;
	$file =~ s/\n//;
	print "Checking $file\n";
	if (not_matching($file,$exp_name)){
	    add_overview($exp_name);
	    $exp_name = $file;
	    
	    print OUT ",,,,";
	    write_final(@running_genenet);
	    print OUT "\n";
	    undef @running_genenet;
	}
	
	
	@tmp1 = check_correctness("$file");
	@running_genenet = addit(\@running_genenet,\@tmp1);
	write_out_file($file);
	write_initial(@tmp1);
	if ($tmp1[0] != $tmp1[2]){
	    print OUT ",ERROR: $tmp1[0] $tmp1[2],$tmp1[1],$tmp1[3]";
	}
	else{
	    print OUT ",$tmp1[0],$tmp1[1],$tmp1[3]";
	}
	write_time();
	print OUT "\n";
	#print "Done with $file\n";
	
    }

    print "Adding overview\n";
    add_overview($file);

    print OUT ",,,,";
    write_final(@running_genenet);
    print OUT "\n\n";
    print OUT "Overview:\n$overview\n\n";
}

sub add_overview{
    my $exp_name = shift;
    if ($exp_name =~ m/^.*\/([^\/]*)_[0-9]+\/[^\/]*_([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+)\//){
	$overview = "$overview$1,$3,$4,$5,$6";
    }
    else{
	$exp_name =~ s/.*?\/(.*?)\/.*/$1/;
	$exp_name =~ s/[.]*//;
	$overview = "$overview$exp_name,,,,";
    }

}



sub not_matching{
    my $a = shift;
    my $b = shift;
    if ($a =~ m/^(.*)_([0-9]+)\/[^\/]*_([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+)\//){
	my @a = ($1,$2, $3, $4, $5,$6);
	if ($b =~ m/^(.*)_([0-9]+)\/[^\/]*_([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+)\//){
	    my @b = ($1,$2, $3, $4, $5, $6);
	    if ($a[0] eq $b[0]){
		my @cmp = (5,4,3);
		for (my $i = 0; $i <= $#cmp; $i++){
		    my $ind = $cmp[$i];
		    if (not ($a[$ind] == $b[$ind])){
			return 1;
		    }
		}
		return 0;
	    }
	}
    }
    return 1;
}

sub addit{
    $a1 = shift;
    $a2 = shift;
    @a1 = @$a1;
    @a2 = @$a2;
    for (my $i = 0; $i <= $#a2; $i++){
	$a1[$i] += $a2[$i];
    }
    return @a1;
}

sub write_out_file{
    my $file = shift;
    if ($file =~ m/^.*\/(.*_[0-9]+)\/[^\/]*_([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+)\//){
	my $name = $1;
	my $num_genes = $2;
	my $experiments = $3;
	my $interval = $4;
	my $ending_time = $5;
	print OUT "$name,$num_genes,$experiments,$interval,$ending_time";
    }
    else{
	#print "Error matching $file\n";
	$file =~ s/.*?\/(.*?)\/.*/$1/;
	print OUT "$file,,,,";
    }
}

sub write_final{
    my $a0 = shift;
    my $a1 = shift;
    my $a2 = shift;
    my $a3 = shift;
    $tmp_name = write_double($a0,$a1);
    print OUT $tmp_name;
    $overview = "$overview$tmp_name";

    $tmp_name = write_double($a2,$a3);
    print OUT $tmp_name;
    $overview = "$overview$tmp_name\n";
    print OUT "\n";
}

sub write_initial{
    my $a0 = shift;
    my $a1 = shift;
    my $a2 = shift;
    my $a3 = shift;

    print OUT write_double($a0,$a1);
    print OUT write_double($a2,$a3);
}

sub write_double{
    my $a = shift;
    my $b = shift;
    if ($b != 0){
	return ",=$a/$b";
    }
    else {
	return ",u $a/$b";
    }
}

sub write_time{
#0.51user 0.00system 0:00.52elapsed 98%CPU (0avgtext+0avgdata 0maxresident)k
#0inputs+0outputs (0major+853minor)pagefaults 0swaps
    my $f = "$file";
    my $tmp = $file_name_to_use;
    $tmp =~ s/(.*)_.*/$1\_time.txt/;
    $f =~ s/(.*)\/.*/$1\/$tmp/;
    if (-e "$f"){
	open (A, "$f") or die "Cannot open time file $f\n";
	my @a = <A>;
	close A;

	my $a = join ("",@a);
	$a =~ s/\n//g;
	if ($a =~ m/([0-9]+[.]*[0-9]*)user ([0-9]+[.]*[0-9]*)system ([0-9]+[:][0-9]+[.:]*[0-9]*)elapsed ([0-9]+[.]*[0-9]*).CPU .0avgtext.0avgdata 0maxresident.k0inputs.0outputs .([0-9]+)major.([0-9]+)minor.pagefaults ([0-9]+)swaps/){
	    my $out = ",$1,$2,$3,$4,$5,$6,$7";
	    print OUT $out;
	}
	else{
	    print "Unable to match\n'$a'\n";
	    exit(1);
	}

    }
    else{
	print "ERROR: Cannot find time file '$f'\n";
	exit(1);
    }

}

sub check_correctness{
    my $filename = shift;
    my $dot_file = $filename;
    $dot_file =~ s/(.*)\/(.*)\/(.*)\/([^\/]*)/$1\/$2\/$2.dot/;
    #$filename =~ s/[.]csv/.dot/g;
    $filename = "$filename.dot";
    #$dot_file = "$dot_file.dot";

    if (not -e "$dot_file"){
	print "ERROR: unable to check correctness for non exsistant? dot $dot_file\n";
	exit(1);
    }
    if (not -e "$filename"){
	print "ERROR: unable to check correctness for non exsistant? file '$filename'\n";
	exit(1);
    }
    open (IN1, "$dot_file") or die "I cannot check dot correctness for $dot_file\n";
    open (IN2, "$filename") or die "I cannot check correctness for $filename\n";

    my @in1 = <IN1>;
    my @in2 = <IN2>;
    close IN1;
    close IN2;

    my $in1 = join ("",@in1);
    my $in2 = join ("",@in2);

    $in1 =~ s/sp_//g;
    $in2 =~ s/sp_//g;

    my $r_c = 0;
    my $r_t = 0;
    #check precision
    while ($in1 =~ m/s([0-9]+) -> s([0-9]+) .+arrowhead=((vee|tee))/g){
	$r_t++;
	my $state1 = $1;
	my $state2 = $2;
	my $arc = $3;
	if ($in2 =~ m/s$state1 -> s$state2 .*arrowhead=$arc/){
	    $r_c++;
	}
    }
    #print "\tRecall: $r_c/$r_t = '" . $r_c/$r_t . "'\n";
    #print LOG ",Recall: $r_c/$r_t =," . $r_c/$r_t . "\n";

    my $p_c = 0;
    my $p_t = 0;
    #check precision
    while ($in2 =~ m/s([0-9]+) -> s([0-9]+) (.+)arrowhead=((vee|tee))/g){
	$p_t++;
	my $state1 = $1;
	my $state2 = $2;
	my $mid = $3;
	my $arc = $4;
	if ($in1 =~ m/s$state1 -> s$state2 .*arrowhead=$arc/){
	    $p_c++;
	}
    }
    if ($p_t > 0){
	#print "\tPrecision: $p_c/$p_t = '" . $p_c/$p_t . "'\n";
	#print LOG ",Precision: $p_c/$p_t = ," . $p_c/$p_t . "\n";
    }
    else{
	#print "\tPrecision: $p_c/$p_t = '0'\n";
	#print LOG ",Precision: $p_c/$p_t = ,0\n";
    }

    return ($r_c,$r_t,$p_c,$p_t);

}

sub dir_sort_b{
    my $a = shift;
    my $b = shift;
    if ($a =~ m/(.*?)_([0-9]+)_/){
	my $n1 = $1;
	my $i1 = $2;
	if ($b =~ m/(.*?)_([0-9]+)_/){
	    my $n2 = $1;
	    my $i2 = $2;
	    if ($n1 eq $n2 and $1 != $i2){
		return $i1 <=> $i2;
	    }
	}	
    }
    return $a cmp $b;
}

sub dir_sort_a{
    #order is 0 5 4 3 1 - 2 is the # genes which should match
    #       cmp = = = =
    my $a = shift;
    my $b = shift;

    if ($a =~ m/^(.*)_([0-9]+)\/[^\/]*_([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+)\//){
	my @a = ($1,$2, $3, $4, $5,$6);
	if ($b =~ m/^(.*)_([0-9]+)\/[^\/]*_([0-9]+)_([0-9]+)_([0-9]+)_([0-9]+)\//){
	    my @b = ($1,$2, $3, $4, $5, $6);
	    if ($a[0] eq $b[0]){
		my @cmp = (5,4,3,1);
		for (my $i = 0; $i <= $#cmp; $i++){
		    my $ind = $cmp[$i];
		    if (not ($a[$ind] == $b[$ind])){
			return $a[$ind] <=> $b[$ind];
		    }
		}
		return $a[2] <=> $b[2];
	    }
	    else{
		return $a[0] cmp $b[0];
	    }
	}
    }
    return dir_sort_b($a,$b);
}

sub dir_sort{
    my $d = shift;
    my @d = @$d;
    @d = sort {dir_sort_a($a,$b)} (@d);
    return @d;
}



main();

