package org.dawb.common.ui.databinding.model;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;

/**
 * View Model of  Region Table
 * This View Model is the model to be bind with a corresponding widget
 * @author wqk87977
 *
 */
public class RegionTableViewModel {

	private IObservableList rows = new WritableList();
	private RegionRowDataModel axisRow;

	private RegionRowDataModel pixelRow;

	{
		axisRow = new RegionRowDataModel(new String("Axis"), new Double(1), new Double(2), new Double(3), new Double(4), new Double(5), new Double(6));
		pixelRow = new RegionRowDataModel(new String("Pixel"), new Double(1), new Double(2), new Double(3), new Double(4), new Double(5), new Double(6)); 
		rows.add(axisRow);
		rows.add(pixelRow);
	}

	public IObservableList getValues() {
		return rows;
	}

	public void setValues(IObservableList rows){
		this.rows = rows;
	}
}
