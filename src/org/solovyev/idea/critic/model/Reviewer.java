package org.solovyev.idea.critic.model;

import org.jetbrains.annotations.NotNull;

public class Reviewer extends User {

	public Reviewer(@NotNull final String userName) {
		super(userName, null);
	}

	@Override
	public String toString() {
		return "Reviewer [[" + myUserName + "]]";
	}
}