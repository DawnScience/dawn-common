package org.dawb.common.ui.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;

/**
 * Widget that creates a canvas and a Font Dialog button to display text in various Fonts and sizes
 * @author wqk87977
 *
 */
public class FontExtenderWidget extends Composite {

	private Font font;
	private String fontText;
	private Canvas canvas;
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param title
	 *          the name of the widget
	 */
	public FontExtenderWidget(final Composite parent, int style, String title) {
		super(parent, style);
		
		font = new Font(parent.getDisplay(), "Helvetica", 30, SWT.BOLD);
		fontText = "";
		final Clipboard cb = new Clipboard(parent.getDisplay());
//		setLayout(new RowLayout(SWT.VERTICAL));
		setLayout(new GridLayout(1, false));
		
		final Composite menuComposite = new Composite(this, SWT.TOP);
		RowLayout menuLayout = new RowLayout();
		menuLayout.wrap=true;
		menuLayout.center=true;
		menuComposite.setLayout(menuLayout);
		
		Label titleLabel = new Label(menuComposite, SWT.NONE);
		titleLabel.setText(title);
		
		Button button = new Button(menuComposite, SWT.PUSH);
		button.setText("Font...");
		button.setToolTipText("Change display size");
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				FontDialog dlg = new FontDialog(parent.getShell());

				FontData fontData = dlg.open();
				if (fontData != null) {
					if (font != null)
						font.dispose();
					font = new Font(parent.getShell().getDisplay(), fontData);
					
					canvas.redraw();
				}
			}
		});
		
		Button copyButton = new Button(menuComposite, SWT.PUSH);
		copyButton.setText("Copy");
		copyButton.setToolTipText("Copy to clipboard");
		copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { fontText },
						new Transfer[] { textTransfer });
			}
		});
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
//		scrolledComposite.setLayout(new GridLayout());
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		
		canvas = new Canvas(scrolledComposite, SWT.BORDER);
		canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		canvas.setToolTipText("Shows the "+title);
//		canvas.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 9;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				// Set the font into the gc
				event.gc.setFont(font);
				event.gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				//event.gc.setForeground(color);
				// Calcalute the width (nad height) of the string
				//Point pt = event.gc.stringExtent(fontText);

				// Figure out how big our drawing area is
//				Rectangle rect = canvas.getBounds();

				// Calculate the height of the font
//				int height = event.gc.getFontMetrics().getHeight();
//				int middleX = rect.height/3 - height;
//				int middleY = rect.width/3;
				event.gc.drawString(fontText, 0, 0);

			}
		});

		canvas.setSize(600, 100);
		
		scrolledComposite.setContent(canvas);
		scrolledComposite.setMinHeight(100);
		scrolledComposite.setMinWidth(600);
		
		

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * Method that updates the text to draw on the canvas
	 * @param text
	 */
	public void update(String text){
		fontText = text;
		canvas.redraw();
	}
}
