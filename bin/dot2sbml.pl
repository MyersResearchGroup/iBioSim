#!/usr/bin/perl

$mh_con = "500.0";

$spastic = 0.9;


sub getuid{
    return $uid++;
}

sub main{
    if ($#ARGV != 1){
	print "Usage: dot2sbml.pl dotfile outfile\n";
    }
    else{
	my $file = $ARGV[0];
	open (FILE, "$file") or die "I cannot open dot $file\n";
	@dot_file = <FILE>;
	close FILE;
	open (OUT, ">$ARGV[1]") or die "I cannot open out file $ARGV[1]\n";
	$dot_file = join ("", @dot_file);
	
	#create the normal sbml file
	fill_hashes();
	create_real_network($file);
    }

}


#THIS FUNCTION DOES NOT CORRECTLY REMOVE GENE
sub remove_downstream{
    my $gene = shift;
    my $gene_name = $genes[$gene];

    #check downstream and then remove either the activation or repression
    my @keys = keys %act;
    foreach my $key (@keys){
	my @g = split (/,/,$act{$key});
	if ($g[0] eq $gene_name){
	    #print "Activation: Checking $key and $act{$key} for a downstream gene from $gene, named $gene_name\n";
	    #print "\tFound a downstream gene using $act{$key}\n";
	    my $downstream = $g[1];
	    undef $act{$key};
	    $gwa{$downstream}--;
	    if ($gwa{$downstream} == 0){
		#print "\tIt is now not connected by activation\n";
		undef $gwa{$downstream};
	    }
	    $gwp{$downstream}--;
	    if ($gwp{$downstream} == 0){
		#print "\tIt is now not connected at all\n";
		undef $gwp{$downstream};
	    }
	}
    }

    @keys = keys %rep;
    foreach my $key (@keys){
	my @g = split (/,/,$rep{$key});
	if ($g[0] eq $gene_name){
	    #print "Repression: checking $key and $rep{$key} for a downstream gene from $gene, named $gene_name\n";
	    #print "\tFound a downstream gene using $rep{$key}\n";
	    my $downstream = $g[1];
	    undef $rep{$key};
	    $gwr{$downstream}--;
	    #print "\tnow there is $gwr{$downstream} gwr\n";
	    if ($gwr{$downstream} == 0){
		#print "\tIt is now not connected by repivation\n";
		undef $gwr{$downstream};
	    }
	    $gwp{$downstream}--;
	    #print "\tNow there is $gwp{$downstream} gwp\n";
	    if ($gwp{$downstream} == 0){
		#print "\tIt is now not connected at all\n";
		undef $gwp{$downstream};
	    }
	}
    }
}

