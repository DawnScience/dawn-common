/*****************************************************************************
 * Copyright by The HDF Group.                                               *
 * Copyright by the Board of Trustees of the University of Illinois.         *
 * All rights reserved.                                                      *
 *                                                                           *
 * This file is part of the HDF Java Products distribution.                  *
 * The full copyright notice, including terms governing use, modification,   *
 * and redistribution, is contained in the files COPYING and Copyright.html. *
 * COPYING can be found at the root of the source code distribution tree.    *
 * Or, see http://hdfgroup.org/products/hdf-java/doc/Copyright.html.         *
 * If you do not have access to either file, you may request a copy from     *
 * help@hdfgroup.org.                                                        *
 ****************************************************************************/

package ncsa.hdf.object.h5;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.HDFNativeData;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.structs.H5G_info_t;
import ncsa.hdf.hdf5lib.structs.H5L_info_t;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.ScalarDS;

/**
 * H5File is an implementation of the FileFormat class for HDF5 files.
 * <p>
 * The HDF5 file structure is stored in a tree that is made up of Java TreeNode
 * objects. Each tree node represents an HDF5 object: a Group, Dataset, or Named
 * Datatype. Starting from the root of the tree, <i>rootNode</i>, the tree can
 * be traversed to find a specific object.
 * <p>
 * The following example shows how to find an object by a given path
 * 
 * <pre>
 * HObject findObject(FileFormat file, String path) {
 *     if (file == null || path == null)
 *         return null;
 *     if (!path.endsWith(&quot;/&quot;))
 *         path = path + &quot;/&quot;;
 *     DefaultMutableTreeNode theRoot = (DefaultMutableTreeNode) file
 *             .getRootNode();
 *     if (theRoot == null)
 *         return null;
 *     else if (path.equals(&quot;/&quot;))
 *         return (HObject) theRoot.getUserObject();
 *     Enumeration local_enum = ((DefaultMutableTreeNode) theRoot)
 *             .breadthFirstEnumeration();
 *     DefaultMutableTreeNode theNode = null;
 *     HObject theObj = null;
 *     while (local_enum.hasMoreElements()) {
 *         theNode = (DefaultMutableTreeNode) local_enum.nextElement();
 *         theObj = (HObject) theNode.getUserObject();
 *         String fullPath = theObj.getFullName() + &quot;/&quot;;
 *         if (path.equals(fullPath))
 *             break;
 *     }
 *     return theObj;
 * }
 * </pre>
 * 
 * @author Peter X. Cao
 * @version 2.4 9/4/2007
 */
public class H5File extends FileFormat {
    /**
     * @see ncsa.hdf.object.HObject#serialVersionUID
     */
    public static final long serialVersionUID = HObject.serialVersionUID;

    /**
     * the file access flag. Valid values are HDF5Constants.H5F_ACC_RDONLY,
     * HDF5Constants.H5F_ACC_RDWR and HDF5Constants.H5F_ACC_CREAT.
     */
    private int flag;
    
   /**
    * The index type. Valid values are HDF5Constants.H5_INDEX_NAME,
    * HDF5Constants.H5_INDEX_CRT_ORDER.
    */
    private int indexType;

    /**
     * The root node of the file hierarchy.
     */
    private DefaultMutableTreeNode rootNode;

    /**
     * How many characters maximum in an attribute name?
     */
    private static final int attrNameLen = 256;
    
    /**
     * The library version bounds
     */
    private int[] libver ;
    
    private boolean attrFlag;

    /***************************************************************************
     * Constructor
     **************************************************************************/
    /**
     * Constructs an H5File instance with an empty file name and read-only
     * access.
     */
    public H5File() {
        this("", READ);
    }

    /**
     * Constructs an H5File instance with specified file name and read/write
     * access.
     * <p>
     * This constructor does not open the file for access, nor does it confirm
     * that the file can be opened read/write.
     * 
     * @param fileName
     *            A valid file name, with a relative or absolute path.
     * @throws NullPointerException
     *             If the <code>fileName</code> argument is <code>null</code>.
     */
    public H5File(String fileName) {
        this(fileName, WRITE);
    }

    /**
     * Constructs an H5File instance with specified file name and access.
     * <p>
     * The access parameter values and corresponding behaviors:
     * <ul>
     * <li>READ: Read-only access; open() will fail file doesn't exist.
     * <li>WRITE: Read/Write access; open() will fail if file doesn't exist or
     * if file can't be opened with read/write access.
     * <li>CREATE: Read/Write access; create a new file or truncate an existing
     * one; open() will fail if file can't be created or if file exists but
     * can't be opened read/write.
     * </ul>
     * <p>
     * This constructor does not open the file for access, nor does it confirm
     * that the file can later be opened read/write or created.
     * <p>
     * The flag returned by {@link #isReadOnly()} is set to true if the access
     * parameter value is READ, even though the file isn't yet open.
     * 
     * @param fileName
     *            A valid file name, with a relative or absolute path.
     * @param access
     *            The file access flag, which determines behavior when file is
     *            opened. Acceptable values are <code> READ, WRITE, </code> and
     *            <code>CREATE</code>.
     * @throws NullPointerException
     *             If the <code>fileName</code> argument is <code>null</code>.
     */
    public H5File(String fileName, int access) {
        // Call FileFormat ctor to set absolute path name
        super(fileName);
        
        libver = new int[2];
        attrFlag = false;
        
        // set metadata for the instance
        rootNode = null;
        this.fid = -1;
        isReadOnly = (access == READ);

        // At this point we just set up the flags for what happens later.
        // We just pass unexpected access values on... subclasses may have
        // their own values.
        if (access == READ) {
            flag = HDF5Constants.H5F_ACC_RDONLY;
        }
        else if (access == WRITE) {
            flag = HDF5Constants.H5F_ACC_RDWR;
        }
        else if (access == CREATE) {
            flag = HDF5Constants.H5F_ACC_CREAT;
        }
        else {
            flag = access;
        }
    }

    /***************************************************************************
     * Class methods
     **************************************************************************/

    /**
     * Copies the attributes of one object to another object.
     * <p>
     * This method copies all the attributes from one object (source object) to
     * another (destination object). If an attribute already exists in the
     * destination object, the attribute will not be copied. Attribute names
     * exceeding 256 characters will be truncated in the destination object.
     * <p>
     * The object can be an H5Group, an H5Dataset, or a named H5Datatype. This
     * method is in the H5File class because there is no H5Object class and it
     * is specific to HDF5 objects.
     * <p>
     * The copy can fail for a number of reasons, including an invalid source or
     * destination object, but no exceptions are thrown. The actual copy is
     * carried out by the method: {@link #copyAttributes(int, int)}
     * 
     * @param src
     *            The source object.
     * @param dst
     *            The destination object.
     * @see #copyAttributes(int, int)
     */
    public static final void copyAttributes(HObject src, HObject dst) {
        if ((src != null) && (dst != null)) {
            int srcID = src.open();
            int dstID = dst.open();

            if ((srcID >= 0) && (dstID >= 0)) {
                copyAttributes(srcID, dstID);
            }

            if (srcID >= 0) {
                src.close(srcID);
            }

            if (dstID >= 0) {
                dst.close(dstID);
            }
        }
    }

