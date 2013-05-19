#!/usr/bin/perl
##
## File:	backupFile.pl
## Author:	Martin Chaine (chaine.martin@gmail.com)
## Contrib:	Martin Chaine (chaine.martin@gmail.com)
## Created:	07 May 2013 18:49:21
## Modified:	07 May 2013 22:50:37
## Description:	Generate a backup file based on the current day.
##

use strict;

my $BASE_URI = 'http://sd.casimir-lab.net';

if (@ARGV < 1)
{
  print STDERR "$0: No template file\n";
  exit 1;
}

for (<>)
{
  my @line = split /,/;
  my $date = (time + (eval $line[2]) * 24 * 60 * 60 - 720) * 1000;
  $date -= $date % (1000 * 60 * 60 * 24);
  $line[2] = $date;
  print join ('/', $BASE_URI, @line);
}
