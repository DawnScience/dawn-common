/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.dawb.common.services.IThumbnailService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.Activator;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.slicing.ISlicablePlottingPart;
import org.dawb.common.ui.slicing.ISliceReceiver;
import org.dawb.common.ui.slicing.SliceComponent;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.object.ObjectUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.nebula.jface.galleryviewer.GalleryTreeViewer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

/**
 * A part similar to ImageMonitorView but it does not monitor.
 * Instead it navigates a h5 file.
 * 
 * @author gerring
 *
 */
public class H5GalleryView extends ViewPart implements MouseListener, SelectionListener, ISliceReceiver {

	public static final String ID = "org.dawb.workbench.views.h5GalleryView"; //$NON-NLS-1$
    
	private static Logger  logger = LoggerFactory.getLogger(H5GalleryView.class);
	
	private Gallery                  gallery;
	private GalleryItem              galleryGroup;
	private BlockingDeque<ImageItem> queue;
	private Thread                   imageThread;
	private H5GalleryInfo            info;
	private MenuAction               dimensionList;
	
	public H5GalleryView() {
		this.queue = new LinkedBlockingDeque<ImageItem>(Integer.MAX_VALUE);
		createImageThread();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());

		this.gallery = new Gallery(parent, SWT.V_SCROLL | SWT.VIRTUAL | SWT.MULTI);
		gallery.setToolTipText("This part is used to navigate an image set inside an hdf5/nexus file.");
		
		// Renderers
		final DefaultGalleryGroupRenderer gr = new DefaultGalleryGroupRenderer();
		gr.setMinMargin(2);
		
