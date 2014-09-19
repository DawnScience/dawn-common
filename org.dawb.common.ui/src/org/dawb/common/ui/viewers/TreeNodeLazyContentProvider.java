/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.viewers;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class TreeNodeLazyContentProvider implements ILazyTreeContentProvider {

	private final TreeViewer tree;

	public TreeNodeLazyContentProvider(TreeViewer tree) {
		this.tree = tree;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateElement(Object parent, int index) {
		
		TreeNode child = ((TreeNode) parent).getChildAt(index);
		tree.replace(parent, index, child);
		updateChildCount(child, -1);
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		int count = 0;
		if (element instanceof TreeNode) {
			count = ((TreeNode)element).getChildCount();
		}
		tree.setChildCount(element, count);
	}

	@Override
	public Object getParent(Object element) {
		if (element == null || !(element instanceof TreeNode)) {
			return null;
		}
		
		final TreeNode object = (TreeNode)element;
		return object.getParent();
	}

}
