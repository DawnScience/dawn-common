package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawnsci.conversion.converters.CustomTomoConverter;
import org.dawnsci.conversion.converters.CustomTomoConverter.TomoInfoBean;
import org.dawnsci.conversion.ui.Activator;
import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.LoggerFactory;

public final class TomoConvertPage extends ResourceChoosePage implements IConversionWizardPage {
	
	private static final String[] IMAGE_FORMATS = new String[]{"tiff", "png", "jpg"};
	private static final Map<String,int[]> BIT_DEPTHS;
	static {
		BIT_DEPTHS = new HashMap<String, int[]>(3);
		// TODO investigate other bit depths for different formats.
		BIT_DEPTHS.put("tiff", new int[]{33,16});
		BIT_DEPTHS.put("png", new int[]{16});
		BIT_DEPTHS.put("jpg", new int[]{8});
	}
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TomoConvertPage.class);
	
	private Label         nameChoice;
	private String         datasetName;
	private String         imageFormat;
	private int            bitDepth;
	private Text           projPrefixBox;
	private Text           darkPrefixBox;
	private Text           flatPrefixBox;
	private Label           exampleFilePath;
	private CLabel warningLabel;
	private IConversionContext context;

	public TomoConvertPage() {
		super("wizardPage", "Page for slicing Tomography HDF5 data into a directory of images.", null);
		setTitle("Convert Tomography to Images");
		setDirectory(true);
	}

	@Override
	public void createContentBeforeFileChoose(Composite container) {
		
	
		new Label(container, SWT.NULL);
		new Label(container, SWT.NULL);
		new Label(container, SWT.NULL);
		new Label(container, SWT.NULL);

		
		Label label = new Label(container, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Dataset Name:");
		
		nameChoice = new Label(container, SWT.NULL);
		nameChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		nameChoice.setText("");
		
	}
	
	@Override
	protected void createContentAfterFileChoose(Composite container) {
		
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output");
		
		Label sep = new Label(container, SWT.HORIZONTAL|SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		addTomographyFileBoxes(container);

		createAdvanced(container);
		
		sep = new Label(container, SWT.HORIZONTAL|SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		pathChanged();

	}
	
	private void createAdvanced(final Composite parent) {
		
		final ExpandableComposite advancedComposite = new ExpandableComposite(parent, SWT.NONE);
		advancedComposite.setExpanded(false);
		advancedComposite.setText("Advanced");
		advancedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		final Composite advanced = new Composite(parent, SWT.NONE);
		advanced.setLayout(new GridLayout(3, false));
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			
		Label label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Image Format");
		
		final CCombo imf = new CCombo(advanced, SWT.READ_ONLY|SWT.BORDER);
		imf.setItems(IMAGE_FORMATS);
		imf.select(0);
		imageFormat = "tiff";
		imf.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Bit Depth");
		
		final CCombo bd = new CCombo(advanced, SWT.READ_ONLY|SWT.BORDER);
		bd.setItems(getStringArray(BIT_DEPTHS.get(imageFormat)));
		bd.select(0);
		bitDepth = 33;
		bd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		bd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final String str = bd.getItem(bd.getSelectionIndex());
				bitDepth = Integer.parseInt(str);
				GridUtils.setVisible(warningLabel, bitDepth<33);
				warningLabel.getParent().layout();
				pathChanged();
			}
		});
		imf.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				imageFormat = imf.getItem(imf.getSelectionIndex());
				
				final int [] depths = BIT_DEPTHS.get(imageFormat);
				final String[] sa   = getStringArray(depths);
				bd.setItems(sa);
				bitDepth = depths[0];
				bd.select(0);
				GridUtils.setVisible(warningLabel, bitDepth<33);
				warningLabel.getParent().layout();
				pathChanged();
			}
		});
		
		this.warningLabel = new CLabel(advanced, SWT.NONE);
		warningLabel.setImage(Activator.getImage("icons/warning.gif"));
		warningLabel.setText("Lower dit depths will not support larger data values.");
		warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		GridUtils.setVisible(warningLabel, false);

		GridUtils.setVisible(advanced, false);
		ExpansionAdapter expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.setVisible(advanced, !advanced.isVisible());
				parent.layout(new Control[]{advanced, advancedComposite});
				parent.layout();
				parent.getParent().layout();
			}
		};
		advancedComposite.addExpansionListener(expansionListener);
		
	}

	private String[] getStringArray(int[] is) {
		final String[] sa = new String[is.length];
		for (int i = 0; i < is.length; i++) {
			sa[i] = String.valueOf(is[i]);
		}
		return sa;
	}
	
	@Override
	public boolean isOpen() {
		return false;
	}

	/**
	 * Checks the path is ok.
	 */
	protected void pathChanged() {

		final File outputDir = new File(getAbsoluteFilePath());
		try {
			if (outputDir.isFile()) {
				setErrorMessage("The directory "+outputDir+" is a file.");
				return;			
			}
			if (!outputDir.getParentFile().exists()) {
				setErrorMessage("The directory "+outputDir.getParent()+" does not exist.");
				return;			
			}
			updatePaths();
		} catch (Exception ne) {
			setErrorMessage(ne.getMessage()); // Not very friendly...
			return;			
		}
		setErrorMessage(null);
		return;
	}
	
    public boolean isPageComplete() {
    	if (context==null) return false;
        return super.isPageComplete();
    }
	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		context.setDatasetName(datasetName);
		context.setOutputPath(getAbsoluteFilePath());
		context.addSliceDimension(0, "all");
		final CustomTomoConverter.TomoInfoBean bean = new CustomTomoConverter.TomoInfoBean();
		try {
			bean.setTomographyDefinition(getSourcePath(context));
		} catch (Exception e) {
			logger.error("Cannot set tomo definition, please contact your support representative.", e);
		}
		bean.setExtension(imageFormat);
		bean.setBits(bitDepth);
		bean.setDarkFieldPath(darkPrefixBox.getText());
		bean.setFlatFieldPath(flatPrefixBox.getText());
		bean.setProjectionPath(projPrefixBox.getText());
		context.setUserObject(bean);
		
		return context;
	}
	
	private void updatePaths() {
		
		String ext = "";
		
		if (imageFormat == null) {
			ext = imageFormat;
		} else {
			ext = IMAGE_FORMATS[0];
		}
		
		exampleFilePath.setText(TomoInfoBean.convertToFullPath(getAbsoluteFilePath(), projPrefixBox.getText()) +"." + ext);
		exampleFilePath.getShell().layout(true,true);
	}


	@Override
	public void setContext(IConversionContext context) {
		this.context = context;
		setErrorMessage(null);
		if (context==null) {
			// Clear any data
	        setPageComplete(false);
			return;
		}
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
                    final List<String> names = getActiveDatasets(context, monitor);
                    if (names==null || names.isEmpty()) return;
                    
                    Display.getDefault().asyncExec(new Runnable() {
                    	public void run() {
                    		nameChoice.setText(names.get(0));
                    		datasetName = names.get(0);
                    	}
                    });
                    
				} catch (Exception ne) {
					throw new InvocationTargetException(ne);
				}
			}
		});
	}
	
	@Override
	protected List<String> getActiveDatasets(IConversionContext context, IProgressMonitor monitor) throws Exception {
		
		final String source = getSourcePath(context);
		if (source==null || "".equals(source)) return null;
		final ConversionScheme scheme = context.getConversionScheme();
		
		if (scheme == null || scheme != ConversionScheme.CUSTOM_TOMO) return null;
		
		TomoInfoBean bean = new TomoInfoBean();
		
		if (!bean.setTomographyDefinition(source)) return null;
		
		return Arrays.asList(bean.getTomoDataName());
	
	}
	
	private void addTomographyFileBoxes(Composite composite) {
		
		Label label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		label.setText("Set paths for tomography images:");
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		label.setText("Use %s to refer to the output folder path");
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		label.setText("Use %0d (where d is an integer) to set the leading zero width of the file name");
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Projection Images");
		
		this.projPrefixBox = new Text(composite, SWT.BORDER);
		projPrefixBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		projPrefixBox.setText("%s/projection/p_%05d");
		projPrefixBox.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updatePaths();
				
			}
		});
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Dark Field Images");
		
		this.darkPrefixBox = new Text(composite, SWT.BORDER);
		darkPrefixBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		darkPrefixBox.setText("%s/dark/d_%05d");
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Flat Field Images");
		
		this.flatPrefixBox = new Text(composite, SWT.BORDER);
		flatPrefixBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		flatPrefixBox.setText("%s/flat/f_%05d");
		
		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Current:");
		
		exampleFilePath = new Label(composite, SWT.WRAP);
		exampleFilePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 2));
		exampleFilePath.setText(TomoInfoBean.convertToFullPath(getAbsoluteFilePath(), projPrefixBox.getText()) +"." + IMAGE_FORMATS[0]);

	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}

}
