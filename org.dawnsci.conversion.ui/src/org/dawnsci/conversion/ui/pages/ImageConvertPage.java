package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.slicing.DimsData;
import org.dawb.common.ui.slicing.DimsDataList;
import org.dawb.common.ui.slicing.SliceComponent;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawnsci.conversion.internal.ImageConverter;
import org.dawnsci.conversion.ui.Activator;
import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
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

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public final class ImageConvertPage extends ResourceChoosePage implements IConversionWizardPage {
	
	private static final String LAST_SET_KEY = "org.dawnsci.conversion.ui.pages.lastDataSet";
	
	private static final String[] IMAGE_FORMATS = new String[]{"tiff", "png", "jpg"};
	private static final Map<String,int[]> BIT_DEPTHS;
	static {
		BIT_DEPTHS = new HashMap<String, int[]>(3);
		// TODO investigate other bit depths for different formats.
		BIT_DEPTHS.put("tiff", new int[]{33,16});
		BIT_DEPTHS.put("png", new int[]{16});
		BIT_DEPTHS.put("jpg", new int[]{8});
	}
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImageConvertPage.class);

	
	
	private CCombo         nameChoice;
	private String         datasetName;
	private String         imageFormat;
	private int            bitDepth;
	private Text           imagePrefixBox;
	private String         path;
	private SliceComponent sliceComponent;
	private CLabel warningLabel;
	private IConversionContext context;

	public ImageConvertPage() {
		super("wizardPage", "Page for slicing HDF5 data into a directory of images.", null);
		setTitle("Convert to Images");
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
		label.setText("Dataset Name");
		
		nameChoice = new CCombo(container, SWT.READ_ONLY);
		nameChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		nameChoice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				datasetName = nameChoice.getItem(nameChoice.getSelectionIndex());
				pathChanged();
				nameChanged();
				Activator.getDefault().getPreferenceStore().setValue(LAST_SET_KEY, datasetName);
			}
		});
		
	}
	
	@Override
	protected void createContentAfterFileChoose(Composite container) {
		
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output");

		createAdvanced(container);
		
		Label sep = new Label(container, SWT.HORIZONTAL|SWT.SEPARATOR);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		this.sliceComponent = new SliceComponent("org.dawb.workbench.views.h5GalleryView");
		final Control slicer = sliceComponent.createPartControl(container);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		data.minimumHeight=560;
		slicer.setLayoutData(data);
		sliceComponent.setVisible(true);
		sliceComponent.setAxesVisible(false);
		sliceComponent.setRangesAllowed(true);
		sliceComponent.setToolBarEnabled(false);
		
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
		
		final CCombo imf = new CCombo(advanced, SWT.READ_ONLY);
		imf.setItems(IMAGE_FORMATS);
		imf.select(0);
		imageFormat = "tiff";
		imf.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Bit Depth");
		
		final CCombo bd = new CCombo(advanced, SWT.READ_ONLY);
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

		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Image Prefix");

		this.imagePrefixBox = new Text(advanced, SWT.BORDER);
		imagePrefixBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		imagePrefixBox.setText("image");
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());

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
	
	private void nameChanged() {

		try {
			DataHolder dh = LoaderFactory.getData(context.getFilePath(), new IMonitor.Stub());
			final ILazyDataset lz = dh.getLazyDataset(datasetName);
			sliceComponent.setData(lz, datasetName, context.getFilePath());
			
			try {
				final String name = datasetName.substring(datasetName.lastIndexOf('/')+1);
				imagePrefixBox.setText(name);
			} catch (Exception ignored) {
				imagePrefixBox.setText(datasetName);
			}

			
		} catch (Exception ne) {
			setErrorMessage("Cannot read data set '"+datasetName+"'");
			logger.error("Cannot get data", ne);
		}
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
		
		final ImageConverter.ConversionInfoBean bean = new ImageConverter.ConversionInfoBean();
		bean.setExtension(imageFormat);
		bean.setBits(bitDepth);
		bean.setAlternativeNamePrefix(imagePrefixBox.getText());
		context.setUserObject(bean);
		
		final DimsDataList dims = sliceComponent.getDimsDataList();
		for (DimsData dd : dims.getDimsData()) {
			if (dd.isSlice()) {
				context.addSliceDimension(dd.getDimension(), String.valueOf(dd.getSlice()));
			} else if (dd.isRange()) {
				context.addSliceDimension(dd.getDimension(), dd.getSliceRange()!=null ? dd.getSliceRange() : "all");				
			}
		}
		
		return context;
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
                    		nameChoice.setItems(names.toArray(new String[names.size()]));
                    		final String lastName = Activator.getDefault().getPreferenceStore().getString(LAST_SET_KEY);
                    		
                    		int index = 0;
                    		if (lastName!=null && names.contains(lastName)) {
                    			index = names.indexOf(lastName);
                    		}
                    		
                    		nameChoice.select(index);
                    		datasetName = names.get(index);
                    		nameChanged();
                    	}
                    });
                    
				} catch (Exception ne) {
					throw new InvocationTargetException(ne);
				}

			}

		});
	}
	
	@Override
	public IWizardPage getNextPage() {
		return null;
	}

}