sub fill_hashes{
    undef @genes;
    undef %mapping;
    undef @gene_symbols;
    undef %gwp;
    undef %gwa;
    undef %gwr;
    undef %rep;
    undef %act;
    undef %dimers;
    $uid = 0;
    

    while ($dot_file =~ m/(^|\n) *([^- \n]*) *\[.*label=\"([^\"]*)\"/g){
	#print "gene '$2' labeled '$3' found\n";
	if (defined $mapping{$2}){
	    print "Error, already have a mapping for $2: it is $mapping{$2}, trying to add another to $3\n";
	    exit(0);
	}
	$mapping{$2} = $3;
	$genes[$#genes+1] = $3;
	$gene_symbols[$#gene_symbols+1] = $2;
    }
    $num_genes = $#genes+1;
#    $rnap_con = "10";
    
    for ($i = 0; $i <= $#genes; $i++){
	$initial_concentration[$i] = "0.0";
    }

    while ($dot_file =~ m/(^|\n) *([^ \n]*) *\-\> *([^ \n]*) *\[(.*)arrowhead=([^,\]]*)(.*)/g){
	my $g1 = $2;
	my $g2 = $3;
	my $info = $4;
	my $arrowhead = $5;
	my $extra_info = $6;
	$g1 = $mapping{$g1};
	$g2 = $mapping{$g2};

	my $total_info = "$info,$extra_info";

	#fill in that the gene has a promoter
	$gwp{$g2}++;

	if ($arrowhead eq "vee"){
	    #print "ACT Found '$2' -> '$3' with arrowhead '$4'\n";
	    $gwa{$g2}++;
	    $act{"$g2:$gwa{$g2}"} = "$g1,$g2,$total_info";
	}
	elsif($arrowhead eq "tee"){
	    #print "REP Found '$2' -> '$3' with arrowhead '$4'\n";
	    $gwr{$g2}++;
	    $rep{"$g2:$gwr{$g2}"} = "$g1,$g2,$total_info";
	}
	else{
	    print "Found '$2' -> '$3' with arrowhead '$4'\n";
	    print "Unhandled arrowhead case $arrowhead: exiting\n";
	}
	if ($total_info =~ m/label="*([0-9]+[,|\"])/){ #"
	    print "Found a dimer for $g1 in '$total_info'\n";
	    my $d = $1;
	    if (exists($dimers{$g1}) and $dimers{$g1} != $d){
		print "ERROR: unhandled dimerization.  Unable to create different dimerizations, '$dimers{$g1}' and '$d'";
		exit(0);
	    }
	    else{
		$dimers{$g1} = $d;
	    }
	}
    }
}


sub create_real_network{
    my $abv_name = shift;

    print OUT "<?xml version=\"1.0\"?>\n<sbml xmlns=\"http://www.sbml.org/sbml/level2\" version=\"1\" level=\"2\">\n<model id=\"$abv_name\">\n<listOfCompartments>\n  <compartment id=\"default\" />\n</listOfCompartments>\n\n<listOfSpecies>\n";

    #Print the genes
    for ($i = 0; $i <= $#genes; $i++){
	print OUT "\t<species id = \"$genes[$i]\"  name = \"$genes[$i]\" compartment = \"default\" initialAmount = \"$initial_concentration[$i]\"/>\n";
    }
    #print the rnap
    print OUT "\t<species id = \"rnap\"  name = \"RNAP\" compartment = \"default\" initialAmount = \"30\"/>\n";

    foreach $name (@genes){
	if ($name =~ m/spastic/){
	}
	else{
	    #print out a promoter'
	    print OUT "\t<species id = \"pro_$name\"  name = \"pro_$name\" compartment = \"default\" initialAmount = \"1.0\"/>\n";
	    #print out the rnap bindings to the promoters species
	    print OUT "\t<species id = \"rnap_pro_$name\"  name = \"rnap_pro_$name\" compartment = \"default\" initialAmount = \"0.0\"/>\n";
	    
	    for (my $i = 1; $i <= $gwa{$name}; $i++){ #gene has activation promoter
		#print out the rnap bindings to the promoters species
		my @g = split (/,/,$act{"$name:$i"});
		while ($g[0] eq ""){
		    $i++;
		    @g = split (/,/,$act{"$name:$i"});
		}
		print OUT "\t<species id = \"rnap_apro_$i\_$name\"  name = \"rnap_apro_$i\_$name\" compartment = \"default\" initialAmount = \"0.0\"/>\n";
	    }
	    for (my $i = 1; $i <= $gwr{$name}; $i++){ #gene has repression promoter
		#print the bound state of the rep promoters
		my @g = split (/,/,$rep{"$name:$i"});
		while ($g[0] eq ""){
		    $i++;
		    @g = split (/,/,$rep{"$name:$i"});
		}
		print OUT "\t<species id = \"bound_rep_$i\_$name\"  name = \"bound_rep_$i\_$name\" compartment = \"default\" initialAmount = \"0.0\"/>\n";
	    }
	}
    }
    #print out the dimers
    foreach $key (keys %dimers){
	print OUT "\t<species id = \"$key\_$dimers{$key}\"  name = \"$key\_$dimers{$key}\" compartment = \"default\" initialAmount = \"0.0\"/>\n";
    }

    print OUT "</listOfSpecies>\n\n<listOfReactions>\n";

#setup the dedgadations
for (my $i = 0; $i <= $#genes; $i++){
    #do not allow genes with high mutated concentrations to degrade
    if (not $initial_concentration[$i] eq $mh_con){

	if (not $genes[$i] =~ m/spastic/i){
	    my $deg = 0.0003;
print OUT <<END;
<reaction id = "degradation_$genes[$i]" reversible="false">
  <listOfReactants>
    <speciesReference species = "$genes[$i]" stoichiometry = "1"/>
  </listOfReactants>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>k_deg</ci>
        <ci>$genes[$i]</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "k_deg" name = "k_deg" value = "$deg"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END
        }
	else{
	    #the gene is spastic, and the activation and deg_rates should match
print OUT <<END;
<reaction id = "degradation_$genes[$i]" reversible="false">
  <listOfReactants>
    <speciesReference species = "$genes[$i]" stoichiometry = "1"/>
  </listOfReactants>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <ci>k_deg</ci>
    </math>
    <listOfParameters>
      <parameter id = "k_deg" name = "k_deg" value = "$spastic"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END

	}
    }
}



#setup the dimers
foreach $key (keys %dimers){
print OUT <<END;
<reaction id = "dimerization_$key\_$dimers{$key}" reversible="false">
  <listOfReactants>
    <speciesReference species = "$key" stoichiometry = "$dimers{$key}"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "$key\_$dimers{$key}" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf_d</ci>
          <ci>$key</ci>
        </apply>
        <apply>
          <times/>
          <ci>kr</ci>
          <ci>$key\_$dimers{$key}</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf_d" name = "kf_d" value = "20"/>
      <parameter id = "kr" name = "kr" value = "1.0"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END

    
}


#setup the activation and degradation for the spastic things
#for (my $i = 0; $i <= $#genes; $i++){
#if ($genes[$i] =~ m/spastic/i){
#    #add in the degradation for the newly created gene
#print OUT <<END;
#<reaction id = "degradation_helper_$genes[$i]" reversible="false">
#  <listOfReactants>
#  <speciesReference species = "pro_$genes[$i]" stoichiometry = "1"/>
#  </listOfReactants>
#  <kineticLaw>
#    <math xmlns="http://www.w3.org/1998/Math/MathML">
#      <apply><times/><ci>kr_deg_helper_$genes[$i]</ci><ci>pro_$genes[$i]</ci></apply></math>
#    <listOfParameters>
#      <parameter id = "kr_deg_helper_$genes[$i]" name = "k" value = "1.0"/>
#    </listOfParameters>
#  </kineticLaw>
#</reaction>
#
#<reaction id = "activation_helper_$genes[$i]" reversible="false">
#  <listOfProducts>
#    <speciesReference species = "pro_$genes[$i]" stoichiometry = "1"/>
#  </listOfProducts>
#  <kineticLaw>
#    <math xmlns="http://www.w3.org/1998/Math/MathML">
#      <apply><times/><ci>kr_act_helper_$genes[$i]</ci><ci>pro_$genes[$i]</ci></apply></math>
#    <listOfParameters>
#      <parameter id = "kr_act_helper_$genes[$i]" name = "k" value = "1.0"/>
#    </listOfParameters>
#  </kineticLaw>
#</reaction>
#
#END
#
#}
#}


#setup main gene generation pathways rnap binding and then ocr
foreach $name (@genes){
    my $rnap_binding = 0.033;
    my $ocr = 0.01;
    my $uid = getuid();

    #print "Checking $name for turnoff\n";
    if ($turn_off{$name} != 1){
	if ($name =~ m/spastic/i){
print OUT <<END;
<reaction id = "creation_$name" reversible="false">
  <listOfProducts>
    <speciesReference species = "$name" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <ci>kf_un</ci>
    </math>
    <listOfParameters>
      <parameter id = "kf_un" name = "kf_un" value = "$spastic"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END
	}
	else{
	    if ($gwa{$name} == 0 and $gwr{$name} > 0){
		#is not activated give it the full rate
		$ocr = 0.1;
	    }
print OUT <<END; 
<reaction id = "R_rnap_$name" reversible="true">
  <listOfReactants>
    <speciesReference species = "rnap" stoichiometry = "1"/>
    <speciesReference species = "pro_$name" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_pro_$name" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf</ci>
          <ci>pro_$name</ci>
          <ci>rnap</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>rnap_pro_$name</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$rnap_binding"/>
      <parameter id = "kr" name = "kr" value = "1.0"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

<reaction id = "R_rnap_pro_$name" reversible="false">
  <listOfReactants>
    <speciesReference species = "rnap_pro_$name" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "$name" stoichiometry = "1"/>
    <speciesReference species = "rnap_pro_$name" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>koc</ci>
        <ci>rnap_pro_$name</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "koc" name = "koc" value = "$ocr"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END
}


#setup the activation pathway
    for (my $i = 1; $i <= $gwa{$name}; $i++){ #gene has activation promoter
        my $uid = getuid();
        my @g = split (/,/,$act{"$name:$i"});
        while ($g[0] eq ""){
            $i++;
            @g = split (/,/,$act{"$name:$i"});
        }
#        my $bind_rate = 0.0011;
        my $bind_rate = 0.00033;
        my $a_rate = 0.1;
#	if ($g[2] =~ m/label=([0-9]+[.]*[0-9]*)/){
#	    $a_rate = $1;
#	}

	my $activating_species = $g[0];
	if ($act{"$name:$i"} =~ m/label="*([0-9]+)/){
	    $activating_species = "$g[0]\_$1";
	}

print OUT <<END; 

<reaction id = "R_$i\_$activating_species\_activates_$g[1]" reversible="true">
  <listOfReactants>
    <speciesReference species = "rnap" stoichiometry = "1"/>
    <speciesReference species = "pro_$g[1]" stoichiometry = "1"/>
    <speciesReference species = "$activating_species" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_apro_$i\_$g[1]" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
          <apply>
            <times/>
            <ci>kf</ci>
            <ci>rnap</ci>
            <ci>pro_$g[1]</ci>
            <ci>$g[0]</ci>
          </apply>
        <apply>
          <times/>
          <ci>kr</ci>
          <ci>rnap_apro_$i\_$g[1]</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$bind_rate"/>
      <parameter id = "kr" name = "kr" value = "1.0"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

<reaction id = "R_$i\_activation_part2_$g[0]_to_$g[1]" reversible="false">
  <listOfReactants>
    <speciesReference species = "rnap_apro_$i\_$g[1]" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "$g[1]" stoichiometry = "1"/>
    <speciesReference species = "rnap_apro_$i\_$g[1]" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>koc</ci>
        <ci>rnap_apro_$i\_$g[1]</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "koc" name = "koc" value = "$a_rate"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
}


#setup the repression pathway
    for (my $i = 1; $i <= $gwr{$name}; $i++){ #gene has repression promoter
	my $uid = getuid();
	my @g = split (/,/,$rep{"$name:$i"});
	#if we removed an earilier $i, we need to increment to the next one
	while ($g[0] eq ""){
	    $i++;
	    @g = split (/,/,$rep{"$name:$i"});
	}
#	my $r_rate = 0.033;
	my $r_rate = 0.8;
#	if ($g[2] =~ m/label=([0-9]+[.]*[0-9]*)/){
#	    $r_rate = $1;
#	}

	my $repression_species = $g[0];
	if ($rep{"$name:$i"} =~ m/label="*([0-9]+)/){
	    $repression_species = "$g[0]\_$1";
	}
print OUT <<END; 

<reaction id = "R_$i\_$repression_species\_represses_$g[1]" reversible="true">
    <listOfReactants>
      <speciesReference species = "pro_$g[1]" stoichiometry = "1"/>
      <speciesReference species = "$repression_species" stoichiometry = "1"/>
    </listOfReactants>
    <listOfProducts>
      <speciesReference species = "bound_rep_$i\_$g[1]" stoichiometry = "1"/>
    </listOfProducts>
    <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf_rep</ci>
          <ci>pro_$g[1]</ci>
          <ci>$g[0]</ci>
        </apply>
        <apply>
          <times/>
          <ci>kr</ci>
          <ci>bound_rep_$i\_$g[1]</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf_rep" name = "kf_rep" value = "$r_rate"/>
      <parameter id = "kr" name = "kr" value = "1.0"/>
    </listOfParameters>
    </kineticLaw>
</reaction>
END


}
}
}



print OUT <<END;
</listOfReactions>
</model>
</sbml>
END
}



main();

