package org.dawb.hdf5.model.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.model.IHierarchicalDataFileModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

public class HierarchicalDataModelTest {

	private List<String> createdFileModels;
	private HierarchicalDataModel model;

	@Before
	public void before() {
		createdFileModels = new ArrayList<>();
		model = new HierarchicalDataModel(
				new IHierarchicalDataModelGetFileModel() {
					@Override
					public IHierarchicalDataFileModel createFileModel(
							final String fullPath) {
						// We are faking the file model. The paths being passed
						// in are being recorded in my list so that they can be
						// verified, but we are actually
						createdFileModels.add(fullPath);
						return new HierarchicalDataFileModel(
								new IHierarchicalDataFileGetReader() {
									@Override
									public IHierarchicalDataFile getReader()
											throws Exception {
										fail("This test should not be accessing underlying file");
										return null;
									}
								});
					}
				});
	}

	private IFile makeIFile(String path) {
		IPath ipath = new Path(path);
		IFile ifile = mock(IFile.class);
		when(ifile.getRawLocation()).thenReturn(ipath);
		return ifile;
	}

	private void assertPathEquals(String expected, String actual) {
		String expectedToOs = new Path(expected).toOSString();
		assertEquals(expectedToOs, actual);
	}

	@Test
	public void testBasic() {
		assertEquals(0, createdFileModels.size());
		assertNotNull(model.getFileModel(makeIFile("/path/to/file")));
		assertPathEquals("/path/to/file", createdFileModels.get(0));
		assertEquals(1, createdFileModels.size());
	}

	@Test
	public void testHitsCache() {
		// get the model twice and make sure we only create one
		// File Model
		IHierarchicalDataFileModel fileModelA = model.getFileModel(makeIFile("/path/to/file"));
		IHierarchicalDataFileModel fileModelB = model.getFileModel(makeIFile("/path/to/file"));
		assertNotNull(fileModelA);
		assertTrue(fileModelA == fileModelB);
		assertPathEquals("/path/to/file", createdFileModels.get(0));
		assertEquals(1, createdFileModels.size());
	}

	@Test
	public void testClearCache() {
		// get the model, clear the file from cache and make
		// sure we get a new copy
		model.getFileModel(makeIFile("/path/to/file"));
		model.getFileModel(makeIFile("/path/to/file"));
		assertPathEquals("/path/to/file", createdFileModels.get(0));
		assertEquals(1, createdFileModels.size());
		model.clearFileCache(makeIFile("/path/to/file"));
		model.getFileModel(makeIFile("/path/to/file"));
		assertPathEquals("/path/to/file", createdFileModels.get(1));
		assertEquals(2, createdFileModels.size());
	}

	@Test
	public void testTwoFiles() {
		// Make sure that when alternating between two files that
		// we get the expected files
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		assertPathEquals("/path/to/file0", createdFileModels.get(0));
		assertPathEquals("/path/to/file1", createdFileModels.get(1));
		assertEquals(2, createdFileModels.size());
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		assertEquals(2, createdFileModels.size());
	}

	@Test
	public void testTwoFilesClear() {
		// Make sure that when we clear 1 file from the cache the
		// unmentioned file is not reloaded
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		assertPathEquals("/path/to/file0", createdFileModels.get(0));
		assertPathEquals("/path/to/file1", createdFileModels.get(1));
		assertEquals(2, createdFileModels.size());
		model.clearFileCache(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		model.getFileModel(makeIFile("/path/to/file1"));
		model.getFileModel(makeIFile("/path/to/file0"));
		assertPathEquals("/path/to/file1", createdFileModels.get(2));
		assertEquals(3, createdFileModels.size());
	}

}
