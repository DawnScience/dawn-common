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
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.PatternSyntaxException;


/**
 * Sync performs one-way directory or file synchronization.
 */
public class Sync
{
	/******************************************
	 * CONSTANTS AND MISCELLANEOUS PARAMETERS *
	 ******************************************/

	/** constant: program title */
	private static final String PROGRAM_TITLE =
			"Sync 2.1   Copyright 2007 Zach Scrivena   2007-12-09";

	/** constant: time format string (yyyy-MM-dd HH:mm:ss.SSS) */
	static final String TIME_FORMAT_STRING = "%1$tF %1$tT.%1$tL";

	/** parameter: true if this is a Windows OS, false otherwise */
	private static boolean isWindowsOperatingSystem = false;

	/******************************
	 * SYNCHRONIZATION PARAMETERS *
	 ******************************/

	/** parameter: simulate only; do not modify target (default = false) */
	private static boolean simulateOnly = false;

	/** parameter: ignore warnings; do not pause (default = false) */
	private static boolean ignoreWarnings = false;

	/** parameter: canonical full pathname of log file (default = null) */
	private static String logName = null;

	/** parameter: log file PrintWriter (default = null) */
	static PrintWriter log = null;

	/** parameter: do not recurse into subdirectories (default = false) */
	private static boolean noRecurse = false;

	/** enum type: synchronization mode (DIRECTORY, FILE) */
	private static enum SyncMode
	{
		DIRECTORY,
		FILE;
	}

	/** parameter: synchronization mode (DIRECTORY, FILE) */
	private static SyncMode syncMode;

	/** parameter: match file/directory name (default = true) */
	private static boolean matchName = true;

	/** parameter: match file/directory size (always true) */
	private static final boolean matchSize = true;

	/** parameter: match file/directory last-modified time (default = true) */
	private static boolean matchTime = true;

	/** parameter: match file/directory CRC-32 checksum (default = true) */
	private static boolean matchCrc = true;

	/** parameter: time-tolerance in milliseconds for file-matching (default = 0) */
	private static long matchTimeTolerance = 0L;

	/** parameter: string representation of match attributes, e.g. "(name,size,time,CRC)" */
	private static String matchNstcString;

	/** partial FileUnit comparator for file-matching */
	private static FileUnitComparator matchFileUnitComparator;

	/** partial FileUnit comparator for searching (should be a "truncated" version of Sync.matchFileUnitComparator) */
	private static FileUnitComparator searchFileUnitComparator;

	/** name-only FileUnit comparator */
	private static FileUnitComparator nameOnlyFileUnitComparator;

	/** parameter: canonical full pathname of source file/directory (ends with a separator for a directory) */
	private static String sourceName;

	/** parameter: canonical full pathname of target file/directory (ends with a separator for a directory) */
	private static String targetName;

	/** parameter: source file/directory (canonical full pathname) */
	private static File source;

	/** parameter: target file/directory (canonical full pathname) */
	private static File target;

	/** parameter: default action on renaming matched files (default = '\0') */
	private static char defaultActionOnRenameMatched = '\0';

	/** parameter: default action on synchronizing time of matched files (default = '\0') */
	private static char defaultActionOnTimeSyncMatched = '\0';

	/** parameter: default action on deleting unmatched target files/directories (default = '\0') */
	private static char defaultActionOnDeleteUnmatched = '\0';

	/** parameter: default action on overwriting existing target files (default = '\0') */
	static char defaultActionOnOverwrite = '\0';

	/** parameter: filter for source file/directory names (default = null) */
	private static FilterNode sourceFilter = null;

	/** parameter: filter for target file/directory names (default = null) */
	private static FilterNode targetFilter = null;

	/** parameter: filter relative pathnames instead of filenames (default = false) */
	private static boolean filterRelativePathname = false;

	/** parameter: use lower case names for filtering (default = false) */
	private static boolean filterLowerCase = false;

	/*********************
	 * REPORT STATISTICS *
	 *********************/

	/** statistic: number of warnings encountered */
	private static int reportNumWarnings = 0;

	
	/**
	 * Method added to allow a sync to be done by calling method directly
	 * 
	 * @param source
	 * @param target
	 * @throws Exception 
	 */
	public static void syncFolders(final String source, final String target) throws Exception {
		
		sync(new String[]{"--rename:y", "--synctime:y", "--overwrite:y", "--delete:y", source, target});
	}
	
	public static void main(final String[] args) throws Exception {
		
        sync(args);		
	}
	
	/**
	 * Main entry point for the Sync program.
	 *
	 * @param args
	 *     Command-line argument strings
	 * @throws Exception 
	 */
	public static void sync(final String[] args) throws Exception {

		/* display program title */
		SyncIO.printFlush("\n" + Sync.PROGRAM_TITLE);

		/* exit status code to be reported to the OS when exiting (default = 0) */
		int exitCode = 0;

		try
		{
			/* determine if this is a Windows OS */
			Sync.isWindowsOperatingSystem = System.getProperty("os.name").toUpperCase(Locale.ENGLISH).contains("WINDOWS") &&
					(File.separatorChar == '\\');

			/* process command-line arguments and configure synchronization parameters */
			processArguments(args);

			SyncIO.printLog("\n" + Sync.PROGRAM_TITLE);

			/* perform synchronization */
			switch (Sync.syncMode)
			{
				case DIRECTORY:
					syncDirectory();
					break;

				case FILE:
					syncFile();
					break;
			}

			SyncIO.print("\n\nSync is done!\n\n");
		}
		catch (TerminatingException e)
		{
			/* terminating exception thrown; proceed to abort program */
			/* (this should be the only place where a TerminatingException is caught) */

			exitCode = e.getExitCode();

			if (exitCode != 0)
			{
				/* abnormal termination; SyncIO.print error message */
				SyncIO.printToErr("\n\nERROR: " + e.getMessage() + "\n");
				SyncIO.print("\nSync aborted.\n\n");
			}
		}
		catch (Exception e)
		{
			/* catch all other exceptions; proceed to abort program */
			SyncIO.printToErr("\n\nERROR: An unexpected error has occurred:\n" +
					getExceptionMessage(e) + "\n");

			exitCode = 1;
			SyncIO.print("\nSync aborted.\n\n");
		}
		finally
		{
			/* perform clean-up before exiting */

			if (Sync.log != null)
			{
				Sync.log.flush();
				Sync.log.close();
				Sync.log = null;
			}
		}

	}


