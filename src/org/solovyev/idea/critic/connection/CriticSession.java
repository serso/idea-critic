package org.solovyev.idea.critic.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.connection.exceptions.CriticApiLoginException;
import org.solovyev.idea.critic.model.*;

import java.io.IOException;
import java.util.List;

public interface CriticSession {
	String REVIEW_SERVICE = "/rest-service/reviews-v1";
	String REVIEW_ITEMS = "/reviewitems";
	String DETAIL_REVIEW_INFO = "/details";
	String VERSION = "/versionInfo";
	String AUTH_SERVICE = "/rest-service/auth-v1";
	String LOGIN = "/login";
	String FILTERED_REVIEWS = "/filter";
	String COMMENTS = "/comments";
	String REPLIES = "/replies";
	String REPOSITORIES = "/rest-service/repositories-v1";
	String COMPLETE = "/complete";
	String PUBLISH = "/publish";

	void login() throws CriticApiLoginException;

	@Nullable
	CriticVersionInfo getServerVersion();

	List<BasicReview> getReviewsForFilter(@NotNull final CriticFilter filter) throws IOException;

	String downloadFile(@NotNull String relativeUrl) throws IOException;

	Review getDetailsForReview(@NotNull final String permId) throws IOException;

	@Nullable
	Comment postComment(@NotNull final Comment comment, boolean isGeneral, String reviewId);

	void completeReview(@NotNull final String reviewId);

	void fillRepositories() throws IOException;

	Repositories getRepositories();

	void publishComment(@NotNull Review review, @NotNull Comment comment) throws IOException;
}
