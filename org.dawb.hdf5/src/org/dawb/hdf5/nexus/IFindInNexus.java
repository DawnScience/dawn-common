package org.dawb.hdf5.nexus;

import ncsa.hdf.object.HObject;

/**
 * This interface describes a single method that returns a boolean when given a
 * Hierarchical object. This allows classes to be created specifically to test
 * the type/name/contents of a Hierarchical object, and return whether the object
 * matches or not. To be used with methods that iterate over Hierarchical files to 
 * locate specific objects.
 */
public interface IFindInNexus
{
    boolean inNexus(HObject nexusObject);
    
}