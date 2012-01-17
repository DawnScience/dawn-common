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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Perform miscellaneous input-output operations.
 */
class SyncIO
{
	
	private static final Logger logger = LoggerFactory.getLogger(SyncIO.class);
	
	/** buffer size (1 Mb) */
	private static final int BUFFER_SIZE = 1048576;


	/**
	 * Set the last-modified time of a specified file.
	 *
	 * @param file
	 *     File for which to set time
	 * @param time
	 *     New last-modified time for the specified file
	 * @return
	 *     null if last-modifed time was successfully set; an error message otherwise
	 */
	static String setFileTime(
			final File file,
			final long time)
	{
		final boolean success = file.setLastModified(time);

		if (!success)
			return "Failed to set last-modified time using Java's File.setLastModified() method.";

		return null; // success
	}


	/**
	 * Rename a specified file. An existing file/directory may be overwritten if necessary.
	 *
	 * @param source
	 *     File to be renamed
	 * @param target
	 *     Desired target file
	 * @return
	 *     null if the file was successfully renamed, an empty string if file rename was aborted,
	 *     an error message otherwise
	 */
	static String renameFile(
			final File source,
			final File target)
	{
		/* rename the specified file? */
		boolean renameFile = false;

		/* existing target file (to be overwritten), if any */
		File existingFile = null;
		String existingName = null;
		boolean existingIsDirectory = false;

		/* check if a distinct target file/directory already exists */
		if (target.exists() && !target.equals(source))
		{
			/* get attributes of the existing file/directory */
			try
			{
				existingFile = target.getCanonicalFile();
			}
			catch (Exception e)
			{
				existingFile = target;
			}

			existingIsDirectory = existingFile.isDirectory();
			existingName = trimTrailingSeparator(existingFile.getName()) +
					(existingIsDirectory ? File.separatorChar : "");

			if (Sync.defaultActionOnOverwrite == 'Y')
			{
				SyncIO.printFlush("\n  Overwriting existing " +
						(existingIsDirectory ? "directory" : "file") +
						" \"" + existingName + "\"");
				renameFile = true;
			}
			else if (Sync.defaultActionOnOverwrite == 'N')
			{
				SyncIO.printFlush("\n  Skipping overwriting of existing " +
						(existingIsDirectory ? "directory" : "file") +
						" \"" + existingName + "\"");
			}
			else if (Sync.defaultActionOnOverwrite == '\0')
			{
				SyncIO.print("\n  Overwrite existing " +
						(existingIsDirectory ? "directory" : "file") +
						" \"" + existingName + "\"?\n");

				final char choice = SyncIO.userCharPrompt(
						"  (Y)es/(N)o/(A)lways/Neve(R): ",
						"YNAR");

				if (choice == 'Y')
				{
					renameFile = true;
				}
				else if (choice == 'A')
				{
					Sync.defaultActionOnOverwrite = 'Y';
					renameFile = true;
				}
				else if (choice == 'R')
				{
					Sync.defaultActionOnOverwrite = 'N';
				}
			}
		}
		else
		{
			/* target file does not exist, or is the same File as the source */
			renameFile = true;
		}

		if (renameFile)
		{
			/* delete existing file/directory first, if any */
			if (existingFile != null)
			{
				if (existingIsDirectory)
				{
					final String error = deleteDirTreeOperation(existingFile);

					if (error != null)
						return "Failed to delete existing directory \"" +
								trimTrailingSeparator(existingFile.getPath()) + File.separatorChar +
								"\":\n" + error;
				}
				else
				{
					final boolean success = existingFile.delete();

					if (!success)
						return "Failed to delete existing file \"" +
								existingFile.getPath() + "\" using Java's File.delete() method.";
				}
			}

			/* rename file */
			final boolean success = source.renameTo(target);

			if (success)
			{
				return null; // success
			}
			else
			{
				return "Failed to rename file using Java's File.renameTo() method.";
			}
		}

		return ""; // aborted file rename
	}


