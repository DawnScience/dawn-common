/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.common.ui.plot.roi;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

import org.eclipse.swt.layout.GridData;

/**
 * Custom widget to display ROI information
 */
public class ROIMetadataComposite {

	private Text xyStartText;
	private Text xyEndText;
	private Text xStartText;
	private Text xEndText;
	private Text yStartText;
	private Text yEndText;
	private Text sumText;
	private Text maxText;
	private Text minText;

	public ROIMetadataComposite(final Composite parent) {
//		super(parent, style);
//		parent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
//		parent.setLayout(new GridLayout(1, false));
		
		final ScrolledComposite scrollComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		
		final Composite contentComposite = new Composite(scrollComposite, SWT.FILL);
		contentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		contentComposite.setLayout(new GridLayout(1, false));
		
		Label metadataLabel = new Label(contentComposite, SWT.NONE);
		metadataLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		metadataLabel.setAlignment(SWT.CENTER);
		metadataLabel.setText("ROI MetaData");
		
		Group rectangularROIGroup = new Group(contentComposite, SWT.NONE);
		rectangularROIGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rectangularROIGroup.setLayout(new GridLayout(2, false));
		rectangularROIGroup.setText("Rectangular ROI");
		
		Label xyStartLabel = new Label(rectangularROIGroup, SWT.NONE);
		xyStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xyStartLabel.setText("x, y Start");
		
		xyStartText = new Text(rectangularROIGroup, SWT.BORDER);
		xyStartText.setEditable(false);
		xyStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label xyEndLabel = new Label(rectangularROIGroup, SWT.NONE);
		xyEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xyEndLabel.setText("x, y End");
		
		xyEndText = new Text(rectangularROIGroup, SWT.BORDER);
		xyEndText.setEditable(false);
		xyEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label sumLabel = new Label(rectangularROIGroup, SWT.NONE);
		sumLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		sumLabel.setText("Sum");
		
		sumText = new Text(rectangularROIGroup, SWT.BORDER);
		sumText.setEditable(false);
		sumText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label maxLabel = new Label(rectangularROIGroup, SWT.NONE);
		maxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		maxLabel.setText("Max");
		
		maxText = new Text(rectangularROIGroup, SWT.BORDER);
		maxText.setEditable(false);
		maxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label minLabel = new Label(rectangularROIGroup, SWT.NONE);
		minLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		minLabel.setText("Max");
		
		minText = new Text(rectangularROIGroup, SWT.BORDER);
		minText.setEditable(false);
		minText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	
		Group xROIGroup = new Group(contentComposite, SWT.NONE);
		xROIGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xROIGroup.setLayout(new GridLayout(2, false));
		xROIGroup.setText("X ROI");
		
		Label xStartLabel = new Label(xROIGroup, SWT.NONE);
		xStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xStartLabel.setText("x Start");
		
		xStartText = new Text(xROIGroup, SWT.BORDER);
		xStartText.setEditable(false);
		xStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label xEndLabel = new Label(xROIGroup, SWT.NONE);
		xEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xEndLabel.setText("x End");
		
		xEndText = new Text(xROIGroup, SWT.BORDER);
		xEndText.setEditable(false);
		xEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group yROIGroup = new Group(contentComposite, SWT.NONE);
		yROIGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		yROIGroup.setLayout(new GridLayout(2, false));
		yROIGroup.setText("Y ROI");
		
		Label yStartLabel = new Label(yROIGroup, SWT.NONE);
		yStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		yStartLabel.setText("y Start");
		
		yStartText = new Text(yROIGroup, SWT.BORDER);
		yStartText.setEditable(false);
		yStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label yEndLabel = new Label(yROIGroup, SWT.NONE);
		yEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		yEndLabel.setText("y End");
		
		yEndText = new Text(yROIGroup, SWT.BORDER);
		yEndText.setEditable(false);
		yEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		scrollComposite.setContent(contentComposite);
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrollComposite.getClientArea();
				scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
			}
		});
	}

	public boolean isDisposed(){
		boolean isDisposed = false;
		if(xyStartText != null && xyEndText != null 
				&& (xyStartText.isDisposed() || xyEndText.isDisposed())){
			isDisposed = false;
		} else {
			isDisposed = true;
		}
		return isDisposed; 
	}

	public void setEditingRegion(ROIBase roi) {
		if(roi instanceof RectangularROI){
			RectangularROI rroi = (RectangularROI)roi;
			double xStart = rroi.getPointX();
			double yStart = rroi.getPointY();
			double xEnd = rroi.getEndPoint()[0];
			double yEnd = rroi.getEndPoint()[1];
//			double sum = rroi.
			xyStartText.setText(String.valueOf(xStart)+", "+ String.valueOf(yStart));
			xyEndText.setText(String.valueOf(xEnd)+", "+ String.valueOf(yEnd));
			
		}
	}

}