		// Size image - parameterize this so that the user can change it.
		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
		final int    size      = store.getInt("org.dawb.workbench.views.image.monitor.thumbnail.size");
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (!event.getProperty().equals("org.dawb.workbench.views.image.monitor.thumbnail.size")) return;
				if (gr.getGallery().isDisposed()) return;
				int side = ObjectUtils.getInteger(event.getNewValue());
				gr.setItemHeight(side);
				gr.setItemWidth(side);
				refreshAll();
			}
		});
		gr.setItemHeight(size);
		gr.setItemWidth(size);
		gr.setAutoMargin(true);
		gallery.setGroupRenderer(gr);

		DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();
		gallery.setItemRenderer(ir);
		
		
		// Virtual
		gallery.setVirtualGroups(true);
		gallery.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				
				GalleryItem item = (GalleryItem) event.item;
				int index = gallery.indexOf(item);
				item.setItemCount(index);
				
				item.setText(index+"");
				
				final ImageItem ii = new ImageItem();
				ii.setIndex(index);
				ii.setItem(item);
							 	
			 	// Add to render queue
			 	queue.offerFirst(ii);	
			}

		});

		this.galleryGroup = new GalleryItem(gallery, SWT.VIRTUAL);
		galleryGroup.setText("Please choose a directory to monitor...");
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		getSite().setSelectionProvider(new GalleryTreeViewer(gallery));
		
		gallery.addMouseListener(this);
		gallery.addSelectionListener(this);
	}
	
	public void createImageGallery(H5GalleryInfo info) {
		this.info = info;
		refreshAll();
	}
	
	public void refreshAll() {
		refreshAll(false);
	}
	
	private void refreshAll(final boolean updateSelection) {
		
		queue.clear();
		
		// We use a job for this as the file list can be large
		final Job refresh = new Job("Refresh Image Monitor") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				
		
			    Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						galleryGroup.clearAll();
						galleryGroup.setItemCount(info.getSize());
						galleryGroup.setExpanded(true);
						galleryGroup.setText(info.getSlice().getName());
						
						gallery.update();
						gallery.getParent().layout(new Control[]{gallery});

						GalleryItem item = galleryGroup.getItem(galleryGroup.getItemCount()-1);
						gallery.setSelection(new GalleryItem[]{item});
					    
					}
			    });

				return Status.OK_STATUS;
			}
		};
		refresh.setPriority(Job.BUILD);
		refresh.schedule();
		
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site);

		try {
			if (memento==null || memento.getString("DIR")==null) return;
			//this.directoryPath = memento.getString("DIR");
		} catch (Exception ne) {
			throw new PartInitException(ne.getMessage());
		}
	}

	@Override
	public void saveState(IMemento memento) {
		try {
			//memento.putString("DIR", directoryPath);
		} catch (Exception e) {
			logger.error("Cannot save plot bean", e);
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		final MenuManager menuManager = new MenuManager();
		gallery.setMenu(menuManager.createContextMenu(gallery));
		getSite().registerContextMenu(menuManager, null);
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		
		dimensionList = new MenuAction("Slice dimension");
		dimensionList.setImageDescriptor(Activator.getImageDescriptor("icons/slice_dimension.gif"));
		toolbarManager.add(dimensionList);
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		if (gallery!=null&&!gallery.isDisposed()) {
			gallery.setFocus();
		}
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		//System.out.println("Open slice!");
	}
	
	@Override
	public void mouseDown(MouseEvent e) {
        //updateSelection();
	}
	@Override
	public void widgetSelected(SelectionEvent e) {
		updateSelection();
	}
	
	private void updateSelection() {
		
		final GalleryItem[] items = gallery.getSelection();
		if (items==null || items.length<1) return;
		
		final IEditorPart part = EclipseUtils.getActiveEditor();
		if (part instanceof ISlicablePlottingPart) {
			
			ISlicablePlottingPart prov = (ISlicablePlottingPart)part;
			
			
			final SliceComponent sliceComponent = prov.getSliceComponent();
			if (sliceComponent!=null) {
				sliceComponent.setSliceIndex(info.getSliceDimension(), items[0].getItemCount(), items.length<=1);
			}
			if (items.length<=1) return;
			
			List<AbstractDataset> ys = getSlices(items);
			final IAdaptable      adaptable = (IAdaptable)prov.getDataSetComponent();
			final IPlottingSystem system    = (IPlottingSystem)adaptable.getAdapter(IPlottingSystem.class);
			system.clear();

			if (ys.get(0).getShape().length==1) {
				system.createPlot1D(null, ys, null);
			} else if (ys.get(0).getShape().length==2) {
				// Average the images, then plot
			    AbstractDataset added = Maths.add(Arrays.asList(ys.toArray(new IDataset[ys.size()])), false);
			    AbstractDataset mean  = Maths.divide(added, ys.size());
			    system.createPlot2D(mean, null, null);
			}
		}
	}
	
	private List<AbstractDataset> getSlices(GalleryItem[] items) {
		
		final List<AbstractDataset> ys = new ArrayList<AbstractDataset>(11);
		for (GalleryItem item : items) {
			final ImageItem ii = new ImageItem();
			ii.setIndex(item.getItemCount());
			ii.setItem(item);
            try {
            	AbstractDataset slice = getSlice(ii);
            	slice.setName("Slice "+item.getItemCount());
				ys.add(slice);
			} catch (Exception e) {
				logger.error("Cannot slice ", e);
				continue;
			}
		}
        return ys;
	}

	public void dispose() {
		
		queue.clear();
		queue.add(new ImageItem()); // stops queue.
		
		if (gallery!=null&&!gallery.isDisposed()) {
			// Dispose images, may be a lot!
			for (int i = 0; i<gallery.getItemCount() ; ++i) {
				if (gallery.getItem(i).getImage()!=null) {
					gallery.getItem(i).getImage().dispose();
				}
			}
			gallery.removeSelectionListener(this);
			gallery.removeMouseListener(this);
			gallery.dispose();
		}
		
		// Nullify variables
		gallery=null;
		galleryGroup=null;
		queue=null;
		imageThread=null;
		info=null;
		
		super.dispose();

	}
	
	private void createImageThread() {

		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
			
		this.imageThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
	                while(true) { // This thead is going all the time.
	                	if (queue==null) return;
	                	final ImageItem ii = queue.take();
	                   	if (ii.getItem()==null) return;
	                   	if (ii.getIndex()<0)    return;
	                   	if (ii.getItem().isDisposed()) continue;
	                   	
	                   	final AbstractDataset set = getSlice(ii);
	                   	if (set==null) continue;
	            		
	            		// Generate thumbnail
	            		int             size  = store.getInt("org.dawb.workbench.views.image.monitor.thumbnail.size");
	            		if (size<1) size = 96;
	            		
	            		final IThumbnailService service = (IThumbnailService)ServiceManager.getService(IThumbnailService.class);	            		
	            		final Image image = service.getThumbnailImage(set, size);

	            		Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (ii.getItem().isDisposed()) return;
								if (ii.getItem().getParent().isDisposed()) return;
								if (image.isDisposed()) return;
			                   	if (image!=null) ii.getItem().setImage(image);
							}
	                   	});
	                }
				} catch (Throwable ne) {
					logger.error("Cannot process images", ne);
				}
			}

		}, "Image View Processing Daemon");
		
		imageThread.setDaemon(true);
		imageThread.start();
	}
	
	private AbstractDataset getSlice(final ImageItem ii) throws Exception {
		// Do slice
		final SliceObject slice = info.getSlice();
		slice.setSliceStart(getSliceStart(ii.getIndex()));
		slice.setSliceStop(getSliceStop(ii.getIndex()));
		slice.setSliceStep(getSliceStep(ii.getIndex()));

		AbstractDataset set=null;
		try {
			set   = LoaderFactory.getSlice(slice, null);
			if (set==null) return null;
		} catch (java.lang.IllegalArgumentException ne) {
			// We do not want the thread to stop in this case.
			logger.debug("Encountered invalid shape with "+slice);
			return null;
		}
		set.setShape(slice.getSlicedShape());

		return set;
	}

	
	protected int[] getSliceStart(int index) {
		
        final int [] start = new int[info.getShape().length];
        for (int i = 0; i < start.length; i++) {
			if (i==info.getSliceDimension()) {
				start[i] = index;
			} else{
				start[i] = info.getStart(i); // 0 for 2D, the current slice index for this dim for 1D
			}
		}
        return start;
	}

	protected int[] getSliceStop(int index) {
		
        final int [] stop = new int[info.getShape().length];
        for (int i = 0; i < stop.length; i++) {
			if (i==info.getSliceDimension()) {
				stop[i] = index+1;
			} else{
				stop[i] = info.getStop(i);  // size dim for 2D, the current slice index for this dim  +1 for 1D
			}
		}
        return stop;
	}
	protected int[] getSliceStep(int index) {
		
        final int [] step = new int[info.getShape().length];
        for (int i = 0; i < step.length; i++) step[i]=1;
        return step;
	}

	@Override
	public void mouseUp(MouseEvent e) {
		//System.out.println(e);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Auto-generated method stub
		
	}

	@Override
	public void updateSlice(int[] shape, SliceObject slice) {

		final H5GalleryInfo info = new H5GalleryInfo();
		info.setShape(shape);		
		info.setSlice(slice);
		info.createDefaultSliceDimension();
		createImageGallery(info);

		dimensionList.clear();
		
		final CheckableActionGroup grp = new CheckableActionGroup();
		final List<Integer> dims = info.getSliceableDimensions();
		for (final int dim : dims) {
			final IAction dimAction = new Action(""+(dim+1), IAction.AS_CHECK_BOX) {
				public void run() {
					info.setSliceDimension(dim);
					refreshAll();
				}
			};
			if (info.getSliceDimension()==dim) dimAction.setChecked(true);
			dimAction.setToolTipText("Slice using the dimension "+dim+" of the data.");
			dimensionList.add(dimAction);
			grp.add(dimAction);
		}
		
		getViewSite().getActionBars().getToolBarManager().update(true);
	}

}