	/**
	 * Copy a specified file. An existing file/directory may be overwritten if necessary.
	 *
	 * @param source
	 *     File to be copied
	 * @param target
	 *     File object representing the target file
	 * @return
	 *     null if the file was successfully copied, an empty string if file copy was aborted,
	 *     an error message otherwise
	 */
	static String copyFile(
			final File source,
			final File target)
	{
		/* copy the specified file? */
		boolean copyFile = false;

		/* existing target file (to be overwritten), if any */
		File existingFile = null;
		String existingName = null;
		boolean existingIsDirectory = false;

		/* check if a target file/directory already exists */
		if (target.exists())
		{
			/* get attributes of the existing file/directory */
			try
			{
				existingFile = target.getCanonicalFile();
			}
			catch (Exception e)
			{
				existingFile = target;
			}

			existingIsDirectory = existingFile.isDirectory();
			existingName = trimTrailingSeparator(existingFile.getName()) +
					(existingIsDirectory ? File.separatorChar : "");

			if (Sync.defaultActionOnOverwrite == 'Y')
			{
				SyncIO.printFlush("\n  Overwriting existing " +
						(existingIsDirectory ? "directory" : "file") +
						" \"" + existingName + "\"");
				copyFile = true;
			}
			else if (Sync.defaultActionOnOverwrite == 'N')
			{
				SyncIO.printFlush("\n  Skipping overwriting of existing " +
						(existingIsDirectory ? "directory" : "file") +
						" \"" + existingName + "\"");
			}
			else if (Sync.defaultActionOnOverwrite == '\0')
			{
				SyncIO.print("\n  Overwrite existing " +
						(existingIsDirectory ? "directory" : "file") +
						" \"" + existingName +	"\"?\n");

				final char choice = SyncIO.userCharPrompt(
						"  (Y)es/(N)o/(A)lways/Neve(R): ",
						"YNAR");

				if (choice == 'Y')
				{
					copyFile = true;
				}
				else if (choice == 'A')
				{
					Sync.defaultActionOnOverwrite = 'Y';
					copyFile = true;
				}
				else if (choice == 'R')
				{
					Sync.defaultActionOnOverwrite = 'N';
				}
			}
		}
		else
		{
			/* target file does not exist */
			copyFile = true;
		}

		if (copyFile)
		{
			/* delete existing file/directory first, if any */
			if (existingFile != null)
			{
				if (existingIsDirectory)
				{
					/* delete existing directory first */
					final String error = deleteDirTreeOperation(existingFile);

					if (error != null)
						return "Failed to delete existing directory \"" +
								trimTrailingSeparator(existingFile.getPath()) + File.separatorChar +
								"\":\n" + error;
				}
				else
				{
					final boolean success = existingFile.delete();

					if (!success)
						return "Failed to delete existing file \"" +
								existingFile.getPath() + "\" using Java's File.delete() method.";
				}
			}

			/* copy file */
			final String error = copyFileOperation(source, target);

			if (error == null)
			{
				return null; // success
			}
			else
			{
				return "Failed to copy file:\n" + error;
			}
		}

		return ""; // aborted file copy
	}


	/**
	 * Delete a specified file/directory.
	 *
	 * @param file
	 *     File object representing the file/directory to be deleted
	 * @return
	 *     null if the file/directory is successfully deleted; an error message otherwise
	 */
	static String deleteFileDir(
			final File file)
	{
		if (file.isDirectory())
		{
			final String error = deleteDirTreeOperation(file);

			if (error != null)
				return "Failed to delete directory:\n" + error;
		}
		else
		{
			final boolean success = file.delete();

			if (!success)
				return "Failed to delete file using Java's File.delete() method.";
		}

		return null; // success
	}


	/**
	 * Delete a directory and all its contents (subdirectories and
	 * files) recursively.
	 *
	 * @param dir
	 *     Directory to be deleted, along with all its contents
	 * @return
	 *     null if operation is successful; an error message otherwise
	 */
	private static String deleteDirTreeOperation(
			final File dir)
	{
		/* error message(s), if any */
		final List<String> errors = new ArrayList<String>();

		/* perform a DFS deletion of the directory using two stacks:             */
		/* - fullDirs contains subdirectories whose files have yet to be deleted */
		/* - emptyDirs contains empty subdirectories to be deleted, after their  */
		/*   contents have been deleted                                          */

		final Deque<File> fullDirs = new ArrayDeque<File>();
		final Deque<File> emptyDirs = new ArrayDeque<File>();

		final File marker = new File(""); // special marker

		fullDirs.push(dir);

		DeleteNextDirectory:
		while (!fullDirs.isEmpty())
		{
			final File fd = fullDirs.pop();

			if (fd == marker)
			{
				final File ed = emptyDirs.pop();
				final boolean success = ed.delete();

				if (!success)
					errors.add("Failed to delete directory \"" + ed.getPath() +
							"\" using Java's File.delete() method (it could still be nonempty)");

				continue DeleteNextDirectory;
			}

			/* get directory contents */
			final File[] files = fd.listFiles();

			if (files == null)
			{
				errors.add("Failed to get contents of directory \"" + fd.getPath() + "\"");
			}
			else
			{
				/* push this directory onto emptyStack for subsequent deletion */
				emptyDirs.push(fd);
				fullDirs.push(marker); // special marker

				for (File f : files)
				{
					if (f.isDirectory())
					{
						fullDirs.push(f);
					}
					else
					{
						final boolean success = f.delete();

						if (!success)
							errors.add("Failed to delete file \"" + f.getPath() +
									"\" using Java's File.delete() method");
					}
				}
			}
		}

		if (!errors.isEmpty())
		{
			/* return concatenated error message */
			final StringBuilder t = new StringBuilder();
			for (String s : errors)
			{
				t.append(s);
				t.append("; ");
			}
			t.delete(t.length() - 2, t.length());

			return t.toString();
		}

		return null; // success
	}


