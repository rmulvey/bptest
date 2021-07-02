//========================================================================
// Licensed under the Apache License, Version 2.0 (the "License"); you may not 
// use this file except in compliance with the License.  You may obtain a copy 
// of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software 
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   See the 
// License for the specific language governing permissions and limitations under
// the License.
//======================================================================== 

package org.xtuml.bp.test.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.ui.Activator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.model.compare.contentmergeviewer.ModelContentMergeViewer;
import org.xtuml.bp.test.TestUtil;

import junit.framework.TestCase;

@SuppressWarnings("restriction")
public class GitUtil {
	public static final java.lang.String VIEW_ID = "org.eclipse.egit.ui.RepositoriesView";
	
	/**
	 * Utility method to open the git repositories view, will return the
	 * <code>IViewPart<code> or <code>null<code> if an exception
	 * occured.
	 */
	public static IViewPart showGitRepositoriesView() {
		try {
			IViewPart viewPart = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.showView(VIEW_ID);
			TestCase.assertNotNull("", viewPart);
			return viewPart;
		} catch (PartInitException e) {
			CorePlugin.logError("Unable to open git repositories view.", e);
		}
		return null;
	}

	public static void loadRepository(String location, String branch) {
		RepositoryUtil util = Activator.getDefault().getRepositoryUtil();
		util.addConfiguredRepository(new File(location + "/" + ".git"));
		// if the repository is not in a clean
		// state reset it here
		String[] repositoryPath = location.split("/");
		String repositoryName = repositoryPath[repositoryPath.length - 1];
		BaseTest.dispatchEvents();
		resetRepository(repositoryName, branch);
		BaseTest.dispatchEvents();
	}
	
	/**
	 * Loads a local repository into memory using the given repository path
	 */
	public static void loadRepository(String location) {
		GitUtil.loadRepository(location, "master");
	}

	/**
	 * Loads a project with the given name from a git repository at the given
	 * location.
	 */
	public static IProject loadProject(String projectName, String repositoryName) {
		IProject project = GitUtil.loadProject(projectName, repositoryName, "master");
		return project;
	}

	public static IProject loadProject(String projectName, String repositoryName, String branchName) {
		IViewPart gitRepositoryView = showGitRepositoriesView();
		CommonNavigator view = (CommonNavigator) gitRepositoryView;
		Control control = view.getCommonViewer().getControl();
		Tree gitRepositoryTree = (Tree) control;
		
		// Note that we are looking in the git view and at the top of the menu is the
		// repository itself and this is the first thing we find.
		String repoString = repositoryName + " [" + branchName;
		TreeItem repoItem = UITestingUtilities.findItemInTree(gitRepositoryTree,
				repoString);
		TestCase.assertNotNull(
				"Could not locate repository in the git repository view ("
						+ repoItem + ").", repoString);
		
	UITestingUtilities.expandTree(repoItem, 3);
	BaseTest.dispatchEvents();
	repoItem = UITestingUtilities.findItemInTree(gitRepositoryTree,
			repoString);
		
	TreeItem projectItem = UITestingUtilities.findItemInWorkingTree(repoItem, projectName);				

		TestCase.assertNotNull(
				"Could not locate project (" + projectName
						+ ") under the git repository (" + repoString+ ") in the git repo view.", projectItem);
		
				
		view.getCommonViewer().setSelection(
				new StructuredSelection(projectItem.getData()));
		// import projects from repository
		TestUtil.nextToDialog(200);
		TestUtil.finishToDialog(1000);
		UITestingUtilities.activateMenuItem(gitRepositoryTree.getMenu(),
				"&Import Projects...");
		BaseTest.dispatchEvents();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		TestCase.assertTrue("Unable to load project from git repository: "
				+ repositoryName, project.exists());
		BaseTest.dispatchEvents();
		
		long maxWait = 500;
		long startTime = System.currentTimeMillis();
		while(!project.exists() && System.currentTimeMillis() - startTime < maxWait) {
			BaseTest.dispatchEvents();
		}
		
		return project;
	}
	
	public static void mergeBranch(String remoteBranch, String repositoryName) {
		GitUtil.mergeBranch(remoteBranch, repositoryName, "master");
	}
	
	public static void mergeBranch(String remoteBranch, String repositoryName, String localBranch) {
		IViewPart gitRepositoryView = showGitRepositoriesView();
		CommonNavigator view = (CommonNavigator) gitRepositoryView;
		Control control = view.getCommonViewer().getControl();
		Tree gitRepositoryTree = (Tree) control;
		TreeItem item = UITestingUtilities.findItemInTree(gitRepositoryTree,
				repositoryName + " [" + localBranch);
		view.getCommonViewer().setSelection(new StructuredSelection(item.getData()));
		TestUtil.selectItemInTreeDialog(300, remoteBranch);
		TestUtil.mergeToDialog(500);
		TestUtil.okToDialog(700);
		UITestingUtilities.activateMenuItem(gitRepositoryTree.getMenu(), "&Merge...");
		BaseTest.dispatchEvents();
	}

