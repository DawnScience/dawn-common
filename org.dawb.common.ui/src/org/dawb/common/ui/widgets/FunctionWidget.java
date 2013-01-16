package org.dawb.common.ui.widgets;

import java.util.Vector;

import org.dawb.common.ui.plot.function.FunctionEditTable;
import org.dawb.common.ui.plot.function.FunctionModifiedEvent;
import org.dawb.common.ui.plot.function.FunctionType;
import org.dawb.common.ui.plot.function.IFunctionModifiedListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

/**
 * A custom Widget for editing a Mathematical Function. Uses <b>FunctionEditTable</b> widget.
 * Linked to a model file using an IWorkflowUpdater object
 */
public class FunctionWidget {

	private static final Logger logger = LoggerFactory.getLogger(FunctionWidget.class);
	
	private FunctionEditTable functionEditor;
	private CCombo functionType;
	private Spinner polynomialDegree;
	private Label labelDegree;

//	private FunctionUpdateJob updateFunctionJob;

	private Vector<IFunctionModifiedListener> functionModifiedListeners = new Vector<IFunctionModifiedListener>();

//	private IWorkflowUpdater updater;

	//private String functionID;

	private IFunctionModifiedListener functionListener;

	/**
	 * Widget constructor
	 * @param parent
	 */
	public FunctionWidget(Composite parent) {

	//	this.functionID = functionID;
//		this.updateFunctionJob = new FunctionUpdateJob("ARPES Worflow Updating Function");

		final Composite top= new Composite(parent, SWT.LEFT);
		top.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		top.setLayout(new GridLayout(2, false));
		
		final Label label = new Label(top, SWT.NONE);
		label.setText("Function type    ");
		
		functionType = new CCombo(top, SWT.READ_ONLY|SWT.BORDER);
		functionType.setItems(FunctionType.getTypes());
		functionType.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		functionType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					AFunction myFunction = FunctionType.createNew(functionType.getSelectionIndex());
					functionEditor.setFunction(myFunction, null);
					if(FunctionType.getType(functionType.getSelectionIndex())==FunctionType.POLYNOMIAL){
						labelDegree.setVisible(true);
						polynomialDegree.setVisible(true);
					}else{
						labelDegree.setVisible(false);
						polynomialDegree.setVisible(false);
					}
					FunctionWidget.this.functionModified();
				} catch (Exception e1) {
					logger.error("Cannot create function "+FunctionType.getType(functionType.getSelectionIndex()).getName(), e1);
				}
			}
		});
		
		labelDegree = new Label(top, SWT.NONE);
		labelDegree.setText("Polynomial degree ");
		labelDegree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		labelDegree.setVisible(false);
		
		polynomialDegree = new Spinner(top, SWT.NONE);
		polynomialDegree.setToolTipText("Polynomial degree");
		polynomialDegree.setMinimum(1);
		polynomialDegree.setMaximum(100);
		polynomialDegree.setVisible(false);
		polynomialDegree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					AFunction myFunction = FunctionType.createNew(FunctionType.POLYNOMIAL);
					Polynomial polynom = (Polynomial)myFunction;
					polynom.setDegree(polynomialDegree.getSelection()-1);
					functionEditor.setFunction(myFunction, null);
					FunctionWidget.this.functionModified();
				} catch (Exception e1) {
					logger.error("Cannot create function "+FunctionType.POLYNOMIAL, e1);
				}
			}
		});
		
		this.functionEditor = new FunctionEditTable();
		functionEditor.createPartControl(parent);
//		functionEditor.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				FunctionWidget.this.functionModified();
//			}
//		});
	
		if(functionListener != null)
			functionModifiedListeners.addElement(functionListener);
	}
	
	protected boolean isResizable() {
		return true;
	}

	public void setFunctionTypeEnabled(boolean isEnabled){
		functionType.setEnabled(isEnabled);
	}

	public void setFunction(AFunction function) {
		final int index = FunctionType.getIndex(function.getClass());
		functionType.select(index);
		functionEditor.setFunction(function, null);
	}
	
	public AFunction getFunction() {
		return functionEditor.getFunction();
	}

	protected void functionModified() {
		FunctionModifiedEvent e = new FunctionModifiedEvent(this);
		int size = functionModifiedListeners.size();
		for (int i = 0; i < size; i++) {
			IFunctionModifiedListener listener = functionModifiedListeners.elementAt(i);
			listener.functionModified(e);
		}
	}

	public void setFunctionModifiedListener(IFunctionModifiedListener functionListener){
		this.functionListener = functionListener;
		functionModifiedListeners.addElement(functionListener);
	}

//	private IFunctionModifiedListener functionListener = new IFunctionModifiedListener() {
//		@Override
//		public void functionModified(FunctionModifiedEvent event) {
//			updateFunctionJob.setUpdater(updater);
//			updateFunctionJob.setActorID(functionID);
//			updateFunctionJob.setFunction(getFunction());
//			updateFunctionJob.schedule();
//		}
//	};
//	public void addFunctionModifiedListener(IFunctionModifiedListener functionListener){
//		this.functionListener = functionListener;
//		functionModifiedListeners.addElement(functionListener);
//	}
//
	public void dispose(){
		functionModifiedListeners.removeElement(functionListener);
	}

//	/**
//	 * Set updater if a new file is loaded
//	 * @param updater
//	 */
//	public void setUpdater(IWorkflowUpdater updater){
//		this.updater = updater;
//	}

	/**
	 * Updates the Function widget content
	 * @param function AFunction
	 */
	public void update(AFunction function){
		//AFunction myFunction = updater.getFunctionFromActor(functionID);
//		if (myFunction != null)
		setFunction(function);
		functionModified();
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		functionEditor.addSelectionChangedListener(listener);
		functionModified();
	}
	
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		functionEditor.removeSelectionChangedListener(listener);
	}
	
}