	/**
	 * Copy a specified file.
	 *
	 * @param sourceFile
	 *     Source file
	 * @param targetFile
	 *     Target file
	 * @return
	 *     null if operation is successful; an error message otherwise
	 */
	private static String copyFileOperation(
			final File source,
			final File target)
	{
		/* buffered input stream for reading */
		BufferedInputStream bis = null;

		/* buffered output stream for writing */
		BufferedOutputStream bos = null;

		try
		{
			/* error message(s), if any */
			final List<String> errors = new ArrayList<String>();

			try
			{
				bis = new BufferedInputStream(new FileInputStream(source));
			}
			catch (Exception e)
			{
				return "Failed to open source file for reading (" + e.getMessage() + ")";
			}

			/* parent directory of the target file */
			final File targetParentDir = target.getParentFile();

			/* create parent directory of target file, if necessary */
			if (!targetParentDir.exists())
				targetParentDir.mkdirs();

			if (!targetParentDir.isDirectory())
				return "Failed to create parent directory of target file";

			try
			{
				bos = new BufferedOutputStream(new FileOutputStream(target));
			}
			catch (Exception e)
			{
				return "Failed to open target file for writing (" + e.getMessage() + ")";
			}

			/* byte buffer */
			final byte byteBuffer[] = new byte[SyncIO.BUFFER_SIZE];

			try
			{
				/* copy bytes from the source file to the target file */
				while (true)
				{
					final int byteCount = bis.read(byteBuffer, 0, SyncIO.BUFFER_SIZE);

					if (byteCount == -1)
						break; /* reached EOF */

					bos.write(byteBuffer, 0, byteCount);
				}
			}
			catch (Exception e)
			{
				return "Failed to copy data from source file to target file (" + e.getMessage() + ")";
			}

			try
			{
				bis.close();
				bis = null;
			}
			catch (Exception e)
			{
				errors.add("Failed to close source file after reading (" + e.getMessage() + ")");
			}

			try
			{
				bos.close();
				bos = null;
			}
			catch (Exception e)
			{
				errors.add("Failed to close target file after writing (" + e.getMessage() + ")");
			}

			final boolean success = target.setLastModified(source.lastModified());

			if (!success)
				errors.add("Failed to set last-modified time of target file after writing");

			if (!errors.isEmpty())
			{
				/* return concatenated error message */
				final StringBuilder t = new StringBuilder();
				for (String s : errors)
				{
					t.append(s);
					t.append("; ");
				}
				t.delete(t.length() - 2, t.length());

				return t.toString();
			}

			return null; // success
		}
		finally
		{
			/* close buffered input stream for reading */
			if (bis != null)
			{
				try
				{
					bis.close();
				}
				catch (Exception e)
				{
					/* ignore */
				}
			}

			/* close buffered output stream for writing */
			if (bos != null)
			{
				try
				{
					bos.close();
				}
				catch (Exception e)
				{
					/* ignore */
				}
			}
		}
	}


