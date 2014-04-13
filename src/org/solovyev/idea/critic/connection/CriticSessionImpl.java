package org.solovyev.idea.critic.connection;

import com.google.gson.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.containers.SLRUCache;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.configuration.CriticSettings;
import org.solovyev.idea.critic.connection.exceptions.CriticApiException;
import org.solovyev.idea.critic.connection.exceptions.CriticApiLoginException;
import org.solovyev.idea.critic.model.*;
import org.solovyev.idea.critic.ui.UiUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CriticSessionImpl implements CriticSession {
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final Logger LOG = Logger.getInstance(CriticSessionImpl.class.getName());
	private static final int CONNECTION_TIMEOUT = 5000;

	private final Project myProject;
	private final Repositories myRepositories;

	private SLRUCache<String, String> myDownloadedFilesCache = new SLRUCache<String, String>(50, 50) {
		@NotNull
		@Override
		public String createValue(String relativeUrl) {
			return doDownloadFile(relativeUrl);
		}

		private String doDownloadFile(String relativeUrl) {
			final String url = getHostUrl() + relativeUrl;
			final GetMethod method = new GetMethod(url);
			try {
				executeHttpMethod(method);
				return method.getResponseBodyAsString();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	public CriticSessionImpl(@NotNull final Project project) {
		myProject = project;
		myRepositories = Repositories.create(myProject);
		initSSLCertPolicy();
	}

	private void initSSLCertPolicy() {
		EasySSLProtocolSocketFactory secureProtocolSocketFactory = new EasySSLProtocolSocketFactory();
		Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) secureProtocolSocketFactory, 443));
	}

	@Override
	public void login() throws CriticApiLoginException {
		try {
			final String username = getUsername();
			final String password = getPassword();
			if (username == null || password == null) {
				throw new CriticApiLoginException("Username or Password is empty");
			}
			final String loginUrl = getLoginUrl(username, password);

			final JsonObject jsonObject = buildJsonResponse(loginUrl);
			final JsonElement authToken = jsonObject.get("token");
			final String errorMessage = getExceptionMessages(jsonObject);
			if (authToken == null || errorMessage != null) {
				throw new CriticApiLoginException(errorMessage != null ? errorMessage : "Unknown error");
			}
		} catch (IOException e) {
			throw new CriticApiLoginException(getHostUrl() + ":" + e.getMessage(), e);
		} catch (CriticApiException e) {
			throw new CriticApiLoginException(e.getMessage(), e);
		}
	}

	private String getLoginUrl(String username, String password) {
		final String loginUrlPrefix = getHostUrl() + AUTH_SERVICE + LOGIN;

		final String loginUrl;
		try {
			loginUrl = loginUrlPrefix + "?userName=" + URLEncoder.encode(username, "UTF-8") + "&password="
					+ URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: " + e.getMessage());
		}
		return loginUrl;
	}

	@Nullable
	@Override
	public CriticVersionInfo getServerVersion() {
		final String requestUrl = getHostUrl() + REVIEW_SERVICE + VERSION;
		try {
			final JsonObject jsonObject = buildJsonResponse(requestUrl);
			return CriticApi.parseVersion(jsonObject);
		} catch (IOException e) {
			LOG.warn(e);
		}
		return null;
	}

	private String getAuthHeaderValue() {
		return "Basic " + encode(getUsername() + ":" + getPassword());
	}

	public static String encode(String str2encode) {
		try {
			Base64 base64 = new Base64();
			byte[] bytes = base64.encode(str2encode.getBytes("UTF-8"));
			return new String(bytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is not supported", e);
		}
	}

	protected JsonObject buildJsonResponse(@NotNull final String urlString) throws IOException {
		final GetMethod method = new GetMethod(urlString);
		executeHttpMethod(method);

		JsonParser parser = new JsonParser();
		return parser.parse(new InputStreamReader(method.getResponseBodyAsStream(), CHARSET)).getAsJsonObject();
	}

	private void executeHttpMethod(@NotNull HttpMethodBase method) throws IOException {
		adjustHttpHeader(method);

		final HttpClient client = new HttpClient();
		HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
		params.setConnectionTimeout(CONNECTION_TIMEOUT); //set connection timeout (how long it takes to connect to remote host)
		params.setSoTimeout(CONNECTION_TIMEOUT); //set socket timeout (how long it takes to retrieve data from remote host)
		client.executeMethod(method);
	}

	protected JsonObject buildJsonResponseForPost(@NotNull final String urlString,
												  @NotNull final RequestEntity requestEntity) throws IOException {
		final PostMethod method = new PostMethod(urlString);
		method.setRequestEntity(requestEntity);
		executeHttpMethod(method);
		JsonParser parser = new JsonParser();
		return parser.parse(new InputStreamReader(method.getResponseBodyAsStream(), CHARSET)).getAsJsonObject();
	}

	protected void adjustHttpHeader(@NotNull final HttpMethod method) {
		method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
		method.addRequestHeader(new Header("accept", "application/json"));
	}

	protected String getUsername() {
		return CriticSettings.getInstance().USERNAME;
	}

	protected String getPassword() {
		return CriticSettings.getInstance().getPassword();
	}

	protected String getHostUrl() {
		return UrlUtil.removeUrlTrailingSlashes(CriticSettings.getInstance().SERVER_URL);
	}

	@Nullable
	public static String getExceptionMessages(@NotNull final JsonObject jsonObject) {
		final JsonElement error = jsonObject.get("error");
		final JsonElement statusCode = jsonObject.get("status-code");
		final JsonElement code = jsonObject.get("code");
		if (error != null) {
			return error.getAsString();
		} else if (statusCode != null && "500".equals(statusCode.getAsString())) {
			final JsonPrimitive message = jsonObject.getAsJsonPrimitive("message");
			return message.getAsString();
		} else if (code != null && code.getAsString().equalsIgnoreCase("IllegalState")) {
			final JsonPrimitive message = jsonObject.getAsJsonPrimitive("message");
			return message.getAsString();
		}
		return null;
	}

	@Nullable
	public Comment postComment(@NotNull final Comment comment, boolean isGeneral,
							   @NotNull final String reviewId) {

		String url = getHostUrl() + REVIEW_SERVICE + "/" + reviewId;
		final String parentCommentId = comment.getParentCommentId();

		final boolean isVersioned = !isGeneral;
		boolean reply = comment.getParentCommentId() != null;

		if (isVersioned && !reply) {
			url += REVIEW_ITEMS + "/" + comment.getReviewItemId();
		}

		url += COMMENTS;

		if (reply) {
			url += "/" + parentCommentId + REPLIES;
		}

		try {
			final RequestEntity request = CriticApi.createCommentRequest(comment, !isVersioned);

			final JsonObject jsonObject = buildJsonResponseForPost(url, request);
			final String errorMessage = getExceptionMessages(jsonObject);
			if (errorMessage != null) {
				UiUtils.showBalloon(myProject, "Sorry, comment wasn't added:\n" + errorMessage, MessageType.ERROR);
				return null;
			}

			return CriticApi.parseComment(jsonObject, isVersioned, reply);
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}
		return null;
	}

	@Override
	public void fillRepositories() throws IOException {
		final String url = getHostUrl() + REPOSITORIES;
		final JsonObject jsonObject = buildJsonResponse(url);
		for (Repository repo : CriticApi.parseGitRepositories(jsonObject)) {
			myRepositories.tryPut(repo);
		}
	}

	public List<BasicReview> getReviewsForFilter(@NotNull final CriticFilter filter) throws IOException {
		String url = getHostUrl() + REVIEW_SERVICE + FILTERED_REVIEWS;
		final String urlFilter = filter.getFilterUrl();
		if (!StringUtils.isEmpty(urlFilter)) {
			url += "/" + urlFilter;
		}
		List<BasicReview> reviews = new ArrayList<BasicReview>();

		final JsonObject jsonElement = buildJsonResponse(url);
		final JsonArray reviewData = jsonElement.getAsJsonArray("reviewData");
		if (reviewData != null) {
			for (int i = 0; i != reviewData.size(); ++i) {
				reviews.add(CriticApi.parseReview(reviewData.get(i).getAsJsonObject(), myProject, this));
			}
		}
		return reviews;
	}

	@Override
	public String downloadFile(@NotNull String relativeUrl) throws IOException {
		return myDownloadedFilesCache.get(relativeUrl);
	}

	@NotNull
	public Review getDetailsForReview(@NotNull final String permId) throws IOException {
		String url = getHostUrl() + REVIEW_SERVICE + "/" + permId + DETAIL_REVIEW_INFO;
		final JsonObject jsonObject = buildJsonResponse(url);
		return (Review) CriticApi.parseReview(jsonObject, myProject, this);
	}

	public Repositories getRepositories() {
		return myRepositories;
	}

	@Override
	public void publishComment(@NotNull Review review, @NotNull Comment comment) throws IOException {
		String url = getHostUrl() + REVIEW_SERVICE + "/" + review.getPermaId() + PUBLISH + "/" + comment.getPermId();
		PostMethod method = new PostMethod(url);
		executeHttpMethod(method);
	}

	@Override
	public void completeReview(@NotNull String reviewId) {
		final String url = getHostUrl() + REVIEW_SERVICE + "/" + reviewId + COMPLETE;
		try {
			final PostMethod method = new PostMethod(url);
			executeHttpMethod(method);
		} catch (IOException e) {
			LOG.warn(e.getMessage());
		}
	}
}