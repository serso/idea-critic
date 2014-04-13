package org.solovyev.idea.critic.ui.toolWindow;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.connection.CriticManager;
import org.solovyev.idea.critic.model.BasicReview;
import org.solovyev.idea.critic.model.CriticFilter;
import org.solovyev.idea.critic.utils.CriticBundle;

import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.List;

public class CriticReviewModel extends DefaultTableModel {
	private final Project myProject;

	public CriticReviewModel(Project project) {
		myProject = project;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 4) return Date.class;
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return CriticBundle.message("critic.id");
			case 1:
				return CriticBundle.message("critic.description");
			case 2:
				return CriticBundle.message("critic.state");
			case 3:
				return CriticBundle.message("critic.author");
			case 4:
				return CriticBundle.message("critic.date");
		}
		return super.getColumnName(column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void updateModel(@NotNull final CriticFilter filter) {
		setRowCount(0);
		final CriticManager manager = CriticManager.getInstance(myProject);
		final List<BasicReview> reviews;
		reviews = manager.getReviewsForFilter(filter);
		if (reviews != null) {
			for (BasicReview review : reviews) {
				addRow(new Object[]{review.getPermaId(), review.getDescription(), review.getState(),
						review.getAuthor().getUserName(), review.getCreateDate()});

			}
		}
	}
}
