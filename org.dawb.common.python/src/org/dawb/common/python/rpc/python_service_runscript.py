'''
This serice is used by the workflow system to run arbitrary python commands
during workflow execution.

The idea is that this service replaces the jep connection for workflows, to give the ability to
work in the following way
1. Set primitives from java, strings, doubles, int etc to be variables here
2. Set abtract datasets as numpy arrays
3. Run a user defined script

This script is designed to be passed to scisoftpy.rpc's addHandler, see PythonService2.java
'''

def runScript(scriptPath, inputs):
    '''
    scriptPath  - is the path to the user script that should be run
    inputs      - is a dictionary of input objects 
    '''

    # Create the dictionary that has the input and where to extract the 
    # output from
    # We don't use globals() to creating vars because we are not
    # trying to run within the context of this method
    vars = {'script_inputs': inputs, 'script_outputs': dict(), 
            '__name__': '<script>', '__file__': scriptPath}
    
    # Run the script, inputs and outputs are in vars
    execfile(scriptPath, vars)
    
    return vars['script_outputs']
