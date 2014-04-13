package org.solovyev.idea.critic.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class UiUtils {
	private UiUtils() {
	}

	public static void showBalloon(@NotNull final Project project, @NotNull final String message,
								   @NotNull final MessageType messageType) {
		final JFrame frame = WindowManager.getInstance().getFrame(project.isDefault() ? null : project);
		if (frame == null) return;
		final JComponent component = frame.getRootPane();
		if (component == null) return;
		final Rectangle rect = component.getVisibleRect();
		final Point p = new Point(rect.x + rect.width - 10, rect.y + 10);
		final RelativePoint point = new RelativePoint(component, p);

		final BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().
				createHtmlTextBalloonBuilder(message, messageType.getDefaultIcon(),
						messageType.getPopupBackground(), null);
		balloonBuilder.setShowCallout(false).setCloseButtonEnabled(true)
				.createBalloon().show(point, Balloon.Position.atLeft);
	}
}
