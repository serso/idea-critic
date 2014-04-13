package org.solovyev.idea.critic.ui.toolWindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NotNull;
import org.solovyev.idea.critic.connection.CriticManager;
import org.solovyev.idea.critic.connection.Repositories;
import org.solovyev.idea.critic.model.Review;
import org.solovyev.idea.critic.model.ReviewItem;
import org.solovyev.idea.critic.ui.DescriptionCellRenderer;
import org.solovyev.idea.critic.ui.toolWindow.details.DetailsPanel;
import org.solovyev.idea.critic.ui.toolWindow.tree.CriticRootNode;
import org.solovyev.idea.critic.ui.toolWindow.tree.CriticTreeModel;
import org.solovyev.idea.critic.utils.CriticBundle;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class CriticPanel extends SimpleToolWindowPanel {
	private static final Logger LOG = Logger.getInstance(CriticPanel.class.getName());

	private final Project myProject;
	private final CriticReviewModel myReviewModel;
	private final JBTable myReviewTable;

	public CriticReviewModel getReviewModel() {
		return myReviewModel;
	}

	public CriticPanel(@NotNull final Project project) {
		super(false);
		myProject = project;

		final JBSplitter splitter = new JBSplitter(false, 0.2f);

		myReviewModel = new CriticReviewModel(project);
		myReviewTable = new JBTable(myReviewModel);
		myReviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		myReviewTable.setStriped(true);
		myReviewTable.setExpandableItemsEnabled(false);

		final TableColumnModel columnModel = myReviewTable.getColumnModel();
		columnModel.getColumn(1).setCellRenderer(new DescriptionCellRenderer());

		setUpColumnWidths(myReviewTable);
		myReviewTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					final int viewRow = myReviewTable.getSelectedRow();
					if (viewRow >= 0 && viewRow < myReviewTable.getRowCount()) {
						ApplicationManager.getApplication().invokeLater(new Runnable() {
							@Override
							public void run() {
								final Review review =
										CriticManager.getInstance(myProject).getDetailsForReview((String) myReviewTable.
												getValueAt(viewRow, myReviewTable.getColumnModel().getColumnIndex(CriticBundle.message("critic.id"))));
								if (review != null) {
									openDetailsToolWindow(review);
									myReviewTable.clearSelection();
								}
							}
						}, ModalityState.stateForComponent(myReviewTable));

					}
				}
			}
		});

		final TableRowSorter<TableModel> rowSorter = new TableRowSorter<TableModel>(myReviewModel);
		rowSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(4, SortOrder.ASCENDING)));
		rowSorter.sort();
		myReviewTable.setRowSorter(rowSorter);

		final JScrollPane detailsScrollPane = ScrollPaneFactory.createScrollPane(myReviewTable);

		final SimpleTreeStructure reviewTreeStructure = createTreeStructure();
		final DefaultTreeModel model = new CriticTreeModel();
		final SimpleTree reviewTree = new SimpleTree(model);

		new AbstractTreeBuilder(reviewTree, model, reviewTreeStructure, null);
		reviewTree.invalidate();

		final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
		splitter.setFirstComponent(scrollPane);
		splitter.setSecondComponent(detailsScrollPane);
		setContent(splitter);
	}

	private static void setUpColumnWidths(@NotNull final JBTable table) {
		table.getColumnModel().getColumn(0).setMinWidth(130);     //ID
		table.getColumnModel().getColumn(0).setMaxWidth(130);     //ID
		table.getColumnModel().getColumn(1).setMinWidth(400);          //message
		table.getColumnModel().getColumn(1).setPreferredWidth(400);    //message
		table.getColumnModel().getColumn(2).setMinWidth(130);     //State
		table.getColumnModel().getColumn(2).setMaxWidth(130);     //State
		table.getColumnModel().getColumn(3).setMinWidth(200);     //Author
		table.getColumnModel().getColumn(3).setMaxWidth(200);     //Author
		table.getColumnModel().getColumn(4).setMinWidth(130);     //Date
		table.getColumnModel().getColumn(4).setMaxWidth(130);     //Date
	}

	public void openDetailsToolWindow(@NotNull final Review review) {
		final ToolWindow toolWindow = ToolWindowManager.getInstance(myProject).getToolWindow(CriticBundle.message("critic.toolwindow.id"));
		final ContentManager contentManager = toolWindow.getContentManager();
		final Content foundContent = contentManager.findContent("Details for " + review.getPermaId());
		if (foundContent != null) {
			contentManager.setSelectedContent(foundContent);
			return;
		}

		final DetailsPanel details = new DetailsPanel(myProject, review);
		final Content content = ContentFactory.SERVICE.getInstance().createContent(details,
				"Details for " + review.getPermaId(), false);
		contentManager.addContent(content);
		contentManager.setSelectedContent(content);
		details.setBusy(true);

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				final List<CommittedChangeList> list = new ArrayList<CommittedChangeList>();
				final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
				final VirtualFile projectDir = myProject.getBaseDir();
				final AbstractVcs vcsFor = vcsManager.getVcsFor(projectDir);
				if (vcsFor == null) return;
				final Set<ReviewItem> reviewItems = review.getReviewItems();
				final Set<String> loadedRevisions = new HashSet<String>();

				final Repositories repositories = CriticManager.getInstance(myProject).getRepositories();
				for (ReviewItem reviewItem : reviewItems) {
					VirtualFile root = repositories.getRoot(reviewItem.getRepo());
					if (root == null) {
						root = projectDir;
					}
					try {
						list.addAll(reviewItem.loadChangeLists(myProject, vcsFor, root, loadedRevisions));
					} catch (VcsException e) {
						LOG.error(e);
					}
				}
				details.updateCommitsList(list);
				details.setBusy(false);

			}
		}, ModalityState.stateForComponent(toolWindow.getComponent()));
	}

	private SimpleTreeStructure createTreeStructure() {
		final CriticRootNode rootNode = new CriticRootNode(myReviewModel);
		return new CriticTreeStructure(rootNode);
	}
}
