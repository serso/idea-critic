package org.solovyev.idea.critic.model;

public class CriticVersionInfo {
	private final String myBuildDate;
	private final String myReleaseNumber;

	public CriticVersionInfo(String releaseNumber, String buildDate) {
		myBuildDate = buildDate;
		myReleaseNumber = releaseNumber;
	}

	public String getBuildDate() {
		return myBuildDate;
	}

	public String getReleaseNumber() {
		return myReleaseNumber;
	}
}
