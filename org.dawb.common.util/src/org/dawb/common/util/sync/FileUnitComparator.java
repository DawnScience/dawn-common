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

import java.util.Comparator;

/**
 * Compare two FileUnit objects according to the specified attributes.
 */
class FileUnitComparator
		implements Comparator<FileUnit>
{
	/** use file/directory name for comparison */
	private final boolean name;

	/** use file/directory size for comparison */
	private final boolean size;

	/** use file/directory last-modified time for comparison */
	private final boolean time;

	/** use file/directory CRC-32 checksum for comparison */
	private final boolean crc;


	/**
	 * Constructor.
	 *
	 * @param name
	 *     Use file/directory name for comparison
	 * @param size
	 *     Use file/directory size for comparison
	 * @param time
	 *     Use file/directory last-modified time for comparison
	 * @param crc
	 *     Use file/directory CRC-32 checksum for comparison
	 */
	FileUnitComparator(
			final boolean name,
			final boolean size,
			final boolean time,
			final boolean crc)
	{
		this.name = name;
		this.size = size;
		this.time = time;
		this.crc  = crc;
	}


	/**
	 * Compare the specified FileUnit objects.
	 */
	@Override
	public int compare(
			final FileUnit u1,
			final FileUnit u2)
	{
		if (name)
		{
			/* compare file/directory names */
			final int i = u1.name.compareTo(u2.name);
			if (i != 0) return i;
		}

		if (size)
		{
			/* compare file/directory sizes */
			if (u1.size < u2.size) return -1;
			if (u1.size > u2.size) return 1;
		}

		if (time)
		{
			/* compare file/directory last-modified times */
			if (u1.time < u2.time) return -1;
			if (u1.time > u2.time) return 1;
		}

		if (crc)
		{
			/* compare file/directory CRC-32 checksums */
			if (u1.getCrc() < u2.getCrc()) return -1;
			if (u1.getCrc() > u2.getCrc()) return 1;
		}

		/* FileUnit objects are equal */
		return 0;
	}
}
