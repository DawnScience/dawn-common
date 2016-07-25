/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

public class PlotDataView extends PageBookView {

	public static final String ID = "org.dawb.workbench.views.dataSetView";

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.createControl(book);
		return messagePage;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		
		Page page = (Page)part.getAdapter(Page.class);
		
		/**
		 * The SDA broke this design a bit by introducing an IMetaProvider interface
		 * instead of implementing IAdaptable. Therefore we check for path of part
		 * and return a meta page if there is meta data.
		 */
		if (page==null) {
			try {
				final String filePath = EclipseUtils.getFilePath(((IEditorPart)part).getEditorInput());
				if (filePath!=null) page = new HeaderTablePage(filePath);
			} catch (Throwable ne) {
				page = null;
			}
		}

		if (page!=null) {
			initPage(page);
			page.createControl(getPageBook());			
			return new PageRec(part, page);
		}

		return null;
	}

	@Override
	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.page.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		
		IWorkbenchPage page = getSite().getPage();
		if(page != null) {
			// check whether the active part is important to us
			IWorkbenchPart activePart = page.getActivePart();
			return isImportant(activePart)?activePart:null;
		}
		return null;	
	}

	@Override
	protected boolean isImportant(IWorkbenchPart part) {
		return part instanceof IEditorPart || part instanceof MultiPageEditorPart;
	}

	public void partActivated(IWorkbenchPart part) {
		
		super.partActivated(part);
		
		final IPage page = getCurrentPage();
		final String title = page instanceof IAdaptable ? (String)((IAdaptable)page).getAdapter(String.class) : null;
		if (title!=null) {
			setPartName(title);
		} else {
			setPartName("Data");
		}

	}
}
