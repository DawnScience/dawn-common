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

def runScript(scriptPath, inputs, funcName='run'):
    '''
    scriptPath  - is the path to the user script that should be run
    inputs      - is a dictionary of input objects 
    '''

    # We don't use globals() to creating vars because we are not
    # trying to run within the context of this method
    vars = {'__name__': '<script>',
            '__file__': scriptPath,
            'runScriptFuncName': funcName}

    # Run the script, this generates a function to call
    execfile(scriptPath, vars)

    # Run the function generated, in the Java interface, the runScript method
    # is declared as returning a Map<String, Object>, but that is not enforced
    # in the Python and an incorrect usage will result in a cast exception 
    result = vars[funcName](**inputs)

    return result
