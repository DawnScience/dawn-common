package org.dawnsci.persistence.test.operations;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JunkTestModelROI extends AbstractOperationModel{

	@JsonIgnore
	private SectorROI roi = null;
	@JsonIgnore
	private IDataset data;
	@JsonIgnore
	private IFunction func;
	@JsonIgnore
	private IROI roi2 = null;
	@JsonIgnore
	private IDataset foo;
	@JsonIgnore
	private IFunction bar;
	
	private int xDim = 20;
	
	
	public IDataset getData() {
		return data;
	}

	public void setData(IDataset data) {
		firePropertyChange("data", this.data, this.data = data);
	}

	public IFunction getFunc() {
		return func;
	}

	public void setFunc(IFunction func) {
		firePropertyChange("func", this.func, this.func = func);
	}

	public IROI getRoi2() {
		return roi2;
	}

	public void setRoi2(IROI roi2) {
		firePropertyChange("roi2", this.roi2, this.roi2 = roi2);
	}

	public IDataset getFoo() {
		return foo;
	}

	public void setFoo(IDataset foo) {
		firePropertyChange("foo", this.foo, this.foo = foo);
	}

	public IFunction getBar() {
		return bar;
	}

	public void setBar(IFunction bar) {
		firePropertyChange("bar", this.bar, this.bar = bar);
	}
	
	public int getxDim() {
		return xDim;
	}
	
	public void setxDim(int xDim) {
		firePropertyChange("xDim", this.xDim, this.xDim = xDim);
	}

	public IROI getRoi() {
		return roi;
	}

	public void setRoi(SectorROI roi) {
		firePropertyChange("roi", this.roi, this.roi = roi);
	}
	
}
