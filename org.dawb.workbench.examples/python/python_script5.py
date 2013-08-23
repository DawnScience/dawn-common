import numpy as np

# The Python PyDev actor will execfile this script and then invoke a method
# called "run" in the resulting code. 
# The arguments to the run method are those passed in as inputs to actor.
# To collect any unused inputs use **kwargs
# The run method should return a dictionary of the outputs of the actor and
# should match those listed in the Dataset Outputs.

def run(I0, It, **kwargs):
    lnI0It = np.log(I0/It)
    return {'lnI0It': lnI0It}
