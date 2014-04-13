
package org.solovyev.idea.critic.ui.toolWindow.tree;

import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.ui.toolWindow.CriticReviewModel;

import java.util.ArrayList;
import java.util.List;

public class CriticRootNode extends SimpleNode {
	private static final String NAME = "All My Reviews";
	private final CriticReviewModel myReviewModel;
	private final List<SimpleNode> myChildren = new ArrayList<SimpleNode>();

	public CriticRootNode(@NotNull final CriticReviewModel reviewModel) {
		myReviewModel = reviewModel;
		myChildren.add(new CriticToReviewNode(myReviewModel));
		myChildren.add(new CriticRequireApprovalNode(myReviewModel));
		myChildren.add(new CriticOutForReviewNode(myReviewModel));
		myChildren.add(new CriticClosedNode(myReviewModel));
	}

	@NotNull
	public String toString() {
		return NAME;
	}

	@Override
	public SimpleNode[] getChildren() {
		if (myChildren.isEmpty()) {
			myChildren.add(new CriticToReviewNode(myReviewModel));
			myChildren.add(new CriticRequireApprovalNode(myReviewModel));
			myChildren.add(new CriticOutForReviewNode(myReviewModel));
			myChildren.add(new CriticClosedNode(myReviewModel));
		}
		return myChildren.toArray(new SimpleNode[myChildren.size()]);
	}
}
