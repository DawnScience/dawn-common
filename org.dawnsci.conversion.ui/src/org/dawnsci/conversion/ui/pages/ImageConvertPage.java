package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawnsci.conversion.ui.AbstractConversionPage;
import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImageConvertPage extends AbstractConversionPage implements IConversionWizardPage {

	private CCombo  nameChoice;
	private String  datasetName;
	private Label   txtLabel;
	private Text    txtPath;
	private String  path;

	public ImageConvertPage() {
		super("wizardPage");
		setTitle("Convert Data");
		setDescription("Convert data from synchrotron formats and compressed files to common simple data formats.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(3, false));

		Label label = new Label(container, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Image Format");
		
		CCombo imageFormat = new CCombo(container, SWT.READ_ONLY);
		imageFormat.setItems(new String[]{"tiff", "png", "jpg"});
		imageFormat.select(0);
		imageFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(container, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Dataset Name");
		
		nameChoice = new CCombo(container, SWT.READ_ONLY);
		nameChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		nameChoice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				datasetName = nameChoice.getItem(nameChoice.getSelectionIndex());
			}
		});
		
		txtLabel = new Label(container, SWT.NULL);
		txtLabel.setText("Export &Folder  ");
		txtPath = new Text(container, SWT.BORDER);
		txtPath.setEditable(false);
		txtPath.setEnabled(false);
		txtPath.setText(getPath());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtPath.setLayoutData(gd);
		txtPath.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				pathChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		// TODO GUI for slices.
		
	}
	
	/**
	 * Checks the path is ok.
	 */
	private void pathChanged() {

		
		setErrorMessage(null);
		return;
	}
	
    public boolean isPageComplete() {
    	if (context==null) return false;
        return super.isPageComplete();
    }

	protected String getPath() {
		if (path==null) { // We make one up from the source
			String sourcePath = getSourcePath();
			final File source = new File(sourcePath);
			final String strName = "convert";
			this.path = (new File(source.getParentFile(), strName)).getAbsolutePath();
		}
		return path;
	}
	
	private void handleBrowse() {
		
		final DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		dialog.setText("Choose output folder");
		final String filePath = getPath();
		if (filePath!=null) {
			final File file = new File(filePath);
			if (file.isDirectory()) {
				dialog.setFilterPath(file.getAbsolutePath());
			} else {
				dialog.setFilterPath(file.getParent());
			}
		}
		final String path = dialog.open();
		if (path!=null) {
			this.path    = path;
		    txtPath.setText(this.path);
		}

		pathChanged();
	}



	@Override
	public void setContext(IConversionContext context) {
		this.context = context;
		
		// We populate the names later using a wizard task.
        try {
        	getNamesOfSupportedRank();
		} catch (Exception e) {
			logger.error("Cannot extract data sets!", e);
		}
        
        setPageComplete(true);
	}

	protected void getNamesOfSupportedRank() throws Exception {
		
		getContainer().run(true, true, new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				try {
                    final List<String> names = getActiveDatasets(monitor);
                    if (names==null || names.isEmpty()) return;
                    
                    Display.getDefault().syncExec(new Runnable() {
                    	public void run() {
                    		nameChoice.setItems(names.toArray(new String[names.size()]));
                    		nameChoice.select(0);
                    		datasetName = names.get(0);
                    	}
                    });
                    
				} catch (Exception ne) {
					throw new InvocationTargetException(ne);
				}

			}

		});
	}

}
