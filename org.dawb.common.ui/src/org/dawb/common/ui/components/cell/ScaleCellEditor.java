
package org.dawb.common.ui.components.cell;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Scale;

/**
 * A cell editor that presents a list of items in a spinner box.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ScaleCellEditor extends CellEditor {
	/**
	 * The custom combo box control.
	 */
	protected Scale scale;

	private FocusAdapter focusListener;

	private KeyListener keyListener;

	/**
	 * Default ComboBoxCellEditor style
	 */
	private static final int defaultStyle = SWT.NONE;

	/**
	 * Creates a new cell editor with no control and no st of choices.
	 * Initially, the cell editor has no cell validator.
	 *
	 * @since 2.1
	 * @see CellEditor#setStyle
	 * @see CellEditor#create
	 * @see ComboBoxCellEditor#setItems
	 * @see CellEditor#dispose
	 */
	public ScaleCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Spinner Editor
	 *
	 * @param parent
	 *            the parent control
	 */
	public ScaleCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Spinner Editor
	 *
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style bits
	 * @since 2.1
	 */
	public ScaleCellEditor(Composite parent,int style) {
		super(parent, style);
	}

	@Override
	protected Control createControl(Composite parent) {

		scale = new Scale(parent, getStyle());
		scale.setFont(parent.getFont());
		this.focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				ScaleCellEditor.this.focusLost();
			}
		};
		scale.addFocusListener(focusListener);
		this.keyListener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\n') {
					ScaleCellEditor.this.focusLost();
				}
				if (e.character == '\r') {
					ScaleCellEditor.this.focusLost();
				}
			}
		};
		scale.addKeyListener(keyListener);

		return scale;
	}
	
	@Override
	public void dispose() {
		if (focusListener!=null) scale.removeFocusListener(focusListener);
		if (keyListener!=null)   scale.removeKeyListener(keyListener);
		super.dispose();
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method returns the zero-based index
	 * of the current selection.
	 *
	 * @return the zero-based index of the current selection wrapped as an
	 *         <code>Integer</code>
	 */
	@Override
	protected Object doGetValue() {
		return scale.getSelection();
	}

	@Override
	protected void doSetFocus() {
		scale.setFocus();
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method sets the minimum width of the
	 * cell. The minimum width is 10 characters if <code>comboBox</code> is
	 * not <code>null</code> or <code>disposed</code> else it is 60 pixels
	 * to make sure the arrow button and some text is visible. The list of
	 * CCombo will be wide enough to show its longest item.
	 * @return  layoutData
	 */
	@Override
	public LayoutData getLayoutData() {
		LayoutData layoutData = super.getLayoutData();
		if ((scale == null) || scale.isDisposed()) {
			layoutData.minimumWidth = 60;
		} else {
			// make the comboBox 10 characters wide
			GC gc = new GC(scale);
			layoutData.minimumWidth = (gc.getFontMetrics()
					.getAverageCharWidth() * 10) + 10;
			gc.dispose();
		}
		return layoutData;
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method accepts a zero-based index of
	 * a selection.
	 *
	 * @param value
	 *            the zero-based index of the selection wrapped as an
	 *            <code>Integer</code>
	 */
	@Override
	protected void doSetValue(Object value) {
		Assert.isTrue(scale != null && (value instanceof Integer));
		scale.setSelection(((Integer)value).intValue());
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor
	 */
	void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		Object newValue = doGetValue();
		markDirty();
		boolean isValid = isCorrect(newValue);
		setValueValid(isValid);
		fireApplyEditorValue();
		deactivate();
	}

	@Override
	protected void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivate();
		}
	}

	/**
	 * @param i
	 */
	public void setMaximum(int i) {
		if (scale!=null) scale.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(int i) {
		if (scale!=null) scale.setMinimum(i);
	}
	
	/**
	 * Listen to the scale value without exiting the editor.
	 * @param l
	 */
	public void addSelectionListener(SelectionListener l) {
		// TODO Remove this again on dispose()?
		this.scale.addSelectionListener(l);
	}
}
