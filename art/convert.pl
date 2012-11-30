#!/usr/bin/perl
##
## File:	convert.pl
## Author:	Martin Chaine (chaine.martin@gmail.com)
## Contrib:	Martin Chaine (chaine.martin@gmail.com)
## Created:	25 Nov 2012 15:45:47
## Modified:	29 Nov 2012 05:23:26
## Description:	TODO Put the description here.
##

use warnings;
use File::Path 'make_path';
use File::Basename;

#===============================================================================
# 				   CONSTANTS
#===============================================================================

my $EXEC_NAME = $0;
my $DIR_SRC = './unscaled';

my @DIRS = qw(../res/drawable-xhdpi ../res/drawable-hdpi ../res/drawable-mdpi ../res/drawable-ldpi);
# Dimensions from http://developer.android.com/guide/practices/ui_guidelines/icon_design_launcher.html#size
my @SIZES = qw(96x96 72x72 48x48 36x36);
# Dimensions from http://developer.android.com/guide/practices/ui_guidelines/icon_design_action_bar.html#size11
my @SIZES_ACT = qw(48x48 36x36 24x24 18x18);

#===============================================================================
# 				      MAIN
#===============================================================================

make_path(@DIRS);

unless (-d $DIR_SRC)
{
  print STDERR "$EXEC_NAME: dir $DIR_SRC not found\n";
  exit 1;
}

print 'Converting...';
for (<"$DIR_SRC/ic_*.png">)
{
  my $file = basename($_);
  for ($i = 0; $i < 4; $i++)
  {
    my $size = ($file =~ /^ic_act_/) ? $SIZES_ACT[$i] : $SIZES[$i];
    system "convert $DIR_SRC/$file -resize $size $DIRS[$i]/$file\n";
  }
}
print "OK\n";
