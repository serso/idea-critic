package org.solovyev.idea.critic.ui.toolWindow.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class CriticTreeModel extends DefaultTreeModel {

	public CriticTreeModel() {
		super(new DefaultMutableTreeNode(), false);
	}
}
