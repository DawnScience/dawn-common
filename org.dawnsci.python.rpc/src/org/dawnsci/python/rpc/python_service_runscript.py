'''
This serice is used by the workflow system to run arbitrary python commands
during workflow execution.

The idea is that this service replaces the jep connection for workflows, to give the ability to
work in the following way
1. Set primitives from java, strings, doubles, int etc to be variables here
2. Set abtract datasets as numpy arrays
3. Run a user defined script

This script is designed to be passed to scisoftpy.rpc's addHandler, see PythonRunScriptService.java
'''

import os, sys, threading

sys_path_0_lock = threading.Lock()
sys_path_0_set = False
state_cache = {}

def runScript(scriptPath, inputs, funcName='run'):
    '''
    scriptPath  - is the path to the user script that should be run
    inputs      - is a dictionary of input objects 
    '''

    # Add the directory of the Python script to PYTHONPATH
    # Doing this introduces a potential race condition that
    # we protect against, if multiple calls
    # to runScript are done in parallel there is no control
    # over which one will be set when the execfile below actually
    # runs. We protect against that race condition by ensuring
    # that the sys.path[0] isn't changed unexpectedly.
    # See the Python PyDev Workflow actor (PythonPydevScript#getService)
    # for an implementation of when to spawn new Python executables
    # and when they can be reused.
    global sys_path_0_lock
    global sys_path_0_set
    sys_path_0_lock.acquire()
    try:
        scriptDir = os.path.dirname(scriptPath)
        sys_path_0 = sys.path[0]
        # larch has a bad habit of messing up sys.path by prepending it with its own plugin directories
        # this hack ensures that we can execute our larch operations more than once with the interpreter
        larchImported = 'larch' in sys.modules
        if not larchImported and sys_path_0_set and scriptDir != sys_path_0:
            raise Exception("runScript attempted to change sys.path[0] in a way that "
                            "could cause a race condition. Current sys.path[0] is {!r}, "
                            "trying to set to {!r}".format(sys_path_0, scriptDir))
        else:
            sys.path[0] = scriptDir
            sys_path_0_set = True
    finally:
        sys_path_0_lock.release()

    inputs["state_cache"] = state_cache
	
    # We don't use globals() to creating vars because we are not
    # trying to run within the context of this method
    vars = {'__name__': '<script>',
            '__file__': scriptPath,
            'runScriptFuncName': funcName}

    # Run the script, this generates a function to call
    exec(open(scriptPath).read(), vars)

    # Run the function generated, in the Java interface, the runScript method
    # is declared as returning a Map<String, Object>, but that is not enforced
    # in the Python and an incorrect usage will result in a cast exception 
    result = vars[funcName](**inputs)

    return result
	
def clearCache():
    state_cache.clear()