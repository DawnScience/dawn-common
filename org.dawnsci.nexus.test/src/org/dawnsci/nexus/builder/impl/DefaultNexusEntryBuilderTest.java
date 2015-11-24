package org.dawnsci.nexus.builder.impl;

import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_SAMPLE;
import static org.eclipse.dawnsci.nexus.impl.NXentryImpl.NX_ENTRY_IDENTIFIER;
import static org.eclipse.dawnsci.nexus.impl.NXentryImpl.NX_EXPERIMENT_IDENTIFIER;
import static org.eclipse.dawnsci.nexus.impl.NXentryImpl.NX_PROGRAM_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;

public class DefaultNexusEntryBuilderTest {
	
	public static class TestPositioner extends AbstractNexusObjectProvider<NXpositioner> {
	
		public TestPositioner() {
			super("positioner", NexusBaseClass.NX_POSITIONER);
		}
		
		public TestPositioner(String name) {
			super(name, NexusBaseClass.NX_POSITIONER);
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			return nodeFactory.createNXpositioner();
		}
		
	}
	
	public static class TestDetector extends AbstractNexusObjectProvider<NXdetector> {

		public TestDetector() {
			super(NexusBaseClass.NX_DETECTOR);
		}
		
		@Override
		protected NXdetector doCreateNexusObject(NexusNodeFactory nodeFactory) {
			return nodeFactory.createNXdetector();
		}
		
	}
	
	public static class TestSource extends AbstractNexusObjectProvider<NXsource> {
		
		public TestSource() {
			super(NexusBaseClass.NX_SOURCE);
		}
		
		@Override
		protected NXsource doCreateNexusObject(NexusNodeFactory nodeFactory) {
			return nodeFactory.createNXsource();
		}
		
	}

	private NexusEntryBuilder nexusEntryBuilder;
	
	private NXentryImpl nxEntry;
	
	@Before
	public void setUp() throws NexusException {
		nexusEntryBuilder = new DefaultNexusFileBuilder("test").newEntry();
		nxEntry = nexusEntryBuilder.getNXentry();
	}
	
	@Test
	public void testGetNXentry() {
		assertThat(nexusEntryBuilder.getNXentry(), notNullValue(NXentry.class));
	}
	
	@Test
	public void testGetNodeFactory() {
		assertThat(nexusEntryBuilder.getNodeFactory(), notNullValue(NexusNodeFactory.class));
	}
	
	@Test
	public void testAddDefaultGroups() {
		assertThat(nxEntry.getNumberOfGroupNodes(), is(0));
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		assertThat(nxEntry.getInstrument(), notNullValue(NXinstrument.class));
		assertThat(nxEntry.getSample(), notNullValue(NXsample.class));
	}
	
	@Test
	public void testAdd() throws NexusException {
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		NXinstrument instrument = nxEntry.getInstrument();
		assertThat(instrument.getNumberOfGroupNodes(), is(0));
		
		TestPositioner positionerProvider = new TestPositioner();
		nexusEntryBuilder.add(positionerProvider);
		
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		assertThat(instrument.getNumberOfGroupNodes(), is(1));
		assertThat(instrument.getPositioner(), is(sameInstance(positionerProvider.getNexusObject())));
	}
	
	@Test
	public void testAdd_namedGroup() throws NexusException {
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		NXinstrument instrument = nxEntry.getInstrument();
		assertThat(instrument.getNumberOfGroupNodes(), is(0));
		
		TestPositioner positionerProvider = new TestPositioner("x");
		nexusEntryBuilder.add(positionerProvider);

		assertThat(instrument.getNumberOfGroupNodes(), is(1));
		assertThat(instrument.getPositioner(), is(nullValue()));
		assertThat(instrument.getPositioner("x"), is(sameInstance(positionerProvider.getNexusObject())));
	}
	
	@Test
	public void testAdd_samplePositioner() throws NexusException {
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		NXinstrument instrument = nxEntry.getInstrument();
		assertThat(instrument.getNumberOfGroupNodes(), is(0));
		NXsample sample = nxEntry.getSample();
		assertThat(sample.getNumberOfGroupNodes(), is(0));
		
		TestPositioner positionerProvider = new TestPositioner();
		positionerProvider.setCategory(NX_SAMPLE);
		nexusEntryBuilder.add(positionerProvider);
		
		assertThat(instrument.getNumberOfGroupNodes(), is(0));
		assertThat(sample.getNumberOfGroupNodes(), is(1));
		assertThat(sample.getPositioner(), is(sameInstance(positionerProvider.getNexusObject())));
	}
	
