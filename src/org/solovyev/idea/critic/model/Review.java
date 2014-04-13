package org.solovyev.idea.critic.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.idea.critic.connection.CriticManager;
import org.solovyev.idea.critic.connection.Repositories;

import java.io.File;
import java.util.*;

public class Review extends BasicReview {
	private final List<Comment> myGeneralComments = new ArrayList<Comment>();
	private final List<Comment> myComments = new ArrayList<Comment>();

	private final Set<ReviewItem> myItems = new HashSet<ReviewItem>();

	public Review(@NotNull final String id, @NotNull final User author,
				  @Nullable final User moderator) {
		super(id, author, moderator);
	}

	public void addGeneralComment(@NotNull Comment generalComment) {
		myGeneralComments.add(generalComment);
	}

	public void addComment(@NotNull Comment comment) {
		myComments.add(comment);
	}

	@NotNull
	public List<Comment> getGeneralComments() {
		return myGeneralComments;
	}

	@NotNull
	public List<Comment> getComments() {
		return myComments;
	}

	@NotNull
	public Set<ReviewItem> getReviewItems() {
		return myItems;
	}

	public void addReviewItem(@NotNull final ReviewItem item) {
		myItems.add(item);
	}

	@Nullable
	public String getPathById(@NotNull final String id) {
		for (ReviewItem item : myItems) {
			if (item.getId().equals(id))
				return item.getPath();
		}
		return null;
	}

	@Nullable
	public String getIdByPath(@NotNull final String path, Project project) {
		final Repositories repositories = CriticManager.getInstance(project).getRepositories();

		for (ReviewItem item : myItems) {
			final String repo = item.getRepo();
			VirtualFile root = repositories.getRoot(repo);
			if (root == null) {
				root = project.getBaseDir();
			}
			String relativePath = FileUtil.getRelativePath(new File(root.getPath()), new File(path));
			if (FileUtil.pathsEqual(relativePath, item.getPath())) {
				return item.getId();
			}
		}
		return null;
	}

	public boolean isInPatch(@NotNull Comment comment) {
		final String reviewItemId = comment.getReviewItemId();
		return null != ContainerUtil.find(myItems, new Condition<ReviewItem>() {
			@Override
			public boolean value(ReviewItem item) {
				return item.getId().equalsIgnoreCase(reviewItemId) && item.isPatch();
			}
		});
	}
}