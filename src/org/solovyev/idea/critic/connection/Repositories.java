package org.solovyev.idea.critic.connection;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.commands.GitRemoteProtocol;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.model.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Repositories {

	private final Map<String, VirtualFile> myMap = new HashMap<String, VirtualFile>();
	private final Project myProject;

	private Repositories(Project project) {
		this.myProject = project;
	}

	public static Repositories create(Project project) {
		return new Repositories(project);
	}

	public void tryPut(Repository repository) {
		final VirtualFile localPath = getLocalPath(repository);
		if (localPath != null) {
			myMap.put(repository.getName(), localPath);
		}
	}


	@Nullable
	private VirtualFile getLocalPath(@NotNull Repository repository) {
		final GitRepositoryManager manager = GitUtil.getRepositoryManager(myProject);
		final String location = unifyLocation(repository.getUrl());
		for (GitRepository projectRepo : manager.getRepositories()) {
			GitRemote origin = GitUtil.findRemoteByName(projectRepo, GitRemote.ORIGIN_NAME);
			if (origin != null && location != null) {
				String originFirstUrl = origin.getFirstUrl();
				if (originFirstUrl == null) continue;
				String originLocation = unifyLocation(originFirstUrl);
				if (location.equals(originLocation)) {
					return projectRepo.getRoot();
				}
			}
		}
		return null;
	}

	@Nullable
	private static String unifyLocation(@NotNull String location) {
		final GitRemoteProtocol protocol = GitRemoteProtocol.fromUrl(location);
		if (protocol == null) return null;
		switch (protocol) {
			case GIT:
				return StringUtil.trimEnd(StringUtil.trimStart(location, "git://"), ".git");
			case HTTP:
				Pattern pattern = Pattern.compile("https?://(.*)\\.git");
				Matcher matcher = pattern.matcher(location);
				boolean found = matcher.find();
				return found ? matcher.group(1) : null;
			case SSH:
				pattern = Pattern.compile("git@(.*)?:(.*)(\\.git)?");
				matcher = pattern.matcher(location);
				found = matcher.find();
				return found ? matcher.group(1) + "/" + matcher.group(2) : null;
			default:
		}
		return null;
	}

	public Iterable<Map.Entry<String, VirtualFile>> asIterable() {
		return myMap.entrySet();
	}

	public static Repositories createEmpty(Project project) {
		return new Repositories(project);
	}

	public VirtualFile getRoot(String repositoryName) {
		return myMap.get(repositoryName);
	}
}
