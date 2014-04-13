package org.solovyev.idea.critic.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Comment {

	@NotNull
	private final User myAuthor;
	@NotNull
	private final String myMessage;
	private boolean myDraft;

	private String myLine;
	private String myReviewItemId;
	private String myPermId;
	private String myRevision;
	private Date myCreateDate = new Date();
	private final List<Comment> myReplies = new ArrayList<Comment>();
	private String myParentCommentId;

	public Comment(@NotNull final User commentAuthor, @NotNull final String message, boolean draft) {
		myAuthor = commentAuthor;
		myMessage = message;
		myDraft = draft;
	}

	@NotNull
	public String getMessage() {
		return myMessage;
	}

	@NotNull
	public User getAuthor() {
		return myAuthor;
	}

	public Date getCreateDate() {
		return new Date(myCreateDate.getTime());
	}

	public void setCreateDate(Date createDate) {
		if (createDate != null) {
			myCreateDate = new Date(createDate.getTime());
		}
	}

	@Override
	public String toString() {
		return getMessage();
	}

	public String getLine() {
		return myLine;
	}

	public void setLine(String line) {
		myLine = line;
	}

	public String getReviewItemId() {
		return myReviewItemId;
	}

	public void setReviewItemId(String reviewItemId) {
		myReviewItemId = reviewItemId;
	}

	public String getRevision() {
		return myRevision;
	}

	public void setRevision(String revision) {
		myRevision = revision;
	}

	public void addReply(Comment reply) {
		myReplies.add(reply);
	}

	public List<Comment> getReplies() {
		return myReplies;
	}

	public void setParentCommentId(String parentCommentId) {
		myParentCommentId = parentCommentId;
	}

	public String getParentCommentId() {
		return myParentCommentId;
	}

	public String getPermId() {
		return myPermId;
	}

	public void setPermId(String id) {
		myPermId = id;
	}

	public boolean isDraft() {
		return myDraft;
	}

	public void setDraft(boolean draft) {
		myDraft = draft;
	}
}
