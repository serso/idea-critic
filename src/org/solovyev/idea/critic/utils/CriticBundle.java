package org.solovyev.idea.critic.utils;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

public class CriticBundle {
	private static Reference<ResourceBundle> ourBundle;

	@NonNls
	private static final String BUNDLE = "org.solovyev.idea.critic.utils.messages";

	private CriticBundle() {
	}

	public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
		return CommonBundle.message(getBundle(), key, params);
	}

	private static ResourceBundle getBundle() {
		ResourceBundle bundle = null;
		if (ourBundle != null) bundle = ourBundle.get();
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(BUNDLE);
			ourBundle = new SoftReference<ResourceBundle>(bundle);
		}
		return bundle;
	}
}
