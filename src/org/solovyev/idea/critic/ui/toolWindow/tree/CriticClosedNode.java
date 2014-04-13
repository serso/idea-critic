package org.solovyev.idea.critic.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.model.CriticFilter;
import org.solovyev.idea.critic.ui.toolWindow.CriticReviewModel;

public class CriticClosedNode extends SimpleNode {
	private static final String NAME = "Closed";
	private final CriticReviewModel myReviewModel;

	public CriticClosedNode(@NotNull final CriticReviewModel reviewModel) {
		myReviewModel = reviewModel;
	}

	@NotNull
	public String toString() {
		return NAME;
	}

	@Override
	public SimpleNode[] getChildren() {
		return new SimpleNode[0];
	}

	@Override
	public boolean isAlwaysLeaf() {
		return true;
	}

	@Override
	public void handleSelection(SimpleTree tree) {
		super.handleSelection(tree);
		myReviewModel.updateModel(CriticFilter.Closed);
	}
}
