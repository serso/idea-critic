package org.solovyev.idea.critic.connection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class CriticTestConnectionTask extends Task.Modal {
	private static final int CHECK_CANCEL_INTERVAL = 500;
	private static final Logger LOG = Logger.getInstance(CriticTestConnectionTask.class.getName());

	public CriticTestConnectionTask(@Nullable Project project) {
		super(project, "Testing Connection", true);
		setCancelText("Stop");
	}

	@Override
	public void run(@NotNull ProgressIndicator indicator) {
		indicator.setText("Connecting...");
		indicator.setFraction(0);
		indicator.setIndeterminate(true);

		final CriticTestConnector connector = new CriticTestConnector(myProject);
		connector.run();

		while (connector.getConnectionState() == CriticTestConnector.ConnectionState.NOT_FINISHED) {
			try {
				if (indicator.isCanceled()) {
					connector.setInterrupted();
					break;
				} else {
					Thread.sleep(CHECK_CANCEL_INTERVAL);
				}
			} catch (InterruptedException e) {
				LOG.info(e.getMessage());
			}
		}

		CriticTestConnector.ConnectionState state = connector.getConnectionState();
		switch (state) {
			case FAILED:
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						Messages.showDialog(myProject, "Reason:  " + connector.getErrorMessage(), "Connection Failed", new String[]{"Ok"}, 0, null);
					}
				});
				break;
			case INTERRUPTED:
				LOG.debug("'Test Connection' canceled");
				break;
			case SUCCEEDED:
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						Messages.showDialog(myProject, "Connected successfully", "Connection OK", new String[]{"Ok"}, 0, null);
					}
				});
				break;
			default: //NOT_FINISHED:
				LOG.warn("Unexpected 'Test Connection' state: "
						+ connector.getConnectionState().toString());
		}
	}
}

