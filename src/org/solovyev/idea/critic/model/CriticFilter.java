package org.solovyev.idea.critic.model;

import org.jetbrains.annotations.NotNull;

public enum CriticFilter {
	ToReview("toReview", "To Review"),
	RequireApprovalReview("requireMyApproval", "Review Required My Approval"),
	OutForReview("outForReview", "Out For Review"),
	Closed("closed", "Closed");

	private final String myFilterUrl;
	private final String myFilterName;

	CriticFilter(@NotNull final String filterUrl, @NotNull final String filterName) {
		myFilterUrl = filterUrl;
		myFilterName = filterName;
	}

	@NotNull
	public String getFilterUrl() {
		return myFilterUrl;
	}

	@NotNull
	public String getFilterName() {
		return myFilterName;
	}
}
