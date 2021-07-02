package org.xtuml.bp.model.compare.test;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.core.Package_c;
import org.xtuml.bp.core.StateMachineState_c;
import org.xtuml.bp.core.Transition_c;
import org.xtuml.bp.core.common.BridgePointPreferencesStore;
import org.xtuml.bp.test.TestUtil;
import org.xtuml.bp.test.common.BaseTest;
import org.xtuml.bp.test.common.CompareTestUtilities;
import org.xtuml.bp.test.common.GitUtil;
import org.xtuml.bp.test.common.UITestingUtilities;
import org.xtuml.bp.test.common.ZipUtil;

public class CompareOpenActionTest extends BaseTest {

	private String test_repositories = Platform.getInstanceLocation().getURL().getFile() + "/" + "test_repositories";

	public static boolean isFirstTime = true;
	static String projectName = "OpenActionFiltering";
	static String stateMachineProjectName = "dts0101042915";

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Before
	public void initialSetup() throws Exception {
		if (!isFirstTime)
			return;
		isFirstTime = false;
		CorePlugin.getDefault().getPreferenceStore()
				.setValue(BridgePointPreferencesStore.ENABLE_ERROR_FOR_EMPTY_SYNCHRONOUS_MESSAGE, false);
		String test_repository_location = BaseTest.getTestModelRespositoryLocation();
		test_repository_location = new Path(test_repository_location).removeLastSegments(1).toString();
		ZipUtil.unzipFileContents(
				test_repository_location + "/" + "test_repositories" + "/" + "OpenActionFiltering.zip",
				test_repositories);
		ZipUtil.unzipFileContents(test_repository_location + "/test_repositories/204.zip", test_repositories);
		// import git repository from models repo
		GitUtil.loadRepository(test_repositories + "/" + stateMachineProjectName);
		// import test project
		GitUtil.loadProject(stateMachineProjectName, stateMachineProjectName);
		// import git repository from models repo
		GitUtil.loadRepository(test_repositories + "/" + projectName);
		// import test project
		GitUtil.loadProject(projectName, projectName);

	}

	private void openMergeToolForStateMachine() throws Exception {
		// start the merge tool
		GitUtil.startMergeTool(stateMachineProjectName);
		m_sys = getSystemModel(stateMachineProjectName);
	}

	private void openMergeToolForClass() {
		// start the merge tool
		GitUtil.startMergeTool(projectName);
	}

	@After
	public void tearDown() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

	private Object getTestElement(Tree tree, String rootItem, boolean editorElement, boolean actionSemantics) {
		TreeItem rootTreeItem = UITestingUtilities.findItemInTree(tree, rootItem);
		UITestingUtilities.expandTree(rootTreeItem, -1);
		if (editorElement) {
			if (actionSemantics) {
				return UITestingUtilities.findItemInTree(tree, "Action Semantics Field");
			} else {
				return UITestingUtilities.findItemInTree(tree, "Description");
			}
		} else {
			if (actionSemantics) {
				// get the parent
				return UITestingUtilities.findItemInTree(tree, "Action Semantics Field").getParentItem();
			} else {
				return UITestingUtilities.findItemInTree(tree, "Description").getParentItem();
			}
		}
	}

	private void performMenuTest(boolean editorElement, boolean actionSemantics, boolean openExpected) {
		performMenuTest(editorElement, actionSemantics, openExpected, null);
	}

	private void performMenuTest(boolean editorElement, boolean actionSemantics, boolean openExpected, Object element) {
		Tree tree = CompareTestUtilities.getTreeViewer(true).getTree();
		Object testElement = UITestingUtilities.findItemInTree(tree, "Class Name");
		if (openExpected) {
			if (element != null) {
				testElement = element;
			} else {
				testElement = getTestElement(tree, "Class", editorElement, actionSemantics);
			}
		}
		CompareTestUtilities.selectElementInTree(true, testElement);
		Menu syncTreeMenu = CompareTestUtilities.getMenu(true);
		if (openExpected) {
			assertTrue(UITestingUtilities.checkItemStatusInContextMenu(syncTreeMenu, "Open", "", false));
		} else {
			assertFalse(UITestingUtilities.checkItemStatusInContextMenu(syncTreeMenu, "Open", "", false));
		}
	}

