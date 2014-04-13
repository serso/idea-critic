package org.solovyev.idea.critic.ui.toolWindow;

import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.solovyev.idea.critic.ui.toolWindow.diff.CommentsDiffTool;
import org.solovyev.idea.critic.utils.CriticBundle;

public class CriticToolWindowFactory implements ToolWindowFactory, DumbAware {

	@Override
	public void createToolWindowContent(Project project, ToolWindow toolWindow) {
		DiffManager.getInstance().registerDiffTool(new CommentsDiffTool());
		final ContentManager contentManager = toolWindow.getContentManager();
		final CriticPanel criticPanel = new CriticPanel(project);
		final Content content = ContentFactory.SERVICE.getInstance().
				createContent(criticPanel, CriticBundle.message("critic.main.name"), false);
		content.setCloseable(false);
		contentManager.addContent(content);
	}
}
