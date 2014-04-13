package org.solovyev.idea.critic.connection;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.configuration.CriticSettings;
import org.solovyev.idea.critic.connection.exceptions.CriticApiException;
import org.solovyev.idea.critic.model.BasicReview;
import org.solovyev.idea.critic.model.Comment;
import org.solovyev.idea.critic.model.CriticFilter;
import org.solovyev.idea.critic.model.Review;
import org.solovyev.idea.critic.ui.UiUtils;
import org.solovyev.idea.critic.utils.CriticBundle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CriticManager {
	private final Project myProject;
	private final Map<String, CriticSession> mySessions = new HashMap<String, CriticSession>();

	private static final Logger LOG = Logger.getInstance(CriticManager.class.getName());

	// implicitly constructed by pico container
	@SuppressWarnings("UnusedDeclaration")
	private CriticManager(@NotNull final Project project) {
		myProject = project;
	}

	public static CriticManager getInstance(@NotNull final Project project) {
		return ServiceManager.getService(project, CriticManager.class);
	}

	@Nullable
	public List<BasicReview> getReviewsForFilter(@NotNull final CriticFilter filter) {
		try {
			final CriticSession session = getSession();
			if (session != null) {
				return session.getReviewsForFilter(filter);
			}
		} catch (IOException e) {
			LOG.warn(e.getMessage());
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
		} catch (CriticApiException e) {
			LOG.warn(e.getMessage());
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
		}
		return null;
	}

	@Nullable
	public Review getDetailsForReview(@NotNull final String permId) {
		try {
			final CriticSession session = getSession();
			if (session != null) {
				return session.getDetailsForReview(permId);
			}
		} catch (CriticApiException e) {
			LOG.warn(e.getMessage());
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.connection.error.message.$0", e.getMessage()), MessageType.ERROR);

		} catch (IOException e) {
			LOG.warn(e.getMessage());
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
		}
		return null;
	}

	@Nullable
	public Comment postComment(@NotNull final Comment comment, boolean isGeneral, String reviewId) {
		try {
			final CriticSession session = getSession();
			if (session != null) {
				return session.postComment(comment, isGeneral, reviewId);
			}
		} catch (CriticApiException e) {
			LOG.warn(e.getMessage());
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
		}
		return null;
	}

	public void completeReview(String reviewId) {
		try {
			final CriticSession session = getSession();
			if (session != null) {
				session.completeReview(reviewId);
			}
		} catch (CriticApiException e) {
			LOG.warn(e.getMessage());
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.connection.error.message.$0", e.getMessage()), MessageType.ERROR);
		}
	}

	@Nullable
	public CriticSession getSession() throws CriticApiException {
		final CriticSettings criticSettings = CriticSettings.getInstance();
		final String serverUrl = criticSettings.SERVER_URL;
		final String username = criticSettings.USERNAME;
		if (StringUtil.isEmptyOrSpaces(serverUrl) || StringUtil.isEmptyOrSpaces(username)) {
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.define.host.username"), MessageType.ERROR);
			return null;
		}
		try {
			new URL(serverUrl);
		} catch (MalformedURLException e) {
			UiUtils.showBalloon(myProject, CriticBundle.message("critic.wrong.host"), MessageType.ERROR);
			return null;
		}
		String key = serverUrl + username + criticSettings.getPassword();
		CriticSession session = mySessions.get(key);
		if (session == null) {
			session = new CriticSessionImpl(myProject);
			session.login();
			mySessions.put(key, session);
			try {
				session.fillRepositories();
			} catch (IOException e) {
				LOG.warn(e.getMessage());
			}
		}
		return session;
	}

	public Repositories getRepositories() {
		try {
			final CriticSession session = getSession();
			if (session != null) {
				return session.getRepositories();
			}
		} catch (CriticApiException e) {
			LOG.warn(e.getMessage());
		}
		return Repositories.createEmpty(myProject);
	}

	public void publishComment(@NotNull Review review, @NotNull Comment comment) throws IOException, CriticApiException {
		CriticSession session = getSession();
		if (session != null) {
			session.publishComment(review, comment);
		}
	}
}
