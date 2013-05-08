package org.dawnsci.conversion.ui;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class ConversionChoicePage extends ResourceChoosePage implements IConversionWizardPage {
	
	private static final Logger logger = LoggerFactory.getLogger(ConversionChoicePage.class);

	private IConversionService service;
	private ConversionScheme chosenConversion;
	private Composite        conversionGroup;
	private Label            multiFilesLabel;
	private boolean          multiFileSelection=false;

	protected ConversionChoicePage(String pageName, IConversionService service) {
		super(pageName, "Please choose what you would like to convert", null);
		this.service = service;
	}
	
	private final static String SCHEME_KEY = "org.dawnsci.conversion.ui.schemeKey";
	@Override
	protected void createContentBeforeFileChoose(Composite container) {

		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		// 4 col grid
		final Label convLabel = new Label(container, SWT.NONE);
		convLabel.setText("Conversion Type");
		convLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		final CCombo choice = new CCombo(container, SWT.READ_ONLY);
		choice.setItems(ConversionScheme.getLabels());
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		choice.select(0);
		this.chosenConversion = ConversionScheme.values()[0]; // Is user visible
		if (Activator.getDefault().getPreferenceStore().contains(SCHEME_KEY)) {
			try {
				this.chosenConversion = ConversionScheme.valueOf(Activator.getDefault().getPreferenceStore().getString(SCHEME_KEY));
				choice.select(choice.indexOf(chosenConversion.getUiLabel()));
			} catch (Throwable ne) {
				logger.warn("Problem with old conversion scheme key!", ne);
			}
		}
		choice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chosenConversion = ConversionScheme.fromLabel(choice.getItem(choice.getSelectionIndex()));
				Activator.getDefault().getPreferenceStore().setValue(SCHEME_KEY, chosenConversion.toString());
				pathChanged();
				getWizard().canFinish();
			}
		});
		
		this.conversionGroup = new Composite(container, SWT.NONE);
		conversionGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		conversionGroup.setLayout(new GridLayout(2, false));
		
		final Button useFiles = new Button(conversionGroup, SWT.RADIO);
		useFiles.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		useFiles.setText("All files selected");
		useFiles.setSelection(true);
		
		final Button singleFile = new Button(conversionGroup, SWT.RADIO);
		singleFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		singleFile.setText("Single file");
		singleFile.setSelection(false);

		multiFilesLabel = new Label(container, SWT.WRAP);
		multiFilesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		SelectionAdapter selAd = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				multiFileSelection = useFiles.getSelection();
				GridUtils.setVisible(multiFilesLabel, multiFileSelection);
				setFileChoosingEnabled(!multiFileSelection);
			}
		};
		useFiles.addSelectionListener(selAd);
		singleFile.addSelectionListener(selAd);
	}
	
	protected void createContentAfterFileChoose(Composite container) {
		
		if (false) { // TODO FIXME Not using regular expressions for now.
			Label spacer = new Label(container, SWT.NONE);
			spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			
			new Label(container, SWT.NONE);
			final Button check = new Button(container, SWT.CHECK);
			check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
			check.setText("File name regular expression");
			check.setSelection(false);
			
			new Label(container, SWT.NONE);
			final Label information = new Label(container, SWT.WRAP);
			information.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
			information.setText("Help with regular expressions:\n\nX* = none or more\nX+ =  one or more\nX? = none or one\nWhere X could be a number or character or '.' to match any character.\n\nFor example: /dls/results/945(.*)nxs or C:\\results\\945([0-9]+)nxs\n\n(Note back slash operator is not supported, so \\d is not allowed for instance.)");
			information.setVisible(false);
			
			check.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setPathEditable(check.getSelection());
					setButtonsEnabled(!check.getSelection());
					GridUtils.setVisible(information, check.getSelection());
					information.getParent().layout(new Control[]{information});
				}
			});
		}
		
		Label helpLabel = new Label(container, SWT.WRAP);
		helpLabel.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 4, 3));
		helpLabel.setText("(This wizard has been started in single file mode. To select multiple files, cancel and use the 'Project Explorer' to select a folder or hold down control and select several files with the mouse. Afterwards restart this wizard and the files selected will be the conversion input files. The files selected should all be of the same type please.)");
		
		final List<IFile> selected = getSelectedFiles();
		if (selected!=null) setPath(selected.get(0).getFullPath().toString());
		
        if (selected==null || selected.size()<2) {
        	GridUtils.setVisible(conversionGroup, false);
        	GridUtils.setVisible(multiFilesLabel, false);
        	GridUtils.setVisible(helpLabel, true);
        } else {
        	multiFileSelection = true;
          	GridUtils.setVisible(conversionGroup, true);
        	GridUtils.setVisible(multiFilesLabel, true);
        	multiFilesLabel.setText("Selected files:   "+selected.get(0).getName()+" - "+selected.get(selected.size()-1).getName()+"  (List of "+selected.size()+" files)");
        	setFileChoosingEnabled(false);
        	GridUtils.setVisible(helpLabel, false);
      }
       
        
        
	}
	
	protected void pathChanged()  {
		final String filePath = getAbsoluteFilePath();
		if (filePath==null || "".equals(filePath)) {
			setErrorMessage("The file to convert has not been specified");
			return;
		}
		
		final File file = new File(filePath);
		if (!file.exists()) {
			setErrorMessage("The file '"+filePath+"' does not exist and cannot be converted.");
			return;
		}
		
		// Test that there are some datasets of the required rank
		try {
			
			final int ranks[] = chosenConversion.getPreferredRanks();
			if (ranks!=null) {
				final IMetaData meta = LoaderFactory.getMetaData(filePath, new IMonitor.Stub());
				final Map<String,int[]> shapes = meta.getDataShapes();
				boolean foundRequiredRank = false;
				for (String name : shapes.keySet()) {
					final int[] shape = shapes.get(name);
					for (int rank : ranks) {
						if (shape!=null && shape.length==rank) {
							foundRequiredRank = true;
							break;
						}						
					}
				}
				
				if (!foundRequiredRank) {
					setErrorMessage("Conversion '"+chosenConversion.getUiLabel()+"', requires datasets of rank(s) "+Arrays.toString(ranks)+" to be in the file.");
					return;
				}
			}
		} catch (Throwable ne) {
			setErrorMessage("Error, cannot read file '"+filePath+"'. Reason: "+ne.getMessage());
			logger.error("Cannot  '"+filePath+"'", ne);
		}
		
		setErrorMessage(null);
		
		return;

	}

	
	@Override
	public IConversionContext getContext() {
		IConversionContext context;
		if (multiFileSelection) {
			final String[] paths = getSelectedPaths();
			context = service.open(paths);
			context.setOutputPath((new File(paths[0])).getParent());
		} else {
			final String filePath = getAbsoluteFilePath();
		    context = service.open(filePath);
			context.setOutputPath((new File(filePath)).getParent());
		}
		context.setConversionScheme(chosenConversion);
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		// Does nothing, we generate the context in this class.
	}
	
    public boolean isPageComplete() {
    	boolean parentOk = super.isPageComplete();
    	
    	if (isCurrentPage()) {
        	final IWizardPage next = getNextPage();
        	if (next instanceof IConversionWizardPage) {
    	    	try {
    		    	final IConversionContext context = getContext();
    		    	final File ourConv   = new File(context.getFilePaths().get(0));
    		    	final File theirConv = new File(((IConversionWizardPage)next).getContext().getFilePaths().get(0));
    	    	    if (!ourConv.equals(theirConv)) {
    	    	    	((IConversionWizardPage)next).setContext(null);
    	    	    	return false;
    	    	    }
    	    	} catch (Exception ne) {
    	    		// Nowt
    	    	}
        	} 
    	}
    	if (!parentOk) return false;
    	
   	
    	return parentOk;
    }

	@Override
	public boolean isOpen() {
		return false;
	}


}
