'''
This is designed as a prototype to run savu-like plugins, written in python in DAWN's processing perspective. 
It is heavily plagiarised from python_service_runscript.py

This script is designed to be passed to scisoftpy.rpc's addHandler, see PythonRunSavuService.java
'''

import os, sys, threading, imp,copy
print("loading runSavu")
try:
    from scripts.dawn_runner import run_savu 
except ImportError:
    print("It looks like run_savu is not on your path, have you added savu to the PYTHONPATH")
except:
    raise
print("loading pu")
try:
    from savu.plugins import utils as pu
except ImportError:
    print("It looks like savu.plugin.utils is not on your path, have you added savu to the PYTHONPATH")
except:
    raise
SAVUVERSION=None
try:
    from scripts.config_generator.config_utils import populate_plugins
    pu.populate_plugins = populate_plugins
    SAVUVERSION = 'master'
    print('Assuming savu version master')
except ImportError:
    SAVUVERSION='1.2'
    print('Assuming savu version 1.2')

print("done with imports")
persistence = {}
persistence['sys_path_0_lock'] = threading.Lock()
persistence['sys_path_0_set'] = False
persistence['plugin_object'] = None
persistence['axis_labels'] = None
persistence['axis_values'] = None
persistence['string_key'] = None
persistence['parameters'] = None
persistence['aux'] = {}

print("calling to the python_service_runsavu")

def get_output_rank(path2plugin, inputs, parameters):
    print("running get_output_rank")
    global persistence
    rank = run_savu.get_output_rank(path2plugin, inputs, parameters, persistence)
    print("done")
    return rank

def runSavu(path2plugin, params, metaOnly, inputs):
    '''
    path2plugin  - is the path to the user script that should be run
    params - are the savu parameters
    metaOnly - a boolean for whether the data is kept in metadata or is passed as data
    inputs      - is a dictionary of input objects 
    this handle needs to remain here for the xmlrpc
    '''
    print("running savu")
    global persistence
    result = run_savu.runSavu(path2plugin, params, metaOnly, inputs, persistence)
    print("done")
    return result

def populate_plugins():
    print("populating plugins")
    pu.populate_plugins()
    print("done")

def get_plugin_info():
    '''
    returns all the info about the plugins in the form of a dict
    need to call this from the ui on click button
    '''
    print("returning the plugin info")
    print pu.dawn_plugins.keys()
    return pu.dawn_plugins


def get_plugin_params(pluginName):
    '''
    returns a hashmap with the parameters
    '''
    print("getting the plugin parameters")
    out = pu.dawn_plugin_params[pluginName]
    print("done")
    return out
