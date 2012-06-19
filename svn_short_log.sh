svn log | perl -ne 'chomp; if (/^-{10}/) {print "\n" if $l; $l=0}; s/[^|]*$// && print if $l==1; print if $l==3; $l++'
