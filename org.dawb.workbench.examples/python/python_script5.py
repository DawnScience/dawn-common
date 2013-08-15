import numpy as np

I0 = script_inputs['I0']
It = script_inputs['It']

lnI0It = np.log(I0/It)

script_outputs['lnI0It'] = lnI0It
