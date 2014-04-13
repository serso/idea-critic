package org.solovyev.idea.critic.ui.toolWindow.details;

import org.solovyev.idea.critic.model.Comment;

import javax.swing.tree.DefaultMutableTreeNode;

class CommentTreeNode extends DefaultMutableTreeNode {

	public Comment getComment() {
		return myComment;
	}

	private final Comment myComment;

	public CommentTreeNode(Comment comment) {
		myComment = comment;
	}
}
