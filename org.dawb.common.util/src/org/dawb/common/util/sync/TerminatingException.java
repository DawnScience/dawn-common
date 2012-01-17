/**
 * Sync 2.1
 * Copyright 2007 Zach Scrivena
 * 2007-12-09
 * zachscrivena@gmail.com
 * http://syncdir.sourceforge.net/
 *
 * Sync performs one-way directory or file synchronization.
 *
 * TERMS AND CONDITIONS:
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawb.common.util.sync;


/**
 * Represent a runtime exception that terminates the program.
 * This exception should be allowed to propagate to the top-level for proper handling.
 */
class TerminatingException
		extends RuntimeException
{
	/** exit status code to be reported to the OS */
	private final int exitCode;


	/**
	 * Constructor.
	 *
	 * @param message
	 *      Exception message; saved for later retrieval by the Throwable.getMessage() method
	 * @param exitCode
	 *      Exit status code; saved for later retrieval by the TerminatingException.getExitCode()
	 *      method
	 */
	public TerminatingException(
			final String message,
			final int exitCode)
	{
		super(message);
		this.exitCode = exitCode;
	}


	/**
	 * Constructor. Exit status code is assumed to be 1.
	 *
	 * @param message
	 *      Exception message; saved for later retrieval by the Throwable.getMessage() method
	 */
	public TerminatingException(
			final String message)
	{
		super(message);
		this.exitCode = 1;
	}


	/**
	 * Return the exit status code to be reported to the OS.
	 */
	public int getExitCode()
	{
		return this.exitCode;
	}
}