	/**
	 * Process command-line arguments and configure synchronization parameters.
	 *
	 * @param args
	 *     Command-line argument strings
	 */
	private static void processArguments(
			final String[] args)
	{
		final String howHelp = "\nTo display help, run Sync without any command-line arguments.";

		/* SyncIO.print usage documentation, if no arguments */
		if (args.length == 0)
		{
			printUsage();
			throw new TerminatingException(null, 0);
		}

		/* check if sufficient arguments */
		if (args.length < 2)
			throw new TerminatingException("Insufficient arguments:\nThe source and target directories/files must be specified." + howHelp);

		/* process source directory/file */
		Sync.source = new File(args[args.length - 2]);

		try
		{
			Sync.source = Sync.source.getCanonicalFile();
		}
		catch (Exception e)
		{
			throw new TerminatingException("Source \"" + Sync.source.getPath() + "\" is not a valid directory/file:\n" + getExceptionMessage(e) + howHelp);
		}

		/* process target directory/file */
		Sync.target = new File(args[args.length - 1]);

		try
		{
			Sync.target = Sync.target.getCanonicalFile();
		}
		catch (Exception e)
		{
			throw new TerminatingException("Target \"" + Sync.target.getPath() + "\" is not a valid directory/file:\n" + getExceptionMessage(e) + howHelp);
		}

		/* determine synchronization mode */
		if (Sync.source.isDirectory())
		{
			/* source is a directory; must check that target is NOT a file */
			if (Sync.target.exists() && !Sync.target.isDirectory())
				throw new TerminatingException("Target \"" + Sync.target.getPath() + "\" is a file.\nFor DIRECTORY synchronization, the target (if it exists) must also be a directory." + howHelp);

			/* DIRECTORY synchronization */
			Sync.syncMode = Sync.SyncMode.DIRECTORY;
			Sync.sourceName = SyncIO.trimTrailingSeparator(Sync.source.getPath()) + File.separatorChar;
			Sync.targetName = SyncIO.trimTrailingSeparator(Sync.target.getPath()) + File.separatorChar;
		}
		else if (source.exists())
		{
			/* source is a file; must check that target is NOT a directory */
			if (Sync.target.isDirectory())
				throw new TerminatingException("Target \"" + Sync.target.getPath() + "\" is a directory.\nFor FILE synchronization, the target (if it exists) must also be a file." + howHelp);

			/* FILE synchronization */
			Sync.syncMode = Sync.SyncMode.FILE;
			Sync.sourceName = SyncIO.trimTrailingSeparator(Sync.source.getPath());
			Sync.targetName = SyncIO.trimTrailingSeparator(Sync.target.getPath());
		}
		else
		{
			/* source does not exist */
			throw new TerminatingException("Source \"" + Sync.source.getPath() + "\" does not exist." + howHelp);
		}

		/* initialize filename filters */
		final List<String> includeSource = new ArrayList<String>();
		final List<String> excludeSource = new ArrayList<String>();
		final List<String> includeTarget = new ArrayList<String>();
		final List<String> excludeTarget = new ArrayList<String>();
		boolean regexFilter = false;

		/* process command-line switches */
		for (int i = 0, n = args.length - 2; i < n; i++)
		{
			final String sw = args[i];

			if ("--simulate".equals(sw) || "-s".equals(sw))
			{
				/* simulate only; do not modify target */
				Sync.simulateOnly = true;
				Sync.ignoreWarnings = true;
			}
			else if ("--ignorewarnings".equals(sw))
			{
				/* ignore warnings; do not pause  */
				Sync.ignoreWarnings = true;
			}
			else if ("--log".equals(sw) || "-l".equals(sw))
			{
				/* create log file "sync.yyyyMMdd-HHmmss.log" */
				if (Sync.logName != null)
					throw new TerminatingException("Switch --log can be specified at most once." + howHelp);

				final String timestamp = String.format("%1$tY%1$tm%1$td-%1$tH%1$tM%1$tS", Calendar.getInstance(Locale.ENGLISH));

				File f = new File("sync." + timestamp + ".log");

				if (f.exists())
				{
					/* find an unused file name */
					for (long k = 0; k < Long.MAX_VALUE; k++)
					{
						f = new File("sync." + timestamp + "." + k + ".log");

						if (f.exists())
						{
							f = null;
						}
						else
						{
							/* use this unused name */
							break;
						}
					}

					if (f == null)
						throw new TerminatingException("Failed to create an unused filename for log file:\nRan out of suffixes n in \"sync." +
								timestamp + ".n.log\"; try specifying a filename, e.g. --log:\"record.txt\"." + howHelp);
				}

				try
				{
					Sync.logName = f.getCanonicalPath();
				}
				catch (Exception e)
				{
					throw new TerminatingException("Failed to create log file \"" + f.getPath() + "\":\n" + getExceptionMessage(e) + howHelp);
				}
			}
			else if (sw.startsWith("--log:") || sw.startsWith("-l:"))
			{
				/* create log file with the specified name */
				if (Sync.logName != null)
					throw new TerminatingException("Switch --log can be specified at most once." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --log parameter:\nA log filename must be specified, e.g. --log:\"record.txt\"." + howHelp);

				File f = new File(a);

				if (f.exists())
					throw new TerminatingException("Log file \"" + f.getPath() + "\" already exists:\nA nonexistent file must be specified." + howHelp);

				try
				{
					Sync.logName = f.getCanonicalPath();
				}
				catch (Exception e)
				{
					throw new TerminatingException("Failed to create log file \"" + f.getPath() + "\":\n" + getExceptionMessage(e) + howHelp);
				}
			}
			else if ("--norecurse".equals(sw) || "-r".equals(sw))
			{
				/* do not recurse into subdirectories */
				Sync.noRecurse = true;
			}
			else if ("--noname".equals(sw) || "-n".equals(sw))
			{
				/* do not use filename for file-matching */
				Sync.matchName = false;
			}
			else if ("--notime".equals(sw) || "-t".equals(sw))
			{
				/* do not use last-modified time for file-matching */
				Sync.matchTime = false;
			}
			else if ("--nocrc".equals(sw)|| "-c".equals(sw))
			{
				/* do not use CRC-32 checksum for file-matching */
				Sync.matchCrc = false;
			}
			else if (sw.startsWith("--time:"))
			{
				/* use specified time-tolerance (in milliseconds) for file-matching */
				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --time parameter:\nTime-tolerance (in milliseconds) must be a nonnegative integer, e.g. --time:2000." + howHelp);

				try
				{
					Sync.matchTimeTolerance = Long.parseLong(a);
				}
				catch (Exception e)
				{
					Sync.matchTimeTolerance = -1L;
				}

				if (Sync.matchTimeTolerance < 0L)
					throw new TerminatingException("Invalid --time parameter \"" +
							a + "\":\nTime-tolerance (in milliseconds) must be a nonnegative integer, e.g. --time:2000." + howHelp);
			}
			else if (sw.startsWith("--rename:"))
			{
				/* rename matched target files? */
				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --rename parameter:\nParameter must be \"y\" or \"n\", e.g. --rename:y." + howHelp);

				if ("y".equals(a))
				{
					Sync.defaultActionOnRenameMatched = 'Y';
				}
				else if ("n".equals(a))
				{
					Sync.defaultActionOnRenameMatched = 'N';
				}
				else
				{
					throw new TerminatingException("Invalid --rename parameter \"" + a + "\":\nParameter must be \"y\" or \"n\", e.g. --rename:y." + howHelp);
				}
			}
			else if (sw.startsWith("--synctime:"))
			{
				/* synchronize time of matched target files? */
				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --synctime parameter:\nParameter must be \"y\" or \"n\", e.g. --synctime:y." + howHelp);

				if ("y".equals(a))
				{
					Sync.defaultActionOnTimeSyncMatched = 'Y';
				}
				else if ("n".equals(a))
				{
					Sync.defaultActionOnTimeSyncMatched = 'N';
				}
				else
				{
					throw new TerminatingException("Invalid --synctime parameter \"" + a + "\":\nParameter must be \"y\" or \"n\", e.g. --synctime:y." + howHelp);
				}
			}
			else if (sw.startsWith("--overwrite:"))
			{
				/* overwrite existing target files? */
				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --overwrite parameter:\nParameter must be \"y\" or \"n\", e.g. --overwrite:y." + howHelp);

