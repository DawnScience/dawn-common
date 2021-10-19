/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValuePageView extends PageBookView {

	/**
	 * Need to keep as old view id so that old workspaces still work.
	 */
	public static final String ID = "org.dawb.common.ui.views.ValueView";
    private static Logger logger = LoggerFactory.getLogger(ValuePageView.class);

	@Override
	protected IPage createDefaultPage(PageBook book) {
		MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.createControl(book);
		return messagePage;
	}

	@Override
	protected PageRec doCreatePage(IWorkbenchPart part) {
		
		Object object = part.getAdapter(IContentProvider.class);
	
		if (object==null || !(object instanceof Page)) {
			
			// If they have not provided the adapter, we check an extension point
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.valueProviderPage");
			if (config!=null && config.length>0) {
				try {
					object = config[0].createExecutableExtension("class");
				} catch (CoreException e) {
					logger.error("Cannot read extension \"org.dawb.common.ui.valueProviderPage\"", e);
				}
			}

		}
 		
		if (object!=null && object instanceof Page) {
			final Page page = (Page)object;
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
}
