package org.dawb.common.ui.wizard;

import java.io.File;

import org.dawb.common.ui.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.ui.content.FileContentProposalProvider;

/**
 * A page with a field for choosing an external file.
 * The file chosen may also be in a project or typed in.
 * @author fcp94556
 *
 */
public class ExternalFileChoosePage extends WizardPage {

	private String path;
	private Text txtPath;
	/**
	 * 
	 * @param pageName
	 * @param description, may be null
	 * @param icon, may be null
	 */
	public ExternalFileChoosePage(String pageName, String description, ImageDescriptor icon) {
		super(pageName, description, icon);
		
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns      = 4;
		layout.verticalSpacing = 9;
		
		createContentBeforeFileChoose(container);
		createFileChooser(container);
		createContentAfterFileChoose(container);
		
		setControl(container);

	}


	/**
	 * 
	 * @param container with a 4-column grid layout
	 */
	protected void createContentBeforeFileChoose(Composite container) {
		
	}
	
	/**
	 * 
	 * @param container with a 4-column grid layout
	 */
	protected void createFileChooser(Composite container) {
		
		Label txtLabel = new Label(container, SWT.NULL);
		txtLabel.setText("&File  ");
		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		this.txtPath = new Text(container, SWT.BORDER);
		txtPath.setEditable(false);
		//txtPath.setEnabled(false);
		
		FileContentProposalProvider prov = new FileContentProposalProvider();
		ContentProposalAdapter ad = new ContentProposalAdapter(txtPath, new TextContentAdapter(), prov, null, null);
		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		txtPath.setToolTipText("File location");
        if (getPath()!=null) txtPath.setText(getPath());
		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtPath.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				pathChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("...");
		button.setImage(Activator.getImageDescriptor("icons/Project-data.png").createImage());
		button.setToolTipText("Browse to file inside a project");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleResourceBrowse();
			}
		});
		
		button = new Button(container, SWT.PUSH);
		button.setText("...");
		button.setImage(Activator.getImageDescriptor("icons/data_folder_link.gif").createImage());
		button.setToolTipText("Browse to an external file.");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleFileBrowse();
			}
		});
		
	}
	
	/**
	 * 
	 * @param container with a 4-column grid layout
	 */
	protected void createContentAfterFileChoose(Composite container) {
		
	}


	protected void handleResourceBrowse() {
		final IFile[] p = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
				           "File location", "Please choose a location to import from.", false,
				            new Object[]{getIResource()}, null);
		if (p!=null && p.length>0) {
			this.path = p[0].getFullPath().toOSString();
		    txtPath.setText(this.path);
			pathChanged();
		}
	}
	
	protected void handleFileBrowse() {
		final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		dialog.setText("Choose file");
		final String filePath = getAbsoluteFilePath();
		if (filePath!=null) {
			final File file = new File(filePath);
			if (file.exists()) {
				if (file.isDirectory()) {
					dialog.setFilterPath(file.getAbsolutePath());
				} else {
					dialog.setFilterPath(file.getParent());
					dialog.setFileName(file.getName());
				}
			}
			
		}
		final String path = dialog.open();
		if (path!=null) {
			setPath(path);
		    txtPath.setText(this.path);
			pathChanged();
		}
	}

	/**
	 * Call to update
	 */
	protected void pathChanged() {
		// TODO Auto-generated method stub
		
	}

	public String getPath() {
		return path;
	}
	
	public String getAbsoluteFilePath() {
		try{
			final IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
			if (res!=null) return res.getLocation().toOSString();
			return getPath();
		} catch (Throwable ignored) {
			return null;
		}
	}

	public void setPath(String path) {
		this.path = path;
		if (txtPath!=null) txtPath.setText(path);
	}
	
	protected IResource getIResource() {
		IResource res = null;
		if (path!=null) {
			res = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
		}
		if (res == null && getPath()!=null) {
			final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
			if (getPath().startsWith(workspace)) {
				String relPath = getPath().substring(workspace.length());
				res = ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
			}
		}
		return res;
	}
}
