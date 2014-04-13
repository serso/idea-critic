package org.solovyev.idea.critic.ui.toolWindow.diff;

import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.impl.DiffPanelImpl;
import com.intellij.openapi.diff.impl.external.DiffManagerImpl;
import com.intellij.openapi.diff.impl.external.FrameDiffTool;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeRequestChain;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.actions.DiffRequestPresentable;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.actions.AddCommentAction;
import org.solovyev.idea.critic.model.Comment;
import org.solovyev.idea.critic.model.Review;
import org.solovyev.idea.critic.utils.CriticBundle;
import org.solovyev.idea.critic.utils.CriticDataKeys;

import java.awt.*;
import java.io.File;
import java.util.List;

public class CommentsDiffTool extends FrameDiffTool {

	@Nullable
	private Review myReview;
	@Nullable
	private Change[] myChanges;

	@Override
	public boolean canShow(DiffRequest request) {
		final boolean canShow = super.canShow(request);
		if (canShow) {
			final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
			final DataContext context = dataContextFromFocus.getResult();
			if (context == null) return false;
			final Review review = CriticDataKeys.REVIEW.getData(context);
			return review != null;
		} else {
			return false;
		}
	}

	@Nullable
	@Override
	protected DiffPanelImpl createDiffPanelImpl(@NotNull DiffRequest request, @Nullable Window window, @NotNull Disposable parentDisposable) {
		final AsyncResult<DataContext> dataContextFromFocus = DataManager.getInstance().getDataContextFromFocus();
		final DataContext context = dataContextFromFocus.getResult();
		if (context == null) return null;
		myReview = CriticDataKeys.REVIEW.getData(context);
		myChanges = VcsDataKeys.SELECTED_CHANGES.getData(context);

		DiffPanelImpl diffPanel = new CommentableDiffPanel(window, request);
		diffPanel.setDiffRequest(request);
		Disposer.register(parentDisposable, diffPanel);
		return diffPanel;
	}

	private static void addCommentAction(@Nullable final Editor editor2,
										 @Nullable final FilePath filePath,
										 @Nullable final Review review) {
		if (editor2 != null && review != null) {
			DefaultActionGroup group = new DefaultActionGroup();
			final AddCommentAction addCommentAction = new AddCommentAction(review, editor2, filePath, CriticBundle.message("critic.add.comment"), false);
			addCommentAction.setContextComponent(editor2.getComponent());
			group.add(addCommentAction);
			PopupHandler.installUnknownPopupHandler(editor2.getContentComponent(), group, ActionManager.getInstance());
		}
	}

	private void addGutter(@NotNull final Review review,
						   @Nullable final ContentRevision revision,
						   Editor editor2, FilePath filePath) {
		final List<Comment> comments = review.getComments();

		for (Comment comment : comments) {
			final String id = comment.getReviewItemId();
			final String path = review.getPathById(id);
			if (revision != null && path != null && filePath.getPath().endsWith(path) &&
					(review.isInPatch(comment) || revision.getRevisionNumber().asString().equals(comment.getRevision()))) {

				final MarkupModel markup = editor2.getMarkupModel();

				final RangeHighlighter highlighter = markup.addLineHighlighter(Integer.parseInt(comment.getLine()) - 1, HighlighterLayer.ERROR + 1, null);
				final ReviewGutterIconRenderer gutterIconRenderer =
						new ReviewGutterIconRenderer(review, filePath, comment);
				highlighter.setGutterIconRenderer(gutterIconRenderer);
			}
		}
	}

	private class CommentableDiffPanel extends DiffPanelImpl {
		public CommentableDiffPanel(Window window, DiffRequest request) {
			super(window, request.getProject(), true, true, DiffManagerImpl.FULL_DIFF_DIVIDER_POLYGONS_OFFSET, CommentsDiffTool.this);
		}

		@Override
		public void setDiffRequest(DiffRequest request) {
			super.setDiffRequest(request);

			Object chain = request.getGenericData().get(VcsDataKeys.DIFF_REQUEST_CHAIN.getName());
			if (chain instanceof ChangeRequestChain) {
				DiffRequestPresentable currentRequest = ((ChangeRequestChain) chain).getCurrentRequest();
				if (currentRequest != null) {
					String path = currentRequest.getPathPresentation();
					FilePath filePath = new FilePathImpl(new File(path), false);
					Editor editor2 = getEditor2();
					addCommentAction(editor2, filePath, myReview);

					if (myChanges != null && myChanges.length == 1 && myReview != null) {
						ContentRevision revision = myChanges[0].getAfterRevision();
						if (revision == null) {
							revision = myChanges[0].getBeforeRevision();
						}
						addGutter(myReview, revision, editor2, filePath);
					}
				}
			}
		}
	}
}
