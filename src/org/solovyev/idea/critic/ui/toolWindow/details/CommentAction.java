package org.solovyev.idea.critic.ui.toolWindow.details;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.model.Comment;
import org.solovyev.idea.critic.model.Review;

public abstract class CommentAction {

	@NotNull
	protected final Project myProject;
	@NotNull
	protected final Review myReview;
	@NotNull
	protected final Comment myComment;
	@NotNull
	private final Type myType;

	enum Type {
		PUBLISH {
			@Override
			<T extends CommentAction> T createAction(@NotNull Project project, @NotNull Review review, @NotNull Comment comment) {
				return (T) new PublishCommentAction(project, review, comment);
			}
		};

		abstract <T extends CommentAction> T createAction(@NotNull Project project, @NotNull Review review, @NotNull Comment comment);
	}

	protected CommentAction(@NotNull Project project, @NotNull Review review, @NotNull Comment comment, @NotNull Type actionType) {
		myProject = project;
		myReview = review;
		myComment = comment;
		myType = actionType;
	}

	public abstract void execute(Runnable runnable);

}
