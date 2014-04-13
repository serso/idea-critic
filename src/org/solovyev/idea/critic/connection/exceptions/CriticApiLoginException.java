package org.solovyev.idea.critic.connection.exceptions;

public class CriticApiLoginException extends CriticApiException {

	public CriticApiLoginException(String message) {
		super(message);
	}

	public CriticApiLoginException(String message, Throwable throwable) {
		super(message, throwable);
	}
}