package org.solovyev.idea.critic.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diff.DiffViewer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.model.Comment;
import org.solovyev.idea.critic.model.Review;
import org.solovyev.idea.critic.ui.toolWindow.details.CommentBalloonBuilder;
import org.solovyev.idea.critic.ui.toolWindow.details.CommentsTree;
import org.solovyev.idea.critic.ui.toolWindow.details.VersionedCommentsTree;

@SuppressWarnings("ComponentNotRegistered")
public class ShowFileCommentsAction extends AnAction implements DumbAware {
	private final Comment myComment;
	private final Review myReview;
	private final FilePath myFilePath;

	public ShowFileCommentsAction(@NotNull final Comment comment, @NotNull final FilePath filePath,
								  @NotNull final Review review) {
		myComment = comment;
		myFilePath = filePath;
		myReview = review;
	}

	public void actionPerformed(final AnActionEvent e) {
		final Project project = e.getData(PlatformDataKeys.PROJECT);
		if (project == null) return;

		final Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
		if (editor == null) return;

		final CommentsTree commentsTree = VersionedCommentsTree.create(project, myReview, myComment, editor, myFilePath, new Runnable() {
			@Override
			public void run() {
				DiffViewer diffViewer = e.getData(PlatformDataKeys.DIFF_VIEWER);
				if (diffViewer == null) {
					return;
				}
				diffViewer.getComponent().repaint();
			}
		});
		CommentBalloonBuilder.showBalloon(commentsTree);
	}
}