	/**
	 * Compute the CRC-32 checksum of a file.
	 *
	 * @param file
	 *     File for which to compute the CRC-32 checksum
	 * @return
	 *     Result of the CRC-32 checksum computation
	 */
	static Crc32Checksum getCrc32Checksum(
			final File file)
	{
		/* checksum of directory is defined as 0 */
		if (file.isDirectory())
			return new Crc32Checksum(null, 0L);

		/* buffered input stream for reading */
		BufferedInputStream bis = null;

		try
		{
			try
			{
				bis = new BufferedInputStream(new FileInputStream(file));
			}
			catch (Exception e)
			{
				return new Crc32Checksum("Failed to open file for reading (" + e.getMessage() + ")", 0L);
			}

			/* byte buffer */
			final byte byteBuffer[] = new byte[SyncIO.BUFFER_SIZE];

			/* CRC-32 object to track checksum computation */
			final CRC32 crc32 = new CRC32();

			try
			{
				/* read bytes from the file, and track the checksum computation */
				while (true)
				{
					final int byteCount = bis.read(byteBuffer, 0, SyncIO.BUFFER_SIZE);

					if (byteCount == -1)
						break; /* reached EOF */

					crc32.update(byteBuffer, 0, byteCount);
				}
			}
			catch (Exception e)
			{
				return new Crc32Checksum("Failed to read data from file (" + e.getMessage() + ")", 0L);
			}

			try
			{
				bis.close();
				bis = null;
			}
			catch (Exception e)
			{
				/* ignore */
			}

			/* successful computation of CRC-32 checksum */
			return new Crc32Checksum(null, crc32.getValue());
		}
		finally
		{
			/* close buffered input stream for reading */
			if (bis != null)
			{
				try
				{
					bis.close();
				}
				catch (Exception e)
				{
					/* ignore */
				}
			}
		}
	}


	/**
	 * Removes a trailing separator, if any, in the specified path string.
	 *
	 * @param path
	 *     Path string to be trimmed
	 * @return
	 *     Path string after removal of a trailing separator
	 */
	static String trimTrailingSeparator(
			final String path)
	{
		if (path.endsWith(File.separator))
			return path.substring(0, path.length() - 1);

		return path;
	}


	/**
	 * Prompt user for a single-character input.
	 *
	 * @param prompt
	 *     Prompt string
	 * @param ops
	 *     Options string containing permitted character responses (automatically converted
	 *     to upper case)
	 * @return
	 *     Character chosen by the user (automatically converted to upper case)
	 */
	static char userCharPrompt(
			final String prompt,
			final String ops)
	{
		/* case-insensitive comparison; convert everything to uppercase */
		final String options = ops.toUpperCase(Locale.ENGLISH);

		final Scanner kb = new Scanner(System.in);

		while (true)
		{
			printFlush(prompt);

			String response = kb.nextLine();
			printLog(response + "\n");

			response = response.trim();

			if (response.length() != 1)
				continue;

			/* convert to char */
			final char c = response.toUpperCase(Locale.ENGLISH).charAt(0);

			if (options.indexOf(c) >= 0)
				return c;
		}
	}


	/**
	 * Convenience method to print to standard output and log file (if any).
	 *
	 * @param s
	 *     String to be printed
	 */
	static void print(
			final String s)
	{
		logger.trace(s);

		if (Sync.log != null)
			Sync.log.print(s);
	}


	/**
	 * Convenience method to print to standard output and log file (if any),
	 * and flush the buffers.
	 *
	 * @param s
	 *     String to be printed
	 */
	static void printFlush(
			final String s)
	{
		logger.trace(s);

		if (Sync.log != null)
		{
			Sync.log.print(s);
			Sync.log.flush();
		}
	}


	/**
	 * Convenience method to print to standard error and log file (if any).
	 *
	 * @param s
	 *     String to be printed
	 */
	static void printToErr(
			final String s)
	{
		logger.error(s);
		if (Sync.log != null)
		{
			Sync.log.print(s);
			Sync.log.flush();
		}
	}


	/**
	 * Convenience method to print to log file (if any) only.
	 *
	 * @param s
	 *     String to be printed
	 */
	static void printLog(
			final String s)
	{
		if (Sync.log != null)
		{
			Sync.log.print(s);
			Sync.log.flush();
		}
	}


	/**
	 * Inner class to represent the result of a file CRC-32 computation.
	 */
	static class Crc32Checksum
	{
		/** null if operation is successful; an error message otherwise */
		public String error = null;

		/** file CRC-32 checksum value */
		public long checksum;


		/**
		 * Constructor.
		 *
		 * @param error
		 *     null if operation is successful; an error message otherwise
		 * @param checksum
		 *     File CRC-32 checksum value
		 */
		Crc32Checksum(
				final String error,
				final long checksum)
		{
			this.error = error;
			this.checksum = checksum;
		}
	}
}