	@Test
	public void testAdd_sample() throws NexusException {
		NexusObjectProvider<NXsample> sampleProvider = new AbstractNexusObjectProvider<NXsample>(NexusBaseClass.NX_SAMPLE) {

			@Override
			protected NXsample doCreateNexusObject(NexusNodeFactory nodeFactory) {
				return nodeFactory.createNXsample();
			}
			
		};
		
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		NXsample oldSample = nxEntry.getSample();
		assertThat(oldSample, is(notNullValue()));
		
		nexusEntryBuilder.add(sampleProvider);
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		assertThat(nxEntry.getSample(), is(sameInstance(sampleProvider.getNexusObject())));
		assertThat(nxEntry.getSample(), is(not(sameInstance(oldSample))));
	}
	
	@Test
	public void testAdd_collection() throws NexusException {
		TestPositioner xPositioner = new TestPositioner("x");
		TestPositioner yPositioner = new TestPositioner("y");
		TestPositioner zPositioner = new TestPositioner("z");
		TestDetector detector = new TestDetector();
		TestPositioner samplePositioner = new TestPositioner();
		samplePositioner.setCategory(NX_SAMPLE);
		
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		NXinstrument instrument = nxEntry.getInstrument();
		assertThat(instrument.getNumberOfGroupNodes(), is(0));
		NXsample sample = nxEntry.getSample();
		assertThat(sample.getNumberOfGroupNodes(), is(0));

		nexusEntryBuilder.add(Arrays.asList(xPositioner, yPositioner, zPositioner, samplePositioner, detector));
		
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		assertThat(instrument.getNumberOfGroupNodes(), is(4));
		assertThat(instrument.getPositioner("x"), is(sameInstance(xPositioner.getNexusObject())));
		assertThat(instrument.getPositioner("y"), is(sameInstance(yPositioner.getNexusObject())));
		assertThat(instrument.getPositioner("z"), is(sameInstance(zPositioner.getNexusObject())));
		assertThat(instrument.getDetector(), is(sameInstance(detector.getNexusObject())));
		assertThat(sample.getPositioner(), is(sameInstance(samplePositioner.getNexusObject())));
	}
	
	@Test
	public void testAddMetadata() throws NexusException {
		nexusEntryBuilder.addDefaultGroups();
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		assertThat(nxEntry.getNumberOfDataNodes(), is(0));
		
		MapBasedMetadataProvider metadata = new MapBasedMetadataProvider();
		metadata.addMetadataEntry(NX_ENTRY_IDENTIFIER, "12345");
		metadata.addMetadataEntry(NX_EXPERIMENT_IDENTIFIER, "myexperiment");
		metadata.addMetadataEntry(NX_PROGRAM_NAME, "GDA 8.36.0");
		metadata.addMetadataEntry("scan_command", "scan foo bar etc");
		metadata.addMetadataEntry("scan_identifier", "a3d668c0-e3c4-4ed9-b127-4a202b2b6bac");
		metadata.addMetadataEntry(NXentryImpl.NX_TITLE, "Test Scan");

		nexusEntryBuilder.addMetadata(metadata);
		
		assertThat(nxEntry.getNumberOfGroupNodes(), is(2));
		assertThat(nxEntry.getNumberOfDataNodes(), is(6));
		assertThat(nxEntry.getEntry_identifierScalar(), is(equalTo("12345")));
		assertThat(nxEntry.getExperiment_identifierScalar(), is(equalTo("myexperiment")));
		assertThat(nxEntry.getProgram_name(), is(equalTo("GDA 8.36.0")));
		assertThat(nxEntry.getString("scan_command"), is(equalTo("scan foo bar etc")));
		assertThat(nxEntry.getString("scan_identifier"), is(equalTo("a3d668c0-e3c4-4ed9-b127-4a202b2b6bac")));
		assertThat(nxEntry.getTitleScalar(), is(equalTo("Test Scan")));
	}

}
