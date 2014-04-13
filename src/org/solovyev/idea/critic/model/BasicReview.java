
package org.solovyev.idea.critic.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class BasicReview {
	private Set<User> myReviewers = new HashSet<User>();
	private final User myAuthor;
	private String myDescription;
	private final User myModerator;

	private final String myPermaId;
	private Date myCreateDate;
	private String myState;

	public BasicReview(@NotNull final String permaId, @NotNull final User author,
					   @Nullable final User moderator) {
		myPermaId = permaId;
		myAuthor = author;
		myModerator = moderator;
	}

	@Override
	public String toString() {
		return myPermaId + "  " + myDescription + "  " + myState + "   " + myAuthor.getUserName();
	}

	public void setDescription(@NotNull final String description) {
		myDescription = description;
	}

	public void setState(@NotNull final String state) {
		myState = state;
	}

	public void addReviewer(@NotNull final User reviewer) {
		myReviewers.add(reviewer);
	}

	@NotNull
	public String getPermaId() {
		return myPermaId;
	}

	@NotNull
	public String getDescription() {
		return myDescription;
	}

	@NotNull
	public String getState() {
		return myState;
	}

	@NotNull
	public User getAuthor() {
		return myAuthor;
	}

	@NotNull
	public Date getCreateDate() {
		return myCreateDate;
	}

	public void setCreateDate(@NotNull final Date createDate) {
		myCreateDate = createDate;
	}

	@NotNull
	public Set<User> getReviewers() {
		return myReviewers;
	}
}