    /**
     * Copies the attributes of one object to another object.
     * <p>
     * This method copies all the attributes from one object (source object) to
     * another (destination object). If an attribute already exists in the
     * destination object, the attribute will not be copied. Attribute names
     * exceeding 256 characters will be truncated in the destination object.
     * <p>
     * The object can be an H5Group, an H5Dataset, or a named H5Datatype. This
     * method is in the H5File class because there is no H5Object class and it
     * is specific to HDF5 objects.
     * <p>
     * The copy can fail for a number of reasons, including an invalid source or
     * destination object identifier, but no exceptions are thrown.
     * 
     * @param src_id
     *            The identifier of the source object.
     * @param dst_id
     *            The identifier of the destination object.
     */
    public static final void copyAttributes(int src_id, int dst_id) {
        int aid_src = -1, aid_dst = -1, atid = -1, asid = -1;
        String[] aName = { "" };
        H5O_info_t obj_info = null;
        
        try {
            obj_info = H5.H5Oget_info(src_id);
        }
        catch (Exception ex) {
        	obj_info.num_attrs = -1;
        }

        if (obj_info.num_attrs < 0) {
            return;
        }

        for (int i = 0; i < obj_info.num_attrs; i++) {
            aName[0] = new String("");

            try {
            	aid_src = H5.H5Aopen_by_idx(src_id, ".", HDF5Constants.H5_INDEX_CRT_ORDER,
            			HDF5Constants.H5_ITER_INC, i, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
                H5.H5Aget_name(aid_src, H5File.attrNameLen, aName);
                atid = H5.H5Aget_type(aid_src);
                asid = H5.H5Aget_space(aid_src);

                aid_dst = H5.H5Acreate(dst_id, aName[0], atid, asid, HDF5Constants.H5P_DEFAULT,HDF5Constants.H5P_DEFAULT);

                // use native data copy
                H5.H5Acopy(aid_src, aid_dst);

            }
            catch (Exception ex) {
            }

            try {
                H5.H5Sclose(asid);
            }
            catch (Exception ex) {
            }
            try {
                H5.H5Tclose(atid);
            }
            catch (Exception ex) {
            }
            try {
                H5.H5Aclose(aid_src);
            }
            catch (Exception ex) {
            }
            try {
                H5.H5Aclose(aid_dst);
            }
            catch (Exception ex) {
            }

        } // for (int i=0; i<num_attr; i++)
    }
    
	/**
	 * Returns a list of attributes for the specified object.
	 * <p>
	 * This method returns a list containing the attributes associated with the
	 * identified object. If there are no associated attributes, an empty list
	 * will be returned.
	 * <p>
	 * Attribute names exceeding 256 characters will be truncated in the
	 * returned list.
	 * 
	 * @param objID
	 *            The identifier for the object whose attributes are to be
	 *            returned.
	 * @return The list of the object's attributes.
	 * @throws HDF5Exception
	 *             If an underlying HDF library routine is unable to perform a
	 *             step necessary to retrieve the attributes. A variety of
	 *             failures throw this exception.
	 * @see #getAttribute(int,int,int)
	 */
    public static final List<Attribute> getAttribute(int objID) throws HDF5Exception {
    	return H5File.getAttribute(objID, HDF5Constants.H5_INDEX_NAME, HDF5Constants.H5_ITER_INC);
    }
    
	/**
	 * Returns a list of attributes for the specified object, in creation or
	 * alphabetical order.
	 * <p>
	 * This method returns a list containing the attributes associated with the
	 * identified object. If there are no associated attributes, an empty list
	 * will be returned. The list of attributes returned can be in increasing or
	 * decreasing, creation or alphabetical order.
	 * <p>
	 * Attribute names exceeding 256 characters will be truncated in the
	 * returned list.
	 * 
	 * @param objID
	 *            The identifier for the object whose attributes are to be
	 *            returned.
	 * @param idx_type
	 *            The type of index. Valid values are:
	 *            <ul>
	 *            <li>H5_INDEX_NAME: An alpha-numeric index by attribute name
	 *            <li>H5_INDEX_CRT_ORDER: An index by creation order
	 *            </ul>
	 * @param order
	 *            The index traversal order. Valid values are:
	 *            <ul>
	 *            <li>H5_ITER_INC: A top-down iteration incrementing the index
	 *            position at each step. <li>H5_ITER_DEC: A bottom-up iteration
	 *            decrementing the index position at each step.
	 *            </ul>
	 * @return The list of the object's attributes.
	 * @throws HDF5Exception
	 *             If an underlying HDF library routine is unable to perform a
	 *             step necessary to retrieve the attributes. A variety of
	 *             failures throw this exception.
	 */
    
    public static final List<Attribute> getAttribute(int objID,int idx_type, int order) throws HDF5Exception {
    	List<Attribute> attributeList = null;
    	int aid = -1, sid = -1, tid = -1;
    	H5O_info_t obj_info = null;

    	try{
    		obj_info = H5.H5Oget_info(objID);
    	}
    	catch(Exception ex) {
    	}
    	if (obj_info.num_attrs <= 0) {
    		return (attributeList = new Vector<Attribute>());
    	}
    	
    	int n = (int)obj_info.num_attrs;
    	attributeList = new Vector<Attribute>(n);
    	
    	for (int i = 0; i < n; i++) {
    		try {      
    			aid = H5.H5Aopen_by_idx(objID,".", idx_type, 
    					order, i, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
    			sid = H5.H5Aget_space(aid);

    			long dims[] = null;
    			int rank = H5.H5Sget_simple_extent_ndims(sid);

    			if (rank == 0) {
    				// for scalar data, rank=0
    				rank = 1;
    				dims = new long[1];
    				dims[0] = 1;
    			}
    			else {
    				dims = new long[rank];
    				H5.H5Sget_simple_extent_dims(sid, dims, null);
    			}

    			String[] nameA = { "" };
    			H5.H5Aget_name(aid, H5File.attrNameLen, nameA);

    			int tmptid = -1;
    			try {
    				tmptid = H5.H5Aget_type(aid);
    				tid = H5.H5Tget_native_type(tmptid);
    			}
    			finally {
    				try {
    					H5.H5Tclose(tmptid);
    				}
    				catch (Exception ex) {
    				}
    			}

    			Datatype attrType = new H5Datatype(tid);
    			Attribute attr = new Attribute(nameA[0], attrType, dims);
    			attributeList.add(attr);

    			boolean is_variable_str = false;
    			boolean isVLEN = false;
    			boolean isCompound = false;
    			try {
    				is_variable_str = H5.H5Tis_variable_str(tid);
    			}
    			catch (Exception ex) {
    			}
    			try {
    				isVLEN = (H5.H5Tget_class(tid) == HDF5Constants.H5T_VLEN);
    			}
    			catch (Exception ex) {
    			}
    			try {
    				isCompound = (H5.H5Tget_class(tid) == HDF5Constants.H5T_COMPOUND);
    			}
    			catch (Exception ex) {
    			}

    			// retrieve the attribute value
    			long lsize = 1;
    			for (int j = 0; j < dims.length; j++) {
    				lsize *= dims[j];
    			}

    			if (lsize <= 0) {
    				continue;
    			}

    			Object value = null;

    			if (isVLEN || is_variable_str || isCompound) {
    				String[] strs = new String[(int) lsize];
    				for (int j = 0; j < lsize; j++) {
    					strs[j] = "";
    				}
    				H5.H5AreadVL(aid, tid, strs);
    				value = strs;
    			}
    			else {
    				value = H5Datatype.allocateArray(tid, (int) lsize);
    				if (value == null) {
    					continue;
    				}

    				if (H5.H5Tget_class(tid) == HDF5Constants.H5T_ARRAY) {
    					int tmptid1 = -1, tmptid2 = -1;
    					try {
    						tmptid1 = H5.H5Tget_super(tid);
    						tmptid2 = H5.H5Tget_native_type(tmptid1);
    						H5.H5Aread(aid, tmptid2, value);
    					}
    					catch (Exception ex) {
    					}
    					finally {
    						try {
    							H5.H5Tclose(tmptid1);
    						}
    						catch (Exception ex) {
    						}
    						try {
    							H5.H5Tclose(tmptid2);
    						}
    						catch (Exception ex) {
    						}
    					}
    				}
    				else {
    					H5.H5Aread(aid, tid, value);
    				}

    				int typeClass = H5.H5Tget_class(tid);
    				if (typeClass == HDF5Constants.H5T_STRING) {
    					value = Dataset.byteToString((byte[]) value, H5
    							.H5Tget_size(tid));
    				}
    				else if (typeClass == HDF5Constants.H5T_REFERENCE) {
    					value = HDFNativeData.byteToLong((byte[]) value);
    				}
    			}

    			attr.setValue(value);

    		}
    		catch (HDF5Exception ex) {
    		}
    		finally {
    			try {
    				H5.H5Tclose(tid);
    			}
    			catch (HDF5Exception ex) {
    			}
    			try {
    				H5.H5Sclose(sid);
    			}
    			catch (HDF5Exception ex) {
    			}
    			try {
    				H5.H5Aclose(aid);
    			}
    			catch (HDF5Exception ex) {
    			}
    		}
    	} // for (int i=0; i<obj_info.num_attrs; i++)

    	return attributeList;
    }

    /**
     * Creates attributes for an HDF5 image dataset.
     * <p>
     * This method creates attributes for two common types of HDF5 images. It
     * provides a way of adding multiple attributes to an HDF5 image dataset
     * with a single call. The
     * {@link #writeAttribute(HObject, Attribute, boolean)} method may be used
     * to write image attributes that are not handled by this method.
     * <p>
     * For more information about HDF5 image attributes, see the <a
     * href="http://hdfgroup.org/HDF5/doc/ADGuide/ImageSpec.html"> HDF5 Image
     * and Palette Specification</a>.
     * <p>
     * This method can be called to create attributes for 24-bit true color and
     * indexed images. The <code>selectionFlag</code> parameter controls whether
     * this will be an indexed or true color image. If
     * <code>selectionFlag</code> is <code>-1</code>, this will be an indexed
     * image. If the value is <code>ScalarDS.INTERLACE_PIXEL</code> or
     * <code>ScalarDS.INTERLACE_PLANE</code>, it will be a 24-bit true color
     * image with the indicated interlace mode.
     * <p>
     * <ul>
     * The created attribute descriptions, names, and values are:
     * <li>The image identifier: name="CLASS", value="IMAGE"
     * <li>The version of image: name="IMAGE_VERSION", value="1.2"
     * <li>The range of data values: name="IMAGE_MINMAXRANGE", value=[0, 255]
     * <li>The type of the image: name="IMAGE_SUBCLASS", value="IMAGE_TRUECOLOR"
     * or "IMAGE_INDEXED"
     * <li>For IMAGE_TRUECOLOR, the interlace mode: name="INTERLACE_MODE",
     * value="INTERLACE_PIXEL" or "INTERLACE_PLANE"
     * <li>For IMAGE_INDEXED, the palettes to use in viewing the image:
     * name="PALETTE", value= 1-d array of references to the palette datasets,
     * with initial value of {-1}
     * </ul>
     * <p>
     * This method is in the H5File class rather than H5ScalarDS because images
     * are typically thought of at the File Format implementation level.
     * 
     * @param dataset
     *            The image dataset the attributes are added to.
     * @param selectionFlag
     *            Selects the image type and, for 24-bit true color images, the
     *            interlace mode. Valid values are:
     *            <ul>
     *            <li>-1: Indexed Image. <li>ScalarDS.INTERLACE_PIXEL: True
     *            Color Image. The component values for a pixel are stored
     *            contiguously. <li>ScalarDS.INTERLACE_PLANE: True Color Image.
     *            Each component is stored in a separate plane.
     *            </ul>
     * @throws Exception
     *             If there is a problem creating the attributes, or if the
     *             selectionFlag is invalid.
     */
    private static final void createImageAttributes(Dataset dataset,
            int selectionFlag) throws Exception {
        String subclass = null;
        String interlaceMode = null;

        if (selectionFlag == ScalarDS.INTERLACE_PIXEL) {
            subclass = "IMAGE_TRUECOLOR";
            interlaceMode = "INTERLACE_PIXEL";
        }
        else if (selectionFlag == ScalarDS.INTERLACE_PLANE) {
            subclass = "IMAGE_TRUECOLOR";
            interlaceMode = "INTERLACE_PLANE";
        }
        else if (selectionFlag == -1) {
            subclass = "IMAGE_INDEXED";
        }
        else {
            throw new HDF5Exception("The selectionFlag is invalid.");
        }

        long[] attrDims = { 1 };
        String attrName = "CLASS";
        String[] classValue = { "IMAGE" };
        Datatype attrType = new H5Datatype(Datatype.CLASS_STRING, classValue[0]
                .length() + 1, -1, -1);
        Attribute attr = new Attribute(attrName, attrType, attrDims);
        attr.setValue(classValue);
        dataset.writeMetadata(attr);

        attrName = "IMAGE_VERSION";
        String[] versionValue = { "1.2" };
        attrType = new H5Datatype(Datatype.CLASS_STRING, versionValue[0]
                .length() + 1, -1, -1);
        attr = new Attribute(attrName, attrType, attrDims);
        attr.setValue(versionValue);
        dataset.writeMetadata(attr);

        attrDims[0] = 2;
        attrName = "IMAGE_MINMAXRANGE";
        byte[] attrValueInt = { 0, (byte) 255 };
        attrType = new H5Datatype(Datatype.CLASS_CHAR, 1, Datatype.NATIVE,
                Datatype.SIGN_NONE);
        attr = new Attribute(attrName, attrType, attrDims);
        attr.setValue(attrValueInt);
        dataset.writeMetadata(attr);

        attrDims[0] = 1;
        attrName = "IMAGE_SUBCLASS";
        String[] subclassValue = { subclass };
        attrType = new H5Datatype(Datatype.CLASS_STRING, subclassValue[0]
                .length() + 1, -1, -1);
        attr = new Attribute(attrName, attrType, attrDims);
        attr.setValue(subclassValue);
        dataset.writeMetadata(attr);

        if ((selectionFlag == ScalarDS.INTERLACE_PIXEL)
                || (selectionFlag == ScalarDS.INTERLACE_PLANE)) {
            attrName = "INTERLACE_MODE";
            String[] interlaceValue = { interlaceMode };
            attrType = new H5Datatype(Datatype.CLASS_STRING, interlaceValue[0]
                    .length() + 1, -1, -1);
            attr = new Attribute(attrName, attrType, attrDims);
            attr.setValue(interlaceValue);
            dataset.writeMetadata(attr);
        }
        else {
            attrName = "PALETTE";
            long[] palRef = { -1 };
            attrType = new H5Datatype(Datatype.CLASS_REFERENCE, 1,
                    Datatype.NATIVE, Datatype.SIGN_NONE);
            attr = new Attribute(attrName, attrType, attrDims);
            attr.setValue(palRef);
            dataset.writeMetadata(attr);
        }
    }

    /**
     * Updates values of scalar dataste object references in copied file.
     * <p>
     * This method has very specific functionality as documented below, and the
     * user is advised to pay close attention when dealing with files that
     * contain references.
     * <p>
     * When a copy is made from one HDF file to another, object references and
     * dataset region references are copied, but the references in the
     * destination file are not updated by the copy and are therefore invalid.
     * <p>
     * When an entire file is copied, this method updates the values of the
     * object references and dataset region references that are in scalar
     * datasets in the destination file so that they point to the correct
     * object(s) in the destination file. The method does not update references
     * that occur in objects other than scalar datasets.
     * <p>
     * In the current release, the updating of object references is not handled
     * completely as it was not required by the projects that funded
     * development. There is no support for updates when the copy does not
     * include the entire file. Nor is there support for updating objects other
     * than scalar datasets in full-file copies. This functionality will be
     * extended as funding becomes available or, possibly, when the underlying
     * HDF library supports the reference updates itself.
     * 
     * @param srcFile
     *            The file that was copied.
     * @param dstFile
     *            The destination file where the object references will be
     *            updated.
     * @throws Exception
     *             If there is a problem in the update process.
     */
    public static final void updateReferenceDataset(H5File srcFile,
            H5File dstFile) throws Exception {
        if ((srcFile == null) || (dstFile == null)) {
            return;
        }

        DefaultMutableTreeNode srcRoot = (DefaultMutableTreeNode) srcFile
                .getRootNode();
        DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode) dstFile
                .getRootNode();

        Enumeration<?> srcEnum = srcRoot.breadthFirstEnumeration();
        Enumeration<?> newEnum = newRoot.breadthFirstEnumeration();

        // build one-to-one table of between objects in
        // the source file and new file
        int did = -1, tid = -1;
        HObject srcObj, newObj;
        Hashtable<String, long[]> oidMap = new Hashtable<String, long[]>();
        List<ScalarDS> refDatasets = new Vector<ScalarDS>();
        while (newEnum.hasMoreElements() && srcEnum.hasMoreElements()) {
            srcObj = (HObject) ((DefaultMutableTreeNode) srcEnum.nextElement())
                    .getUserObject();
            newObj = (HObject) ((DefaultMutableTreeNode) newEnum.nextElement())
                    .getUserObject();
            oidMap.put(String.valueOf((srcObj.getOID())[0]), newObj.getOID());
            did = -1;
            tid = -1;

            // for Scalar DataSets in destination, if there is an object
            // reference in the dataset, add it to the refDatasets list for
            // later updating.
            if (newObj instanceof ScalarDS) {
                ScalarDS sd = (ScalarDS) newObj;
                did = sd.open();
                if (did > 0) {
                    try {
                        tid = H5.H5Dget_type(did);
                        if (H5.H5Tequal(tid, HDF5Constants.H5T_STD_REF_OBJ)) {
                            refDatasets.add(sd);
                        }
                    }
                    catch (Exception ex) {
                    }
                    finally {
                        try {
                            H5.H5Tclose(tid);
                        }
                        catch (Exception ex) {
                        }
                    }
                }
                sd.close(did);
            } // if (newObj instanceof ScalarDS)
        }

        // Update the references in the scalar datasets in the dest file.
        H5ScalarDS d = null;
        int sid = -1, size = 0, rank = 0;
        int n = refDatasets.size();
        for (int i = 0; i < n; i++) {
            d = (H5ScalarDS) refDatasets.get(i);
            byte[] buf = null;
            long[] refs = null;

            try {
                did = d.open();
                tid = H5.H5Dget_type(did);
                sid = H5.H5Dget_space(did);
                rank = H5.H5Sget_simple_extent_ndims(sid);
                size = 1;
                if (rank > 0) {
                    long[] dims = new long[rank];
                    H5.H5Sget_simple_extent_dims(sid, dims, null);
                    for (int j = 0; j < rank; j++) {
                        size *= (int) dims[j];
                    }
                    dims = null;
                }

                buf = new byte[size * 8];
                H5.H5Dread(did, tid, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, buf);

                // update the ref values
                refs = HDFNativeData.byteToLong(buf);
                size = refs.length;
                for (int j = 0; j < size; j++) {
                    long[] theOID = oidMap.get(String.valueOf(refs[j]));
                    if (theOID != null) {
                        refs[j] = theOID[0];
                    }
                }

                // write back to file
                H5.H5Dwrite(did, tid, HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, refs);

            }
            catch (Exception ex) {
                continue;
            }
            finally {
                try {
                    H5.H5Tclose(tid);
                }
                catch (Exception ex) {
                }
                try {
                    H5.H5Sclose(sid);
                }
                catch (Exception ex) {
                }
                try {
                    H5.H5Dclose(did);
                }
                catch (Exception ex) {
                }
            }

            refs = null;
            buf = null;
        } // for (int i=0; i<n; i++)
    }

    /***************************************************************************
     * Implementation Class methods. These methods are related to the
     * implementing H5File class, but not to a particular instance of the class.
     * Since we can't override class methods (they can only be shadowed in
     * Java), these are instance methods.
     **************************************************************************/

    /**
     * Returns the version of the HDF5 library.
     * 
     * @see ncsa.hdf.object.FileFormat#getLibversion()
     */
    @Override
	public String getLibversion() {
        int[] vers = new int[3];
        String ver = "HDF5 ";

        try {
            H5.H5get_libversion(vers);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }

        ver += vers[0] + "." + vers[1] + "." + vers[2];

        return ver;
    }

    /**
     * Checks if the specified FileFormat instance has the HDF5 format.
     * 
     * @see ncsa.hdf.object.FileFormat#isThisType(ncsa.hdf.object.FileFormat)
     */
    @Override
	public boolean isThisType(FileFormat theFile) {
        return (theFile instanceof H5File);
    }

    /**
     * Checks if the specified file has the HDF5 format.
     * 
     * @see ncsa.hdf.object.FileFormat#isThisType(java.lang.String)
     */
    @Override
	public boolean isThisType(String filename) {
        boolean isH5 = false;

        try {
            isH5 = H5.H5Fis_hdf5(filename);
        }
        catch (HDF5Exception ex) {
            isH5 = false;
        }

        return isH5;
    }

    /**
     * Creates an HDF5 file with the specified name and returns a new H5File
     * instance associated with the file.
     * 
     * @throws HDF5Exception
     *             If the file cannot be created or if createFlag has unexpected
     *             value.
     * @see ncsa.hdf.object.FileFormat#createFile(java.lang.String, int)
     * @see #H5File(String, int)
     */
    @Override
	public FileFormat createFile(String filename, int createFlag)
            throws Exception {
        // Flag if we need to create or truncate the file.
        Boolean doCreateFile = true;

        // Won't create or truncate if CREATE_OPEN specified and file exists
        if (createFlag == FILE_CREATE_OPEN) {
            File f = new File(filename);
            if (f.exists()) {
                doCreateFile = false;
            }
        }

        if (doCreateFile) {
            int fileid = H5.H5Fcreate(filename, HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            try {
                H5.H5Fclose(fileid);
            }
            catch (HDF5Exception ex) {
                ;
            }
        }

        return new H5File(filename, WRITE);
    }

    /**
     * Creates an H5File instance with specified file name and access.
     * <p>
     * 
     * @see ncsa.hdf.object.FileFormat#createInstance(java.lang.String, int)
     * @see #H5File(String, int)
     */
    @Override
	public FileFormat createInstance(String filename, int access)
            throws Exception {
        return new H5File(filename, access);
    }

    /***************************************************************************
     * Instance Methods
     * 
     * These methods are related to the H5File class and to particular instances
     * of objects with this class type.
     **************************************************************************/

    /**
     * Opens file and returns a file identifier.
     * 
     * @see ncsa.hdf.object.FileFormat#open()
     */
    @Override
	public int open() throws Exception {
        return open(true);
    }
 
    /**
     * Opens file and returns a file identifier.
     * 
     * @see ncsa.hdf.object.FileFormat#open(int...)
     */
    @Override
	public int open(int ...propList) throws Exception {
    	indexType = propList[0];
        return open(true);
    }
    
    /**
     * Sets the bounds of library versions.
     * @param low
     * 				The earliest version of the library.
     * @param high
     * 				The latest version of the library.
     * @throws HDF5Exception
     */
    public void setLibBounds(int low, int high) throws Exception {
    	int fapl = HDF5Constants.H5P_DEFAULT;

    	if (fid < 0) 
    		return;
    	
    	fapl = H5.H5Fget_access_plist(fid);

    	try{
    		if (low < 0) 
    			low = HDF5Constants.H5F_LIBVER_EARLIEST;
    		
    		if (high < 0) 
    			high = HDF5Constants.H5F_LIBVER_LATEST;
    		
    		H5.H5Pset_libver_bounds( fapl, low, high );
    		H5.H5Pget_libver_bounds( fapl,libver );  		
    	}
    	finally 
    	{
    		try { H5.H5Pclose(fapl); } catch(Exception e){ }
    	}
    }   
    
    /**
     * Gets the bounds of library versions.
     * @return libver
     * 				The earliest and latest version of the library.
     * @throws HDF5Exception
     */
    public int[] getLibBounds() throws Exception{
    	return libver;
    }

    /**
     * Closes file associated with this H5File instance.
     * 
     * @see ncsa.hdf.object.FileFormat#close()
     * @throws HDF5Exception
     */
    @Override
	public void close() throws HDF5Exception {
        // The current working directory may be changed at Dataset.read()
        // by H5Dchdir_ext()by this file to make it work for external
        // datasets. We need to set it back to the orginal current working
        // directory (when hdf-java application started) before the file
        // is closed/opened. Otherwise, relative path, e.g. "./test.h5" may
        // not work
        H5.H5Dchdir_ext(System.getProperty("user.dir"));

        // clean up unused objects
        if (rootNode != null) {
            DefaultMutableTreeNode theNode = null;
            HObject theObj = null;
            Enumeration<?> local_enum = (rootNode).breadthFirstEnumeration();
            while (local_enum.hasMoreElements()) {
                theNode = (DefaultMutableTreeNode) local_enum.nextElement();
                theObj = (HObject) theNode.getUserObject();

                if (theObj instanceof Dataset) {
                    ((Dataset) theObj).clear();
                }
                else if (theObj instanceof Group) {
                    ((Group) theObj).clear();
                }
            }
        }

        // Close all open objects associated with this file.
        try {
            int n = 0, type = -1, oids[];
            n = H5.H5Fget_obj_count(fid, HDF5Constants.H5F_OBJ_ALL);

            if (n > 0) {
                oids = new int[n];
                H5.H5Fget_obj_ids(fid, HDF5Constants.H5F_OBJ_ALL, n, oids);

                for (int i = 0; i < n; i++) {
                    type = H5.H5Iget_type(oids[i]);

                    if (HDF5Constants.H5I_DATASET == type) {
                        try {
                            H5.H5Dclose(oids[i]);
                        }
                        catch (Exception ex2) {
                        }
                    }
                    else if (HDF5Constants.H5I_GROUP == type) {
                        try {
                            H5.H5Gclose(oids[i]);
                        }
                        catch (Exception ex2) {
                        }
                    }
                    else if (HDF5Constants.H5I_DATATYPE == type) {
                        try {
                            H5.H5Tclose(oids[i]);
                        }
                        catch (Exception ex2) {
                        }
                    }
                    else if (HDF5Constants.H5I_ATTR == type) {
                        try {
                            H5.H5Aclose(oids[i]);
                        }
                        catch (Exception ex2) {
                        }
                    }
                } // for (int i=0; i<n; i++)
            } // if ( n>0)
        }
        catch (Exception ex) {
        }

        try {
            H5.H5Fflush(fid, HDF5Constants.H5F_SCOPE_GLOBAL);
        }
        catch (Exception ex) {
        }

        try {
            H5.H5Fclose(fid);
        }
        catch (Exception ex) {
        }

        // Set fid to -1 but don't reset rootNode
        fid = -1;
    }

    /**
     * Returns the root node of the open HDF5 File.
     * 
     * @see ncsa.hdf.object.FileFormat#getRootNode()
     */
    @Override
	public TreeNode getRootNode() {
        return rootNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#get(java.lang.String)
     */
    @Override
	public HObject get(String path) throws Exception {
        HObject obj = null;

        if ((path == null) || (path.length() <= 0)) {
            return null;
        }

        // replace the wrong slash and get rid of "//"
        path = path.replace('\\', '/');
        path = "/" + path;
        path = path.replaceAll("//", "/");

        // the whole file tree is loaded. find the object in the tree
        if (rootNode != null) {
            obj = findObject(this, path);
        }

        // found object in memory
        if (obj != null) {
            return obj;
        }

        // open only the requested object
        String name = null, pPath = null;
        if (path.equals("/")) {
            name = "/"; // the root
        }
        else {
            // separate the parent path and the object name
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 2);
            }
            int idx = path.lastIndexOf('/');
            name = path.substring(idx + 1);
            if (idx == 0) {
                pPath = "/";
            }
            else {
                pPath = path.substring(0, idx);
            }
        }

        // do not open the full tree structure, only the file handler
        int fid_before_open = fid;
        fid = open(false);
        if (fid < 0) {
            return null;
        }

        try {
        	H5O_info_t info ;
        	int objType;
        	int oid = H5.H5Oopen(fid, path, HDF5Constants.H5P_DEFAULT);
        	
        	if(oid>=0){
        		info = H5.H5Oget_info(oid);
        		objType = info.type;
        		if(objType==HDF5Constants.H5O_TYPE_DATASET){
        			int did= -1;
        			try {
        				did = H5.H5Dopen(fid, path, HDF5Constants.H5P_DEFAULT);
        				obj = getDataset(did, name, pPath);
        			}
        			finally {
        				try {
        					H5.H5Dclose(did);
        				}
        				catch (Exception ex) {
        				}
        			}
        		}
        		else if (objType == HDF5Constants.H5O_TYPE_GROUP) {
        			int gid = -1;
        			try {
        				gid = H5.H5Gopen(fid, path, HDF5Constants.H5P_DEFAULT);
        				H5Group pGroup = null;
        				if (pPath != null) {
        					pGroup = new H5Group(this, null, pPath, null);
        					obj = getGroup(gid, name, pGroup);
        					pGroup.addToMemberList(obj);
        				}
        				else {
        					obj = getGroup(gid, name, pGroup);
        				}
        			}
        			finally {
        				try {
        					H5.H5Gclose(gid);
        				}
        				catch (Exception ex) {
        				}
        			}
        		}
        		else if (objType == HDF5Constants.H5O_TYPE_NAMED_DATATYPE) {
        			obj = new H5Datatype(this, name, pPath);
        		}
        	}
        	try { 
        		H5.H5Oclose(oid); 
        	} 
        	catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }
        catch (Exception ex) {
            obj = null;
        }
        finally {
            if ((fid_before_open <= 0) && (obj == null)) {
                // close the fid that is not attached to any object
                try {
                    H5.H5Fclose(fid);
                }
                catch (Exception ex) {
                }
                fid = fid_before_open;
            }
        }

        return obj;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#createDatatype(int, int, int, int,
     * java.lang.String)
     */
    @Override
	public Datatype createDatatype(int tclass, int tsize, int torder,
            int tsign, String name) throws Exception {
        int tid = -1;
        H5Datatype dtype = null;

        try {
            H5Datatype t = (H5Datatype) createDatatype(tclass, tsize, torder,
                    tsign);
            tid = t.toNative();
            
           H5.H5Tcommit(fid, name, tid, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

            byte[] ref_buf = H5.H5Rcreate(fid, name, HDF5Constants.H5R_OBJECT,
                    -1);
            long l = HDFNativeData.byteToLong(ref_buf, 0);

            long[] oid = new long[1];
            oid[0] = l; // save the object ID

            dtype = new H5Datatype(this, null, name);

        }
        finally {
            if (tid > 0) {
                H5.H5Tclose(tid);
            }
        }

        return dtype;
    }

    /***************************************************************************
     * Methods related to Datatypes and HObjects in HDF5 Files. Strictly
     * speaking, these methods aren't related to H5File and the actions could be
     * carried out through the H5Group, H5Datatype and H5*DS classes. But, in
     * some cases they allow a null input and expect the generated object to be
     * of HDF5 type. So, we put them in the H5File class so that we create the
     * proper type of HObject... H5Group for example.
     * 
     * Here again, if there could be Implementation Class methods we'd use
     * those. But, since we can't override class methods (they can only be
     * shadowed in Java), these are instance methods.
     * 
     **************************************************************************/

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#createDatatype(int, int, int, int)
     */
    @Override
	public Datatype createDatatype(int tclass, int tsize, int torder, int tsign)
            throws Exception {
        return new H5Datatype(tclass, tsize, torder, tsign);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#createScalarDS(java.lang.String,
     * ncsa.hdf.object.Group, ncsa.hdf.object.Datatype, long[], long[], long[],
     * int, java.lang.Object)
     */
    @Override
	public Dataset createScalarDS(String name, Group pgroup, Datatype type,
            long[] dims, long[] maxdims, long[] chunks, int gzip, Object data)
            throws Exception {
        if (pgroup == null) {
            // create new dataset at the root group by default
            pgroup = (Group) get("/");
        }

        return H5ScalarDS.create(name, pgroup, type, dims, maxdims, chunks,
                gzip, data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#createCompoundDS(java.lang.String,
     * ncsa.hdf.object.Group, long[], long[], long[], int, java.lang.String[],
     * ncsa.hdf.object.Datatype[], int[], java.lang.Object)
     */
    @Override
	public Dataset createCompoundDS(String name, Group pgroup, long[] dims,
            long[] maxdims, long[] chunks, int gzip, String[] memberNames,
            Datatype[] memberDatatypes, int[] memberSizes, Object data)
            throws Exception {
        int nMembers = memberNames.length;
        int memberRanks[] = new int[nMembers];
        long memberDims[][] = new long[nMembers][1];
        Dataset ds = null;

        for (int i = 0; i < nMembers; i++) {
            memberRanks[i] = 1;
            if (memberSizes == null) {
                memberDims[i][0] = 1;
            }
            else {
                memberDims[i][0] = memberSizes[i];
            }
        }

        if (pgroup == null) {
            // create new dataset at the root group by default
            pgroup = (Group) get("/");
        }
        ds = H5CompoundDS.create(name, pgroup, dims, maxdims, chunks, gzip,
                memberNames, memberDatatypes, memberRanks, memberDims, data);

        return ds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#createImage(java.lang.String,
     * ncsa.hdf.object.Group, ncsa.hdf.object.Datatype, long[], long[], long[],
     * int, int, int, java.lang.Object)
     */
    @Override
	public Dataset createImage(String name, Group pgroup, Datatype type,
            long[] dims, long[] maxdims, long[] chunks, int gzip, int ncomp,
            int interlace, Object data) throws Exception {
        if (pgroup == null) { // create at the root group by default
            pgroup = (Group) get("/");
        }

        H5ScalarDS dataset = H5ScalarDS.create(name, pgroup, type, dims,
                maxdims, chunks, gzip, data);

        try {
            H5File.createImageAttributes(dataset, interlace);
            dataset.setIsImage(true);
        }
        catch (Exception ex) {
        }

        return dataset;
    }

    /***
     * Creates a new group with specified name in existing group.
     * 
     * @see ncsa.hdf.object.FileFormat#createGroup(java.lang.String,
     *      ncsa.hdf.object.Group)
     */
    @Override
	public Group createGroup(String name, Group pgroup) throws Exception {
        return this.createGroup(name, pgroup, HDF5Constants.H5P_DEFAULT);

    }
    
	/***
	 * Creates a new group with specified name in existing group and with the
	 * group creation properties list, gplist.
	 * 
	 * @see ncsa.hdf.object.h5.H5Group#create(java.lang.String,
	 *      ncsa.hdf.object.Group, int)
	 * 
	 */
    public Group createGroup(String name, Group pgroup, int ... gplist) throws Exception {
        // create new group at the root
        if (pgroup == null) {
            pgroup = (Group) this.get("/");
        }

        return H5Group.create(name, pgroup, gplist);
    }
    
	/***
	 * Creates the group creation property list identifier, gcpl. This
	 * identifier is used when creating Groups.
	 * 
	 * @see ncsa.hdf.object.FileFormat#createGcpl(int, int, int)
	 * 
	 */
    public int createGcpl(int creationorder, int maxcompact, int mindense)throws Exception{
    	int gcpl = -1;
    	try {
    		gcpl = H5.H5Pcreate(HDF5Constants.H5P_GROUP_CREATE);
    		if (gcpl >= 0) {
    			// Set link creation order.
    			if (creationorder ==Group.CRT_ORDER_TRACKED){
    				H5.H5Pset_link_creation_order(gcpl,
    						HDF5Constants.H5P_CRT_ORDER_TRACKED); 
    			}
    			else if (creationorder == Group.CRT_ORDER_INDEXED){
    				H5.H5Pset_link_creation_order(gcpl,
    						HDF5Constants.H5P_CRT_ORDER_TRACKED
    						+ HDF5Constants.H5P_CRT_ORDER_INDEXED); 
    			}
    			// Set link storage.
    			H5.H5Pset_link_phase_change(gcpl, maxcompact, mindense); 
    		}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	
    	return gcpl;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#createLink(ncsa.hdf.object.Group,
     * java.lang.String, ncsa.hdf.object.HObject)
     */
    @Override
	public HObject createLink(Group parentGroup, String name, Object currentObj)
    		throws Exception {
    	if(currentObj instanceof HObject)
    		return this.createLink(parentGroup, name, (HObject)currentObj, Group.LINK_TYPE_HARD);
    	else 
    		return this.createLink(parentGroup, name, (String)currentObj, Group.LINK_TYPE_HARD);
    }
    
	/**
	 * Creates a link to an object in the open file.
	 * <p>
	 * If parentGroup is null, the new link is created in the root group.
	 * 
	 * @param parentGroup
	 *            The group where the link is created.
	 * @param name
	 *            The name of the link.
	 * @param currentObj
	 *            The existing object the new link will reference.
	 * @param type
	 *            The type of link to be created. It can be a hard link, a soft
	 *            link or an external link.
	 * @return The object pointed to by the new link if successful; otherwise
	 *         returns null.
	 * @throws Exception
	 *             The exceptions thrown vary depending on the implementing
	 *             class.
	 */
    public HObject createLink(Group parentGroup, String name, HObject currentObj, int lType)
    throws Exception {
    	HObject obj = null;
    	int type = 0;
    	String current_full_name = null, new_full_name = null, parent_path = null;

    	if (currentObj == null) {
    		throw new HDF5Exception(
    				"The object pointed by the link cannot be null.");
    	}
    	if ((parentGroup == null) || parentGroup.isRoot()) {
    		parent_path = HObject.separator;
    	}
    	else {
    		parent_path = parentGroup.getPath() + HObject.separator
    		+ parentGroup.getName() + HObject.separator;
    	}

    	new_full_name = parent_path + name;

    	if (lType == Group.LINK_TYPE_HARD)
    		type = HDF5Constants.H5L_TYPE_HARD;
    	    	
    	else if (lType == Group.LINK_TYPE_SOFT)
    		type = HDF5Constants.H5L_TYPE_SOFT;
    	
    	else if (lType == Group.LINK_TYPE_EXTERNAL)
    		type = HDF5Constants.H5L_TYPE_EXTERNAL;
    	
    	if (H5.H5Lexists(fid, new_full_name, HDF5Constants.H5P_DEFAULT))
		{
				H5.H5Ldelete(fid, new_full_name, HDF5Constants.H5P_DEFAULT);
		}
    	
    	if(type ==HDF5Constants.H5L_TYPE_HARD){   	 	
    		if ((currentObj instanceof Group) && ((Group) currentObj).isRoot()) {
    			throw new HDF5Exception("Cannot make a link to the root group.");
    		}
    		current_full_name = currentObj.getPath() + HObject.separator
    		+ currentObj.getName();

    		H5.H5Lcreate_hard(fid, current_full_name,
    				fid, new_full_name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
    	}

    	else if(type==HDF5Constants.H5L_TYPE_SOFT){     		
    		H5.H5Lcreate_soft( currentObj.getFullName(), fid, new_full_name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
    	}

    	else if(type ==HDF5Constants.H5L_TYPE_EXTERNAL){  
    		H5.H5Lcreate_external(currentObj.getFile(), currentObj.getFullName(), fid, new_full_name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
    	}

    	if (currentObj instanceof Group) {
    		obj = new H5Group(this, name, parent_path, parentGroup);
    	}
    	else if (currentObj instanceof H5Datatype) {
    		obj = new H5Datatype(this, name, parent_path);
    	}
    	else if (currentObj instanceof H5CompoundDS) {
    		obj = new H5CompoundDS(this, name, parent_path);
    	}
    	else if (currentObj instanceof H5ScalarDS) {
    		obj = new H5ScalarDS(this, name, parent_path);
    	}
    	return obj;
    }

	/**
	 * Creates a soft or external links to objects in a file that do not exist
	 * at the time the link is created.
	 * 
	 * @param parentGroup
	 *            The group where the link is created.
	 * @param name
	 *            The name of the link.
	 * @param currentObj
	 *            The name of the object the new link will reference. The object
	 *            doesn't have to exist.
	 * @param type
	 *            The type of link to be created.
	 * @return The H5Link object pointed to by the new link if successful;
	 *         otherwise returns null.
	 * @throws Exception
	 *             The exceptions thrown vary depending on the implementing
	 *             class.
	 */
	public HObject createLink(Group parentGroup, String name,
			String currentObj, int lType) throws Exception {
		HObject obj = null;
		int type = 0;
		String new_full_name = null, parent_path = null;

		if (currentObj == null) {
			throw new HDF5Exception(
			"The object pointed by the link cannot be null.");
		}
		if ((parentGroup == null) || parentGroup.isRoot()) {
			parent_path = HObject.separator;
		} else {
			parent_path = parentGroup.getPath() + HObject.separator
			+ parentGroup.getName() + HObject.separator;
		}

		new_full_name = parent_path + name;

		if (lType == Group.LINK_TYPE_HARD)
			type = HDF5Constants.H5L_TYPE_HARD;

		else if (lType == Group.LINK_TYPE_SOFT)
			type = HDF5Constants.H5L_TYPE_SOFT;

		else if (lType == Group.LINK_TYPE_EXTERNAL)
			type = HDF5Constants.H5L_TYPE_EXTERNAL;

		if (H5.H5Lexists(fid, new_full_name, HDF5Constants.H5P_DEFAULT)) {
			H5.H5Ldelete(fid, new_full_name, HDF5Constants.H5P_DEFAULT);
		}

		if (type == HDF5Constants.H5L_TYPE_SOFT) {
			H5.H5Lcreate_soft(currentObj, fid, new_full_name,
					HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		}

		else if (type == HDF5Constants.H5L_TYPE_EXTERNAL) {
			String fileName = null;
			String objectName = null;

			// separate the object name and the file name
			fileName = currentObj.substring(0, currentObj
					.lastIndexOf(FileFormat.FILE_OBJ_SEP));
			objectName = currentObj.substring(currentObj
					.indexOf(FileFormat.FILE_OBJ_SEP));
			objectName = objectName.substring(3);

			H5.H5Lcreate_external(fileName, objectName, fid, new_full_name,
					HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		}

		if (name.startsWith(HObject.separator)) {
			name = name.substring(1);
		}
		obj = new H5Link(this, name, parent_path);

		return obj;
	}

    /**
     * reload the sub-tree structure from file.
     * <p>
     * reloadTree(Group g) is useful when the structure of the group in file is
     * changed while the group structure in memory is not changed.
     * 
     * @param g
     *            the group where the structure is to be reloaded in memory
     */
    public void reloadTree(Group g) {
        if (fid < 0 || rootNode == null || g == null)
            return;

        HObject theObj = null;
        DefaultMutableTreeNode theNode = null;

        if (g.equals(rootNode.getUserObject()))
            theNode = rootNode;
        else {
            Enumeration<?> local_enum = rootNode.breadthFirstEnumeration();
            while (local_enum.hasMoreElements()) {
                theNode = (DefaultMutableTreeNode) local_enum.nextElement();
                theObj = (HObject) theNode.getUserObject();
                if (g.equals(theObj))
                    break;
            }
        }

        theNode.removeAllChildren();
        depth_first(theNode);
    }

    /*
     * (non-Javadoc) NOTE: Object references are copied but not updated by this
     * method.
     * 
     * @see ncsa.hdf.object.FileFormat#copy(ncsa.hdf.object.HObject,
     * ncsa.hdf.object.Group, java.lang.String)
     */
	@Override
	public TreeNode copy(HObject srcObj, Group dstGroup, String dstName)
			throws Exception {
		TreeNode newNode = null;

		if ((srcObj == null) || (dstGroup == null)) {
			return null;
		}

		if (dstName == null) {
			dstName = srcObj.getName();
		}

		if (srcObj instanceof Dataset) {
			newNode = copyDataset((Dataset) srcObj, (H5Group) dstGroup, dstName);
		} 
		else if (srcObj instanceof H5Group) {
			newNode = copyGroup((H5Group) srcObj, (H5Group) dstGroup, dstName);
		}
		else if (srcObj instanceof H5Datatype) {
			newNode = copyDatatype((H5Datatype) srcObj, (H5Group) dstGroup,
					dstName);
		}

		return newNode;
	}

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#delete(ncsa.hdf.object.HObject)
     */
    @Override
	public void delete(HObject obj) throws Exception {
        if ((obj == null) || (fid < 0)) {
            return;
        }

        String name = obj.getPath() + obj.getName();

        H5.H5Ldelete(fid, name, HDF5Constants.H5P_DEFAULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ncsa.hdf.object.FileFormat#writeAttribute(ncsa.hdf.object.HObject,
     * ncsa.hdf.object.Attribute, boolean)
     */
    @Override
	public void writeAttribute(HObject obj, Attribute attr, boolean attrExisted)
            throws HDF5Exception {
    	String obj_name = obj.getFullName();
    	String name = attr.getName();
        int tid = -1, sid = -1, aid = -1;

        int objID = obj.open();
        if (objID < 0) {
            return;
        }

        try {
            tid = attr.getType().toNative();
            sid = H5.H5Screate_simple(attr.getRank(), attr.getDataDims(), null);

            if (attrExisted) {
            	aid = H5.H5Aopen_by_name(objID, obj_name, name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            	int tmptid = H5.H5Aget_type(aid);
            	boolean isVlen = (H5.H5Tget_class(tmptid)==HDF5Constants.H5T_VLEN || H5.H5Tis_variable_str(tmptid));
            	try {H5.H5Tclose(tmptid); } catch (Exception ex2) {}
            	if (isVlen) {
                    throw (new HDF5Exception( "Writing variable-length attributes is not supported"));
            	}
            }
            else {
            	aid = H5.H5Acreate(objID, name, tid, sid, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            }

            // update value of the attribute
            Object attrValue = attr.getValue();
            if (attrValue != null) {
                if (attr.getType().getDatatypeClass() == Datatype.CLASS_REFERENCE
                        && attrValue instanceof String) { // reference is a
                                                          // path+name to the
                                                          // object
                    attrValue = H5.H5Rcreate(getFID(), (String) attrValue,
                            HDF5Constants.H5R_OBJECT, -1);
                }
                else if (Array.get(attrValue, 0) instanceof String) {
                    String strValue = (String) Array.get(attrValue, 0);
                    int size = H5.H5Tget_size(tid);

                    if (strValue.length() > size) {
                        // truncate the extra characters
                        strValue = strValue.substring(0, size);
                        Array.set(attrValue, 0, strValue);
                    }
                    else {
                        // pad space to the unused space
                        for (int i = strValue.length(); i < size; i++) {
                            strValue += " ";
                        }
                    }

                    byte[] bval = strValue.getBytes();
                    // add null to the end to get rid of the junks
                    bval[(strValue.length() - 1)] = 0;
                    attrValue = bval;
                }

                try {
                    /*
                     * must use native type to write attribute data to file (see
                     * bug 1069)
                     */
                    int tmptid = tid;
                    tid = H5.H5Tget_native_type(tmptid);
                    try {
                        H5.H5Tclose(tmptid);
                    }
                    catch (HDF5Exception ex) {
                    }
                    H5.H5Awrite(aid, tid, attrValue);
                }
                catch (Exception ex) {
                }
            } // if (attrValue != null) {
        }
        finally {
            try {
                H5.H5Tclose(tid);
            }
            catch (HDF5Exception ex) {
            }
            try {
                H5.H5Sclose(sid);
            }
            catch (HDF5Exception ex) {
            }
            try {
                H5.H5Aclose(aid);
            }
            catch (HDF5Exception ex) {
            }
        }

        obj.close(objID);
    }

    /***************************************************************************
     * Implementations for methods specific to H5File
     **************************************************************************/

    /**
     * Opens a file with specific file access property list.
     * <p>
     * This function does the same as "int open()" except the you can also pass
     * an HDF5 file access property to file open. For example,
     * 
     * <pre>
     * //All open objects remaining in the file are closed then file is closed
     * int plist = H5.H5Pcreate(HDF5Constants.H5P_FILE_ACCESS);
     * H5.H5Pset_fclose_degree(plist, HDF5Constants.H5F_CLOSE_STRONG);
     * int fid = open(plist);
     * </pre>
     * 
     * @param plist
     *            a file access property list identifier.
     * @return the file identifier if successful; otherwise returns negative
     *         value.
     */
    public int open(int plist) throws Exception {
        return open(true, plist);
    }
    
    /***************************************************************************
     * Private methods.
     **************************************************************************/

    /**
     * Opens access to this file.
     * 
     * @param loadFullHierarchy
     *            if true, load the full hierarchy into memory; otherwise just
     *            opens the file idenfitier.
     * @return the file identifier if successful; otherwise returns negative
     *         value.
     */
    private int open(boolean loadFullHierarchy) throws Exception {
        int the_fid = -1;

        int plist = HDF5Constants.H5P_DEFAULT;

        /*
         * // BUG: HDF5Constants.H5F_CLOSE_STRONG does not flush cache try {
         * //All open objects remaining in the file are closed // then file is
         * closed plist = H5.H5Pcreate (HDF5Constants.H5P_FILE_ACCESS);
         * H5.H5Pset_fclose_degree ( plist, HDF5Constants.H5F_CLOSE_STRONG); }
         * catch (Exception ex) {;} the_fid = open(loadFullHierarchy, plist);
         * try { H5.H5Pclose(plist); } catch (Exception ex) {}
         */

        the_fid = open(loadFullHierarchy, plist);

        return the_fid;
    }

    /**
     * Opens access to this file.
     * 
     * @param loadFullHierarchy
     *            if true, load the full hierarchy into memory; otherwise just
     *            opens the file identifier.
     * @return the file identifier if successful; otherwise returns negative
     *         value.
     */
    private int open(boolean loadFullHierarchy, int plist) throws Exception {
        if (fid > 0) {
            return fid; // file is opened already
        }

        // The cwd may be changed at Dataset.read() by H5Dchdir_ext()
        // to make it work for external datasets. We need to set it back
        // before the file is closed/opened.
        H5.H5Dchdir_ext(System.getProperty("user.dir"));

        // check for valid file access permission
        if (flag < 0) {
            throw new HDF5Exception("Invalid access identifer -- " + flag);
        }
        else if (HDF5Constants.H5F_ACC_CREAT == flag) {
            // create a new file
            fid = H5.H5Fcreate(fullFileName, HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
            H5.H5Fflush(fid, HDF5Constants.H5F_SCOPE_LOCAL);
            H5.H5Fclose(fid);
            flag = HDF5Constants.H5F_ACC_RDWR;
        }
        else if (!exists()) {
            throw new HDF5Exception("File does not exist -- " + fullFileName);
        }
        else if (((flag == HDF5Constants.H5F_ACC_RDWR) || (flag == HDF5Constants.H5F_ACC_CREAT))
                && !canWrite()) {
            throw new HDF5Exception(
                    "Cannot write file, try open as read-only -- "
                            + fullFileName);
        }
        else if ((flag == HDF5Constants.H5F_ACC_RDONLY) && !canRead()) {
            throw new HDF5Exception("Cannot read file -- " + fullFileName);
        }

        try {
            fid = H5.H5Fopen(fullFileName, flag, plist);
        }
        catch (Exception ex) {
            isReadOnly = true;
            fid = H5.H5Fopen(fullFileName, HDF5Constants.H5F_ACC_RDONLY,
                    HDF5Constants.H5P_DEFAULT);
        }

        if ((fid >= 0) && loadFullHierarchy) {
            // load the hierearchy of the file
            rootNode = loadTree();
        }

        return fid;
    }

    /**
     * Reads the file structure into memory (tree node)
     * 
     * @return the root node of the file structure.
     */
    private DefaultMutableTreeNode loadTree() {
        if (fid < 0) {
            return null;
        }

        DefaultMutableTreeNode root = null;

        long[] rootOID = { 0 };
        H5Group rootGroup = new H5Group(this, getName(), // set the node name to
                                                         // the file name
                null, // root node does not have a parent path
                null, // root node does not have a parent node
                rootOID);

        root = new DefaultMutableTreeNode(rootGroup) {
            public static final long serialVersionUID = HObject.serialVersionUID;

            @Override
			public boolean isLeaf() {
                return false;
            }
        };

        depth_first(root);

        return root;
    }

    /**
     * Retrieves the file structure by depth-first order, recursively. The
     * current implementation retrieves group and dataset only. It does not
     * include named datatype and soft links.
     * <p>
     * It also detects and stops loops. A loop is detected if there exists
     * object with the same object ID by tracing path back up to the root.
     * <p>
     * 
     * @param parentNode
     *            the parent node.
     */
    private void depth_first(MutableTreeNode parentNode) {
    	// System.out.println("H5File.depth_first() pnode = "+parentNode);
    	int nelems;
    	MutableTreeNode node = null;
    	String fullPath = null;
    	String ppath = null;
//    	String objName = null;
    	DefaultMutableTreeNode pnode = (DefaultMutableTreeNode) parentNode;
    	int gid = -1;

    	H5Group pgroup = (H5Group) (pnode.getUserObject());
    	ppath = pgroup.getPath();

    	if (ppath == null) {
    		fullPath = HObject.separator;
//    		objName = "/";
    	}
    	else {
//    		objName = pgroup.getName();
    		fullPath = ppath + pgroup.getName() + HObject.separator;
    	}

    	nelems = 0;
    	try {
    		gid = pgroup.open();
    		H5G_info_t info = H5.H5Gget_info(gid);
    		nelems = (int) info.nlinks;
    	}
    	catch (HDF5Exception ex) {
    		nelems = -1;
    	}

    	if (nelems <= 0) {
    		pgroup.close(gid);
    		return;
    	}

    	// since each call of H5.H5Gget_objname_by_idx() takes about one second.
    	// 1,000,000 calls take 12 days. Instead of calling it in a loop,
    	// we use only one call to get all the information, which takes about
    	// two seconds
    	int[] objTypes = new int[nelems];
     	long[] objRefs = new long[nelems];
    	String[] objNames = new String[nelems];
  
//    	int indx_type = HDF5Constants.H5_INDEX_NAME;
//
//    	if(indexType==0){
//    		indx_type = HDF5Constants.H5_INDEX_NAME;
//    	}
//    	else if(indexType==1){
//    		indx_type = HDF5Constants.H5_INDEX_CRT_ORDER;
//    	}
//    	
    	try {
    		H5.H5Gget_obj_info_all(fid, fullPath, objNames, objTypes, objRefs);
    	}
    	catch (HDF5Exception ex) {
    		return;
    	}

    	int i0 = Math.max(0, getStartMembers());
    	int i1 = getMaxMembers();
    	if (i1 >= nelems) {
    		i1 = nelems;
    		i0 = 0; // load all members
    	}
    	i1 += i0;
    	i1 = Math.min(i1, nelems);

    	long[] oid = null;
    	String obj_name;
    	int obj_type;
    	//int lnk_type;  

    	// Iterate through the file to see members of the group
    	for (int i = i0; i < i1; i++) {
    		oid = null;
    		obj_name = objNames[i];
    		obj_type = objTypes[i];
     		long l = objRefs[i];
    		int[] otype = { 1 };

    		if (obj_name == null) {
    			continue;
    		}

    		try {
    			//  if (lnk_type == HDF5Constants.H5L_TYPE_HARD) {
    			// find the object linked to
    			byte[] ref_buf = null;
    			// String realName = H5.H5Lget_val( fid, fullPath+obj_name,
    			// HDF5Constants.H5P_DEFAULT);
    			String realName = fullPath + obj_name;
    			if ((realName != null)
    					&& !realName.startsWith(HObject.separator)) {
    				realName = fullPath + realName;
    			}
    			ref_buf = H5.H5Rcreate(fid, realName,
    					HDF5Constants.H5R_OBJECT, -1);
    			if ((realName != null) && (realName.length() > 0)
    					&& (ref_buf != null)) {
    				obj_type = H5.H5Rget_obj_type(fid, HDF5Constants.H5R_OBJECT, ref_buf, otype);
    			}
    			//  }
    		oid = new long[1];
    		oid[0] = l; // save the object ID
    		}
    		catch (HDF5Exception ex) {
    			// ex.printStackTrace(); 
    		}

    		// we need to use the OID for this release. we will rewrite this so
    		// that we do not use the deprecated constructor

    		if ((oid == null)&&(obj_type == HDF5Constants.H5O_TYPE_UNKNOWN)) {
    			H5Link link = new H5Link(this, obj_name, fullPath, oid);

    			node = new DefaultMutableTreeNode(link);
    			pnode.add(node);
    			pgroup.addToMemberList(link);
    			continue; // do the next one, if the object is not identified.
    		}

    		// create a new group
    		if (obj_type == HDF5Constants.H5O_TYPE_GROUP) {
    			H5Group g = new H5Group(this, obj_name, fullPath, pgroup, oid); // deprecated!
    			node = new DefaultMutableTreeNode(g) {
    				public static final long serialVersionUID = HObject.serialVersionUID;

    				@Override
    				public boolean isLeaf() {
    					return false;
    				}
    			};
    			pnode.add(node);
    			pgroup.addToMemberList(g);

    			// detect and stop loops
    			// a loop is detected if there exists object with the same
    			// object ID by tracing path back up to the root.
    			boolean hasLoop = false;
    			HObject tmpObj = null;
    			DefaultMutableTreeNode tmpNode = pnode;
    			while (tmpNode != null) {
    				tmpObj = (HObject) tmpNode.getUserObject();
    				if (tmpObj.equalsOID(oid)) {
    					hasLoop = true;
    					break;
    				}
    				else {
    					tmpNode = (DefaultMutableTreeNode) tmpNode.getParent();
    				}
    			}

    			// recursively go through the next group
    			// stops if it has loop.
    			if (!hasLoop) {
    				depth_first(node);
    			}
    		}
    		else if (obj_type == HDF5Constants.H5O_TYPE_DATASET) {
    			int did = -1, tid = -1, tclass = -1;
    			try {
    				did = H5.H5Dopen(fid, fullPath + obj_name, HDF5Constants.H5P_DEFAULT);
    				tid = H5.H5Dget_type(did);

    				tclass = H5.H5Tget_class(tid);
    				if ((tclass == HDF5Constants.H5T_ARRAY)
    						|| (tclass == HDF5Constants.H5T_VLEN)) {
    					// for ARRAY, the type is determined by the base type
    					int btid = H5.H5Tget_super(tid);
    					tclass = H5.H5Tget_class(btid);
    					try {
    						H5.H5Tclose(btid);
    					}
    					catch (HDF5Exception ex) {
    					}
    				}
    			}
    			catch (HDF5Exception ex) {
    			}
    			finally {
    				try {
    					H5.H5Tclose(tid);
    				}
    				catch (HDF5Exception ex) {
    				}
    				try {
    					H5.H5Dclose(did);
    				}
    				catch (HDF5Exception ex) {
    				}
    			}
    			Dataset d = null;

    			if (tclass == HDF5Constants.H5T_COMPOUND) {
    				// create a new compound dataset
    				d = new H5CompoundDS(this, obj_name, fullPath, oid); // deprecated!
    			}
    			else {
    				// create a new scalar dataset
    				d = new H5ScalarDS(this, obj_name, fullPath, oid); // deprecated!
    			}

    			node = new DefaultMutableTreeNode(d);
    			pnode.add(node);
    			pgroup.addToMemberList(d);
    		}
    		else if (obj_type == HDF5Constants.H5O_TYPE_NAMED_DATATYPE) {
    			Datatype t = new H5Datatype(this, obj_name, fullPath, oid); // deprecated!

    			node = new DefaultMutableTreeNode(t);
    			pnode.add(node);
    			pgroup.addToMemberList(t);
    		}

    	} // for ( i = 0; i < nelems; i++)

    	pgroup.close(gid);

    } // private depth_first()

    private TreeNode copyDataset(Dataset srcDataset, H5Group pgroup,
    		String dstName) throws Exception {
    	Dataset dataset = null;
    	TreeNode newNode;
    	int srcdid = -1, dstdid = -1;
    	int ocp_plist_id = -1;
    	String dname = null, path = null;

    	if (pgroup.isRoot()) {
    		path = HObject.separator;
    	}
    	else {
    		path = pgroup.getPath() + pgroup.getName() + HObject.separator;
    	}

    	if ((dstName == null) || dstName.equals(HObject.separator)
    			|| (dstName.length() < 1)) {
    		dstName = srcDataset.getName();
    	}
    	dname = path + dstName;
   
    	try {
    		srcdid = srcDataset.open();
    		dstdid = pgroup.open();

    		try{
    			ocp_plist_id = H5.H5Pcreate(HDF5Constants.H5P_OBJECT_COPY);
    			H5.H5Pset_copy_object(ocp_plist_id, HDF5Constants.H5O_COPY_EXPAND_REFERENCE_FLAG);
    			H5.H5Ocopy(srcdid, ".", dstdid, dstName, ocp_plist_id, HDF5Constants.H5P_DEFAULT);    			
    		}
    		catch (Exception ex) {
    		}
    		finally {
    			try {
    				H5.H5Pclose(ocp_plist_id);
    			}
    			catch (Exception ex) {
    			}
    		}

    		if (srcDataset instanceof H5ScalarDS) {
    			dataset = new H5ScalarDS(pgroup.getFileFormat(), dstName, path);
    		}
    		else {
    			dataset = new H5CompoundDS(pgroup.getFileFormat(), dstName,
    					path);
    		}

    		pgroup.addToMemberList(dataset);
    		newNode = new DefaultMutableTreeNode(dataset);

    	}
    		finally { 	
    			try {
    				srcDataset.close(srcdid);
    			}
    			catch (Exception ex) {
    			}
    			try {
    				pgroup.close(dstdid);
    			}
    			catch (Exception ex) {
    			}
    		}
    	
    	return newNode;
    }

    /**
     * Constructs a dataset for specified dataset idenfitier.
     * 
     * @param did
     *            the dataset idenfifier
     * @param name
     *            the name of the dataset
     * @param path
     *            the path of the dataset
     * @return the dataset if successful; otherwise return null.
     * @throws HDF5Exception
     */
    private Dataset getDataset(int did, String name, String path)
            throws HDF5Exception {
        Dataset dataset = null;
        int tid = -1, tclass = -1;
        try {
            tid = H5.H5Dget_type(did);
            tclass = H5.H5Tget_class(tid);
            if (tclass == HDF5Constants.H5T_ARRAY) {
                // for ARRAY, the type is determined by the base type
                int btid = H5.H5Tget_super(tid);
                tclass = H5.H5Tget_class(btid);
                try {
                    H5.H5Tclose(btid);
                }
                catch (HDF5Exception ex) {
                }
            }
        }
        finally {
            try {
                H5.H5Tclose(tid);
            }
            catch (HDF5Exception ex) {
            }
        }

        if (tclass == HDF5Constants.H5T_COMPOUND) {
            dataset = new H5CompoundDS(this, name, path);
        }
        else {
            dataset = new H5ScalarDS(this, name, path);
        }

        return dataset;
    }

    /**
     * Copies a named datatype to another location
     * 
     * @param srcType
     *            the source datatype
     * @param pgroup
     *            the group which the new datatype is copied to
     * @param dstName
     *            the name of the new dataype
     * @return the tree node containing the new datatype.
     * @throws Exception
     */
    private TreeNode copyDatatype(Datatype srcType, H5Group pgroup,
    		String dstName) throws Exception {
    	Datatype datatype = null;
    	int tid_src = -1, gid_dst = -1;
    	String path = null;
    	DefaultMutableTreeNode newNode = null;

    	if (pgroup.isRoot()) {
    		path = HObject.separator;
    	}
    	else {
    		path = pgroup.getPath() + pgroup.getName() + HObject.separator;
    	}

    	if ((dstName == null) || dstName.equals(HObject.separator)
    			|| (dstName.length() < 1)) {
    		dstName = srcType.getName();
    	}

    	try {
    		tid_src = srcType.open();
        	gid_dst = pgroup.open();

    		try {
    			H5.H5Ocopy(tid_src, ".", gid_dst, dstName, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
    		}
    		catch (Exception ex) {
    		}
    		datatype = new H5Datatype(pgroup.getFileFormat(), dstName, path);

    		pgroup.addToMemberList(datatype);
    		newNode = new DefaultMutableTreeNode(datatype);
    	}
    	finally {
    		try {
    			srcType.close(tid_src);
    		}
    		catch (Exception ex) {
    		}   
    		try {
    			pgroup.close(gid_dst);
    		}
    		catch (Exception ex) {
    		}
    	}

    	return newNode;
    }

    /**
     * Copies a group and its members to a new location
     * 
     * @param srcGroup
     *            the source group
     * @param pgroup
     *            the location which the new group is located
     * @param dstName
     *            the name of the new group
     * @return the tree node containing the new group;
     */
    private TreeNode copyGroup(H5Group srcGroup, H5Group pgroup, String dstName)
            throws Exception {
        H5Group group = null;
        DefaultMutableTreeNode newNode = null;
        int srcgid = -1, dstgid = -1;
        String gname = null, path = null;

        if (pgroup.isRoot()) {
            path = HObject.separator;
        }
        else {
            path = pgroup.getPath() + pgroup.getName() + HObject.separator;
        }

        if ((dstName == null) || dstName.equals(HObject.separator)
                || (dstName.length() < 1)) {
            dstName = srcGroup.getName();
        }

        gname = path + dstName;
        
        try {
        	srcgid = srcGroup.open();
        	dstgid = pgroup.open();
        	try {
        		H5.H5Ocopy(srcgid, ".", dstgid, dstName, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
        	}
        	catch (Exception ex) {
        		ex.printStackTrace();
        	}
        	
        	group = new H5Group(pgroup.getFileFormat(), dstName, path, pgroup);
        	newNode = new DefaultMutableTreeNode(group) {
        		public static final long serialVersionUID = HObject.serialVersionUID;

        		@Override
				public boolean isLeaf() {
        			return false;
        		}
        	};
        	depth_first(newNode);
        	pgroup.addToMemberList(group);    	
        }
        
    	finally {
    		try {
    			srcGroup.close(srcgid);
    		}
    		catch (Exception ex) {
    		}   
    		try {
    			pgroup.close(dstgid);
    		}
    		catch (Exception ex) {
    		}
    	}
    	
        return newNode;
    }

    /**
     * Constructs a group for specified group identifier and retrieves members.
     * 
     * @param gid
     *            The group identifier.
     * @param name
     *            The group name.
     * @param pGroup
     *            The parent group, or null for the root group.
     * @return The group if successful; otherwise returns false.
     * @throws HDF5Exception
     */
    private H5Group getGroup(int gid, String name, Group pGroup)
    throws HDF5Exception {
    	String parentPath = null;
    	String thisFullName = null;
    	String memberFullName = null;

    	if (pGroup == null) {
    		thisFullName = name = "/";
    	}
    	else {
    		parentPath = pGroup.getFullName();
    		if ((parentPath == null) || parentPath.equals("/")) {
    			thisFullName = "/" + name;
    		}
    		else {
    			thisFullName = parentPath + "/" + name;
    		}
    	}

    	// get rid of any extra "/"
    	if (parentPath != null) {
    		parentPath = parentPath.replaceAll("//", "/");
    	}
    	if (thisFullName != null) {
    		thisFullName = thisFullName.replaceAll("//", "/");
    	}

    	H5Group group = new H5Group(this, name, parentPath, pGroup);

    	H5G_info_t group_info = null;
    	H5O_info_t obj_info = null;
    	int oid = -1;
    	String link_name = null;
    	try{
    		group_info = H5.H5Gget_info(gid);
    	}
    	catch(HDF5Exception ex){
    	}
    	try{
    		oid = H5.H5Oopen(gid, thisFullName, HDF5Constants.H5P_DEFAULT);
    	}
    	catch(HDF5Exception ex){
    	}

    	//retrieve only the immediate members of the group, do not follow subgroups
    	for (int i = 0; i < group_info.nlinks; i++) {
    		try {
    			link_name = H5.H5Lget_name_by_idx(gid, thisFullName, HDF5Constants.H5_INDEX_NAME, HDF5Constants.H5_ITER_INC, i, HDF5Constants.H5P_DEFAULT);
    			obj_info  = H5.H5Oget_info_by_idx(oid, thisFullName, HDF5Constants.H5_INDEX_NAME, HDF5Constants.H5_ITER_INC,i, HDF5Constants.H5P_DEFAULT);	
    		}
    		catch (HDF5Exception ex) {
    			ex.printStackTrace();
    			// do not stop if accessing one member fails
    			continue;
    		}
    		// create a new group
    		if (obj_info.type == HDF5Constants.H5O_TYPE_GROUP) {
    			H5Group g = new H5Group(this, link_name, thisFullName, group);
    			group.addToMemberList(g);
    		}
    		else if (obj_info.type == HDF5Constants.H5O_TYPE_DATASET) {
    			int did = -1;
    			Dataset d = null;

    			if ((thisFullName == null) || thisFullName.equals("/")) {
    				memberFullName = "/" + link_name;
    			}
    			else {
    				memberFullName = thisFullName + "/" + link_name;
    			}

    			try {
    				did = H5.H5Dopen(fid, memberFullName, HDF5Constants.H5P_DEFAULT);
    				d = getDataset(did, link_name, thisFullName);
    			}
    			finally {
    				try {
    					H5.H5Dclose(did);
    				}
    				catch (HDF5Exception ex) {
    				}
    			}
    			group.addToMemberList(d);
    		} 
    	} // End of for loop.
    	try{
    		if(oid>=0)
    			H5.H5Oclose(oid);
    	}
    	catch(HDF5Exception ex){
    	}
    	return group;
    }
 
    /**
     * Retrieves the name of the target object that is being linked to.
     * 
     * @param obj
     *            The current link object.
     * @return The name of the target object.
     * @throws HDF5Exception
     */
    public static String getLinkTargetName(HObject obj) throws Exception{
    	String[] link_value = {null,null};
    	String targetObjName = null;
    	
    	if(obj==null){
    		return null;
    	}

    	H5L_info_t link_info = null;
    	try {
    		link_info = H5.H5Lget_info(obj.getFID(), obj.getFullName(), HDF5Constants.H5P_DEFAULT);
    	}
    	catch (Throwable err) {
    	}
    	if(link_info!=null){
    		if((link_info.type==HDF5Constants.H5L_TYPE_SOFT)||(link_info.type==HDF5Constants.H5L_TYPE_EXTERNAL)){
    			try{
    				H5.H5Lget_val(obj.getFID(), obj.getFullName(), link_value, HDF5Constants.H5P_DEFAULT);
    			}catch(Exception ex){
    				ex.printStackTrace();
    			}
    			if(link_info.type==HDF5Constants.H5L_TYPE_SOFT)
    				targetObjName = link_value[0];
    			else if(link_info.type==HDF5Constants.H5L_TYPE_EXTERNAL){

    				targetObjName = link_value[1] + FileFormat.FILE_OBJ_SEP + link_value[0] ;
    			}
    		}
    	}
    	return targetObjName ;
    }
 
    /**
     * Renames an attribute.
     * 
     * @param obj
     *            The object whose attribute is to be renamed.
     * @param oldAttrName
     *            The current name of the attribute.
     * @param newAttrName
     *            The new name of the attribute.
     * @throws HDF5Exception
     */
    public void renameAttribute(HObject obj, String oldAttrName, String newAttrName) throws Exception {
    	if(!attrFlag){
    		attrFlag = true;
    		H5.H5Arename_by_name(obj.getFID(), obj.getName(), oldAttrName, newAttrName, HDF5Constants.H5P_DEFAULT); 
    	}
    	
    }
}
