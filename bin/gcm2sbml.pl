#!/usr/bin/perl -w
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

use Getopt::Std;

#Global indicing variables
$STATE = 0;
$PROTEIN = 1;
$CONST = 2;

$PROMOTER = 0;
$INPUT = 1;
$OUTPUT = 2;
$ARROWHEAD = 3;
$STOC = 4;
$TYPE = 5;

#Global reaction params
$deg = 0.0003;
$kf_dimer = 20.0;
$kr_dimer = 1.0;

$kf_complex = 20.0;
$kr_complex = 1.0;

$kf_rep = 0.8;
$kr_rep = 1.0;

$kf_bind_dimer = 10.0;
$kr_bind_dimer = 1.0;

$kf_act = .00033;
$kr_act = 1.0;

$rnap_binding = 0.033;
$rnap_unbinding = 1.0;
$ocr = 0.1;
$unactived_production = 0.01;
$activated_production = 0.1;
$num_promo = 1.0;
$stochiometry = 1.;
$num_RNAP = 30.0;
$dimer_deg = .0003;

$spastic = 0.9;

sub getuid{
    return $uid++;
}

sub main{    
    if ($#ARGV == 1){
        #open up the dotfile and store it to an array
	my $file = $ARGV[0];
	$abv_name = $ARGV[0];
	open (FILE, "$file") or die "I cannot open dot $file\n";
	@dot_file = <FILE>;
	close FILE;
        #open out outfile and start writing to it when writing to OUT
	open (OUT, ">$ARGV[1]") or die "I cannot open out file $ARGV[1]\n";	
	$dot_file = join ("", @dot_file);		
	
	#create the normal sbml file
	fill_hashes();
        build_reactions();
#        print_all_info();
	
	create_real_network($file);
    } elsif ($#ARGV >= 2) {
        getopts ('dcf:');
        if ($opt_c) {
            print "Applying biochemical abstraction\n";
        }
        if ($opt_d) {
            print "Applying dimer degradation\n";
        }        
        #open up the dotfile and store it to an array
        $abv_name = $ARGV[$#ARGV-1];
	my $file = $ARGV[$#ARGV-1];
	open (FILE, "$file") or die "I cannot open dot $file\n";
	@dot_file = <FILE>;	
	close FILE;
	
	if ($opt_f) {
	    $file = $opt_f;
            open (FILE, "$file") or die "I cannot open parameterfile $file\n";
            @parameter_file = <FILE>;	
            close FILE;	
            $parameter_file = join ("", @parameter_file);		            
            load_parameters();
        }
	
        #open out outfile and start writing to it when writing to OUT
	open (OUT, ">$ARGV[$#ARGV]") or die "I cannot open out file $ARGV[$#ARGV]\n";	
	$dot_file = join ("", @dot_file);		
	
	#create the normal sbml file
	fill_hashes();
        build_reactions();
#        print_all_info();
	
	create_real_network($file);
        
    } else {
        print "Usage: dot2sbml.pl dotfile outfile or dot2sbml.pl -options dotfile outfile\n";
    } 

}

sub load_parameters{
        print "Loading parameters\n";
        if ($parameter_file =~ m/deg[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing degradation to: $1\n";
            $deg = $1;
        } 
        if ($parameter_file =~ m/dimer_deg[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing dimer degradation to: $1\n";
            $dimer_deg = $1;
        }         
        if ($parameter_file =~ m/kf_dimer[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing dimer formation to: $1\n";
            $kf_dimer = $1;
        }
        if ($parameter_file =~ m/kr_dimer[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing dimer breaking to: $1\n";
            $kr_dimer = $1;
        }         
        if ($parameter_file =~ m/kf_complex[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing complex formation to: $1\n";
            $kf_complex = $1;
        } 
        if ($parameter_file =~ m/kr_complex[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing complex breakup to: $1\n";
            $kr_complex = $1;
        } 
        if ($parameter_file =~ m/kf_rep[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing repression binding to: $1\n";
            $kf_rep = $1;
        } 
        if ($parameter_file =~ m/kr_rep[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing repression unbinding to: $1\n";
            $kr_rep = $1;
        } 
        if ($parameter_file =~ m/kf_act[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing activation binding to: $1\n";
            $kf_act = $1;
        } 
        if ($parameter_file =~ m/kr_act[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing activation unbinding to: $1\n";
            $kr_act = $1;
        } 
        if ($parameter_file =~ m/rnap_binding[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing rnap binding to: $1\n";
            $rnap_binding = $1;
        } 
        if ($parameter_file =~ m/rnap_unbinding[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing rnap unbinding to: $1\n";
            $rnap_unbinding = $1;
        } 
        if ($parameter_file =~ m/ocr[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing open complex production reaction binding to: $1\n";
            $ocr = $1;
        } 
        if ($parameter_file =~ m/basal[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing open complex production (basal): $1\n";
            $unactived_production = $1;
        }
        if ($parameter_file =~ m/activated[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing open complex production (activated): $1\n";
            $activated_production = $1;
        }
        if ($parameter_file =~ m/dimer_binding[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing dimer binding rate: $1\n";
            $kf_bind_dimer = $1;
        }
        if ($parameter_file =~ m/dimer_unbinding[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing dimer unbinding rate: $1\n";
            $kr_bind_dimer = $1;
        }
        if ($parameter_file =~ m/stochiometry[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Stochiometry: $1\n";
            $stochiometry = $1;
        }         
        if ($parameter_file =~ m/promoters[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing number of promoters: $1\n";
            $num_promo = $1;
        }
        if ($parameter_file =~ m/RNAP[\s]*=[\s]*([0-9]*\.?[0-9]*)/) {
            print "Changing RNAP: $1\n";
            $num_RNAP = $1;
        } 
}

sub fill_hashes{
    undef %dimers;     
    undef %proteins;  #list of proteins in the soup
    undef %promoters; #list of promoters
    undef %biochem; #list of biochem reactions
    undef %states; #list of states to proteins
    undef %spastics; #list of spastic proteins
    
    $uid = 0;
#   print "Try to parse inside bracket of species\n";
    while ($dot_file =~ m/(^|\n) *([^- \n]*) *\[(.*)\]/g){
        my $constant;
        my $protein;
        my $state = $2;
        my $temp = $3;
        my @struc;
#        print "$temp\n";
        # Parse the label
        if ($temp =~ m/label=\"(.*)\"/) {            
#           print "Protein Name: $1\n";
            $protein = $1;
            if ($protein =~ m/spastic/){
                $spastics{$protein} =$protein;
            }
        } else {
            print "Error, must have label for $state\n";
	    exit(0);
        } 
#        print "$temp\n";
        if ($temp =~ m/const=([^,|\s]*)/) {
#           print "Constant Source: true\n";
            if ($1 eq "true") {
                $constant = 1;
            } else {
                $constant = 0;
            }
        } else {
#           print "Constant Source: false\n";
            $constant = 0;
        }
        #State name, protein name, is constant?, number of sources
        @struct = ($state, $protein, $constant, 0);

        $proteins{$state} = [@struct];
        $states{$protein}=$state;
    }
#    print "Done parse inside bracket of species\n";
#    print "Try to parse inside bracket of edge\n";
    while ($dot_file =~ m/(^|\n) *([^ \n]*) *\-\> *([^ \n]*) *\[(.*)arrowhead=([^,\]]*)(.*)/g){
	my $start = $2;
	my $end = $3;
	my $info = $4;
	my $arrowhead = $5;
	my $extra_info = $6;
	my $stoc = 1;
	
        my $promoter;	
        my $type="regular";        
	
        # custom data struct to hold information
	my @struct;
	
	my $total_info = "$info,$extra_info";

	if ($total_info =~ m/label="*([0-9]+)[,|\"]/){
            #dimerization for first species
#	    print "Found a dimer for $start in '$total_info'\n";
	    $stoc = $1;
	    if (exists($dimers{$start}) and $dimers{$start} != $stoc){
		print "ERROR: unhandled dimerization.  Unable to create different dimerizations, '$dimers{$start}' and '$start'";
		exit(0);
	    }
	    else{
		$dimers{$start} = $stoc;
	    }
        }

        if ($total_info =~ m/promoter="(.*)"/){
#            print "Found promoter $1 in '$total_info'\n";
            $promoter = $1;
        } else {
            # If no promoter name is given, use the default promoter, the end name
            #my $uid = getuid();
            $promoter = "Promoter_$end";
        }
        if ($total_info =~ m/type=(.*)[,]/){        
#           print "Found biochemical reaction\n";
            $type = $1;
        } 
        build_interaction(($promoter, $proteins{$start}[1], $proteins{$end}[1], $arrowhead, $stoc, $type));        
#	if ($total_info =~ m/label="*([0-9]+[,|\"])/){ #"
#	}
    }
#    print "Done parse inside bracket of edge\n";
}

#Builds the interaction map used to generate the SBML
sub build_interaction {
    my @params = @_;
#    print "Printing params\n@params\n";
    #TODO: Check for problems if species used in more than 1 reaction
    #Check to see if the promoter exists yet, if not, add it
    if (not exists($promoters{$params[$PROMOTER]})) {
        my @input = [[{$params[$INPUT]=>$params[$STOC]}, $params[$TYPE], $params[$ARROWHEAD]]];
#        print "First promo addr: @input\n";
        $promoters{$params[$PROMOTER]} = [$params[$PROMOTER], @input, {$params[$OUTPUT]=>$params[$OUTPUT]}];
    }
    #If it does exist, check to see if it is a biochem reaction, and if so, see if it can be combined
    #with an existing input
    else {
        my $found = 0;                
        #Check to see if it can be added to any reactions
        #by cycling through each promoter and checking the input arrays
        #and checking to see if the arrowhead matches
        for (my $i = 0; $i <= $#{$promoters{$params[$PROMOTER]}[$INPUT]}; $i++) {
            @aref = $promoters{$params[$PROMOTER]}[$INPUT][$i];            
            if ($aref[0][2] eq $params[$ARROWHEAD] and $aref[0][1] eq "biochemical") {
                $aref[0][0]{$params[$INPUT]} = $params[$STOC];
                $found = 1;                      
                last;
            } elsif ($aref[0][2] eq $params[$ARROWHEAD]){
                $aref[0][0]{$params[$INPUT]} = $params[$STOC];
                $found = 1;                      
                last;
            }
        }

        if ($found == 0) {
            my @input = [{$params[$INPUT]=>$params[$STOC]}, $params[$TYPE], $params[$ARROWHEAD]];
            push(@{$promoters{$params[$PROMOTER]}[1]}, @input);
        }
        $promoters{$params[$PROMOTER]}[$OUTPUT]{$params[$OUTPUT]}=$params[$OUTPUT];
    }
}

#Removes all duplicate biochemical reactions
#Might not work if there's more than 1 reaction something can go to
#or if one reaction uses the same proteins as another
sub build_reactions {
    #cycle through each promoter and check to see if there is a biochemical reaction
    foreach $key  (keys %promoters) {        
        for ($i = 0; $i <= $#{$promoters{$key}[$INPUT]}; $i++) {                 
            my @temp = $promoters{$key}[$INPUT][$i];
            if ($temp[0][1] eq "biochemical") {
                my @reactants;
                foreach $t (keys %{@{$temp[0]}[0]}) {push(@reactants, $t);}
                $complex = get_complex(@reactants);
                if ($complex eq "") {
                    $complex = "";
                    foreach $ref (@reactants) {$complex = "$complex\_$ref";}
                    $complex = "Complex$complex";
                    push(@{$biochem{$complex}},@reactants);
                      
                }                
            }
        }
    }
}

sub get_num_act {
  my $key = $_[0];
  my $num = 0;
  for ($i = 0; $i <= $#{$promoters{$key}[$INPUT]}; $i++) {            
      my @temp = $promoters{$key}[$INPUT][$i];
      if ($temp[0][2] eq "vee") {$num++;}
  }
  return $num;
}

sub get_num_rep {
  my $key = $_[0];
  my $num = 0;
  for ($i = 0; $i <= $#{$promoters{$key}[$INPUT]}; $i++) {            
      my @temp = $promoters{$key}[$INPUT][$i];
      if ($temp[0][2] eq "tee") {$num++;}
  }
  return $num;
}

sub get_complex {
    @params = @_;
    print "Passed: @params\n";
    foreach $key (keys %biochem) {
        my $all_found = 0;               
        foreach $ref (@params) {
            $all_found = 1;
            if (not ($key =~ m/$ref/)){
                print "Couldn't find $ref in $key\n";
                $all_found = 0;
                last;
            }
        }
        if ($all_found == 1) {
            print "Found $key\n";
            return $key;
            last;
        }                            
    }
    print "Couldn't find @params\n";
    return "";
}

sub print_all_info {
   print "State index: $STATE";
   print "Printing Protein Structure\n";
    foreach $key  (keys %proteins) {
        print "\nProteins{$key}: @{$proteins{$key}}\n";
    }
    
    print "Printing Promoter Structure\n";
    foreach $key  (keys %promoters) {
        print "\nPromoter{$key}:\n";        
        for ($i = 0; $i <= $#{$promoters{$key}[$INPUT]}; $i++) {            
            my @temp = $promoters{$key}[$INPUT][$i];
            print "---------------\n\tInputs:\n";
            foreach $ref (keys %{$temp[0][0]}) {print "\t\t$ref,$temp[0][0]{$ref}\n";};
            print "\tType: $temp[0][1]\n";
            print "\tArrow: $temp[0][2]\n";            
        }
        print "---------------\n\tOutput:\n";
        foreach $ref (keys %{$promoters{$key}[$OUTPUT]}) {
            print "\t\t$ref\n";
        }
    }
    
    print "Printing Reactions\n";
    foreach $key (keys %biochem) {
        print "\nReactions{$key}:\n";
        foreach $ref (@{$biochem{$key}}) {
            print "\t$ref\n";
        }
    }
}

sub make_input{
    my @params = @_;
        
    my $binds = "";
    
    if ($params[0][1] eq "biochemical") {
        my @reactants;
        foreach my $t (keys %{@{$params[0]}[0]}) {push(@reactants, $t);}
#        print "LOOKING: @reactants\n";
        $binds = get_complex(@reactants);
#        print "FOUND: $binds\n";
    }
    else {
        foreach my $ref (keys %{$params[0][0]}) {
          my $stoc = $params[0][0]{$ref};
          if ($stoc > 1) {
              $binds = "$ref\_$stoc,$binds";
          } else {
              $binds = "$ref,$binds";
          }
        }
   }
   return $binds;
}

sub create_real_network{
#    print "Building Real Network\n------------------------------------\n";

    print OUT "<?xml version=\"1.0\"?>\n<sbml xmlns=\"http://www.sbml.org/sbml/level2\" version=\"1\" level=\"2\">\n<model id=\"$abv_name\">\n<listOfCompartments>\n  <compartment id=\"default\" />\n</listOfCompartments>\n\n<listOfSpecies>\n";

    #Print the proteins in the network
    foreach $key (keys %proteins){
	print OUT "\t<species id = \"$proteins{$key}[$PROTEIN]\"  name = \"$proteins{$key}[$PROTEIN]\" compartment = \"default\" initialAmount = \"0\"/>\n";
    }
        
    #Print each dimer
    foreach $key (keys %dimers) {        
        print OUT "\t<species id = \"$proteins{$key}[1]\_$dimers{$key}\"  name = \"$proteins{$key}[1]\_$dimers{$key}\" compartment = \"default\" initialAmount = \"0\"/>\n";
    }
    
    if (!$opt_c) {    
        #Print each biochem reaction
        foreach $key (keys %biochem) {
            print OUT "\t<species id = \"$key\"  name = \"$key\" compartment = \"default\" initialAmount = \"0\"/>\n";
        }
    }
        
    #Print each promoter, bound promoter, unbound promoter
    foreach my $key (keys %promoters){
	print OUT "\t<species id = \"$key\"  name = \"$key\" compartment = \"default\" initialAmount = \"$num_promo\"/>\n";
	print OUT "\t<species id = \"rnap\_$key\"  name = \"rnap\_$key\" compartment = \"default\" initialAmount = \"0.0\"/>\n";
        for ($i = 0; $i <= $#{$promoters{$key}[$INPUT]}; $i++) {            
            my @temp = $promoters{$key}[$INPUT][$i];
            my $binds = make_input(@temp);
            my @protein = split (/,/,$binds);
            foreach my $ref (@protein) {               
                if ($temp[0][2] eq "vee") {
                    print OUT "\t<species id = \"rnap\_$ref\_a$key\"  name = \"rnap\_$ref\_a$key\" compartment = \"default\" initialAmount = \"0.0\"/>\n";     
                }
                elsif ($temp[0][2] eq "tee") {
                    print OUT "\t<species id = \"bound\_$ref\_$key\"  name = \"bound\_$ref\_$key\" compartment = \"default\" initialAmount = \"0.0\"/>\n";
                }
            }
        }
    }

    #print the rnap
    print OUT "\t<species id = \"rnap\"  name = \"RNAP\" compartment = \"default\" initialAmount = \"$num_RNAP\"/>\n";
    #begin printing out reactions
    print OUT "</listOfSpecies>\n\n<listOfReactions>\n";

    #setup the dedgadations
    foreach $key (keys %proteins) {        
        if ($proteins{$key}[$PROTEIN] =~ m/spastic/){
            print OUT <<END;
<reaction id = "degradation_$proteins{$key}[$PROTEIN]" reversible="false">
  <listOfReactants>
    <speciesReference species = "$proteins{$key}[$PROTEIN]" stoichiometry = "1"/>
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
        elsif ($proteins{$key}[$CONST] != 1) {
            print OUT <<END;
<reaction id = "degradation_$proteins{$key}[$PROTEIN]" reversible="false">
  <listOfReactants>
    <speciesReference species = "$proteins{$key}[$PROTEIN]" stoichiometry = "1"/>
  </listOfReactants>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>k_deg</ci>
        <ci>$proteins{$key}[$PROTEIN]</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "k_deg" name = "k_deg" value = "$deg"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
        }
    }
#  If dimers are allowed to degrade, then degrade them
    if ($opt_d) {
        foreach $key (keys %dimers) {
        if ($proteins{$key} [$CONST] != 1) {
            print OUT <<END;
<reaction id = "degradation_$proteins{$key}[1]\_$dimers{$key}" reversible="false">
  <listOfReactants>
    <speciesReference species = "$proteins{$key}[1]\_$dimers{$key}" stoichiometry = "1"/>
  </listOfReactants>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>k_deg</ci>
        <ci>$proteins{$key}[1]\_$dimers{$key}</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "k_deg" name = "k_deg" value = "$dimer_deg"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
      }
  }
}

#     #set up degradations for dimers    
#     foreach $key (keys %dimers) {
# print OUT <<END;
# <reaction id = "degradation_$proteins{$key}[1]\_$dimers{$key}" reversible="false">
#   <listOfReactants>
#     <speciesReference species = "$proteins{$key}[1]\_$dimers{$key}" stoichiometry = "1"/>
#   </listOfReactants>
#   <kineticLaw>
#     <math xmlns="http://www.w3.org/1998/Math/MathML">
#       <apply>
#         <times/>
#         <ci>k_deg</ci>
#         <ci>$proteins{$key}[1]\_$dimers{$key}</ci>
#       </apply>
#     </math>
#     <listOfParameters>
#       <parameter id = "k_deg" name = "k_deg" value = "$deg"/>
#     </listOfParameters>
#   </kineticLaw>
# </reaction>
# 
# END
#     }
#     
#     #set up degradations for biochem reaction
#     foreach $key (keys %biochem) {
# print OUT <<END;
# <reaction id = "degradation_$key" reversible="false">
#   <listOfReactants>
#     <speciesReference species = "$key" stoichiometry = "1"/>
#   </listOfReactants>
#   <kineticLaw>
#     <math xmlns="http://www.w3.org/1998/Math/MathML">
#       <apply>
#         <times/>
#         <ci>k_deg</ci>
#         <ci>$key</ci>
#       </apply>
#     </math>
#     <listOfParameters>
#       <parameter id = "k_deg" name = "k_deg" value = "$deg"/>
#     </listOfParameters>
#   </kineticLaw>
# </reaction>
# 
# END
# }

#setup the dimers
foreach $key (keys %dimers){
print OUT <<END;
<reaction id = "dimerization_$proteins{$key}[1]\_$dimers{$key}" reversible="true">
  <listOfReactants>
    <speciesReference species = "$proteins{$key}[1]" stoichiometry = "$dimers{$key}"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "$proteins{$key}[1]\_$dimers{$key}" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf_d</ci>
          <apply>            
            <power/>
              <ci>$proteins{$key}[1]</ci>
              <cn type="integer"> 2 </cn>
          </apply>
        </apply>
        <apply>
          <times/>
          <ci>kr</ci>
          <ci>$proteins{$key}[1]\_$dimers{$key}</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf_d" name = "kf_d" value = "$kf_dimer"/>
      <parameter id = "kr" name = "kr" value = "$kr_dimer"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END
}

#set up biochem reactions, if there is no 
#abstraction
if (!$opt_c) {
foreach $key (keys %biochem){
print OUT <<END;
<reaction id = "complex_formation_$key" reversible="true">
  <listOfReactants>
END
    foreach $ref (@{$biochem{$key}}) {
        print OUT "   <speciesReference species = \"$ref\" stoichiometry = \"1\"/>\n";
    }
print OUT <<END;
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "$key" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf_d</ci>
END
foreach $ref (@{$biochem{$key}}) {
print OUT <<END;
          <ci>$ref</ci>
END
}
print OUT <<END;
        </apply>
        <apply>
          <times/>
          <ci>kr</ci>
          <ci>$key</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf_d" name = "kf_d" value = "$kf_complex"/>
      <parameter id = "kr" name = "kr" value = "$kr_complex"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

END
}}

foreach $name (keys %spastics) {
print OUT <<END;
<reaction id = "creation_$name" reversible="false">
  <listOfProducts>
    <speciesReference species = "$name" stoichiometry = "$stochiometry"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <ci>kf</ci>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$spastic"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
}

#cycle through each promoter and build each reaction
foreach $promoter (keys %promoters) {
#setup main gene generation pathways rnap binding and then ocr
print OUT <<END; 
<reaction id = "R_rnap_$promoter" reversible="true">
  <listOfReactants>
    <speciesReference species = "rnap" stoichiometry = "1"/>
    <speciesReference species = "$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf</ci>
          <ci>$promoter</ci>
          <ci>rnap</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>rnap_$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$rnap_binding"/>
      <parameter id = "kr" name = "kr" value = "$rnap_unbinding"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

<reaction id = "R_rnap_pro_$promoter" reversible="false">
  <listOfReactants>
    <speciesReference species = "rnap_$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$promoter" stoichiometry = "1"/>
END
foreach $output (keys %{$promoters{$promoter}[2]}) {
print OUT<<END;
    <speciesReference species = "$output" stoichiometry = "$stochiometry"/>
END
}

#figure out how to handle a promoter that acts and reps
if (get_num_act($promoter) >= get_num_rep($promoter)) {
    $my_ocr = $unactived_production;
} else {
    $my_ocr = $ocr;
} 

print OUT<<END;
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>koc</ci>
        <ci>rnap_$promoter</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "koc" name = "koc" value = "$my_ocr"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END

#now set up any activated production or repression by cycling through the
#possible inputs
  for ($i = 0; $i <= $#{$promoters{$promoter}[$INPUT]}; $i++) {      
      my @temp = $promoters{$promoter}[$INPUT][$i];            
      #First, take care of repression
      if ($temp[0][2] eq "tee") {
          #Check for biochemical reaction
          if ($temp[0][1] eq "biochemical") {
              my @reactants;
              foreach my $t (keys %{@{$temp[0]}[0]}) {push(@reactants, $t);}
              $binds = get_complex(@reactants);
if (!$opt_c) {
print OUT<<END;
<reaction id = "Repress_$binds\_$promoter" reversible="true">
  <listOfReactants>
    <speciesReference species = "$binds" stoichiometry = "1"/>
    <speciesReference species = "$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "bound_$binds\_$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf</ci>
          <ci>$promoter</ci>
          <ci>$binds</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>bound_$binds\_$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$kf_rep"/>
      <parameter id = "kr" name = "kr" value = "$kr_rep"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
} else {
print OUT<<END;
<reaction id = "Repress_$binds\_$promoter" reversible="true">
  <listOfReactants>
END;
foreach my $t (@reactants) {
print OUT<<END;
    <speciesReference species = "$t" stoichiometry = "1"/>
END;
}
print OUT<<END;
    <speciesReference species = "$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "bound_$binds\_$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>KComplex</ci>
          <ci>kf</ci>
          <ci>$promoter</ci>          
END;
foreach my $t (@reactants) {
print OUT<<END;
          <ci>$t</ci>
END;
}
my $K = $kf_complex/$kr_complex;
print OUT<<END;
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>bound_$binds\_$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "KComplex" name = "KComplex" value = "$K"/>
      <parameter id = "kf" name = "kf" value = "$kf_rep"/>
      <parameter id = "kr" name = "kr" value = "$kr_rep"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
}
          }          
          else {
#Regular repression
  foreach $binds (keys %{@{$temp[0]}[0]}) {
    my $kf = $kf_rep;
    my $kr = $kr_rep;
    if ($temp[0][0]{$binds} > 1) {
        $binds = "$binds\_$temp[0][0]{$binds}";
        $kf = $kf_bind_dimer;
        $kr = $kr_bind_dimer;
    }
print OUT<<END;
<reaction id = "Repress_$binds\_$promoter" reversible="true">
  <listOfReactants>
    <speciesReference species = "$binds" stoichiometry = "1"/>
    <speciesReference species = "$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "bound_$binds\_$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf</ci>
          <ci>$promoter</ci>
          <ci>$binds</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>bound_$binds\_$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$kf"/>
      <parameter id = "kr" name = "kr" value = "$kr"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
          }
        }
    }
    else {
          #Take care of activation
          #Check for biochemical reaction
          if ($temp[0][1] eq "biochemical") {
              my @reactants;
              foreach my $t (keys %{@{$temp[0]}[0]}) {push(@reactants, $t);}
              $binds = get_complex(@reactants);
if (!$opt_c) {              
print OUT<<END;
<reaction id = "Activate_$binds\_$promoter" reversible="true">
  <listOfReactants>
    <speciesReference species = "$binds" stoichiometry = "1"/>
    <speciesReference species = "$promoter" stoichiometry = "1"/>
    <speciesReference species = "rnap" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf</ci>
          <ci>$promoter</ci>
          <ci>$binds</ci>
          <ci>rnap</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>rnap_$binds\_a$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$kf_act"/>
      <parameter id = "kr" name = "kr" value = "$kr_act"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

<reaction id = "R_rnap_act_$binds\_$promoter" reversible="false">
  <listOfReactants>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
END
foreach $output (keys %{$promoters{$promoter}[2]}) {
print OUT<<END;
    <speciesReference species = "$output" stoichiometry = "$stochiometry"/>
END
}
print OUT<<END;
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>koc</ci>
        <ci>rnap_$binds\_a$promoter</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "koc" name = "koc" value = "$activated_production"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
} else {
print OUT<<END;
<reaction id = "Activate_$binds\_$promoter" reversible="true">
  <listOfReactants>
END
foreach my $t (@reactants) {
print OUT<<END;
    <speciesReference species = "$t" stoichiometry = "1"/>
END
}
print OUT<<END;
    <speciesReference species = "$promoter" stoichiometry = "1"/>
    <speciesReference species = "rnap" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>KComplex</ci>
          <ci>kf</ci>
          <ci>$promoter</ci>
END
foreach my $t (@reactants) {
print OUT<<END;
          <ci>$t</ci>
END
}
my $K = $kf_complex/$kr_complex;
print OUT<<END;
          <ci>rnap</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>rnap_$binds\_a$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "KComplex" name = "KComplex" value = "$K"/>
      <parameter id = "kf" name = "kf" value = "$kf_act"/>
      <parameter id = "kr" name = "kr" value = "$kr_act"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

<reaction id = "R_rnap_act_$binds\_$promoter" reversible="false">
  <listOfReactants>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
END
foreach $output (keys %{$promoters{$promoter}[2]}) {
print OUT<<END;
    <speciesReference species = "$output" stoichiometry = "$stochiometry"/>
END
}
print OUT<<END;
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>koc</ci>
        <ci>rnap_$binds\_a$promoter</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "koc" name = "koc" value = "$activated_production"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
}}
          else {
          #Take care of activation, no biochemical
foreach my $binds (keys %{$temp[0][0]}) {
#check to make sure that it's not a dimer
    my $kf = $kf_act;
    my $kr = $kr_act;
    if ($temp[0][0]{$binds} > 1) {
        $binds = "$binds\_$temp[0][0]{$binds}";        
        $kf = $kf_bind_dimer*$kf;
        $kr = $kr_bind_dimer*$kr;
    }
print OUT<<END;
<reaction id = "Activate_$binds\_$promoter" reversible="true">
  <listOfReactants>
    <speciesReference species = "$binds" stoichiometry = "1"/>
    <speciesReference species = "$promoter" stoichiometry = "1"/>
    <speciesReference species = "rnap" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <minus/>
        <apply>
          <times/>
          <ci>kf</ci>
          <ci>$promoter</ci>
          <ci>$binds</ci>
          <ci>rnap</ci>
        </apply>
        <apply>
        <times/>
        <ci>kr</ci>
        <ci>rnap_$binds\_a$promoter</ci>
        </apply>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "kf" name = "kf" value = "$kf"/>
      <parameter id = "kr" name = "kr" value = "$kr"/>
    </listOfParameters>
  </kineticLaw>
</reaction>

<reaction id = "R_rnap_act_$binds\_$promoter" reversible="false">
  <listOfReactants>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
  </listOfReactants>
  <listOfProducts>
    <speciesReference species = "rnap_$binds\_a$promoter" stoichiometry = "1"/>
END
foreach $output (keys %{$promoters{$promoter}[2]}) {
print OUT<<END;
    <speciesReference species = "$output" stoichiometry = "$stochiometry"/>
END
}
print OUT<<END;
  </listOfProducts>
  <kineticLaw>
    <math xmlns="http://www.w3.org/1998/Math/MathML">
      <apply>
        <times/>
        <ci>koc</ci>
        <ci>rnap_$binds\_a$promoter</ci>
      </apply>
    </math>
    <listOfParameters>
      <parameter id = "koc" name = "koc" value = "$activated_production"/>
    </listOfParameters>
  </kineticLaw>
</reaction>
END
}}


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