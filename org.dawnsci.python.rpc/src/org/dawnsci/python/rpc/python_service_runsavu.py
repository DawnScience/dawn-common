'''
This is designed as a prototype to run savu-like plugins, written in python in DAWN's processing perspective. 
It is heavily plagiarised from python_service_runscript.py

This script is designed to be passed to scisoftpy.rpc's addHandler, see PythonRunSavuService.java
'''

import os, sys, threading, imp,copy
from savu.data.experiment_collection import Experiment
from savu.data.meta_data import MetaData
import numpy as np
from savu.plugins.utils import find_args, load_plugin
import savu
import inspect
from copy import deepcopy as copy
from savu.plugins import utils as pu
import time

sys_path_0_lock = threading.Lock()
sys_path_0_set = False
plugin_object = False
axis_labels = False
axis_values = False
string_key = None
parameters = None
aux = {}
print "I ran the script"
def runSavu(path2plugin, params, metaOnly, inputs):
    '''
    path2plugin  - is the path to the user script that should be run
    params - are the savu parameters
    metaOnly - a boolean for whether the data is kept in metadata or is passed as data
    inputs      - is a dictionary of input objects 
    '''
    t1 = time.time()


    print "in the python "
    global sys_path_0_lock
    global sys_path_0_set
    global plugin_object
    global axis_labels
    global axis_values
    global string_key
    global parameters
    global aux
    sys_path_0_lock.acquire()
    try:
        result = copy(inputs)

        scriptDir = os.path.dirname(path2plugin)
        sys_path_0 = sys.path[0]
        if sys_path_0_set and scriptDir != sys_path_0:
            raise Exception("runSavu attempted to change sys.path[0] in a way that "
                            "could cause a race condition. Current sys.path[0] is {!r}, "
                            "trying to set to {!r}".format(sys_path_0, scriptDir))
        else:
            sys.path[0] = scriptDir
            sys_path_0_set = True
        
        if not plugin_object:
            parameters = {}
                # slight repack here
            for key in params.keys():
                print "here"
                val = params[key]["value"]
                if type(val)==type(''):
                    val = val.replace('\n','').strip()
                print val
                parameters[key] = val
            print "initialising the object"
            plugin_object, axis_labels, axis_values = process_init(path2plugin, inputs, parameters)
            chkstring =  [any(isinstance(ix, str) for ix in axis_values[label]) for label in axis_labels]
            if any(chkstring): # are any axis values strings we instead make this an aux out
                metaOnly = True
                print "AXIS LABELS"+str(axis_values)
                string_key = axis_labels[chkstring.index(True)]
                aux = dict.fromkeys(axis_values[string_key])
                print aux.keys()
            else:
                string_key = axis_labels[0]# will it always be the first one?
            if not metaOnly:
                if len(axis_labels) == 1:
                    result['xaxis']=axis_values[axis_labels[0]]
                    result['xaxis_title']=axis_labels[0]
                if len(axis_labels) == 2:
                    result['xaxis']=axis_values[axis_labels[0]]
                    result['xaxis_title']=axis_labels[0]
                    result['yaxis']=axis_values[axis_labels[1]]
                    result['yaxis_title']=axis_labels[1]
        else:
            pass
    finally:
        sys_path_0_lock.release()

    if plugin_object.get_max_frames()>1: # we need to get round this since we are frame independant
        data = np.expand_dims(inputs['data'], 0)
    else:
        data = inputs['data']
        
    if not metaOnly:    
        result['data'] = plugin_object.filter_frames([data])[0]
        
    elif metaOnly:
        print "metadata only operation"
        result['data'] = inputs['data']
        print type(result['data'])
        out_array = plugin_object.filter_frames([data])[0]
        k=0
        print aux.keys()
        for key in aux.keys():
            print "assigning the dict in aux"
            aux[key]=np.array(out_array[k])# wow really
            k+=1
        result['auxiliary'] = aux
    print "ran the python part fine"
    print result
    t2 = time.time()
    print "time to runSavu = "+str((t2-t1))
    return result


