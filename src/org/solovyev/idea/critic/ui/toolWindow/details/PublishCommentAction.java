package org.solovyev.idea.critic.ui.toolWindow.details;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.connection.CriticManager;
import org.solovyev.idea.critic.model.Comment;
import org.solovyev.idea.critic.model.Review;

public class PublishCommentAction extends CommentAction {

	private static final Logger LOG = Logger.getInstance(CommentAction.class);

	public PublishCommentAction(@NotNull Project project, @NotNull Review review, @NotNull Comment comment) {
		super(project, review, comment, Type.PUBLISH);
	}

	@Override
	public void execute(@NotNull Runnable onSuccess) {
		try {
			CriticManager.getInstance(myProject).publishComment(myReview, myComment);
			myComment.setDraft(false);
			onSuccess.run();
		} catch (Exception e) {
			Messages.showErrorDialog(myProject, "Couldn't publish comment: " + e.getMessage(), "Comment Publish Failed");
			LOG.warn(e);
		}
	}

}
