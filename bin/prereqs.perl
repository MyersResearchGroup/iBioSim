# Script to extract URLs from HTML files. 
# (c) 2002 Eric Robert Peskin

eval "set PATH=/usr/bin:/usr/local/bin:/Perl/bin:${PATH};exec perl -S $0"
    if ''; #above line won't run if already in Perl

use English; # Allow the long names for perl's built-in variables.
use Getopt::Long;  # To process GNU style --long options.

($progname = $PROGRAM_NAME) =~ s|.*/||;  # Strip directory part.
###############################################################################
$usage = <<"EOH;";

Usage: $progname [<option>|<file>]...
       or typically
       g++ [option]...  <filename>.c -M | $progname > <filename>.d

Expand prerequisite output from a copiler with the -M (or -MM) flag
such that the .d file will depend only on the prerequisite files that
currently exist, but the object file and the TAGS file will depend on
all the prequisites.  This is a modification of the strategy at
info:make\#Automatic_Prerequisites or
http://www.gnu.org/manual/make/html_node/make_47.html#SEC51

In the modified form:

The practice we recommend for automatic prerequisite generation is to
have one makefile corresponding to each source file. For each source
file "name.c" there is a makefile "name.d" which lists what files the
object file "name.o" depends on. That way only the source files that
have changed need to be rescanned to produce the new prerequisites.

Here is the pattern rule to generate a file of prerequisites (i.e., a
makefile) called "name.d" from a C source file called "name.c":

  
  %.d: %.c
          \@set -e; rm -f \$@; \
           \$(CC) -M \$(CPPFLAGS) \$< > \$@.\$\$\$\$; \
           $progname \$@.\$\$\$\$ > \$@; \
           rm -f \$@.\$\$\$\$

See section 10.5 Defining and Redefining Pattern Rules, for
information on defining pattern rules. The "-e" flag to the shell
causes it to exit immediately if the \$(CC) command (or any other
command) fails (exits with a nonzero status).

With the GNU C compiler, you may wish to use the "-MM" flag instead of
"-M". This omits prerequisites on system header files. See section
"Options Controlling the Preprocessor" in Using GNU CC, for details.

The purpose of the $progname command is to translate (for example): 
  
  main.o : main.c defs.h

into: 
  
  main_prereqs = main.c defs.h
  main.o TAGS: \${main_prereqs}
  main.d:  \$(wildcard $PROGRAM_NAME \${main_prereqs})

This makes each ".d" file depend on the currently existing source and
header files that the corresponding ".o" file depends on. make then
knows it must regenerate the prerequisites whenever any of the source
or header files changes.

Once you havve defined the rule to remake the ".d" files, you then use
the include directive to read them all in.  See section 3.3 Including
Other Makefiles. For example:

  
  sources = foo.c bar.c

  include \$(sources:.c=.d)

Options:

    -h, --help          Print this usage information and exit.

Questions?  Comments?  Bug reports?  mailto:peskin\@vlsigroup.ece.utah.edu

EOH;
###############################################################################
$help = '';
GetOptions('help' => \$help) or die $usage;
if($help){
    print $usage;
    exit;
}

$OUTPUT_FIELD_SEPARATOR  = "\n";
$OUTPUT_RECORD_SEPARATOR = "\n";

local $INPUT_RECORD_SEPARATOR;
<> =~ /(\S+)\.o\s*:\s*/ or die "Could not find prerequisites";
print "$1_prereqs=$POSTMATCH";
print "$1.o TAGS: \${$1_prereqs}";
print "$1.d:  \$(wildcard $PROGRAM_NAME \${$1_prereqs})"
