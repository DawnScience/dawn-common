'''
This is designed as a prototype to run savu-like plugins, written in python in DAWN's processing perspective. 
It is heavily plagiarised from python_service_runscript.py

This script is designed to be passed to scisoftpy.rpc's addHandler, see PythonRunSavuService.java
'''

import os, sys, threading, imp,copy

sys_path_0_lock = threading.Lock()
sys_path_0_set = False
plugin_object = False
print "I ran the script"
def runSavu(scriptPath, inputs, funcName='filter_frames'):
    '''
    scriptPath  - is the path to the user script that should be run
    inputs      - is a dictionary of input objects 
    '''
    print "HELLO"
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
    # we also need to prime the class for pre-processing etc...
    global sys_path_0_lock
    global sys_path_0_set
    global plugin_object
    sys_path_0_lock.acquire()
    try:
        print "here I am"
        scriptDir = os.path.dirname(scriptPath)
        sys_path_0 = sys.path[0]
        if sys_path_0_set and scriptDir != sys_path_0:
            raise Exception("runSavu attempted to change sys.path[0] in a way that "
                            "could cause a race condition. Current sys.path[0] is {!r}, "
                            "trying to set to {!r}".format(sys_path_0, scriptDir))
        else:
            sys.path[0] = scriptDir
            sys_path_0_set = True
        
        if not plugin_object:
            print "rocking like a hurricane"
            # just test this for now
            a= imp.load_source('clazz', scriptPath)
            plugin_object = a.PyMcaRefactorLikeSavu()# need to abstract this
            plugin_object.base_pre_process()
            plugin_object.pre_process()
        else:
            pass
    finally:
        sys_path_0_lock.release()

    # We don't use globals() to creating vars because we are not
    # trying to run within the context of this method
#     vars = {'__name__': '<script>',
#             '__file__': scriptPath,
#             'runScriptFuncName': funcName}

    # Run the script, this generates a function to call
#     execfile(scriptPath, vars) already have this

    # Run the function generated, in the Java interface, the runScript method
    # is declared as returning a Map<String, Object>, but that is not enforced
    # in the Python and an incorrect usage will result in a cast exception 
#     result = vars[funcName](**inputs)
    result = copy.deepcopy(inputs)
    print inputs
    result['data'] = plugin_object.filter_frames([inputs['data']])

    return result