				if ("y".equals(a))
				{
					Sync.defaultActionOnOverwrite = 'Y';
				}
				else if ("n".equals(a))
				{
					Sync.defaultActionOnOverwrite = 'N';
				}
				else
				{
					throw new TerminatingException("Invalid --overwrite parameter \"" + a + "\":\nParameter must be \"y\" or \"n\", e.g. --overwrite:y." + howHelp);
				}
			}
			else if (sw.startsWith("--delete:"))
			{
				/* delete unmatched target files/directories? */
				if (syncMode != syncMode.DIRECTORY)
					throw new TerminatingException("Switch --delete can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --delete parameter:\nParameter must be \"y\" or \"n\", e.g. --delete:y." + howHelp);

				if ("y".equals(a))
				{
					Sync.defaultActionOnDeleteUnmatched = 'Y';
				}
				else if ("n".equals(a))
				{
					Sync.defaultActionOnDeleteUnmatched = 'N';
				}
				else
				{
					throw new TerminatingException("Invalid --delete parameter \"" + a + "\":\nParameter must be \"y\" or \"n\", e.g. --delete:y." + howHelp);
				}
			}
			else if ("--force".equals(sw))
			{
				/* equivalent to the combination: "--rename:y --synctime:y --overwrite:y --delete:y" */
				Sync.defaultActionOnRenameMatched = 'Y';
				Sync.defaultActionOnTimeSyncMatched = 'Y';
				Sync.defaultActionOnOverwrite = 'Y';
				Sync.defaultActionOnDeleteUnmatched = 'Y';
			}
			else if ("--path".equals(sw) || "-p".equals(sw))
			{
				/* filter relative pathnames instead of filenames (e.g. "work\report\jan.txt" instead of "jan.txt") */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --path can be used for only DIRECTORY synchronization." + howHelp);

				Sync.filterRelativePathname = true;
			}
			else if ("--lower".equals(sw) || "-w".equals(sw))
			{
				/* use lower case names for filtering (e.g. "HelloWorld2007.JPG" ---> "helloworld2007.jpg") */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --lower can be used for only DIRECTORY synchronization." + howHelp);

				Sync.filterLowerCase = true;
			}
			else if ("--regex".equals(sw))
			{
				 /* use REGEX instead of GLOB filename filters */
				regexFilter = true;
			}
			else if (sw.startsWith("--include:") || sw.startsWith("-i:"))
			{
				/* include source and target files/directories with names matching specified GLOB/REGEX expression */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --include can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --include parameter:\nA GLOB (or REGEX) expression must be specified, e.g. --include:\"*.{mp3,jpg}\"." + howHelp);

				includeSource.add(a);
				includeTarget.add(a);
			}
			else if (sw.startsWith("--exclude:") || sw.startsWith("-x:"))
			{
				/* exclude source and target files/directories with names matching specified GLOB/REGEX expression */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --exclude can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --exclude parameter:\nA GLOB (or REGEX) expression must be specified, e.g. --exclude:\"*.{mp3,jpg}\"." + howHelp);

				excludeSource.add(a);
				excludeTarget.add(a);
			}
			else if (sw.startsWith("--includesource:") || sw.startsWith("-is:"))
			{
				/* include source files/directories with names matching specified GLOB/REGEX expression */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --includesource can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --includesource parameter:\nA GLOB (or REGEX) expression must be specified, e.g. --includesource:\"*.{mp3,jpg}\"." + howHelp);

				includeSource.add(a);
			}
			else if (sw.startsWith("--excludesource:") || sw.startsWith("-xs:"))
			{
				/* exclude source files/directories with names matching specified GLOB/REGEX expression */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --excludesource can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --excludesource parameter:\nA GLOB (or REGEX) expression must be specified, e.g. --excludesource:\"*.{mp3,jpg}\"." + howHelp);

				excludeSource.add(a);
			}
			else if (sw.startsWith("--includetarget:") || sw.startsWith("-it:"))
			{
				/* include target files/directories with names matching specified GLOB/REGEX expression */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --includetarget can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --includetarget parameter:\nA GLOB (or REGEX) expression must be specified, e.g. --includetarget:\"*.{mp3,jpg}\"." + howHelp);

				includeTarget.add(a);
			}
			else if (sw.startsWith("--excludetarget:") || sw.startsWith("-xt:"))
			{
				/* exclude target files/directories with names matching specified GLOB/REGEX expression */
				if (Sync.syncMode != Sync.syncMode.DIRECTORY)
					throw new TerminatingException("Switch --excludetarget can be used for only DIRECTORY synchronization." + howHelp);

				final String a = sw.substring(sw.indexOf(':') + 1);

				if (a.isEmpty())
					throw new TerminatingException("Empty --excludetarget parameter:\nA GLOB (or REGEX) expression must be specified, e.g. --excludetarget:\"*.{mp3,jpg}\"." + howHelp);

				excludeTarget.add(a);
			}
			else
			{
				/* invalid switch */
				throw new TerminatingException("\"" + sw + "\" is not a valid switch." + howHelp);
			}
		}

		/* process source filename filters, if any */
		if (includeSource.isEmpty())
		{
			if (excludeSource.isEmpty())
			{
				Sync.sourceFilter = null;
			}
			else
			{
				Sync.sourceFilter = new FilterNode(FilterNode.LogicType.NOR);

				for (String s : excludeSource)
				{
					try
					{
						Sync.sourceFilter.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}
			}
		}
		else
		{
			if (excludeSource.isEmpty())
			{
				Sync.sourceFilter = new FilterNode(FilterNode.LogicType.OR);

				for (String s : includeSource)
				{
					try
					{
						Sync.sourceFilter.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}
			}
			else
			{
				final FilterNode includes = new FilterNode(FilterNode.LogicType.OR);
				final FilterNode excludes = new FilterNode(FilterNode.LogicType.NOR);

				for (String s : includeSource)
				{
					try
					{
						includes.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}

				for (String s : excludeSource)
				{
					try
					{
						excludes.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}

				Sync.sourceFilter = new FilterNode(FilterNode.LogicType.AND);
				Sync.sourceFilter.addFilter(includes);
				Sync.sourceFilter.addFilter(excludes);
			}
		}

		/* process target filename filters, if any */
		if (includeTarget.isEmpty())
		{
			if (excludeTarget.isEmpty())
			{
				Sync.targetFilter = null;
			}
			else
			{
				Sync.targetFilter = new FilterNode(FilterNode.LogicType.NOR);

				for (String s : excludeTarget)
				{
					try
					{
						Sync.targetFilter.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}
			}
		}
		else
		{
			if (excludeTarget.isEmpty())
			{
				Sync.targetFilter = new FilterNode(FilterNode.LogicType.OR);

				for (String s : includeTarget)
				{
					try
					{
						Sync.targetFilter.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}
			}
			else
			{
				final FilterNode includes = new FilterNode(FilterNode.LogicType.OR);
				final FilterNode excludes = new FilterNode(FilterNode.LogicType.NOR);

				for (String s : includeTarget)
				{
					try
					{
						includes.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}

				for (String s : excludeTarget)
				{
					try
					{
						excludes.addFilter(new FilterNode(
								regexFilter ? FilterNode.FilterType.REGEX : FilterNode.FilterType.GLOB,
								false,
								Sync.isWindowsOperatingSystem ? s.replace("/", "\\\\") : s));
					}
					catch (PatternSyntaxException e)
					{
						throw new TerminatingException("Failed to compile the specified " +
								(regexFilter ? "REGEX" : "GLOB") + " expression \"" + s + "\":\n" + getExceptionMessage(e) + howHelp);
					}
				}

				Sync.targetFilter = new FilterNode(FilterNode.LogicType.AND);
				Sync.targetFilter.addFilter(includes);
				Sync.targetFilter.addFilter(excludes);
			}
		}

		/* check certain combinations of switches */

		if ((Sync.sourceFilter == null) && (Sync.targetFilter == null))
		{
			if (Sync.filterRelativePathname)
				throw new TerminatingException("Switch --path cannot be used when no filter is specified." + howHelp);

			if (Sync.filterLowerCase)
				throw new TerminatingException("Switch --lower cannot be used when no filter is specified." + howHelp);
		}

		if (Sync.simulateOnly)
		{
			if (Sync.defaultActionOnRenameMatched != '\0')
				throw new TerminatingException("Switch --rename cannot be used in simulation mode." + howHelp);

			if (Sync.defaultActionOnTimeSyncMatched != '\0')
				throw new TerminatingException("Switch --synctime cannot be used in simulation mode." + howHelp);

			if (Sync.defaultActionOnOverwrite != '\0')
				throw new TerminatingException("Switch --overwrite cannot be used in simulation mode." + howHelp);

			if (Sync.defaultActionOnDeleteUnmatched != '\0')
				throw new TerminatingException("Switch --delete cannot be used in simulation mode." + howHelp);

			Sync.defaultActionOnRenameMatched = 'Y';
			Sync.defaultActionOnTimeSyncMatched = 'Y';
			Sync.defaultActionOnOverwrite = 'Y';
			Sync.defaultActionOnDeleteUnmatched = 'Y';
		}

		/* prepare log file, if specified */
		if (Sync.logName != null)
		{
			try
			{
				Sync.log = new PrintWriter(Sync.logName);
			}
			catch (Exception e)
			{
				throw new TerminatingException("Failed to create log file \"" + Sync.logName + "\":\n" + getExceptionMessage(e) + howHelp);
			}
		}

		/* disable filename matching for FILE synchronization */
		if (Sync.syncMode == Sync.syncMode.FILE)
			Sync.matchName = false;

		/* create string representation of match attributes, e.g. "(name,size,time,crc)" */
		Sync.matchNstcString = getNstcString(
				Sync.matchName, Sync.matchSize, Sync.matchTime, Sync.matchCrc);

		/* create partial FileUnit comparator for file-matching */
		Sync.matchFileUnitComparator = new FileUnitComparator(
				Sync.matchName, Sync.matchSize, Sync.matchTime, Sync.matchCrc);

		/* create partial FileUnit comparator for searching (should be a "truncated" version of Sync.matchFileUnitComparator) */
		Sync.searchFileUnitComparator = new FileUnitComparator(
				Sync.matchName, Sync.matchSize, false, false);

		/* create name-only FileUnit comparator */
		Sync.nameOnlyFileUnitComparator = new FileUnitComparator(
				true, false, false, false);
	}


	/**
	 * Perform DIRECTORY synchronization from Sync.source to Sync.target.
	 * The source must be an existing directory; the target must be a directory if it exists.
	 */
	private static void syncDirectory()
	{
		final StringBuilder s = new StringBuilder();

		s.append("\n\nDIRECTORY SYNCHRONIZATION");

		if (Sync.simulateOnly)
			s.append(" (SIMULATION MODE)");

		/* display log file, if any */
		if (Sync.log != null)
			s.append("\n\nLog file: \"" + Sync.logName + "\"");

		/* display source and target directories */
		s.append("\n\nSource directory: \"" + Sync.sourceName + "\"" +
				"\nTarget directory: \"" + Sync.targetName + "\"\n");

		/* display file-matching attributes */
		s.append("\nFile-matching attributes: " + Sync.matchNstcString + "\n");

		if (Sync.matchTimeTolerance > 0L)
			s.append(", with " + Sync.matchTimeTolerance + "-millisecond time-tolerance\n");

		/* display source and target file/directorry filters, if any */
		if (Sync.sourceFilter != null)
			s.append("\nSource file/directory filter: " + Sync.sourceFilter.toString());

		if (Sync.targetFilter != null)
			s.append("\nTarget file/directory filter: " + Sync.targetFilter.toString());

		if ((Sync.sourceFilter != null) || (Sync.targetFilter != null))
			s.append("\nFilter mode: " + (Sync.filterLowerCase ? "lower-case " : "") +
					(Sync.filterRelativePathname ? "relative pathname" : "filename") + "\n");

		SyncIO.printFlush(s.toString());

		/* validate source and target */
		if (Sync.source.equals(Sync.target))
			throw new TerminatingException("The source directory \"" + Sync.sourceName +
					"\" cannot be the same as the target directory \"" + Sync.targetName + "\".");

		if (Sync.sourceName.startsWith(Sync.targetName))
			throw new TerminatingException("The source directory \"" + Sync.sourceName +
					"\" cannot be a subdirectory of the target directory \"" + Sync.targetName + "\".");

		if (Sync.targetName.startsWith(Sync.sourceName))
			throw new TerminatingException("The target directory \"" + Sync.targetName +
					"\" cannot be a subdirectory of the source directory \"" + Sync.sourceName + "\".");

		final int sourceNameLength = Sync.sourceName.length();
		final int targetNameLength = Sync.targetName.length();

		/* report statistics */
		int reportNumSourceDirsScanned = 0;
		int reportNumSourceFilesScanned = 0;
		int reportNumTargetFilesScanned = 0;
		int reportNumSourceFilesMatched = 0;
		int reportNumSyncTime = 0;
		int reportNumSyncTimeSuccess = 0;
		int reportNumRenameOperations = 0;
		int reportNumRenameOperationsSuccess = 0;
		int reportNumUnmatchedSourceFiles = 0;
		int reportNumUnmatchedSourceFilesCopied = 0;
		int reportNumUnmatchedTargetFilesDirs = 0;
		int reportNumUnmatchedTargetFilesDirsDeleted = 0;

		/* perform a DFS synchronization of the subdirectories using two stacks: */
		/* - contentStack contains directories whose contents are to be synced   */
		/* - timeStack contains directories whose time should be synced after    */
		/*   processing all their contents                                       */

		final Deque<FilePair> contentStack = new ArrayDeque<FilePair>();
		final Deque<FilePair> timeStack = new ArrayDeque<FilePair>();

		final FilePair marker = new FilePair(null, null); // special marker

		contentStack.push(new FilePair(Sync.source, Sync.target));

		SyncNextDirectory:
		while (!contentStack.isEmpty())
		{
			/* get subdirectory-pair for synchronizing contents */
			final FilePair pair = contentStack.pop();

			/* check for special marker */
			if (pair == marker)
			{
				/**************************************************
				 * SYNC LAST-MODIFIED TIME OF TARGET SUBDIRECTORY *
				 **************************************************/

				final FilePair timePair = timeStack.pop();

				final File timeSourceDir = timePair.source;
				final File timeTargetDir = timePair.target;

				if (timeSourceDir.isDirectory() && timeTargetDir.isDirectory())
				{
					final long sourceTime = timeSourceDir.lastModified();
					final long targetTime = timeTargetDir.lastModified();

					/* create the target subdirectory only if the times are different, and */
					/* the target's name matches the filter (if specified)                 */
					boolean syncTime = false;

					if (targetTime != sourceTime)
					{
						if (Sync.sourceFilter == null)
						{
							syncTime = true;
						}
						else
						{
							final String timeTargetDirName = SyncIO.trimTrailingSeparator(timeTargetDir.getPath()) + File.separatorChar;

							String name = null;

							if (timeTargetDirName.length() == targetNameLength)
							{
								/* this is the base target directory */
								name = "";
							}
							else
							{
								name = Sync.filterRelativePathname ?
									timeTargetDirName.substring(targetNameLength) :
									(SyncIO.trimTrailingSeparator(timeTargetDir.getName()) + File.separatorChar);
							}

							if (Sync.filterLowerCase)
								name = name.toLowerCase(Locale.ENGLISH);

							if (Sync.sourceFilter.matches(name))
								syncTime = true;
						}
					}

					if (syncTime)
					{
						if (!Sync.simulateOnly)
						{
							final boolean success = timeTargetDir.setLastModified(sourceTime);

							if (!success)
								reportWarning("Failed to set last-modified time of target subdirectory \"" +
										SyncIO.trimTrailingSeparator(timeTargetDir.getPath()) + File.separatorChar +
										"\":\n " + String.format(Locale.ENGLISH, Sync.TIME_FORMAT_STRING, new Date(targetTime)) +
										" ---> " + String.format(Locale.ENGLISH, Sync.TIME_FORMAT_STRING, new Date(sourceTime)) + ".");
						}
					}
				}

				continue SyncNextDirectory;
			}

			/*********************************
			 * (1) DISPLAY RELATIVE PATHNAME *
			 *********************************/

			/* source subdirectory */
			final File sourceDir = pair.source;
			final String sourceDirName = SyncIO.trimTrailingSeparator(sourceDir.getPath()) + File.separatorChar;

			/* target subdirectory */
			final File targetDir = pair.target;
			final String targetDirName = SyncIO.trimTrailingSeparator(targetDir.getPath()) + File.separatorChar;

			/* relative pathname of the subdirectory */
			final String relativePathname = sourceDirName.substring(sourceNameLength);

			SyncIO.printFlush("\n\nSUBDIRECTORY: \"" + (relativePathname.isEmpty() ? ("." + File.separatorChar) : relativePathname) + "\"");

			if (targetDir.exists() && !targetDir.isDirectory())
			{
				reportWarning("The target \"" + targetDir.getPath() +
						"\" already exists but is not a directory; could it be a file?\nThis subdirectory will be ignored.");
				continue SyncNextDirectory;
			}

			/*******************************************
			 * (2) GET CONTENTS OF SOURCE SUBDIRECTORY *
			 *******************************************/

			/* get source files (filtered if necessary) and subdirectories */
			final File[] sFileList = sourceDir.listFiles();

			if (sFileList == null)
			{
				reportWarning("Failed to get contents of source subdirectory \"" +
							sourceDirName + "\".\nThis subdirectory will be ignored.");
				continue SyncNextDirectory;
			}

			final List<FileUnit> sFiles = new ArrayList<FileUnit>();
			final List<FileUnit> sDirs = new ArrayList<FileUnit>();

			for (File f : sFileList)
			{
				final FileUnit u = new FileUnit(f);

				if (u.isDirectory)
				{
					sDirs.add(u);
				}
				else
				{
					/* apply filter on file, if necessary */
					boolean addFile = false;

					if (Sync.sourceFilter == null)
					{
						addFile = true;
					}
					else
					{
						String name = Sync.filterRelativePathname ?
							u.file.getPath().substring(sourceNameLength) : u.name;

						if (Sync.filterLowerCase)
							name = name.toLowerCase(Locale.ENGLISH);

						if (Sync.sourceFilter.matches(name))
							addFile = true;
					}

					if (addFile)
						sFiles.add(u);
				}
			}

			reportNumSourceDirsScanned++;
			reportNumSourceFilesScanned += sFiles.size();

			/*******************************************
			 * (3) GET CONTENTS OF TARGET SUBDIRECTORY *
			 *******************************************/

			/* get target files and subdirectories */
			final List<FileUnit> tFiles = new ArrayList<FileUnit>();
			final List<FileUnit> tDirs = new ArrayList<FileUnit>();

			if (targetDir.isDirectory())
			{
				/* target subdirectory already exists; get its contents */
				final File[] tFileList = targetDir.listFiles();

				if (tFileList == null)
				{
					reportWarning("Failed to get contents of target subdirectory \"" +
							targetDirName + "\".\nThis subdirectory will be ignored.");
					continue SyncNextDirectory;
				}

				for (File f : tFileList)
				{
					final FileUnit u = new FileUnit(f);

					if (u.isDirectory)
					{
						tDirs.add(u);
					}
					else
					{
						/* apply filter on file, if necessary */
						boolean addFile = false;

						if (Sync.targetFilter == null)
						{
							addFile = true;
						}
						else
						{
							String name = Sync.filterRelativePathname ?
								u.file.getPath().substring(targetNameLength) : u.name;

							if (Sync.filterLowerCase)
								name = name.toLowerCase(Locale.ENGLISH);

							if (Sync.targetFilter.matches(name))
								addFile = true;
						}

						if (addFile)
							tFiles.add(u);
					}
				}
			}
			else if (targetDir.exists())
			{
				/* target already exists, but is not a directory */
				reportWarning("Target \"" + targetDir.getPath() +
						"\" already exists but is not a directory; could it be a file?" +
						"\nThis subdirectory will be ignored.");
				continue SyncNextDirectory;
			}
			else
			{
				/* target subdirectory does not exist;                                     */
				/* proceed to create it only if its name matches the filter (if specified) */
				boolean createDir = false;

				if (Sync.sourceFilter == null)
				{
					createDir = true;
				}
				else
				{
					String name = null;

					if (targetDirName.length() == targetNameLength)
					{
						/* this is the base target directory */
						name = "";
					}
					else
					{
						name = Sync.filterRelativePathname ?
							targetDirName.substring(targetNameLength) :
							(SyncIO.trimTrailingSeparator(targetDir.getName()) + File.separatorChar);
					}

					if (Sync.filterLowerCase)
						name = name.toLowerCase(Locale.ENGLISH);

					if (Sync.sourceFilter.matches(name))
						createDir = true;
				}

				if (createDir)
				{
					if (!Sync.simulateOnly)
					{
						targetDir.mkdirs();

						if (!targetDir.isDirectory())
						{
							reportWarning("Failed to create target subdirectory \"" +
									targetDirName + "\".\nThis subdirectory will be ignored.");
							continue SyncNextDirectory;
						}
					}
				}
			}

			reportNumTargetFilesScanned += tFiles.size();

			/**************************************************************
			 * (4) CHECK FOR FILE-MATCHING KEY CLASHES AMONG SOURCE FILES *
			 **************************************************************/

			/* sort source files by file-matching attributes */
			Collections.sort(sFiles, Sync.matchFileUnitComparator);

			FileUnit w = null;
			for (FileUnit u : sFiles)
			{
				/* check if consecutive files have the same file-matching attributes */
				if ((w != null) && (Sync.matchFileUnitComparator.compare(u, w) == 0))
				{
					reportWarning("File-matching key clash in source subdirectory \"" + sourceDirName +
							"\":\nThe following source files have the same " + Sync.matchNstcString + ":" +
							"\n [1] \"" + u.file.getPath() + "\"" +
							"\n [2] \"" + w.file.getPath() + "\"" +
							"\nThe files in this subdirectory will be ignored.");

					/* recurse into subdirectories:                                    */
					/* (this block of code should be identical to the one below)       */
					/* push subdirectory-pair onto time-stack for subsequent time-sync */
					timeStack.push(new FilePair(sourceDir, targetDir));
					contentStack.push(marker); // special marker

					if (!Sync.noRecurse)
					{
						for (int i = sDirs.size() - 1; i >= 0; i--)
						{
							/* source subdirectory */
							final File sDir = sDirs.get(i).file;

							/* corresponding target subdirectory */
							final File tDir = new File(targetDir, sDir.getName());

							/* push subdirectory-pair onto content-stack for subsequent content-sync */
							contentStack.push(new FilePair(sDir, tDir));
						}
					}

					continue SyncNextDirectory;
				}

				w = u;
			}

			/*************************************************************
			 * (5) PERFORM FILE-MATCHING BETWEEN SOURCE AND TARGET FILES *
			 *************************************************************/

			/* perform file-matching */
			final boolean uniqueMatching = performSourceTargetFileMatching(sFiles, tFiles);

			/* matched source files, to be time-synced or renamed if necessary */
			final List<FileUnit> sFilesMatched = new ArrayList<FileUnit>();

			/* unmatched source files, to be copied */
			final List<FileUnit> sFilesUnmatched = new ArrayList<FileUnit>();

			/* unmatched target files and subdirectories, to be deleted */
			final Map<File,FileUnit> tFilesDirsUnmatched = new TreeMap<File,FileUnit>();

			/* process source files */
			for (FileUnit u : sFiles)
			{
				if (u.match == null)
				{
					/* this is an unmatched source file, to be copied */
					sFilesUnmatched.add(u);
				}
				else
				{
					/* this is a matched source file */
					sFilesMatched.add(u);
				}
			}

			/* process target files */
			for (FileUnit u : tFiles)
			{
				if (u.match == null)
				{
					/* this is an unmatched target file, to be deleted */
					tFilesDirsUnmatched.put(u.file, u);
				}
			}

			/* perform directory-matching (matching by name only) */
			performSourceTargetDirMatching(sDirs, tDirs);

			/* process target directories */
			for (FileUnit u : tDirs)
			{
				if (u.match == null)
				{
					/* this is an unmatched target subdirectory, to be deleted */
					tFilesDirsUnmatched.put(u.file, u);
				}
			}

			/*******************************************
			 * (6) DISPLAY MATCHED SOURCE-TARGET FILES *
			 *******************************************/

			/* number of matched target files to time-sync and rename */
			int numSyncTime = 0;
			int numSyncName = 0;

			if (!sFiles.isEmpty())
			{
				final int numSourceFilesMatched = sFilesMatched.size();

				SyncIO.print("\n\n No. of source files matched: " + numSourceFilesMatched + " of " + sFiles.size());

				for (FileUnit u : sFilesMatched)
				{
					reportNumSourceFilesMatched++;

					/* display matched source-target pair and matching attributes */
					SyncIO.print("\n [M" + reportNumSourceFilesMatched + ":" +
							(u.sameName ? "n" : " ") +
							(u.sameSize ? "s" : " ") +
							(u.sameTime ? "t" : " ") +
							(Sync.matchCrc ? (u.sameCrc ? "c" : " ") : "") +
							"] \"" + u.name + "\"" +
							(u.sameName ? "" : (" <---> \"" + u.match.name + "\"")));

					/* need to sync filename of matched target file? */
					if (!u.sameName)
						numSyncName++;

					/* need to sync time of matched target file? */
					if (!u.sameTime)
						numSyncTime++;
				}

				/* warn on poor file-matching */
				if (!uniqueMatching)
					reportWarning("Matching between files in source subdirectory \"" + sourceDirName +
							"\" and target subdirectory \"" + targetDirName + "\" involves arbitrarily broken ties.");
			}

			/*****************************************
			 * (7) SYNC TIME OF MATCHED TARGET FILES *
			 *****************************************/

			if (numSyncTime > 0)
			{
				boolean syncTime = false;

				if (Sync.defaultActionOnTimeSyncMatched == 'Y')
				{
					SyncIO.print("\n\n Synchronizing last-modified time of " +
							numSyncTime + " matched target " +
							((numSyncTime == 1) ? "file:" : "files:"));
					syncTime = true;
				}
				else if (Sync.defaultActionOnTimeSyncMatched == 'N')
				{
					SyncIO.print("\n\n Skipping last-modified time synchronization of " +
							numSyncTime + " matched target " +
							((numSyncTime == 1) ? "file" : "files"));
				}
				else if (Sync.defaultActionOnTimeSyncMatched == '\0')
				{
					SyncIO.print("\n\n Synchronize last-modified time of " +
							numSyncTime + " matched target " +
							((numSyncTime == 1) ? "file" : "files") + "?\n");

					final char choice = SyncIO.userCharPrompt(
							"  (Y)es/(N)o/(A)lways/Neve(R): ",
							"YNAR");

					if (choice == 'Y')
					{
						syncTime = true;
					}
					else if (choice == 'A')
					{
						Sync.defaultActionOnTimeSyncMatched = 'Y';
						syncTime = true;
					}
					else if (choice == 'R')
					{
						Sync.defaultActionOnTimeSyncMatched = 'N';
					}
				}

				if (syncTime)
				{
					/* proceed to synchronize time of matched target files */
					for (FileUnit u : sFilesMatched)
					{
						final FileUnit t = u.match;

						if (!t.sameTime)
						{
							/* set last-modified time of the matched target file to that of the source file */

							reportNumSyncTime++;
							SyncIO.printFlush("\n [T" + reportNumSyncTime + "] \"" +
									t.name + "\"\n  " + t.getTimeString() + " ---> " + u.getTimeString());

							if (!Sync.simulateOnly)
							{
								final String error = SyncIO.setFileTime(t.file, u.time);

								if (error == null)
								{
									/* last-modified time of file was successfully set */
									reportNumSyncTimeSuccess++;
								}
								else if (!error.isEmpty())
								{
									reportWarning("Failed to set last-modified time of matched target file \"" +
											t.file.getPath() + "\":\n " + t.getTimeString() + " ---> " + u.getTimeString() +
											":\n" + error);
								}
							}
						}
					}
				}
			}

			/***********************************
			 * (8) RENAME MATCHED TARGET FILES *
			 ***********************************/

			if (numSyncName > 0)
			{
				boolean syncName = false;

				if (Sync.defaultActionOnRenameMatched == 'Y')
				{
					SyncIO.print("\n\n Renaming " +
							numSyncName + " matched target " +
							((numSyncName == 1) ? "file:" : "files:"));
					syncName = true;
				}
				else if (Sync.defaultActionOnRenameMatched == 'N')
				{
					SyncIO.print("\n\n Skipping renaming of " +
							numSyncName + " matched target " +
							((numSyncName == 1) ? "file" : "files"));
				}
				else if (Sync.defaultActionOnRenameMatched == '\0')
				{
					SyncIO.print("\n\n Rename " +
							numSyncName + " matched target " +
							((numSyncName == 1) ? "file" : "files") +
							"?\n");

					final char choice = SyncIO.userCharPrompt(
							"  (Y)es/(N)o/(A)lways/Neve(R): ",
							"YNAR");

					if (choice == 'Y')
					{
						syncName = true;
					}
					else if (choice == 'A')
					{
						Sync.defaultActionOnRenameMatched = 'Y';
						syncName = true;
					}
					else if (choice == 'R')
					{
						Sync.defaultActionOnRenameMatched = 'N';
					}
				}

				if (syncName)
				{
					/* determine actual file renaming operations */
					final List<FilePair> renamePairs = new ArrayList<FilePair>();

					/* get desired source-target rename pair */
					for (FileUnit u : sFilesMatched)
					{
						if (!u.sameName)
							renamePairs.add(new FilePair(u.match.file, new File(targetDir, u.name)));
					}

					final List<FilePair> renameOperations =	getRenameOperations(renamePairs);

					/* proceed to rename matched target file */
					for (FilePair p : renameOperations)
					{
						reportNumRenameOperations++;
						SyncIO.printFlush("\n [R" + reportNumRenameOperations + "] \"" +
								p.source.getName() + "\" ---> \"" + p.target.getName() + "\"");

						if (!Sync.simulateOnly)
						{
							final String error = SyncIO.renameFile(p.source, p.target);

							if (error == null)
							{
								/* file was successfully renamed */
								reportNumRenameOperationsSuccess++;
								tFilesDirsUnmatched.remove(p.target);
							}
							else if (!error.isEmpty())
							{
								reportWarning("Failed to rename matched target file \"" +
										p.source.getPath() + "\" ---> \"" +
										p.target.getPath() + "\":\n" + error);
							}
						}
					}
				}
			}

			/***********************************
			 * (9) COPY UNMATCHED SOURCE FILES *
			 ***********************************/

			if (!sFilesUnmatched.isEmpty())
			{
				final int numUnmatchedSourceFiles = sFilesUnmatched.size();

				/* display unmatched source files to be copied to the target subdirectory */
				SyncIO.print("\n\n No. of unmatched source files to be copied: " + numUnmatchedSourceFiles);

				for (FileUnit u : sFilesUnmatched)
				{
					reportNumUnmatchedSourceFiles++;
					SyncIO.printFlush("\n [C" + reportNumUnmatchedSourceFiles + "] \"" +
							u.name + "\" (" + u.getSizeString() + ")");

					if (!Sync.simulateOnly)
					{
						/* desired target file for copy operation */
						final File targetFile = new File(targetDir, u.name);

						final String error = SyncIO.copyFile(u.file, targetFile);

						if (error == null)
						{
							/* file was successfully copied */
							reportNumUnmatchedSourceFilesCopied++;
							tFilesDirsUnmatched.remove(targetFile);
						}
						else if (!error.isEmpty())
						{
							Sync.reportWarning("Failed to copy unmatched source file \"" +
									u.file.getPath() + "\" ---> \"" +
									targetFile.getPath() + "\":\n" + error);
						}
					}
				}
			}

			/**************************************************
			 * (10) DELETE UNMATCHED TARGET FILES/DIRECTORIES *
			 **************************************************/

			if (!tFilesDirsUnmatched.isEmpty())
			{
				final int numUnmatchedTargetFilesDirs = tFilesDirsUnmatched.size();

				SyncIO.print("\n\n No. of unmatched target files/directories to be deleted: " + numUnmatchedTargetFilesDirs);

				/* delete unmatched target files (first pass), and subdirectories (second pass) */
				for (boolean isDirectory : new boolean[]{false, true})
				{
					for (FileUnit u : tFilesDirsUnmatched.values())
					{
						if (u.isDirectory == isDirectory)
						{
							reportNumUnmatchedTargetFilesDirs++;
							SyncIO.print("\n [D" + reportNumUnmatchedTargetFilesDirs + "] ");

							boolean stillExists = false;

							if (u.file.exists() &&
									(u.file.isDirectory() == u.isDirectory))
							{
								/* proceed to check full canonical pathname */
								String pathname = null;

								try
								{
									pathname = u.file.getCanonicalPath();
								}
								catch (Exception e)
								{
									pathname = null;
								}

								if ((pathname != null) && pathname.equals(u.file.getPath()))
									stillExists = true;
							}

							if (stillExists)
							{
								boolean deleteFileDir = false;

								if (Sync.defaultActionOnDeleteUnmatched == 'Y')
								{
									SyncIO.printFlush("\"" + u.name + "\"");
									deleteFileDir = true;
								}
								else if (Sync.defaultActionOnDeleteUnmatched == 'N')
								{
									SyncIO.printFlush("Skipping \"" + u.name + "\"");
								}
								else if (Sync.defaultActionOnDeleteUnmatched == '\0')
								{
									SyncIO.print("Delete \"" + u.name + "\"?\n");

									final char choice = SyncIO.userCharPrompt(
											"  (Y)es/(N)o/(A)lways/Neve(R): ",
											"YNAR");

									if (choice == 'Y')
									{
										deleteFileDir = true;
									}
									else if (choice == 'A')
									{
										Sync.defaultActionOnDeleteUnmatched = 'Y';
										deleteFileDir = true;
									}
									else if (choice == 'R')
									{
										Sync.defaultActionOnDeleteUnmatched = 'N';
									}
								}

								if (deleteFileDir)
								{
									if (!Sync.simulateOnly)
									{
										final String error = SyncIO.deleteFileDir(u.file);

										if (error == null)
										{
											/* file/directory was successfully deleted */
											reportNumUnmatchedTargetFilesDirsDeleted++;
										}
										else if (!error.isEmpty())
										{
											Sync.reportWarning("Failed to delete unmatched target " +
													(u.isDirectory ? "directory" : "file") + " \"" +
													SyncIO.trimTrailingSeparator(u.file.getPath()) +
													(u.isDirectory ? File.separatorChar : "") +
													"\":\n" + error);
										}
									}
								}
							}
							else
							{
								/* file/directory does not exist anymore */
								SyncIO.printFlush("\"" + u.name + "\" does not exist anymore");
							}
						}
					}
				}
			}

			/************************************
			 * (11) RECURSE INTO SUBDIRECTORIES *
			 ************************************/

			/* push subdirectory-pair onto time-stack for subsequent time-sync */
			timeStack.push(new FilePair(sourceDir, targetDir));
			contentStack.push(marker); // special marker

			if (!Sync.noRecurse)
			{
				for (int i = sDirs.size() - 1; i >= 0; i--)
				{
					/* source subdirectory */
					final File sDir = sDirs.get(i).file;

					/* corresponding target subdirectory */
					final File tDir = new File(targetDir, sDir.getName());

					/* push subdirectory-pair onto content-stack for subsequent content-sync */
					contentStack.push(new FilePair(sDir, tDir));
				}
			}
		}

		/*********************
		 * REPORT STATISTICS *
		 *********************/

		final StringBuilder report = new StringBuilder();
		report.append("\n\nSYNCHRONIZATION REPORT");

		if (Sync.reportNumWarnings > 0)
			report.append("\n " + Sync.reportNumWarnings + ((Sync.reportNumWarnings == 1) ? " warning" : " warnings") + " encountered.");

		report.append(
				"\n No. of source subdirectories scanned          : " + reportNumSourceDirsScanned +
				"\n No. of source files scanned                   : " + reportNumSourceFilesScanned +
				"\n No. of target files scanned                   : " + reportNumTargetFilesScanned +
				"\n No. of source files matched [M]               : " + reportNumSourceFilesMatched);

		if (reportNumSyncTime > 0)
			report.append("\n No. of successful time-sync operations [T]    : " +
					reportNumSyncTimeSuccess + " of " + reportNumSyncTime);

		if (reportNumRenameOperations > 0)
			report.append("\n No. of successful file rename operations [R]  : " +
					reportNumRenameOperationsSuccess + " of " + reportNumRenameOperations);

		report.append(
				"\n No. of unmatched source files [C]             : " + reportNumUnmatchedSourceFiles +
					" (" + reportNumUnmatchedSourceFilesCopied + " copied)" +
				"\n No. of unmatched target files/directories [D] : " + reportNumUnmatchedTargetFilesDirs +
					" (" + reportNumUnmatchedTargetFilesDirsDeleted + " deleted)");

		SyncIO.print(report.toString());
	}


	/**
	 * Perform FILE synchronization from Sync.source to Sync.target.
	 * The source must be an existing file; the target must be a file if it exists.
	 */
	private static void syncFile()
	{
		final StringBuilder s = new StringBuilder();

		s.append("\n\nFILE SYNCHRONIZATION");

		if (Sync.simulateOnly)
			s.append(" (SIMULATION MODE)");

		/* display log file, if any */
		if (Sync.log != null)
			s.append("\n\nLog file: \"" + Sync.logName + "\"");

		/* display source and target directories */
		s.append("\n\nSource file: \"" + Sync.sourceName + "\"" +
				"\nTarget file: \"" + Sync.targetName + "\"\n");

		/* display file-matching attributes */
		s.append("\nFile-matching attributes: " + Sync.matchNstcString);

		if (Sync.matchTimeTolerance > 0L)
			s.append(",\n with " + Sync.matchTimeTolerance + "-millisecond time-tolerance");

		SyncIO.printFlush(s.toString());

		/* validate source and target */
		if (Sync.source.equals(Sync.target))
			throw new TerminatingException("The source file \"" + Sync.sourceName +
					"\" cannot be the same as the target file \"" + Sync.targetName + "\".");

		/* source and target files */
		final FileUnit sourceFile = new FileUnit(Sync.source);
		final FileUnit targetFile = Sync.target.exists() ? new FileUnit(Sync.target) : null;

		if (targetFile == null)
		{
			/***************************************************
			 * (1) COPY SOURCE FILE TO NONEXISTENT TARGET FILE *
			 ***************************************************/

			/* target file does not exist; proceed to copy source to target */
			SyncIO.printFlush("\n\nTarget file does not exist\n\nCopying \"" +
					Sync.source.getPath() + "\"\n  --->  \"" +
					Sync.target.getPath() + "\"");

			if (!Sync.simulateOnly)
			{
				final String error = SyncIO.copyFile(Sync.source, Sync.target);

				if (error == null)
				{
					/* file was successfully copied */
					SyncIO.printFlush("\n\n1 file copied.");
				}
				else if (!error.isEmpty())
				{
					Sync.reportWarning("Failed to copy source file \"" +
							Sync.source.getPath() + "\" ---> \"" +
							Sync.target.getPath() + "\":\n" + error);
				}
			}
		}
		else if (Sync.matchFileUnitComparator.compare(sourceFile, targetFile) != 0)
		{
			/***********************************************************
			 * (2) COPY UNMATCHED SOURCE FILE TO EXISTING TARGET FILE  *
			 ***********************************************************/

			/* source and target files do not match; proceed to copy source to target */
			SyncIO.printFlush("\n\nSource and target files do not match\n\nCopying \"" +
					Sync.source.getPath() + "\"\n  --->  \"" +
					Sync.target.getPath() + "\"");

			if (!Sync.simulateOnly)
			{
				final String error = SyncIO.copyFile(Sync.source, Sync.target);

				if (error == null)
				{
					/* file was successfully copied */
					SyncIO.printFlush("\n\n1 file copied.");
				}
				else if (!error.isEmpty())
				{
					Sync.reportWarning("Failed to copy unmatched source file \"" +
							Sync.source.getPath() + "\" ---> \"" +
							Sync.target.getPath() + "\":\n" + error);
				}
			}
		}
		else
		{
			/*************************************
			 * (3) SOURCE AND TARGET FILES MATCH *
			 *************************************/

			SyncIO.printFlush("\n\nSource and target files have the same ");

			targetFile.sameName = sourceFile.name.equals(targetFile.name);
			targetFile.sameSize = (sourceFile.size == targetFile.size);
			targetFile.sameTime = (sourceFile.time == targetFile.time);
			targetFile.sameCrc  = Sync.matchCrc ? (sourceFile.getCrc() == targetFile.getCrc()) : false;

			/* display matched source-target pair and matching attributes */
			SyncIO.printFlush(getNstcString(
					targetFile.sameName, targetFile.sameSize, targetFile.sameTime, targetFile.sameCrc));

			/******************************************
			 * (3.1) SYNC TIME OF MATCHED TARGET FILE *
			 ******************************************/

			if (!targetFile.sameTime)
			{
				boolean syncTime = false;

				if (Sync.defaultActionOnTimeSyncMatched == 'Y')
				{
					SyncIO.printFlush("\n\n Synchronizing last-modified time of matched target file\n  " +
							targetFile.getTimeString() + " ---> " + sourceFile.getTimeString());
					syncTime = true;
				}
				else if (Sync.defaultActionOnTimeSyncMatched == 'N')
				{
					SyncIO.printFlush("\n\n Skipping last-modified time synchronization of matched target file");
				}
				else if (Sync.defaultActionOnTimeSyncMatched == '\0')
				{
					SyncIO.print("\n\n Synchronize last-modified time of matched target file\n  " +
							targetFile.getTimeString() + " ---> " + sourceFile.getTimeString() + "?\n");

					final char choice = SyncIO.userCharPrompt(
							"  (Y)es/(N)o: ",
							"YN");

					if (choice == 'Y')
						syncTime = true;
				}

				if (syncTime)
				{
					if (!Sync.simulateOnly)
					{
						final String error = SyncIO.setFileTime(Sync.target, sourceFile.time);

						if ((error != null) && !error.isEmpty())
							reportWarning("Failed to set last-modified time of matched target file \"" +
									Sync.target.getPath() + "\":\n " +
									targetFile.getTimeString() + " ---> " + sourceFile.getTimeString() +
									":\n" + error);
					}
				}
			}

			/************************************
			 * (3.2) RENAME MATCHED TARGET FILE *
			 ************************************/

			if (!targetFile.sameName)
			{
				boolean syncName = false;

				if (Sync.defaultActionOnRenameMatched == 'Y')
				{
					SyncIO.printFlush("\n\n Renaming matched target file\n  \"" +
							targetFile.name + "\" ---> \"" + sourceFile.name + "\"");
					syncName = true;
				}
				else if (Sync.defaultActionOnRenameMatched == 'N')
				{
					SyncIO.printFlush("\n\n Skipping renaming of matched target file");
				}
				else if (Sync.defaultActionOnRenameMatched == '\0')
				{
					SyncIO.print("\n\n Rename matched target file\n  \"" +
							targetFile.name + "\" ---> \"" + sourceFile.name + "\"?\n");

					final char choice = SyncIO.userCharPrompt(
							"  (Y)es/(N)o: ",
							"YN");

					if (choice == 'Y')
						syncName = true;
				}

				if (syncName)
				{
					if (!Sync.simulateOnly)
					{
						final File newTarget = new File(Sync.target.getParentFile(), sourceFile.name);

						final String error = SyncIO.renameFile(Sync.target, newTarget);

						if ((error != null) && !error.isEmpty())
							reportWarning("Failed to rename matched target file \"" +
										Sync.target.getPath() + "\" ---> \"" +
										newTarget.getPath() + "\":\n" + error);
					}
				}
			}
		}

		/*********************
		 * REPORT STATISTICS *
		 *********************/

		if (Sync.reportNumWarnings > 0)
			SyncIO.print("\n\n" + Sync.reportNumWarnings + ((Sync.reportNumWarnings == 1) ? " warning" : " warnings") + " encountered.");
	}


	/**
	 * Perform source-target file-matching.
	 *
	 * @param sFiles
	 *     Source files to be matched
	 * @param tFiles
	 *     Target files (candidate matches)
	 * @return
	 *     True if matching is unique; false otherwise
	 */
	private static boolean performSourceTargetFileMatching(
			final List<FileUnit> sFiles,
			final List<FileUnit> tFiles)
	{
		/* return value */
		boolean uniqueMatching = true;

		/* no source files to be matched? */
		if (sFiles.isEmpty())
			return uniqueMatching;

		/* sort target files by "search" attributes for file-matching */
		Collections.sort(tFiles, Sync.searchFileUnitComparator);

		/* for each source file, find a matching target file */
		MatchNextSourceFile:
		for (FileUnit s : sFiles)
		{
			final int i = Collections.binarySearch(tFiles, s, Sync.searchFileUnitComparator);

			/* no candidate match found */
			if (i < 0)
				continue MatchNextSourceFile;

			/* candidate match found; proceed to find a valid matching target file */
			int matchIndex = -1;

			MatchNextTargetFileUp:
			for (int j = i - 1; j >= 0; j--)
			{
				final FileUnit t = tFiles.get(j);

				if (Sync.searchFileUnitComparator.compare(s, t) != 0)
					break MatchNextTargetFileUp;

				/* proceed to match last-modified time and CRC-32 checksum, if necessary */
				if ((Sync.matchTime && (Math.abs(s.time - t.time) > Sync.matchTimeTolerance)) ||
						((Sync.matchCrc && (s.getCrc() != t.getCrc()))))
				{
					continue MatchNextTargetFileUp;
				}

				if ((matchIndex >= 0) || (t.match != null))
				{
					/* the source or target file has already been matched */
					uniqueMatching = false;
					continue MatchNextTargetFileUp;
				}

				/* valid matching target file found */
				matchIndex = j;
			}

			MatchNextTargetFileDown:
			for (int j = i; j < tFiles.size(); j++)
			{
				final FileUnit t = tFiles.get(j);

				if (Sync.searchFileUnitComparator.compare(s, t) != 0)
					break MatchNextTargetFileDown;

				/* proceed to match last-modified time and CRC-32 checksum, if necessary */
				if ((Sync.matchTime && (Math.abs(s.time - t.time) > Sync.matchTimeTolerance)) ||
						((Sync.matchCrc && (s.getCrc() != t.getCrc()))))
				{
					continue MatchNextTargetFileDown;
				}

				if ((matchIndex >= 0) || (t.match != null))
				{
					/* the source or target file has already been matched */
					uniqueMatching = false;
					continue MatchNextTargetFileDown;
				}

				/* valid matching target file found */
				matchIndex = j;
			}

			if (matchIndex >= 0)
			{
				/* valid matching target file found */
				final FileUnit t = tFiles.get(matchIndex);
				s.match = t;
				t.match = s;

				/* file-matching attributes */
				s.sameName = s.name.equals(t.name);
				s.sameSize = (s.size == t.size);
				s.sameTime = (s.time == t.time);
				s.sameCrc  = Sync.matchCrc ? (s.getCrc() == t.getCrc()) : false;

				t.sameName = s.sameName;
				t.sameSize = s.sameSize;
				t.sameTime = s.sameTime;
				t.sameCrc  = s.sameCrc;
			}
		}

		return uniqueMatching;
	}


	/**
	 * Perform source-target directory-matching (matching by name only).
	 *
	 * @param sDirs
	 *     Source subdirectories to be matched
	 * @param tDirs
	 *     Target subdirectories (candidate matches)
	 */
	private static void performSourceTargetDirMatching(
			final List<FileUnit> sDirs,
			final List<FileUnit> tDirs)
	{
		/* no source subdirectories to be matched? */
		if (sDirs.isEmpty())
			return;

		/* sort target directories by name only for directory-matching */
		Collections.sort(tDirs, Sync.nameOnlyFileUnitComparator);

		/* for each source subdirectory, find a matching target subdirectory */
		for (FileUnit s : sDirs)
		{
			final int i = Collections.binarySearch(tDirs, s, Sync.nameOnlyFileUnitComparator);

			if (i >= 0)
			{
				/* valid matching target directory found */
				final FileUnit t = tDirs.get(i);
				s.match = t;
				t.match = s;
			}
		}
	}


	/**
	 * Determine sequence of actual rename operations to be performed, in order
	 * to effect the desired rename operations.
	 *
	 * @param renamePairs
	 *     Desired rename operations
	 * @return
	 *     Sequence of actual rename operations to be performed
	 */
	private static List<FilePair> getRenameOperations(
			final List<FilePair> renamePairs)
	{
		/* determine target files, check validity, and detect clashes */
		final Map<File,FilePair> targetMap = new TreeMap<File,FilePair>();

		for (FilePair p : renamePairs)
		{
			/* check for clash (i.e. nonunique target filenames) */
			final FilePair q = targetMap.get(p.target);

			if (q == null)
			{
				targetMap.put(p.target, p);
			}
			else
			{
				throw new TerminatingException("(INTERNAL) Target filename clash:\n" +
						"[1] \"" + q.source.getPath() + "\"\n  ---> \"" + q.target.getPath() + "\"\n" +
						"[2] \"" + p.source.getPath() + "\"\n  ---> \"" + p.target.getPath() + "\"");
			}
		}

		/* determine actual renaming sequence */
		final Map<File,LinkedList<FilePair>> sequenceHeads = new TreeMap<File,LinkedList<FilePair>>();
		final Map<File,LinkedList<FilePair>> sequenceTails = new TreeMap<File,LinkedList<FilePair>>();

		for (FilePair p : renamePairs)
		{
			/* look for a sequence head with source = this target */
			final LinkedList<FilePair> headSequence = sequenceHeads.get(p.target);

			/* look for a sequence tail with target = this source */
			final LinkedList<FilePair> tailSequence = sequenceTails.get(p.source);

			if ((headSequence == null) && (tailSequence == null))
			{
				/* add this file rename pair as a new sequence */
				final LinkedList<FilePair> s = new LinkedList<FilePair>();
				s.add(p);
				sequenceHeads.put(p.source, s);
				sequenceTails.put(p.target, s);
			}
			else if ((headSequence != null) && (tailSequence == null))
			{
				/* add this pair to the head of an existing sequence */
				headSequence.addFirst(p);
				sequenceHeads.remove(p.target);
				sequenceHeads.put(p.source, headSequence);
			}
			else if ((headSequence == null) && (tailSequence != null))
			{
				/* add this pair to the tail of an existing sequence */
				tailSequence.addLast(p);
				sequenceTails.remove(p.source);
				sequenceTails.put(p.target, tailSequence);
			}
			else if ((headSequence != null) && (tailSequence != null))
			{
				if (headSequence == tailSequence)
				{
					/* loop detected, so we use a temporary target file/directory name */

					/* create a temporary file/directory name */
					File temp = new File(p.target.getParentFile(),
								p.target.getName() + ".sync");

					if (temp.exists() || targetMap.containsKey(temp))
					{
						for (long i = 0; i < Long.MAX_VALUE; i++)
						{
							temp = new File(p.target.getParentFile(),
									p.target.getName() + ".sync." + i);

							if (temp.exists() || targetMap.containsKey(temp))
							{
								temp = null;
							}
							else
							{
								/* use this unused name */
								break;
							}
						}
					}

					if (temp == null)
						throw new TerminatingException("Ran out of suffixes for temporary name of file \"" +
								p.target.getPath() + "\".");

					/* use this unused filename */
					targetMap.put(temp, null);

					/* add a leading and trailing rename file pair to the existing sequence */
					final FilePair tempTail = new FilePair(p.source, temp);
					final FilePair tempHead = new FilePair(temp, p.target);

					headSequence.addFirst(tempHead);
					tailSequence.addLast(tempTail);

					sequenceHeads.remove(p.target);
					sequenceHeads.put(temp, headSequence);
					sequenceTails.remove(p.source);
					sequenceTails.put(temp, tailSequence);
				}
				else
				{
					/* link two distinct sequences together */
					tailSequence.addLast(p);
					tailSequence.addAll(headSequence);

					sequenceHeads.remove(p.target);
					sequenceTails.remove(p.source);
					sequenceTails.put(tailSequence.peekLast().target, tailSequence);
				}
			}
		}

		/* prepare return value */
		final List<FilePair> renameOperations = new ArrayList<FilePair>();

		for (LinkedList<FilePair> s : sequenceHeads.values())
		{
			/* get reversed order of rename file pairs within the sequence */
			Collections.reverse(s);
			renameOperations.addAll(s);
		}

		return renameOperations;
	}


	/**
	 * Return string representation of match attributes, e.g. "(name,size,time,crc)".
	 */
	private static String getNstcString(
			final boolean n,
			final boolean s,
			final boolean t,
			final boolean c)
	{
		final StringBuilder a = new StringBuilder();

		a.append('(');

		if (n) a.append("name,");
		if (s) a.append("size,");
		if (t) a.append("time,");
		if (c) a.append("crc,");

		a.deleteCharAt(a.length() - 1);
		a.append(')');

		return a.toString();
	}


	/**
	 * Print a warning message and pause.
	 *
	 * @param message
	 *     Warning message to be printed on issuing the warning
	 */
	static void reportWarning(
			final Object message)
	{
		Sync.reportNumWarnings++;

		if (Sync.ignoreWarnings)
		{
			SyncIO.printToErr("\n\nWARNING: " + message + "\n");
		}
		else
		{
			SyncIO.printToErr("\n\nWARNING: " + message + "\nPress ENTER to continue...");

			(new Scanner(System.in)).nextLine(); // blocks until user responds
		}
	}


	/**
	 * Get custom exception message string for the given exception.
	 * Message contains the exception class name, error description string,
	 * and stack trace.
	 *
	 * @param e
	 *     Exception for which to generate the custom message string
	 */
	static String getExceptionMessage(
			final Exception e)
	{
		final StringBuilder s = new StringBuilder();

		s.append("\nJava exception information (" + e.getClass() +
				"):\n\"" + e.getMessage() + "\"");

		for (StackTraceElement t : e.getStackTrace())
		{
			s.append("\n  at ");
			s.append(t.toString());
		}

		s.append('\n');
		return s.toString();
	}


	/**
	 * Print usage documentation.
	 */
	private static void printUsage()
	{
		/* RULER   00000000011111111112222222222333333333344444444445555555555666666666677777777778 */
		/* RULER   12345678901234567890123456789012345678901234567890123456789012345678901234567890 */
		SyncIO.print("\n" +
				"\nSync performs one-way directory or file synchronization." +
				"\n" +
				"\nUSAGE:  java -jar Sync.jar  <switches>  [\"Source\"]  [\"Target\"]" +
				"\n" +
				"\nSynchronize [\"Target\"] to match [\"Source\"]. Only [\"Target\"] is modified." +
				"\nBy default, the filename, size, last-modified time, and CRC-32 checksum" +
				"\nare used for file-matching. The synchronization mode depends on [\"Source\"]:" +
				"\n" +
				"\n [\"Source\"] is a DIRECTORY: Match source and target directories recursively." +
				"\n  Matched target files are time-synced and renamed if necessary," +
				"\n  unmatched source files are copied to the target directory, and" +
				"\n  unmatched target files/directories are deleted." +
				"\n" +
				"\n [\"Source\"] is a FILE: Match source and target files, ignoring filename." +
				"\n  If files match, then the target file is time-synced and renamed if necessary." +
				"\n  If target file does not exist, then the source file is copied to the target." +
				"\n" +
				"\n<Switches>:" +
				"\n" +
				"\n -s, --simulate        Simulate only; do not modify target" +
				"\n     --ignorewarnings  Ignore warnings; do not pause" +
				"\n -l, --log:<\"x\">       Create log file x; if x is not specified," +
				"\n                        \"sync.yyyyMMdd-HHmmss.log\" is used" +
				"\n -r, --norecurse       Do not recurse into subdirectories" +
				"\n" +
				"\n -n, --noname          Do not use filename for file-matching" +
				"\n -t, --notime          Do not use last-modified time for file-matching" +
				"\n -c, --nocrc           Do not use CRC-32 checksum for file-matching" +
				"\n" +
				"\n     --time:[x]        Use a x-millisecond time-tolerance for file-matching" +
				"\n                        (0-millisecond time-tolerance is used by default;" +
				"\n                         use --time:1000 or more to avoid mismatches across" +
				"\n                         different file systems)" +
				"\n" +
				"\n     --rename:[y|n]    Always[y]/never[n] rename matched target files" +
				"\n     --synctime:[y|n]  ... synchronize time of matched target files" +
				"\n     --overwrite:[y|n] ... overwrite existing target files/directories" +
				"\n     --delete:[y|n]    ... delete unmatched target files/directories" +
				"\n     --force           Equivalent to the combination:" +
				"\n                        --rename:y --synctime:y --overwrite:y --delete:y" +
				"\n" +
				"\n A subset of source and/or target files/directories can be selected for" +
				"\n synchronization using GLOB (or REGEX) filename filters. A file/directory is" +
				"\n selected if it matches any of the \"include\" filters and none of the \"exclude\"" +
				"\n filters." +
				"\n" +
				"\n -i,  --include:[\"x\"]   Include source and target files/directories with names" +
				"\n                         matching GLOB expression x" +
				"\n -x,  --exclude:[\"x\"]   Exclude source and target files/directories with names" +
				"\n                         matching GLOB expression x" +
				"\n -is, --includesource:[\"x\"]   Include source files/directories ..." +
				"\n -xs, --excludesource:[\"x\"]   Exclude source files/directories ..." +
				"\n -it, --includetarget:[\"x\"]   Include target files/directories ..." +
				"\n -xt, --excludetarget:[\"x\"]   Exclude target files/directories ..." +
				"\n -p,  --path            Filter relative pathnames instead of filenames" +
				"\n                         (e.g. \"work\\report\\jan.txt\" instead of \"jan.txt\")" +
				"\n -w,  --lower           Use lower case names for filtering" +
				"\n                         (e.g. \"HelloWorld2007.JPG\" ---> \"helloworld2007.jpg\")" +
				"\n      --regex           Use REGEX instead of GLOB filename filters" +
				"\n                         (see Java API for REGEX syntax)" +
				"\n" +
				"\n      GLOB syntax:" +
				"\n       *    Match a string of 0 or more characters" +
				"\n       ?    Match exactly 1 character" +
				"\n      [ ]   Match exactly 1 character inside the brackets:" +
				"\n             [abc]       match a, b, or c" +
				"\n             [!abc]      match any character except a, b, or c (negation)" +
				"\n             [a-z0-9]    match any character a through z, or 0 through 9," +
				"\n                          inclusive (range)" +
				"\n      { }   Match exactly 1 comma-delimited string inside the braces:" +
				"\n             {a,bc,def}  match either a, bc, or def" +
				"\n" +
				"\n      To use a construct symbol (e.g. [, {, ?) as a literal character," +
				"\n      insert a backslash before it, e.g. use \\[ for the literal character [." +
				"\n      Use \\\\ for the literal backslash character \\." +
				"\n      The file separator in Windows can be specified by \\\\ or /." +
				"\n" +
				"\nEXAMPLES:" +
				"\n" +
				"\n 1. Synchronize target \"C:\\Backup\" to look like source \"C:\\Original\"," +
				"\n     matching files by (name,size,time,crc):" +
				"\n    java -jar Sync.jar \"C:\\Original\" \"C:\\Backup\"" +
				"\n" +
				"\n 2. As in example 1, but never delete unmatched target files/directories:" +
				"\n    java -jar Sync.jar --delete:n \"C:\\Original\" \"C:\\Backup\"" +
				"\n" +
				"\n 3. As in example 1, but match files by (name,size,time) with a time-tolerance" +
				"\n     of 2 seconds instead:" +
				"\n    java -jar Sync.jar --nocrc --time:2000 \"C:\\Original\" \"C:\\Backup\"" +
				"\n" +
				"\n 4. As in example 1, but always rename and synchronize time of matched target" +
				"\n     files, overwrite existing target files, and delete unmatched target" +
				"\n     files/directories:" +
				"\n    java -jar Sync.jar --force \"C:\\Original\" \"C:\\Backup\"" +
				"\n" +
				"\n 5. As in example 1, but synchronize only jpg and html files:" +
				"\n    java -jar Sync.jar --include:\"*.{jpg,html}\" \"C:\\Original\" \"C:\\Backup\"" +
				"\n" +
				"\n 6. As in example 5, but skip files that begin with a tilde '~':" +
				"\n    java -jar Sync.jar --include:\"*.{jpg,html}\" --exclude:\"~*\"" +
				"\n     \"C:\\Original\" \"C:\\Backup\"" +
				"\n\n");
	}
}
