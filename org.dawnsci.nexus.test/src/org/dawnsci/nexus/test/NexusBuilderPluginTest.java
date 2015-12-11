package org.dawnsci.nexus.test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;
import org.junit.Test;

public class NexusBuilderPluginTest {
	
	private static NexusBuilderFactory nexusBuilderFactory;

	public static void setNexusBuilderFactory(NexusBuilderFactory nexusBuilderFactory) {
		NexusBuilderPluginTest.nexusBuilderFactory = nexusBuilderFactory; 
	}
	
	public static NexusBuilderFactory getNexusBuilderFactory() {
		return nexusBuilderFactory;
	}
	
	@Test
	public void testNexusBuilderDS() {
		assertThat(nexusBuilderFactory, is(notNullValue()));
	}
	
}
