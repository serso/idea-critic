package org.solovyev.idea.critic.ui.toolWindow;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NotNull;

public class CriticTreeStructure extends SimpleTreeStructure {
	private final SimpleNode myRootElement;

	public CriticTreeStructure(@NotNull final SimpleNode root) {
		super();
		myRootElement = root;
	}

	public SimpleNode getRootElement() {
		return myRootElement;
	}
}
