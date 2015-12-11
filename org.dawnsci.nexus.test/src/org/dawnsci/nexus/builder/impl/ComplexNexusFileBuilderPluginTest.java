package org.dawnsci.nexus.builder.impl;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.dawnsci.nexus.test.NexusBuilderPluginTest;
import org.eclipse.dawnsci.nexus.builder.NexusBuilderFactory;

public class ComplexNexusFileBuilderPluginTest extends
		ComplexNexusFileBuilderTest {

	@Override
	protected NexusBuilderFactory getNexusBuilderFactory() {
		return NexusBuilderPluginTest.getNexusBuilderFactory();
	}
	
	@Override
	protected void checkNexusBuilderFactory(NexusBuilderFactory nexusBuilderFactory) {
		assertThat(nexusBuilderFactory, is(sameInstance(NexusBuilderPluginTest.getNexusBuilderFactory())));
	}
	
	
}
