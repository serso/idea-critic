package org.solovyev.idea.critic.configuration;

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "CriticSettings",
		storages = {
				@Storage(file = StoragePathMacros.APP_CONFIG + "/criticConnector.xml")
		}
)
public class CriticSettings implements PersistentStateComponent<CriticSettings> {
	public String SERVER_URL = "";
	public String USERNAME = "";

	@Override
	public CriticSettings getState() {
		return this;
	}

	@Override
	public void loadState(CriticSettings state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public static CriticSettings getInstance() {
		return ServiceManager.getService(CriticSettings.class);
	}

	public static final String CRITIC_SETTINGS_PASSWORD_KEY = "CRITIC_SETTINGS_PASSWORD_KEY";
	private static final Logger LOG = Logger.getInstance(CriticConfigurable.class.getName());

	public void savePassword(String pass) {
		try {
			PasswordSafe.getInstance().storePassword(null, this.getClass(), CRITIC_SETTINGS_PASSWORD_KEY, pass);
		} catch (PasswordSafeException e) {
			LOG.info("Couldn't get password for key [" + CRITIC_SETTINGS_PASSWORD_KEY + "]", e);
		}
	}

	@Nullable
	public String getPassword() {
		try {
			return PasswordSafe.getInstance().getPassword(null, this.getClass(), CRITIC_SETTINGS_PASSWORD_KEY);
		} catch (PasswordSafeException e) {
			LOG.info("Couldn't get the password for key [" + CRITIC_SETTINGS_PASSWORD_KEY + "]", e);
			return null;
		}
	}
}
