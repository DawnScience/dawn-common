package org.dawb.common.ui.viewers;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * This class assumes that the root is the content and that the 
 * children of the root are the top level to be shown.
 * 
 * @author fcp94556
 *
 */
public class TreeNodeContentProvider implements ITreeContentProvider {

	protected Viewer   viewer;
	protected TreeNode rootNode;
	private TreeNode limit;
	
	public TreeNodeContentProvider() {
		this(null);
	}
	public TreeNodeContentProvider(TreeNode limit) {
		this.limit = limit;
	}

	@Override
	public void dispose() {
		this.viewer   = null;
        this.rootNode = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer   = viewer;
        this.rootNode = (TreeNode)newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		
		final TreeNode node = (TreeNode)parentElement;
		if (limit!=null && node.getParent()==limit) return null;
		
		if (node.getChildCount()<1) return null;
		
		final Object[] oa   = new Object[node.getChildCount()];
		for (int i = 0; i < node.getChildCount(); i++) {
			oa[i] = node.getChildAt(i);
		}
		return oa;
	}

	@Override
	public Object getParent(Object element) {		
		final TreeNode object = (TreeNode)element;
		return object.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		final TreeNode node = (TreeNode)element;
		if (limit!=null && node.getParent()==limit) return false;
		return node.getChildCount()>0;
	}

	public Viewer getViewer() {
		return viewer;
	}
	
	public TreeNode getRoot() {
		return rootNode;
	}
}