	private void performDoubleClickTest(boolean editorElement, boolean actionSemantics, boolean openExpected) {
		performDoubleClickTest(editorElement, actionSemantics, openExpected, null);
	}

	private void performDoubleClickTest(boolean editorElement, boolean actionSemantics, boolean openExpected,
			Object element) {
		Tree tree = CompareTestUtilities.getTreeViewer(true).getTree();
		Object testElement = UITestingUtilities.findItemInTree(tree, "Class Name");
		if (openExpected) {
			if (element != null) {
				testElement = element;
				if (testElement instanceof String) {
					testElement = UITestingUtilities.findItemInTree(tree, (String) testElement);
				}
			} else {
				testElement = getTestElement(tree, "Class", editorElement, actionSemantics);
			}
		}
		TestUtil.dismissDialog(2000);
		CompareTestUtilities.doubleClickElementInTree((TreeItem) testElement);
		if (openExpected) {
			assertTrue(TestUtil.wasCompareDialog);
		} else {
			assertFalse(TestUtil.wasCompareDialog);
		}
	}

	@Test
	public void testOpenMenuOnState() throws Exception {
		openMergeToolForStateMachine();
		Package_c pkg = Package_c.getOneEP_PKGOnR1401(m_sys);
		StateMachineState_c state = StateMachineState_c.StateMachineStateInstance(pkg.getModelRoot());
		performMenuTest(true, true, true, state);
	}

	@Test
	public void testDoubleClickOnState() throws Exception {
		openMergeToolForStateMachine();
		Package_c pkg = Package_c.getOneEP_PKGOnR1401(m_sys);
		StateMachineState_c state = StateMachineState_c.StateMachineStateInstance(pkg.getModelRoot());
		performDoubleClickTest(true, true, true, state.getName());
	}

	@Test
	public void testOpenMenuOnTransition() throws Exception {
		openMergeToolForStateMachine();
		Package_c pkg = Package_c.getOneEP_PKGOnR1401(m_sys);
		Transition_c transition = Transition_c.TransitionInstance(pkg.getModelRoot());
		performMenuTest(true, true, true, transition);
	}

	@Test
	public void testDoubleClickOnTransition() throws Exception {
		openMergeToolForStateMachine();
		Package_c pkg = Package_c.getOneEP_PKGOnR1401(m_sys);
		Transition_c transition = Transition_c.TransitionInstance(pkg.getModelRoot());
		performDoubleClickTest(true, true, true, transition.Get_name());
	}

	@Test
	public void testOpenMenuOnActionSemantics() {
		openMergeToolForClass();
		performMenuTest(true, true, true);
	}

	@Test
	public void testOpenMenuOnActionSemanticsParent() {
		openMergeToolForClass();
		performMenuTest(false, true, true);
	}

	@Test
	public void testOpenMenuOnDescription() {
		openMergeToolForClass();
		performMenuTest(true, false, true);
	}

	@Test
	public void testOpenMenuOnDescriptionParent() {
		openMergeToolForClass();
		performMenuTest(false, false, true);
	}

	@Test
	public void testDoubleClickOnActionSemantics() {
		openMergeToolForClass();
		performDoubleClickTest(true, true, true);
	}

	@Test
	public void testDoubleClickOnActionSemanticsParent() {
		openMergeToolForClass();
		performDoubleClickTest(false, true, true);
	}

	@Test
	public void testDoubleClickOnDescription() {
		openMergeToolForClass();
		performDoubleClickTest(true, false, true);
	}

	@Test
	public void testDoubleClickOnDescriptionParent() {
		openMergeToolForClass();
		performDoubleClickTest(false, false, true);
	}

	@Test
	public void testOpenMenuOnNonEditorElement() {
		openMergeToolForClass();
		performMenuTest(false, false, false);
	}

	@Test
	public void testDoubleClickOnNonEditorElement() {
		openMergeToolForClass();
		performDoubleClickTest(false, false, false);
	}
}
