/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.common.ui.util;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class DisplayUtils {

	/**
	 * Run a runnable in the display thread
	 * @param async if true and not in display thread then run asynchronously or non-blocking
	 * @param control used to get the display (if null, then {@link Display#getDefault()} is used)
	 * @param runnable
	 */
	public static void runInDisplayThread(boolean async, Control control, Runnable runnable) {
		Display display = control == null ? Display.getDefault() : control.getDisplay();
		if (display.getThread() != Thread.currentThread()) {
			if (async)
				display.asyncExec(runnable);
			else
				display.syncExec(runnable);
		} else {
			runnable.run();
		}

	}
}
