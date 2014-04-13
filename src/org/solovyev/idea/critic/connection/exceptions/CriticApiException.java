package org.solovyev.idea.critic.connection.exceptions;

public class CriticApiException extends Exception {

	public CriticApiException(String message) {
		super(message);
	}

	public CriticApiException(String message, Throwable throwable) {
		super(message, throwable);
	}
}