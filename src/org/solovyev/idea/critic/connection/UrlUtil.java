package org.solovyev.idea.critic.connection;

import org.jetbrains.annotations.NotNull;


public abstract class UrlUtil {
	private UrlUtil() {
	}

	public static String removeUrlTrailingSlashes(@NotNull String address) {
		while (address.endsWith("/")) {
			address = address.substring(0, address.length() - 1);
		}
		return address;
	}
}
