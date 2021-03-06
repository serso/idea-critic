package org.solovyev.idea.critic.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.vcs.Git;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReviewItem {

	private String myId;
	private String myPath;
	private String myRepo;
	private Set<String> myRevisions = new HashSet<String>();

	public ReviewItem(@NotNull final String id, @NotNull final String path, @Nullable final String repo) {
		myId = id;
		myPath = path;
		myRepo = repo;
	}

	@NotNull
	public List<CommittedChangeList> loadChangeLists(@NotNull Project project, @NotNull AbstractVcs vcsFor, @NotNull VirtualFile root,
													 @NotNull Set<String> loadedRevisions) throws VcsException {
		final Set<String> revisions = getRevisions();
		List<CommittedChangeList> changeLists = new ArrayList<CommittedChangeList>();
		for (String revision : revisions) {
			if (!loadedRevisions.contains(revision)) {
				final VcsRevisionNumber revisionNumber = vcsFor.parseRevisionNumber(revision);
				if (revisionNumber != null) {
					final CommittedChangeList changeList = Git.loadRevisions(project, root, revisionNumber);
					if (changeList != null) changeLists.add(changeList);
				}
				loadedRevisions.add(revision);
			}
		}
		return changeLists;
	}

	@NotNull
	public String getRepo() {
		return myRepo;
	}

	@NotNull
	public String getPath() {
		return myPath;
	}

	@NotNull
	public String getId() {
		return myId;
	}

	public void addRevision(@NotNull final String revision) {
		myRevisions.add(revision);
	}

	@NotNull
	public Set<String> getRevisions() {
		return myRevisions;
	}

	public boolean isPatch() {
		return false;
	}
}