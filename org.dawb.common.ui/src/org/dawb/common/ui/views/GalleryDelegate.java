/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.views;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.dawb.common.ui.preferences.ViewConstants;
import org.dawb.common.util.object.ObjectUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.image.IPlotImageService;
import org.eclipse.dawnsci.plotting.api.image.PlotImageData;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * May be used to create a lazy gallery of images.
 * 
 * @author Matthew Gerring
 *
 */
public class GalleryDelegate implements SelectionListener {
	
	// OSGi injected
	private static IPlotImageService pservice;
	public void setPlotImageService(IPlotImageService service) {
		pservice = service;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(GalleryDelegate.class);

	private Composite                parent;
	private Thread                   imageThread;
	protected Gallery                gallery;
	private GalleryItem              galleryGroup;
	private BlockingDeque<ImageItem> queue;
	private GalleryDelegateInfo      info;
	
	private ImageServiceBean lockedHistogram = null;

	public ImageServiceBean getLockedHistogram() {
		return lockedHistogram;
	}

	public void setLockedHistogram(ImageServiceBean lockedHistogram) {
		this.lockedHistogram = lockedHistogram;
		if (lockedHistogram!=null) refreshAll();
	}

	private String groupLabel;

	/**
	 * Does nothing so that OSGi can connect
	 */
	public GalleryDelegate() {
	}
	
	/**
	 * Please call this directly after creation to create the queue and start the thread.
	 */
	public void init() {
		this.queue = new LinkedBlockingDeque<ImageItem>(Integer.MAX_VALUE);
		createImageThread();
	}
	
	public Control getControl() {
		return parent;
	}
	
	public void createContent(String groupLabel, String toolTip, Composite parent) {
		
		this.parent     = parent;
		this.groupLabel = groupLabel;
		this.gallery    = new Gallery(parent, SWT.V_SCROLL | SWT.VIRTUAL | SWT.MULTI);
		gallery.setToolTipText(toolTip);
		
		// Renderers
		final DefaultGalleryGroupRenderer gr = new DefaultGalleryGroupRenderer();
		gr.setMinMargin(2);
		
		// Size image - parameterize this so that the user can change it.
		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");
		final int    size      = store.getInt(ViewConstants.IMAGE_SIZE);
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (!event.getProperty().equals(ViewConstants.IMAGE_SIZE)) return;
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
		
		gallery.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
				// Control-A is bad
				e.doit = false;
			    switch (e.keyCode) {
				case SWT.HOME:
				case SWT.END:
				case SWT.PAGE_UP:
				case SWT.PAGE_DOWN:
				case SWT.ARROW_UP:
				case SWT.ARROW_DOWN:
					e.doit = true;
					return;
				}
			}
		});
		
		// Virtual
		gallery.setVirtualGroups(true);
		gallery.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				
                if (!event.doit) return;
				GalleryItem item = (GalleryItem) event.item;
				int index = gallery.indexOf(item);
				item.setItemCount(index);
				
				item.setText(info.getItemName(index, true));
				
				final ImageItem ii = new ImageItem();
				ii.setIndex(index);
				ii.setItem(item);
							 	
			 	// Add to render queue
			 	queue.offerFirst(ii);	
			}

		});

		this.galleryGroup = new GalleryItem(gallery, SWT.VIRTUAL);
		galleryGroup.setText(groupLabel);
	}
	
	protected void setLayoutData(Object layoutData) {
		gallery.setLayoutData(layoutData);
	}
	
	public void setData(GalleryDelegateInfo info) {
		this.info = info;
		refreshAll();
	}
	
	public GalleryDelegateInfo getData() {
		return info;
	}
	
	public void clear() {
		queue.clear();
		galleryGroup.clearAll();
		galleryGroup.setItemCount(0);
		galleryGroup.setExpanded(true);
		galleryGroup.setText(groupLabel);
		
		gallery.update();
	}
	
	private String selectionDataLabel = "Slice";
	

	public String getSelectionDataLabel() {
		return selectionDataLabel;
	}

	public void setSelectionDataLabel(String selectionDataLabel) {
		this.selectionDataLabel = selectionDataLabel;
	}

	public List<IDataset> getSelectionData(GalleryItem[] items) {
		
		final List<IDataset> ys = new ArrayList<IDataset>(11);
		for (GalleryItem item : items) {
			final ImageItem ii = new ImageItem();
			ii.setIndex(item.getItemCount());
			ii.setItem(item);
            try {
            	IDataset slice = info.getData(true, ii);
            	slice.setName(selectionDataLabel+" "+info.getItemName(item.getItemCount(), false));
				ys.add((Dataset)slice);
			} catch (Exception e) {
				logger.error("Cannot slice ", e);
				continue;
			}
		}
        return ys;
	}
	
	protected List<GallerySelection> getSelectionPaths(GalleryItem[] items) {
		
		final List<GallerySelection> sels = new ArrayList<GallerySelection>(11);
		for (GalleryItem item : items) {
			final ImageItem ii = new ImageItem();
			ii.setIndex(item.getItemCount());
			ii.setItem(item);
            try {
            	GallerySelection sel = new GallerySelection();
            	sel.setPath(info.getPath(ii.getIndex()));
            	sel.setName(selectionDataLabel+" "+info.getItemName(item.getItemCount(), false));
            	sel.setIndex(ii.getIndex());
            	sels.add(sel);
			} catch (Exception e) {
				logger.error("Cannot slice ", e);
				continue;
			}
		}
        return sels;
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
						galleryGroup.setText(info.getName());
						
						gallery.update();
						gallery.getParent().layout(new Control[]{gallery});

						GalleryItem item = galleryGroup.getItem(galleryGroup.getItemCount()-1);
						gallery.setSelection(new GalleryItem[]{item});
					    
					}
			    });

				return Status.OK_STATUS;
			}
		};
		refresh.setPriority(Job.INTERACTIVE);
		refresh.schedule();
		
	}


	private void createImageThread() {

		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.common.ui");

		this.imageThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(gallery==null || !gallery.isDisposed()) { // This thread is going all the time.
					try {
						if (queue==null) break;
						final ImageItem ii = queue.take();
						if (ii.getItem()==null) return;
						if (ii.getIndex()<0)    return;
						if (ii.getItem().isDisposed()) continue;

						final IDataset set = info.getData(false, ii);
						if (set==null && info.getDirectThumbnailPath()==null) continue;

						// Generate thumbnail
						int size  = store.getInt(ViewConstants.IMAGE_SIZE);
						if (size<1) size = 96;

						final Image image;
						if (info.getDirectThumbnailPath()!=null) {
							final String path = info.getPath(ii.getIndex());
							image = new Image(Display.getDefault(), new BufferedInputStream(new FileInputStream(new File(path))));
						} else {
							final PlotImageData id = new PlotImageData(set, size, size);
							if (getLockedHistogram()!=null) id.setImageServiceBean(lockedHistogram);
							image = pservice.getImage(id);
						}

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (ii.getItem().isDisposed()) return;
								if (ii.getItem().getParent().isDisposed()) return;
								if (image!=null) {
									if (image.isDisposed()) return;
									ii.getItem().setImage(image);
									ii.getItem().setText(info.getItemName(ii.getIndex(), true));
								}
							}
						});
					} catch (Throwable ne) {
						logger.error("Cannot process images", ne);
						try {
							Thread.sleep(1000);// Wait a second before carrying on.
						} catch (InterruptedException e) {
							logger.error("Cannot sleep in Gallery Thread!", e);
						} 
					}
				}
			}

		}, "Image View Processing Daemon");

		imageThread.setDaemon(true);
		imageThread.start();
	}

	public void setMenu(MenuManager menuManager) {
		gallery.setMenu(menuManager.createContextMenu(gallery));
	}

	public void setFocus() {
		if (gallery!=null&&!gallery.isDisposed()) {
			gallery.setFocus();
		}
	}

	public GalleryItem[] getSelection() {
		return gallery.getSelection();
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
			gallery.dispose();
		}
		
		// Nullify variables
		gallery=null;
		galleryGroup=null;
		queue=null;
		imageThread=null;
	}

	public void addMouseListener(MouseListener listener) {
		if (!gallery.isDisposed()) gallery.addMouseListener(listener);
	}
	public void addSelectionListener(SelectionListener listener) {
		if (!gallery.isDisposed()) gallery.addSelectionListener(listener);
	}
	public void removeMouseListener(MouseListener listener) {
		if (!gallery.isDisposed()) gallery.removeMouseListener(listener);
	}
	public void removeSelectionListener(SelectionListener listener) {
		if (!gallery.isDisposed()) gallery.removeSelectionListener(listener);
	}

	public String getPath(int itemCount) {
		return info.getPath(itemCount);
	}

	public boolean isVisible() {
		return gallery.isVisible();
	}

	private ISelectionProvider selectionProvider;
	/**
	 * Creates and returns a new selection provider that will notify 
	 * when any item the gallery changes.
	 * 
	 * The array of selections sent by the provider will be the string paths to the data sets.
	 */
	public ISelectionProvider createSelectionProvider() {
		selectionProvider = new ISelectionProvider() {
			
			Collection<ISelectionChangedListener>  listeners = new LinkedHashSet<ISelectionChangedListener>();
			@Override
			public void setSelection(ISelection selection) {
				// TODO Set the UI?
				
				for (ISelectionChangedListener l : listeners) {
					l.selectionChanged(new SelectionChangedEvent(this, selection));
				}
			}
			
			@Override
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				listeners.remove(listener);
			}
			@Override
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
				listeners.add(listener);
			}
			
			@Override
			public ISelection getSelection() {
				final GalleryItem[] items = GalleryDelegate.this.getSelection();
				if (items==null || items.length<1) return null;
				return new StructuredSelection(getSelectionPaths(items));
			}
			
		};
		
		gallery.addSelectionListener(this);
		
		return selectionProvider;
	}
	
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
	
		ISelection selection  = selectionProvider.getSelection();
		if (selection!=null) selectionProvider.setSelection(selection);
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

}
