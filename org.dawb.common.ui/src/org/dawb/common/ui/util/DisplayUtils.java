/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.util;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class DisplayUtils {

	/**
	 * Run a runnable in the display thread
	 * @param async if true and not in display thread then run asynchronously or non-blocking
	 * @param control used to get the display (if null, then {@link Display#getDefault()} is used)
	 * @param runnable
	 */
	public static void runInDisplayThread(boolean async, Control control, Runnable runnable) {
		runInDisplayThread(false, async, control, runnable);
	}

	/**
	 * Run a runnable in the display thread
	 * @param newThread if true and in display thread then run in new thread
	 * @param async if true and not in display thread then run asynchronously or non-blocking
	 * @param control used to get the display (if null, then {@link Display#getDefault()} is used)
	 * @param runnable
	 */
	public static void runInDisplayThread(boolean newThread, boolean async, Control control, Runnable runnable) {
		Display display = control == null ? Display.getDefault() : control.getDisplay();
		if (display.getThread() != Thread.currentThread()) {
			if (async)
				display.asyncExec(runnable);
			else
				display.syncExec(runnable);
		} else {
			if (newThread)
				new Thread(runnable).start(); // TODO FIXME Not the display thread!
			else
				runnable.run();
		}
	}
	
	public static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null && PlatformUI.isWorkbenchRunning()) {
			display = PlatformUI.getWorkbench().getDisplay();
		}
		return display != null ? display : Display.getDefault();
	}
}
