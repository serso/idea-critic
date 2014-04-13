package org.solovyev.idea.critic.ui.toolWindow.details;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.issueLinks.LinkMouseListenerBase;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.actions.AddCommentAction;
import org.solovyev.idea.critic.model.Comment;
import org.solovyev.idea.critic.model.Review;
import org.solovyev.idea.critic.utils.CriticBundle;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class CommentsTree extends Tree {

	private static final Logger LOG = Logger.getInstance(CommentsTree.class);
	@NotNull
	protected final Review myReview;

	protected CommentsTree(@NotNull final Project project, @NotNull final Review review, @NotNull DefaultTreeModel model,
						   @Nullable final Editor editor, @Nullable final FilePath filePath) {
		super(model);
		myReview = review;
		setExpandableItemsEnabled(false);
		setRowHeight(0);
		final CommentNodeRenderer renderer = new CommentNodeRenderer(this, review, project);
		setCellRenderer(renderer);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		final DefaultActionGroup group = new DefaultActionGroup();
		final AddCommentAction replyToComment = new AddCommentAction(review, editor, filePath,
				CriticBundle.message("critic.add.reply"), true);
		replyToComment.setContextComponent(this);
		group.add(replyToComment);
		PopupHandler.installUnknownPopupHandler(this, group, ActionManager.getInstance());
		TreeUtil.expandAll(this);

		new MyLinkMouseListener(renderer, project, review).installOn(this);
	}

	protected static void addReplies(@NotNull Comment comment, @NotNull DefaultMutableTreeNode parentNode) {
		for (Comment reply : comment.getReplies()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(reply);
			parentNode.add(node);
			addReplies(reply, node);
		}
	}

	@Nullable
	public Comment getSelectedComment() {
		Object selected = getLastSelectedPathComponent();
		if (selected == null) {
			return null;
		}
		assert selected instanceof DefaultMutableTreeNode;
		Object userObject = ((DefaultMutableTreeNode) selected).getUserObject();
		assert userObject instanceof Comment;
		return (Comment) userObject;
	}

	public abstract void refresh();

	private class MyLinkMouseListener extends LinkMouseListenerBase {
		private final CommentNodeRenderer myRenderer;
		private final Project myProject;
		private final Review myReview;

		public MyLinkMouseListener(CommentNodeRenderer renderer, Project project, Review review) {
			myRenderer = renderer;
			myProject = project;
			myReview = review;
		}

		@Override
		protected void handleTagClick(Object tag, MouseEvent event) {
			if (tag == null) {
				return;
			}
			CommentAction action = (CommentAction) tag;
			action.execute(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});
		}

		@Nullable
		@Override
		protected Object getTagAt(MouseEvent e) {
			JTree tree = (JTree) e.getSource();
			final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			if (path == null) {
				return null;
			}
			Rectangle bounds = tree.getPathBounds(path);
			if (bounds == null) {
				return null;
			}
			int dx = e.getX() - bounds.x;
			int dy = e.getY() - bounds.y;

			CommentAction.Type linkType = myRenderer.getActionLink(dx, dy);
			if (linkType == null) {
				return null;
			}
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
			Comment comment = (Comment) treeNode.getUserObject();

			return linkType.createAction(myProject, myReview, comment);
		}
	}
}
