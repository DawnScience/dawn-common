/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawb.common.util.list.ListUtils;
import org.dawnsci.conversion.ui.api.IConversionWizardPage;
import org.dawnsci.conversion.ui.api.IConversionWizardPageService;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionScheme;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionService;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.IMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionChoicePage extends ResourceChoosePage implements IConversionWizardPage {
	
	private static final Logger logger = LoggerFactory.getLogger(ConversionChoicePage.class);

	private IConversionService service;
	private IConversionScheme  chosenConversion;
	private Composite          conversionGroup;
	private Label              multiFilesLabel;
	private boolean            multiFileSelection=false;
	private boolean            filterFiles = false;
	private String             extensionsFilter = "*.cbf";

	protected ConversionChoicePage(String pageName, IConversionService service) {
		super(pageName, "Please choose what you would like to convert", null);
		this.service = service;
		setOverwriteVisible(false);
	}
	
	private final static String SCHEME_KEY = "org.dawnsci.conversion.ui.schemeKey";
	@Override
	protected void createContentBeforeFileChoose(Composite container) {
		conversionWizardPageService = ServiceHolder.getConversionWizardPageService();
		Label label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
		
		// 4 col grid
		final Label convLabel = new Label(container, SWT.NONE);
		convLabel.setText("Conversion Type");
		convLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		final Combo choice = new Combo(container, SWT.READ_ONLY|SWT.BORDER);
		choice.setItems(conversionWizardPageService.getLabels(true));
		choice.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		choice.select(0);
		List<IConversionScheme> schemes = conversionWizardPageService.getSchemes(true);
		this.chosenConversion = schemes.get(0); 
		if (Activator.getDefault().getPreferenceStore().contains(SCHEME_KEY)) {
			String scheme = Activator.getDefault().getPreferenceStore().getString(SCHEME_KEY);
			Bundle[] bundles = Activator.getDefault().getBundle().getBundleContext().getBundles();
			for (Bundle bundle : bundles) {
				Class<?> klazz = null;
				try {
					klazz = bundle.loadClass(scheme);
				} catch (ClassNotFoundException e1) {
					continue;
				}
				this.chosenConversion = conversionWizardPageService.getSchemeForClass((Class<IConversionScheme>) klazz);
				choice.select(choice.indexOf(chosenConversion.getUiLabel()));
				break;
			}
		}
		choice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chosenConversion = conversionWizardPageService.fromLabel(choice.getItem(choice.getSelectionIndex()));
				Activator.getDefault().getPreferenceStore().setValue(SCHEME_KEY, chosenConversion.toString());
				pathChanged();
			}
		});
		
		label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));

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
		
		final Button filter = new Button(container, SWT.CHECK);
		filter.setText("Filter");
		filter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		final Text extensions = new Text(container, SWT.BORDER);
		extensions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		extensions.setToolTipText("Comma separated list of file extensions. E.g. '*.cbf' or '*.h5, *.nxs, *.hdf5'");
		extensions.setVisible(false);
		extensions.setText(extensionsFilter);
		extensions.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				extensionsFilter = extensions.getText();
				updateFilesLabel();
			}
		});
		
		filter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				filterFiles = filter.getSelection();
				GridUtils.setVisible(extensions, filter.getSelection());
				extensions.getParent().layout();
				updateFilesLabel();
			}
		});

		
		SelectionAdapter selAd = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				multiFileSelection = useFiles.getSelection();
				GridUtils.setVisible(multiFilesLabel, multiFileSelection);
				setFileChoosingEnabled(!multiFileSelection);
				GridUtils.setVisible(filter, multiFileSelection);
				GridUtils.setVisible(extensions, multiFileSelection && filter.getSelection());
			}
		};
		useFiles.addSelectionListener(selAd);
		singleFile.addSelectionListener(selAd);
	}
	
	/**
	 * Overridden so that filter can be applied.
	 * @return
	 */
	protected List<String> getSelectedFiles() {

		final List<String> selectedFiles = super.getSelectedFiles();
		if (selectedFiles==null) return null;
		final List<String> files = new ArrayList<String>(selectedFiles);
		try {
			if (filterFiles && extensionsFilter.length()>0) {
				final List<String> exts = ListUtils.getList(extensionsFilter);
				
				for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
					String file = (String) iterator.next();
					boolean oneOk = false;
					for (String ext : exts) {
						if (ext.length()>1 && ext.startsWith("*")) ext = ext.substring(1);
						if (file.toLowerCase().endsWith(ext)) {
							oneOk = true;
							break;
						}
					}
					if (!oneOk) iterator.remove();
				}
			}
			return files;
		} catch (Exception ne) {
			logger.error("Cannot filter files!", ne);
			return files;
		}
	}

	private Label infoDiagram, infoText;

	private Link helpLink;
	
	protected void createContentAfterFileChoose(Composite container) {
		
		final Composite infoArea = new Composite(container, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 20;
		infoArea.setLayout(layout);
		infoArea.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 4, 3));
		
		this.infoDiagram = new Label(infoArea, SWT.NONE);
		infoDiagram.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		infoDiagram.setText(" ");
		
		this.infoText    = new Label(infoArea, SWT.WRAP);
		infoText.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		
		this.helpLink = new Link(container, SWT.WRAP);
		helpLink.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 4, 3));
		helpLink.setText("This wizard has been started in single file mode (<a>more</a>)");
		
		final Label helpLabelMore = new Label(container, SWT.WRAP);
		helpLabelMore.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 4, 3));
		helpLabelMore.setText("To select multiple files, cancel and use the 'Project Explorer' to select a folder or hold down control and select several files with the mouse. Afterwards restart this wizard and the files selected will be the conversion input files. The files selected should all be of the same type.");
		GridUtils.setVisible(helpLabelMore, false);
		
		helpLink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				GridUtils.setVisible(helpLabelMore, !helpLabelMore.isVisible());
				if (helpLabelMore.isVisible()) {
					helpLink.setText("This wizard has been started in single file mode (<a>less</a>)");
				} else {
					helpLink.setText("This wizard has been started in single file mode (<a>more</a>)");
				}
				helpLabelMore.getParent().layout();
			}
		});

		
        updateFilesLabel();
		pathChanged();
        
	}
	
	private void updateFilesLabel() {

		final List<String> selectedFiles = getSelectedFiles();
		if (selectedFiles!=null && selectedFiles.size()>0) setPath(selectedFiles.get(0));

		if (!filterFiles && (selectedFiles==null || selectedFiles.size()<2)) {
			GridUtils.setVisible(conversionGroup, false);
			GridUtils.setVisible(multiFilesLabel, false);
			GridUtils.setVisible(helpLink, true);
		} else {
			//sort list
			Collections.sort(selectedFiles);
			multiFileSelection = true;
			GridUtils.setVisible(conversionGroup, true);
			GridUtils.setVisible(multiFilesLabel, true);
			final File start = new File(selectedFiles.get(0));
			final File end   = new File(selectedFiles.get(selectedFiles.size()-1));
			multiFilesLabel.setText("Selected files:   "+start.getName()+" - "+end.getName()+"  (List of "+selectedFiles.size()+" files)");
			setFileChoosingEnabled(false);
			GridUtils.setVisible(helpLink, false);
		}

	}
	
	private Image lastImage;
	/**
	 * If there is an image in this directory called <enum_name>.png
	 * we return it here.
	 * 
	 * @return
	 */
	public Image getImage(IConversionScheme scheme) {
		
		if (lastImage!=null) lastImage.dispose();
		
		this.lastImage = conversionWizardPageService.getImage(scheme).createImage();
		return lastImage;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (lastImage != null && !lastImage.isDisposed()) {
			lastImage.dispose();
		}
	}

	protected void pathChanged()  {
		final String filePath = getAbsoluteFilePath();
		if (filePath==null || "".equals(filePath)) {
			setErrorMessage("The file to convert has not been specified");
			setPageComplete(false);
			return;
		}
		
		if (chosenConversion!=null) {
			infoDiagram.setImage(getImage(chosenConversion));
			infoText.setText(chosenConversion.getDescription());
			
			if (chosenConversion.isNexusOnly()) {
				if (!checkH5(filePath, true)) return; // Must be h5 source
			} else if (!chosenConversion.isNexusSourceAllowed()) {
				if (!checkH5(filePath, false)) return;
			}
			
			infoText.getParent().getParent().layout();

		}
		
		final File file = new File(filePath);
		if (!file.exists()) {
			setErrorMessage("The file '"+filePath+"' does not exist and cannot be converted.");
			setPageComplete(false);
			return;
		}
		
		// Test that there are some datasets of the required rank
		try {
			
			final int ranks[] = chosenConversion.getPreferredRanks();
			if (ranks!=null) {
				IDataHolder holder = null;
				final List<String> selectedFiles = getSelectedFiles();
				if (selectedFiles!=null && selectedFiles.size()>1) {		
					for (String path : selectedFiles) {
						try {
							holder = ServiceHolder.getLoaderService().getData(path, new IMonitor.Stub());
						    if (holder==null) continue;
						    if (holder.size()<1) continue;
						    break;
						} catch (Throwable ne) {
							continue;
						}
					}
				} else {
					holder = ServiceHolder.getLoaderService().getData(filePath, new IMonitor.Stub());
				}
				boolean foundRequiredRank = false;
				for (int i = 0; i < holder.size(); i++) {
					final int[] shape = holder.getLazyDataset(i).getShape();
					for (int rank : ranks) {
						if (shape!=null && shape.length==rank) {
							foundRequiredRank = true;
							break;
						}						
					}
				}
				
				if (!foundRequiredRank) {
					setErrorMessage("Conversion '"+chosenConversion.getUiLabel()+"', requires datasets of rank(s) "+Arrays.toString(ranks)+" to be in the file.");
					setPageComplete(false);
					return;
				}
			}
		} catch (Throwable ne) {
			setErrorMessage("Error, cannot read file '"+filePath+"'. Reason: "+ne.getMessage());
			setPageComplete(false);
			logger.error("Cannot  '"+filePath+"'", ne);
		}
		
		setErrorMessage(null);
		setPageComplete(true);
		
		return;

	}

	
	private boolean checkH5(String filePath, boolean isH5) {
		
		String seg  = isH5 ? "" : "non-";
		final List<String> selectedFiles = getSelectedFiles();
		if (selectedFiles!=null && selectedFiles.size()>1 && multiFileSelection) {		
			for (String path : selectedFiles) {
				if (isH5 == !isH5(path)) {
					setErrorMessage("The conversion '"+chosenConversion.getUiLabel()+"' supports "+seg+"nexus/hdf5 files only. The file '"+((new File(path).getName())+"' is not."));
					setPageComplete(false);
					return false;
				}				
			}
		} else {
			if (isH5 == !isH5(filePath)) {
				setErrorMessage("The conversion '"+chosenConversion.getUiLabel()+"' supports "+seg+"nexus/hdf5 files only.");
				setPageComplete(false);
				return false;
			}
		}
		return true;
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

	public IConversionScheme getScheme() {
		return chosenConversion;
	}


	@Override
	public void setContext(IConversionContext context) {
		// Does nothing, we generate the context in this class.
	}

	@Override
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

	public final static List<String> EXT;

	private IConversionWizardPageService conversionWizardPageService;
	static {
		List<String> tmp = new ArrayList<String>(7);
		tmp.add("h5");
		tmp.add("nxs");
		tmp.add("hd5");
		tmp.add("hdf5");
		tmp.add("hdf");
		tmp.add("nexus");
		EXT = Collections.unmodifiableList(tmp);
	}	

	private final static boolean isH5(final String filePath) {
		final String ext = getFileExtension(filePath);
		if (ext==null) return false;
		return EXT.contains(ext.toLowerCase());
	}
	private final static String getFileExtension(String fileName) {
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? "" : fileName.substring(posExt + 1);
	}

}
