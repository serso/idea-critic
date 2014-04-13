package org.solovyev.idea.critic.utils;

import com.intellij.openapi.actionSystem.DataKey;
import org.solovyev.idea.critic.model.Review;

public final class CriticDataKeys {

	public static DataKey<Review> REVIEW = DataKey.create("critic.review");
}
