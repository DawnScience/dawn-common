'''
This serice is used by the workflow system to run arbitrary python commands
during workflow execution.

The idea is that this service replaces the jep connection for workflows, to give the ability to
work in the following way
1. Set primitives from java, strings, doubles, int etc to be variables here
2. Set abstract datasets as numpy arrays
3. Run a user defined script

the script requires one argument which is the port to start the service on.

'''
import time
import os, sys

'''
Import the rpc part which we will use for workflows.
'''
import scisoftpy.rpc as rpc #@UnresolvedImport
rpcserver = rpc.rpcserver(int(sys.argv[1]))

'''
We tell scisoft the port to be used for plotting
'''
import scisoftpy.plot as plot
plottingPort = int(sys.argv[2]);

if (plottingPort>0):
    plot.setremoteport(rpcport=plottingPort)


def isActive(dummy):
    return True

rpcserver.add_handler("isActive",  isActive)

def runScript(scriptPath, sets, outputs, additionalPaths=None):
    '''
    scriptPath  - is the path to the user script that should be run
    sets        - is a dictionary of object which can be unflattened to numpy arrays
    outputs     - is a String list of the names of global outputs which 
                  should be placed in the returned dictionary
    additionalPaths - list of other python path locations
    '''
    
    '''
    If there are any additional paths such as fabio, add these
    '''
    if (not additionalPaths is None and len(additionalPaths)>0):
        for path in additionalPaths:
            sys.path.append(path)  
    '''
    We set the key value pairs in sets as variables available in the script.
    For instance if it contains the key x, a numpy array called 'x' will be 
    available for the script
    '''
    for name in sets:
        locals()[name] = sets[name]
        # exec('%s = %s' % (name, sets[name]))
    
    '''
    Run the script
    '''
    enc = sys.stdout.encoding
    if enc is None or "UTF-8" not in enc: # cope with locale that is not UTF-8
        import codecs
        writer = codecs.getwriter("utf-8")
        sys.stdout = writer(sys.stdout)
        sys.stderr = writer(sys.stderr)

    exec(open(scriptPath).read(), globals(), locals())  # need to place locals from script in globals

    '''
    Read required results back from globals and locals,
    this enables people to name things how they like in
    the script and then it gets pulled back.
    '''
    result = {}
    if (outputs!=None and len(outputs)>0): 
        for name in outputs:
            if (name in globals()):
                result[name] = globals()[name]
        for name in outputs:
            if (name in locals()):
                result[name] = locals()[name]
  
    return result
    
rpcserver.add_handler("runScript", runScript)

# Run the server's main loop
#print "Starting python service on port "+str(sys.argv[1])
rpcserver.serve_forever()

