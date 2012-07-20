import scisoftpy as dnp

# Jython script for manipulating data sets.
# The variables going into this actor in the workflow are available in the script.
# Expected variable sets available and set when this script is run:
# energy
# I0
# Iref
# It

# Please provide maths of these variables and other available ones in the following lines.
lnI0It = dnp.log(I0/It)
