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
	 * @return display
	 */
	public static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null && PlatformUI.isWorkbenchRunning()) {
			display = PlatformUI.getWorkbench().getDisplay();
		}
		return display != null ? display : Display.getDefault();
	}

	/**
	 * Execute runnable asynchronously in display thread if not currently in display thread otherwise
	 * execute immediately
	 * @param runnable can be null to wake up display thread
	 * @throws RuntimeException
	 */
	public static void asyncExec(Runnable runnable) throws RuntimeException {
		executeInDisplayThread(true, null, runnable);
	}

	/**
	 * Execute runnable asynchronously in display thread if not currently in display thread otherwise
	 * execute immediately
	 * @param control used to get the display (if null, then {@link Display#getDefault()} is used)
	 * @param runnable can be null to wake up display thread
	 * @throws RuntimeException
	 */
	public static void asyncExec(Control control, Runnable runnable) throws RuntimeException {
		executeInDisplayThread(true, control, runnable);
	}

	/**
	 * Execute runnable synchronously in display thread if not currently in display thread otherwise
	 * execute immediately
	 * @param runnable can be null to wake up display thread
	 * @throws RuntimeException
	 */
	public static void syncExec(Runnable runnable) throws RuntimeException {
		executeInDisplayThread(false, null, runnable);
	}

	/**
	 * Execute runnable synchronously in display thread if not currently in display thread otherwise
	 * execute immediately
	 * @param control used to get the display (if null, then {@link Display#getDefault()} is used)
	 * @param runnable can be null to wake up display thread
	 * @throws RuntimeException
	 */
	public static void syncExec(Control control, Runnable runnable) throws RuntimeException {
		executeInDisplayThread(false, control, runnable);
	}

	/**
	 * Execute runnable in display thread if not currently in display thread otherwise
	 * execute immediately
	 * @param async if true, execute asynchronously
	 * @param control used to get the display (if null, then {@link Display#getDefault()} is used)
	 * @param runnable can be null to wake up display thread
	 * @throws RuntimeException
	 */
	private static void executeInDisplayThread(boolean async, Control control, Runnable runnable) throws RuntimeException {
		ThrowableRunnable r = new ThrowableRunnable(control, runnable, async);
		r.run();
		Throwable t = r.getThrowable();
		if (t instanceof RuntimeException) {
			throw (RuntimeException) t;
		}
	}

	static class ThrowableRunnable implements Runnable {
		private Display display;
		private Runnable runnable;
		private boolean async;
		private Throwable exception;

		public ThrowableRunnable(Control control, Runnable runnable, boolean async) {
			this.display = control == null ? Display.getDefault() : control.getDisplay();
			this.runnable = runnable;
			this.async = async;
		}

		public Throwable getThrowable() {
			return exception;
		}

		@Override
		public void run() {
			try {
				if (display.getThread() != Thread.currentThread()) {
					if (async)
						display.asyncExec(runnable);
					else
						display.syncExec(runnable);
				} else if (runnable != null) {
					runnable.run();
				}
			} catch (Exception e) {
				exception = e.getCause();
			}
		}
	}
}
