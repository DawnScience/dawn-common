package org.dawb.common.python;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.core.IInterpreterManager;
import org.python.pydev.core.IPythonNature;
import org.python.pydev.core.IPythonPathNature;
import org.python.pydev.core.MisconfigurationException;
import org.python.pydev.core.PythonNatureWithoutProjectException;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PyDevUtils {
	private static final Logger logger = LoggerFactory
			.getLogger(PyDevUtils.class);

	public static final String PROJECT_INTERPRETER_SUFFIX = "'s PyDev interpreter settings";
	public static final String DEFAULT_INTERPRETER = org.python.pydev.core.IPythonNature.DEFAULT_INTERPRETER;

	public static class AvailableInterpreter {
		public AvailableInterpreter(boolean isDefault, IInterpreterInfo info,
				String displayName, String exeOrJar) {
			this.isDefault = isDefault;
			this.info = info;
			this.displayName = displayName;
			this.exeOrJar = exeOrJar;
		}

		public boolean isDefault;
		public IInterpreterInfo info;
		public String displayName;
		public String exeOrJar;
	}

	/**
	 * Get a list of the available interpreters as user readable strings.
	 * 
	 * @param auto
	 *            Do Auto Configure if no interpreters are setup yet
	 * @param project
	 *            optional project so that the project's configured python can
	 *            be included in the list
	 * @return array of display strings
	 */
	public static String[] getChoices(boolean auto, IProject project) {
		AvailableInterpreter[] infos = PyDevUtils.getInterpreterChoices(
				project, auto, auto);
		String[] choices = new String[infos.length];
		for (int i = 0; i < choices.length; i++) {
			choices[i] = infos[i].displayName + " - ";
			if (infos[i].exeOrJar == null)
				choices[i] += "Python interpreter unset. Configure interpreter and/or set project as PyDev.";
			else
				choices[i] += infos[i].exeOrJar;
		}
		return choices;
	}

	/**
	 * Undo {@link #getChoices(boolean, IProject)} to get the selected
	 * interpreter based on the user selected string.
	 * 
	 * @param choice
	 *            one of values getChoices returned
	 * @param project
	 *            optional project so that the project's configured python can
	 *            be included in the list
	 * @return matching interpreter, or null if no interpreter matched.
	 */
	public static AvailableInterpreter getMatchingChoice(String choice,
			IProject project) {
		if (choice == null)
			return null;
		int dashIndex = choice.indexOf(" - ");
		if (dashIndex < 0)
			return null;
		String choiceDisplayName = choice.substring(0, dashIndex);
		boolean projectDefault = choiceDisplayName
				.endsWith(PyDevUtils.PROJECT_INTERPRETER_SUFFIX);

		AvailableInterpreter[] infos = PyDevUtils.getInterpreterChoices(
				project, false, false);
		for (AvailableInterpreter info : infos) {
			if (choiceDisplayName.equals(info.displayName))
				return info;
			if (projectDefault
					&& info.displayName
							.endsWith(PyDevUtils.PROJECT_INTERPRETER_SUFFIX))
				return info;
		}
		return null;
	}

	/**
	 * Get the list of Python interpreter choices.
	 * 
	 * @return an array of available interpreters. Guaranteed to have 1 entry if
	 *         project == null and 2 if project != null.
	 */
	public static AvailableInterpreter[] getInterpreterChoices(
			IProject project, boolean autoConfig, boolean addNature) {
		List<AvailableInterpreter> list = new ArrayList<AvailableInterpreter>();

		list.add(getDefaultInterpreter(autoConfig));

		IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
		IInterpreterInfo[] interpreterInfos = manager.getInterpreterInfos();
		for (IInterpreterInfo info : interpreterInfos) {
			list.add(new AvailableInterpreter(false, info, info.getName(), info
					.getExecutableOrJar()));
		}

		if (project != null) {
			list.add(0, getProjectInterpreter(project, addNature));
		}

		return list.toArray(new AvailableInterpreter[list.size()]);
	}

	/**
	 * Get the interpreter info for the given project. This is a simplified
	 * wrapper around PyDev's API.
	 * 
	 * @param project
	 *            to query, must be non-null
	 * @param addNature
	 *            add a PyDev nature to the project if there is not one already
	 * @return Guaranteed to return non-null
	 */
	public static AvailableInterpreter getProjectInterpreter(IProject project,
			boolean addNature) {
		IPythonNature pythonNature;
		if (addNature)
			try {
				pythonNature = PythonNature.addNature(project, null, null,
						null, null, null, null);
			} catch (CoreException e1) {
				pythonNature = null;
			}
		else
			pythonNature = PythonNature.getPythonNature(project);

		if (pythonNature != null) {
			// If there is a project, ensure that the root of the project is on
			// the PYTHONPATH, or that some path is already. This is done to
			// ease the flow so that code analysis works on the project.
			try {
				IPythonPathNature pathInfo = pythonNature.getPythonPathNature();
				Set<String> pathSet = pathInfo.getProjectSourcePathSet(true);
				if (pathSet.isEmpty()) {
					SortedSet<String> pathSetSorted = new TreeSet<String>(
							pathSet);
					String projectRoot = project.getFullPath().toString();
					pathSetSorted.add(projectRoot);
					pathInfo.setProjectSourcePath(StringUtils.join("|",
							pathSetSorted.toArray(new String[pathSetSorted.size()])));
					PythonNature.getPythonNature(project).rebuildPath();
				}
			} catch (CoreException e) {
				logger.error(
						"Failed to ensure at least one PYTHONPATH entry existed for project",
						e);
			}
		}

		IInterpreterInfo projectInterpreter = null;
		String exeOrJar = null;
		if (pythonNature != null) {
			try {
				projectInterpreter = pythonNature.getProjectInterpreter();
				if (projectInterpreter != null) {
					exeOrJar = projectInterpreter.getExecutableOrJar();
				}
			} catch (MisconfigurationException e) {
			} catch (PythonNatureWithoutProjectException e) {
			}
		}
		AvailableInterpreter available = new AvailableInterpreter(false,
				projectInterpreter, project.getName()
						+ PROJECT_INTERPRETER_SUFFIX, exeOrJar);
		return available;
	}

	/**
	 * Get the default interpreter info. This is a simplified wrapper around
	 * PyDev's API.
	 * 
	 * @param autoConfig
	 *            Show/run the autoConfig GUI if there is no interpreter info
	 * @return Guaranteed to return non-null
	 */
	public static AvailableInterpreter getDefaultInterpreter(boolean autoConfig) {
		IInterpreterManager manager = PydevPlugin.getPythonInterpreterManager();
		try {
			IInterpreterInfo defaultInterpreterInfo = manager
					.getDefaultInterpreterInfo(autoConfig);
			if (defaultInterpreterInfo != null) {
				return new AvailableInterpreter(true, defaultInterpreterInfo,
						DEFAULT_INTERPRETER,
						defaultInterpreterInfo.getExecutableOrJar());
			}
		} catch (MisconfigurationException e) {
		}

		return new AvailableInterpreter(true, null, DEFAULT_INTERPRETER, null);
	}

}