def process_init(path2plugin, inputs, parameters):
    parameters['in_datasets'] = [inputs['dataset_name']]
    parameters['out_datasets'] = [inputs['dataset_name']]
    plugin = load_plugin(path2plugin.strip('.py'))
    plugin.exp = setup_exp_and_data(inputs, inputs['data'], plugin)
    plugin._set_parameters(parameters)
    plugin._set_plugin_datasets()
    plugin.setup()
    axis_labels = plugin.get_out_datasets()[0].get_axis_label_keys()
    axis_labels.remove('idx') # get the labels
    axis_values = {}
    plugin._clean_up() # this copies the metadata!
    for label in axis_labels:
        axis_values[label] = plugin.get_out_datasets()[0].meta_data.get_meta_data(label)
    plugin.base_pre_process()
    plugin.pre_process()
    print "I went here"
    return plugin, axis_labels, axis_values

def setup_exp_and_data(inputs, data, plugin):
    exp = DawnExperiment(get_options())
    data_obj = exp.create_data_object('in_data', inputs['dataset_name'])
    data_obj.data = None
    if len(inputs['data_dimensions'])==1:
#         print data.shape
        if inputs['xaxis_title'] is None:
            inputs['xaxis_title']='x'
        data_obj.set_axis_labels('idx.units', inputs['xaxis_title'] + '.units')
        data_obj.meta_data.set_meta_data('idx', np.array([1]))
        data_obj.meta_data.set_meta_data(str(inputs['xaxis_title']), inputs['xaxis'])
        data_obj.add_pattern(plugin.get_plugin_pattern(), core_dir=(1,), slice_dir=(0, ))
        data_obj.add_pattern('SINOGRAM', core_dir=(1,), slice_dir=(0, )) # good to add these two on too
        data_obj.add_pattern('PROJECTION', core_dir=(1,), slice_dir=(0, ))
    if len(inputs['data_dimensions'])==2:
        if inputs['xaxis_title'] is None:
            inputs['xaxis_title']='x'
        if inputs['yaxis_title'] is None:
            inputs['yaxis_title']='y'
        data_obj.set_axis_labels('idx.units', inputs['xaxis_title'] + '.units', inputs['yaxis_title'] + '.units')
        data_obj.meta_data.set_meta_data('idx', np.array([1]))
        data_obj.meta_data.set_meta_data(str(inputs['xaxis_title']), inputs['xaxis'])
        data_obj.meta_data.set_meta_data(str(inputs['yaxis_title']), inputs['yaxis'])
        data_obj.add_pattern(plugin.get_plugin_pattern(), core_dir=(1,2,), slice_dir=(0, ))
        data_obj.add_pattern('SINOGRAM', core_dir=(1,2,), slice_dir=(0, )) # good to add these two on too
        data_obj.add_pattern('PROJECTION', core_dir=(1,2,), slice_dir=(0, ))
    data_obj.set_shape((1, ) + data.shape) # need to add for now for slicing...
    data_obj.get_preview().set_preview([])
    return exp

class DawnExperiment(Experiment):
    def __init__(self, options):
        self.index={"in_data": {}, "out_data": {}, "mapping": {}}
        self.meta_data = MetaData(get_options())
        self.nxs_file = None

def get_options():
    options = {}
    options['transport'] = 'hdf5'
    options['process_names'] = 'CPU0'
    options['data_file'] = ''
    options['process_file'] = ''
    options['out_path'] = ''
    options['inter_path'] = ''
    options['log_path'] = ''
    options['run_type'] = ''
    options['verbose'] = 'True'
    return options


def populate_plugins():
    print "method populate plugins was called"
    pu.populate_plugins()

def get_plugin_info():
    '''
    returns all the info about the plugins in the form of a dict
    need to call this from the ui on click button
    '''
    print "returning the plugin descriptions and stuff"
    print pu.dawn_plugins.keys()
    return pu.dawn_plugins


def get_plugin_params(pluginName):
    '''
    returns a hashmap with the parameters
    '''
    out = pu.dawn_plugin_params[pluginName]
    return out
