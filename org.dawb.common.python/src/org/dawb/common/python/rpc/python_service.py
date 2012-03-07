'''
This serice is used by the workflow system to run arbitrary python commands
during workflow execution.

The idea is that this service replaces the jep connection for workflows, to give the ability to
work in the following way
1. Set primitives from java, strings, doubles, int etc to be variables here
2. Set abtract datasets as numpy arrays
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
    result = {}
    execfile(scriptPath)
    
    '''
    Read required results back from globals and locals,
    this enables people to name things how they like in
    the script and then it gets pulled back.
    '''
    if (outputs!=None and len(outputs)>0): 
        for name in outputs:
            if (name in globals()):
                result[name] = globals()[name]
        for name in outputs:
            if (name in locals()):
                result[name] = locals()[name]
  
    return result
    
rpcserver.add_handler("runScript", runScript)


def runEdnaPlugin(execPath, pluginName, isDebug, xml, additionalPaths=None):
    
    '''
    execPath     - path to run plugin in
    pluginName   - plugin name
    isDebug      - True if should run edna in debug mode
    xml          - xml input to edna
    additionalPaths - list of other python path locations
    
    You must set EDNA_HOME to use this method
    This method blocks until the EDJob has reached a final status
    
    '''

    if (not 'EDNA_HOME' in os.environ):
        raise Exception("Cannot locate EDNA_HOME. Please set before running Edna plugins.")
    
    if (not 'EDNA_SITE' in os.environ):
        raise Exception(" Please set EDNA_SITE before running Edna plugins.")

    '''
    Add edna to path
    '''
    ednaKernelPath = os.environ['EDNA_HOME']+"/kernel/src"
    sys.path.insert(0, ednaKernelPath)
    
    '''
    If there are any additional paths such as fabio, add these
    '''
    if (not additionalPaths is None and len(additionalPaths)>0):
        for path in additionalPaths:
            sys.path.append(path)
    
    os.chdir(execPath)
    from EDVerbose import EDVerbose
    if (isDebug):
        EDVerbose.setVerboseDebugOn()
    else:
        EDVerbose.setVerboseOn()            
        EDVerbose.setVerboseDebugOff()
        
    from EDJob import EDJob
    
    EDVerbose.setLogFileName(execPath+"/"+pluginName+".log")           
    
    edJob = EDJob(pluginName)
    edJob.setDataInput(xml)
    edJob.execute()
    edJob.synchronize() # In theory should mean that the following loop is not needed
    
    # Unhelpful way of waiting for EDJob to be finished
    # TODO Fix this in EDJob some time
    while(True):
        status = edJob.getStatus()
        if (status is None):
            time.sleep(0.2) # 200 ms
            continue
        
        if ("failure" == status):
            raise Exception("EDJob failed! ")
        
        if ("success" == status):
            break
    
    ret = edJob.getDataOutput()
    
    return str(ret)

rpcserver.add_handler("runEdnaPlugin", runEdnaPlugin)


# Run the server's main loop
print "Starting python service on port "+str(sys.argv[1])
rpcserver.serve_forever()

