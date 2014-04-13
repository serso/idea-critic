package org.solovyev.idea.critic.connection;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.configuration.CriticSettings;
import org.solovyev.idea.critic.connection.exceptions.CriticApiException;

import java.net.MalformedURLException;
import java.net.URL;

public class CriticTestConnector {

	public enum ConnectionState {
		NOT_FINISHED,
		SUCCEEDED,
		FAILED,
		INTERRUPTED,
	}

	private ConnectionState myConnectionState = ConnectionState.NOT_FINISHED;
	private Exception myException;
	private final Project myProject;

	public CriticTestConnector(Project project) {
		myProject = project;
	}

	public ConnectionState getConnectionState() {
		return myConnectionState;
	}

	public void run() {
		try {
			testConnect();
			if (myConnectionState != ConnectionState.INTERRUPTED && myConnectionState != ConnectionState.FAILED) {
				myConnectionState = ConnectionState.SUCCEEDED;
			}
		} catch (CriticApiException e) {
			if (myConnectionState != ConnectionState.INTERRUPTED) {
				myConnectionState = ConnectionState.FAILED;
				myException = e;
			}
		}
	}

	public void setInterrupted() {
		myConnectionState = ConnectionState.INTERRUPTED;
	}

	@Nullable
	public String getErrorMessage() {
		return myException == null ? null : myException.getMessage();
	}

	public void testConnect() throws CriticApiException {
		final CriticSession session = new CriticSessionImpl(myProject);
		final String url = CriticSettings.getInstance().SERVER_URL;
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			myConnectionState = ConnectionState.FAILED;
			myException = e;
			return;
		}
		session.login();
		session.getServerVersion();
	}
}