	public static void compareWithBranch(String remoteBranch, String repositoryName, String localBranch) {
		IViewPart gitRepositoryView = showGitRepositoriesView();
		CommonNavigator view = (CommonNavigator) gitRepositoryView;
		Tree gitRepositoryTree = view.getCommonViewer().getTree();
		view.getCommonViewer().expandToLevel(4);

		TreeItem repo = UITestingUtilities.findItemInTree(gitRepositoryTree,
				localBranch);
		
		TreeItem local = UITestingUtilities.findItemInTree(repo,
				"Local");		
		
		TreeItem remoteItem = UITestingUtilities.findItemInTree(local,
				remoteBranch);
		TreeItem localItem = UITestingUtilities.findItemInTree(local,
				localBranch);
		view.getCommonViewer().setSelection(new StructuredSelection(localItem.getData()));
		view.getCommonViewer().setSelection(new StructuredSelection(remoteItem.getData()));		
		

		List<Object> list = new ArrayList<Object>();
		list.add(localItem.getData());
		list.add(remoteItem.getData());
		StructuredSelection sel = new StructuredSelection(list);
		view.getCommonViewer().setSelection(sel);

		TestUtil.yesToDialog(500);
		
		UITestingUtilities.activateMenuItem(gitRepositoryTree.getMenu(), "&Synchronize with each other");
		BaseTest.dispatchEvents(0);
	}
	
	
	public static void startMergeTool(String projectName) {
		// process any pending events
		TreeViewer treeViewer = ExplorerUtil.getTreeViewer();
		TreeItem item = UITestingUtilities.findItemInTree(treeViewer.getTree(), projectName);
		treeViewer.setSelection(new StructuredSelection(item.getData()));
		UITestingUtilities.activateMenuItem(treeViewer.getTree().getMenu(), "Team::Merge Tool");
		BaseTest.dispatchEvents();
	}

	public static void resetRepository(String repositoryName, String branch) {
		// process any pending events
		IViewPart gitRepositoryView = showGitRepositoriesView();
		gitRepositoryView.getViewSite().getShell().update();
		BaseTest.dispatchEvents();
		CommonNavigator view = (CommonNavigator) gitRepositoryView;
		view.getCommonViewer().refresh();
		BaseTest.dispatchEvents();
		Control control = view.getCommonViewer().getControl();
		Tree gitRepositoryTree = (Tree) control;
		TreeItem item = UITestingUtilities.findItemInTree(gitRepositoryTree,
				repositoryName + " [" + branch);
		if(item == null) {
			// reset the current branch before switching
			item = UITestingUtilities.findItemInTree(gitRepositoryTree,
					repositoryName);
			if(UITestingUtilities.getMenuItem(gitRepositoryTree.getMenu(), "&Reset") != null) {
				TestUtil.selectButtonInDialog(500, "&Hard (index and working directory updated)");
				TestUtil.finishToDialog(700);
				// say Yes to overwriting the local contents
				TestUtil.yesToDialog(900);
				UITestingUtilities.activateMenuItem(gitRepositoryTree.getMenu(), "&Reset");
			}
			// switch to the expected branch
			switchToBranch(branch, repositoryName);
			return;
		}
		view.getCommonViewer().setSelection(new StructuredSelection(item.getData()));
		if(UITestingUtilities.getMenuItem(gitRepositoryTree.getMenu(), "&Reset") != null) {
			TestUtil.selectButtonInDialog(500, "&Hard (index and working directory updated)");
			TestUtil.finishToDialog(700);
			// say Yes to overwriting the local contents
			TestUtil.yesToDialog(900);
			UITestingUtilities.activateMenuItem(gitRepositoryTree.getMenu(), "&Reset");
		}
		BaseTest.dispatchEvents();
	}

	public static void switchToFile(String treeItem) {
		UITestingUtilities.resizeMainWindow(1280, 1024);
		PlatformUI
				.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.toggleZoom(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().getActivePartReference());
		ModelContentMergeViewer viewer = ModelContentMergeViewer.getInstance(null);
		Control control = viewer.getControl();
		Tree tree = UITestingUtilities.findTree(control.getParent().getParent());
		tree.select(tree.getItems()[0]);
		UITestingUtilities.activateMenuItem(tree.getMenu(), "Expand All");
		BaseTest.dispatchEvents(0);
		TreeItem item = UITestingUtilities.findItemInTree(tree, treeItem);
		tree.deselectAll();
		tree.select(item);
		Event event = new Event();
		event.widget = tree;
		tree.notifyListeners(14, event);
		BaseTest.dispatchEvents();
	}

	public static void switchToBranch(String branch, String repositoryName) {
		IViewPart gitRepositoryView = showGitRepositoriesView();
		BaseTest.dispatchEvents();
		CommonNavigator view = (CommonNavigator) gitRepositoryView;
		Control control = view.getCommonViewer().getControl();
		Tree gitRepositoryTree = (Tree) control;
		TreeItem item = UITestingUtilities.findItemInTree(gitRepositoryTree,
				repositoryName);
		view.getCommonViewer().setSelection(
				new StructuredSelection(item.getData()));
		// set the max wait time lower, the dialog will
		// not always be there and in those cases waiting
		// 2 seconds is not necessary
		try {
			TestUtil.maxRunTime = 300;
			TestUtil.okToDialog(0);
		} finally {
			TestUtil.maxRunTime = 2000;
		}
		UITestingUtilities.activateMenuItem(gitRepositoryTree.getMenu(),
				"Switch To::" + branch);	
		// assure that there are no left over threads
		// waiting to close a dialog
		while(!TestUtil.shellProcessorThread.isEmpty()) {
			BaseTest.dispatchEvents();
		}
		BaseTest.dispatchEvents();
	}

}
