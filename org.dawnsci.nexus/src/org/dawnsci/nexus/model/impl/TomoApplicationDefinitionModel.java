/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Dickie - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.dawnsci.nexus.model.impl;

import java.text.MessageFormat;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.impl.NXinstrumentImpl;
import org.eclipse.dawnsci.nexus.impl.NXsampleImpl;
import org.eclipse.dawnsci.nexus.impl.NXsubentryImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.model.api.NexusApplicationDefinitionModel;
import org.eclipse.dawnsci.nexus.model.api.NexusDataModel;
import org.eclipse.dawnsci.nexus.model.api.NexusEntryModel;
import org.eclipse.dawnsci.nexus.model.api.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.validation.NXtomoValidator;
import org.eclipse.dawnsci.nexus.validation.NexusValidationException;

public class TomoApplicationDefinitionModel extends AbstractNexusApplicationDefinitionModel implements NexusApplicationDefinitionModel {

	private static final String NX_Y_TRANSLATION = "y_translation";
	
	private static final String NX_Z_TRANSLATION = "z_translation";
	
	private NXinstrumentImpl instrument = null;

	private NXsampleImpl sample = null;
	
	public TomoApplicationDefinitionModel(final NexusEntryModel nexusEntryModel, final NXsubentryImpl subentry) {
		super(NexusApplicationDefinition.NX_TOMO, nexusEntryModel, subentry);
	}

	@Override
	public <N extends NXobject> N addNexusObject(NexusObjectProvider<N> nexusObjectProvider) throws NexusException {
		throw new UnsupportedOperationException("This method is not supported for this application definition. Please use the specific method for each object provider");
	}

	public void setSource(NexusObjectProvider<NXsource> source) {
		instrument.setSource(source.createNexusObject(getNexusNodeFactory()));
	}

	public void setDetector(NexusObjectProvider<NXdetector> detector) {
		instrument.setDetector(detector.createNexusObject(getNexusNodeFactory()));
	}
	
	private <N extends NXobject> DataNode getDataNode(NexusObjectProvider<N> baseClassProvider) throws NexusException {
		final N nexusObject = baseClassProvider.createNexusObject(getNexusNodeFactory());
		final String dataNodeName = baseClassProvider.getDefaultDataNodeName();
		final DataNode dataNode = nexusObject.getDataNode(dataNodeName);
		if (dataNode == null) {
			throw new NexusException(MessageFormat.format("No such data node for {0} with name ''{1}''",
					baseClassProvider.getClass().getSimpleName(), dataNodeName));
		}
		
		return dataNode;
	}
	
	public void setSample(NexusObjectProvider<NXsample> sample) throws NexusException {
		this.sample = (NXsampleImpl) sample.getNexusObject(); 
		subentry.setSample(this.sample);
	}

	public void setSampleName(String sampleName) {
		sample.setNameScalar(sampleName);
	}
	
	public void setRotationAnglePositioner(NexusObjectProvider<NXpositioner> rotationAnglePositioner) throws NexusException {
		final DataNode rotationAngleDataNode = getDataNode(rotationAnglePositioner);
		sample.addDataNode(NXsampleImpl.NX_ROTATION_ANGLE, rotationAngleDataNode);
	}

	public void setXTranslation(NexusObjectProvider<NXpositioner> xPositioner) throws NexusException {
		final DataNode xTranslation = getDataNode(xPositioner);
		sample.addDataNode(NXsampleImpl.NX_X_TRANSLATION, xTranslation);
	}
	
	public void setXTranslation(DataNode xTranslation) throws NexusException {
		sample.addDataNode(NXsampleImpl.NX_X_TRANSLATION, xTranslation); 
	}
	
	public void setYTranslation(NexusObjectProvider<NXpositioner> yPositioner) throws NexusException {
		final DataNode yTranslation = getDataNode(yPositioner);
		sample.addDataNode(NX_Y_TRANSLATION, yTranslation);
	}
	
	public void setYTranslation(DataNode yTranslation) throws NexusException {
		sample.addDataNode(NX_Y_TRANSLATION, yTranslation);
	}

	public void setZTranslation(NexusObjectProvider<NXpositioner> zPositioner) throws NexusException {
		final DataNode zTranslation = getDataNode(zPositioner);
		sample.addDataNode(NX_Z_TRANSLATION, zTranslation);
	}

	public void setZTranslation(DataNode zTranslation) throws NexusException {
		sample.addDataNode(NX_Z_TRANSLATION, zTranslation);
	}

	public void setControl(NexusObjectProvider<NXmonitor> monitorControl) {
		subentry.setMonitor("control", monitorControl.createNexusObject(getNexusNodeFactory()));
	}

	@Override
	public NexusDataModel newData() throws NexusException {
		NXdataImpl nxData = getNexusNodeFactory().createNXdata();
		subentry.setData(nxData);

		PredeterminedLinksAppDefDataModel dataModel = new PredeterminedLinksAppDefDataModel(nxData);
		addPredeterminedLinks(dataModel);

		return dataModel;
	}

	@Override
	public void validate() throws NexusValidationException {
		NXtomoValidator validator = new NXtomoValidator();
		validator.validate(subentry);
	}

	@Override
	public void addDefaultGroups() {
		NexusNodeFactory nodeFactory = getNexusNodeFactory();
		instrument = nodeFactory.createNXinstrument();
		subentry.setInstrument(instrument);

		sample = nodeFactory.createNXsample();
		subentry.setSample(sample);
	}

	protected void addPredeterminedLinks(PredeterminedLinksAppDefDataModel dataModel) throws NexusException {
		dataModel.addLink("data", getDataNode("instrument/detector/data"));
		dataModel.addLink("rotation_angle", getDataNode("sample/rotation_angle"));
		dataModel.addLink("image_key", getDataNode("instrument/detector/image_key"));
	}

}
