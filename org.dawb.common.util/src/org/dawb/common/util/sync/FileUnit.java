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

import java.io.File;
import java.util.Date;
import java.util.Locale;


/**
 * Represent a file or directory.
 */
class FileUnit
{
	/** File object representing this file/directory (full canonical pathname) */
	final File file;

	/** true if this is a directory; false otherwise */
	final boolean isDirectory;

	/** file/directory name (ends with a trailing separator only if this is a directory) */
	final String name;

	/** size of file/directory in bytes (size of directory is fixed at zero bytes) */
	final long size;

	/** last-modified time in milliseconds since the epoch (00:00:00 GMT, January 1, 1970) */
	final long time;

	/** true if CRC-32 checksum of file/directory has been computed */
	private boolean computedCrc = false;

	/** CRC-32 checksum of file/directory (CRC-32 checksum of directory is fixed at zero) */
	private long crc = 0L;

	/** FileUnit object representing the corresponding matching target/source file/directory, if any */
	FileUnit match = null;

	/** true if the corresponding matching target/source file/directory has the same filename; false otherwise */
	boolean sameName = false;

	/** true if the corresponding matching target/source file/directory has the same size; false otherwise */
	boolean sameSize = false;

	/** true if the corresponding matching target/source file/directory has the same last-modified time; false otherwise */
	boolean sameTime = false;

	/** true if the corresponding matching target/source file/directory has the same CRC-32 checksum; false otherwise */
	boolean sameCrc = false;

	/** string representation of file size */
	private String sizeString = null;

	/** string representation of last-modified time */
	private String timeString = null;

	/** string representation of CRC-32 checksum */
	private String crcString = null;


	/**
	 * Constructor.
	 *
	 * @param f
	 *     File object representing this file/directory (full canonical pathname)
	 */
	FileUnit(
			final File f)
	{
		this.file = f;
		this.time = f.lastModified();
		this.isDirectory = f.isDirectory();

		if (this.isDirectory)
		{
			this.name = SyncIO.trimTrailingSeparator(f.getName()) + File.separatorChar;
			this.size = 0L;
			this.crc = 0L;
			this.computedCrc = true;
		}
		else
		{
			this.name = f.getName();
			this.size = f.length();
		}
	}


	/**
	 * Return the CRC-32 checksum of this file/directory.
	 */
	public long getCrc()
	{
		if (!this.computedCrc)
		{
			final SyncIO.Crc32Checksum result = SyncIO.getCrc32Checksum(this.file);

			if (result.error == null)
			{
				this.crc = result.checksum;
				this.computedCrc = true;
			}
			else
			{
				Sync.reportWarning("Unable to compute CRC-32 checksum of file \"" +
						this.file.getPath() + "\".\nThe CRC-32 checksum of this file will be assumed to be 0.");

				this.crc = 0L;
				this.computedCrc = true;
			}
		}

		return this.crc;
	}


	/**
	 * Return the string representation of the file size.
	 */
	public String getSizeString()
	{
		if (this.sizeString == null)
		{
			if (this.size < 1024)
			{
				this.sizeString = String.format("%d b", this.size);
			}
			else if (this.size < 1048576)
			{
				this.sizeString =  String.format("%.1f kb", this.size / 1024.0);
			}
			else if (this.size < 1073741824)
			{
				this.sizeString =  String.format("%.1f Mb", this.size / 1048576.0);
			}
			else
			{
				this.sizeString =  String.format("%.1f Gb", this.size / 1073741824.0);
			}
		}

		return this.sizeString;
	}


	/**
	 * Return the string representation of the last-modified time.
	 */
	public String getTimeString()
	{
		if (this.timeString == null)
			this.timeString = String.format(Locale.ENGLISH, Sync.TIME_FORMAT_STRING, new Date(this.time));

		return this.timeString;
	}


	/**
	 * Return the string representation of the CRC-32 checksum.
	 */
	public String getCrcString()
	{
		if (this.crcString == null)
			this.crcString = Long.toHexString(this.getCrc()).toUpperCase(Locale.ENGLISH);

		return this.crcString;
	}
}
