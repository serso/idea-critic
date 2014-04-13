package org.solovyev.idea.critic.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.connection.CriticManager;
import org.solovyev.idea.critic.model.CriticFilter;
import org.solovyev.idea.critic.model.Review;
import org.solovyev.idea.critic.ui.toolWindow.CriticPanel;
import org.solovyev.idea.critic.utils.CriticBundle;

@SuppressWarnings("ComponentNotRegistered")
public class CompleteReviewAction extends AnActionButton implements DumbAware {

	private final Review myReview;

	public CompleteReviewAction(@NotNull final Review review, @NotNull final String description) {
		super(description, description, IconLoader.getIcon("/images/complete.png"));
		myReview = review;
	}

	public void actionPerformed(AnActionEvent e) {
		final Project project = e.getData(PlatformDataKeys.PROJECT);
		if (project == null) return;
		CriticManager.getInstance(project).completeReview(myReview.getPermaId());
		final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(CriticBundle.message("critic.toolwindow.id"));
		final ContentManager contentManager = toolWindow.getContentManager();
		final Content foundContent = contentManager.findContent("Details for " + myReview.getPermaId());
		contentManager.removeContent(foundContent, true);

		final Content dash = contentManager.findContent("Dashboard");
		if (dash.getComponent() instanceof CriticPanel) {
			((CriticPanel) dash.getComponent()).getReviewModel().updateModel(CriticFilter.ToReview);
		}
	}
}
