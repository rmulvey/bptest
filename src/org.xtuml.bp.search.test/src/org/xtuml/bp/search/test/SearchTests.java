package org.xtuml.bp.search.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.WorkbenchPart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.core.ModelClass_c;
import org.xtuml.bp.core.Ooaofooa;
import org.xtuml.bp.core.Operation_c;
import org.xtuml.bp.core.Package_c;
import org.xtuml.bp.core.PackageableElement_c;
import org.xtuml.bp.core.SearchResult_c;
import org.xtuml.bp.core.SystemModel_c;
import org.xtuml.bp.core.common.BridgePointPreferencesStore;
import org.xtuml.bp.core.common.ClassQueryInterface_c;
import org.xtuml.bp.core.util.EditorUtil;
import org.xtuml.bp.test.common.BaseTest;
import org.xtuml.bp.test.common.OrderedRunner;
import org.xtuml.bp.test.common.SearchUtilities;
import org.xtuml.bp.test.common.TestingUtilities;
import org.xtuml.bp.ui.explorer.ExplorerView;

@RunWith(OrderedRunner.class)
public class SearchTests extends BaseTest {
	private static SystemModel_c otherSystem;
	private static ModelClass_c searchClass;
	private static Operation_c searchOperation;
	
	public static boolean firstTime = true;
	
	public SearchTests() throws Exception {
		super(null, null);
	}

	private static String projectName = "TestSearch";

	@Override
	@Before
	public void setUp() {
		BaseTest.logFileCheckingEnabled = false;
	}
	
	@Override
	@Before
	public void tearDown() {
		BaseTest.logFileCheckingEnabled = true;
	}
	
	@Override
	@Before
	public void initialSetup() throws CoreException {

		if (!firstTime)
			return;
		loadProject(projectName);
		BaseTest.waitFor(300);
		m_sys = getSystemModel(projectName);
		m_sys = SystemModel_c.SystemModelInstance(Ooaofooa
				.getDefaultInstance(), new ClassQueryInterface_c() {

			public boolean evaluate(Object candidate) {
				return ((SystemModel_c) candidate).getName().equals(
						project.getName());
			}
		});

		CorePlugin.enableParseAllOnResourceChange();
		CorePlugin.getDefault().getPreferenceStore()
				.setValue(BridgePointPreferencesStore.REQUIRE_MASL_STYLE_IDENTIFIERS, false);

		BaseTest.dispatchEvents();

		IProject otherProject = TestingUtilities.createProject("TestSearchNoResults");
		otherSystem = TestingUtilities.getSystemModel(otherProject.getName());
		otherSystem.Newpackage();
		Package_c otherPack = Package_c.getOneEP_PKGOnR1401(otherSystem);
		otherPack.setName("NoResultPackage");
		
		Package_c pkg = Package_c.getOneEP_PKGOnR1401(m_sys);
		searchClass = ModelClass_c.getOneO_OBJOnR8001(PackageableElement_c.getManyPE_PEsOnR8000(pkg));
		searchOperation = Operation_c.getOneO_TFROnR115(searchClass);
	}

	@Test
	public void testCaseSensitivity() {
		SearchUtilities.configureAndRunSearch("find me", false, false, true,
				true, false, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		SearchUtilities.configureAndRunSearch("find me", false, true, true,
				true, false, ISearchPageContainer.WORKSPACE_SCOPE, "");	
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("find me", false,
						true, true, true, false, ISearchPageContainer.WORKSPACE_SCOPE,
						""));
	}
	
	@Test
	public void testRegularExpression() {
		SearchUtilities.configureAndRunSearch("Fi.*e", true, false, true,
				true, false, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		SearchUtilities.configureAndRunSearch("fi.*e", true, true, true,
				true, false, ISearchPageContainer.WORKSPACE_SCOPE, "");	
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("fi.*e", true,
						true, true, true, false, ISearchPageContainer.WORKSPACE_SCOPE,
						""));
	}
	
	@Test
	public void testActionLanguageOnlyUnderComponent() {
		SearchUtilities.configureAndRunSearch("Searched Text", false, false, true,
				false, false, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchedText"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("Searched Text"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("Searched Text", false,
						false, true, false, false, ISearchPageContainer.WORKSPACE_SCOPE,
						""));		
	}
	
	@Test
	public void testActionLanguageOnly() {
		SearchUtilities.configureAndRunSearch("Find me", false, false, true,
				false, false, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("Find me", false,
						false, true, false, false, ISearchPageContainer.WORKSPACE_SCOPE,
						""));		
	}
	
	@Test
	public void testDescriptionsOnly() {
		SearchUtilities.configureAndRunSearch("Find me", false, false, false,
				true, false, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("Find me", false,
						false, false, true, false, ISearchPageContainer.WORKSPACE_SCOPE,
						""));		 
	}
	@Test
	public void testDescriptionsOnlyUnderComponent() {
		SearchUtilities.configureAndRunSearch("Searched Text", false, false, false,
				true, false, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("Searched Text"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchedText"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("Searched Text", false,
						false, false, true, false, ISearchPageContainer.WORKSPACE_SCOPE,
				""));		
	}
	
	@Test
	public void testSelectedResourcesScope() throws PartInitException {
		searchWithSelectedScope(true);
	}

	@Test
	public void testEnclosingProjectScope() throws PartInitException {
		searchWithSelectedScope(false);
	}
	
	private void searchWithSelectedScope(boolean useSelection) throws PartInitException {
		ExplorerView.getExplorerTreeViewer().setSelection(new StructuredSelection(new Object[] {searchClass, searchOperation}));
		BaseTest.dispatchEvents(0);
		SearchUtilities.configureAndRunSearch("Find me", false, false, true,
				true, false, useSelection ? ISearchPageContainer.SELECTION_SCOPE : ISearchPageContainer.SELECTED_PROJECTS_SCOPE, "");
		BaseTest.dispatchEvents(0);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.xtuml.bp.ui.explorer.ExplorerView");
		ExplorerView.getExplorerTreeViewer().setSelection(new StructuredSelection(new Object[] {otherSystem}));
		ExplorerView.getExplorerTreeViewer().getControl().forceFocus();
		BaseTest.dispatchEvents(0);
		SearchUtilities.configureAndRunSearch("Find me", false, false, true,
				true, false, useSelection ? ISearchPageContainer.SELECTION_SCOPE : ISearchPageContainer.SELECTED_PROJECTS_SCOPE, "");
		BaseTest.dispatchEvents(0);
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.xtuml.bp.ui.explorer.ExplorerView");
		ExplorerView.getExplorerTreeViewer().setSelection(new StructuredSelection(new Object[] {otherSystem}));
		ExplorerView.getExplorerTreeViewer().getControl().forceFocus();
		BaseTest.dispatchEvents(0);
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("Find me", false,
						false, true, true, false, useSelection ? ISearchPageContainer.SELECTION_SCOPE : ISearchPageContainer.SELECTED_PROJECTS_SCOPE,
						""));
	}
	
	@Test
	public void testWorkingSetScope() {
		IWorkingSet workingSet = PlatformUI.getWorkbench()
				.getWorkingSetManager().createWorkingSet("searchWorkingSet",
						new IAdaptable[] { searchClass.getFile(), searchOperation.getFile() });
		PlatformUI.getWorkbench().getWorkingSetManager().addWorkingSet(
				workingSet);
		IWorkingSet otherWorkingSet = PlatformUI.getWorkbench()
				.getWorkingSetManager().createWorkingSet("otherWorkingSet",
						new IAdaptable[] { otherSystem.getFile() });
		PlatformUI.getWorkbench().getWorkingSetManager().addWorkingSet(
				otherWorkingSet);
		while(PlatformUI.getWorkbench().getDisplay().readAndDispatch());
		SearchUtilities.configureAndRunSearch("Find me", false, false, true,
				true, false, ISearchPageContainer.WORKING_SET_SCOPE, "searchWorkingSet");
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		SearchUtilities.configureAndRunSearch("Find me", false, false, true,
				true, false, ISearchPageContainer.WORKING_SET_SCOPE, "otherWorkingSet");	
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchClass"));
		assertNull("Search did not produce expected results.", SearchUtilities.findResultInView("SearchOperation"));
		assertTrue("Values were not persisted in Search dialog.",
				SearchUtilities.checkSearchDialogSettings("Find me", false,
						false, true, true, false, ISearchPageContainer.WORKING_SET_SCOPE,
						"otherWorkingSet"));
	}
	
	@Test
	public void testSelectedResourcesMultipleTimes() throws CoreException {
		// import the MicrowaveOven
		loadProject("MicrowaveOven");
		Package_c pkg = selectPackage("MicrowaveOven");
		// add the two test elements to the selection
		ExplorerView.getExplorerTreeViewer().setSelection(
				new StructuredSelection(new Object[] { pkg }));
		while(PlatformUI.getWorkbench().getDisplay().readAndDispatch());
		SearchUtilities.configureAndRunSearch("tube", false, false, true,
				true, false, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 18,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		ExplorerView.getExplorerTreeViewer().setSelection(
				new StructuredSelection(new Object[] { pkg }));
		SearchUtilities.configureAndRunSearch("watt", false, false, true,
				true, false, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 5,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		ExplorerView.getExplorerTreeViewer().setSelection(
				new StructuredSelection(new Object[] { pkg }));
		SearchUtilities.configureAndRunSearch("tube", false, false, true,
				true, false, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 18, 
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
	}

	public Package_c selectPackage(final String pkgName) throws CoreException {
		Package_c pkg = Package_c.getOneEP_PKGOnR1401(SystemModel_c.SystemModelInstance(Ooaofooa.getDefaultInstance(), new ClassQueryInterface_c() {
			@Override
			public boolean evaluate(Object candidate) {
				return ((SystemModel_c) candidate).getName().equals(pkgName);
			}
		}));
		assertNotNull(pkg);
		ExplorerView.getExplorerTreeViewer().setSelection(
				new StructuredSelection(new Object[] { pkg }));
		while(PlatformUI.getWorkbench().getDisplay().readAndDispatch());
		return pkg;
	}	

	private void checkCorrectEditorOpens(String expectedTitleText) throws CoreException {
		while(PlatformUI.getWorkbench().getDisplay().readAndDispatch());

        WorkbenchPart editor = SearchUtilities.openFirstMatch();
        if (editor != null) {
        	String title = editor.getPartName();
        	assertTrue("Expected diagram " + expectedTitleText + " wasn't opened. Found " + title, title.startsWith(expectedTitleText));
        	EditorUtil.closeEditor((IEditorPart) editor);
        } else {
        	assertNotNull("No editor was opened.", editor );
        }
	}

	@Test
	public void testFindNameOfClass() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("Internal", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("Internal Light"));
		checkCorrectEditorOpens("Microwave Oven");
	}
	
	@Test
	public void testFindNameOfAttribute() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("is_secure", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("is_secure"));
		// Don't check editor as attributes don't open on one
	}

	@Test
	public void testFindNameOfEvent() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("release", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 2,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("MO_D1: release"));
		checkCorrectEditorOpens("Door");
	}

	@Test
	public void testFindNameOfEDT() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("tube_wattage", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("tube_wattage"));
		checkCorrectEditorOpens("Datatypes");
	}

	@Test
	public void testFindNameOfEE() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("Architecture", false, true, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("Architecture"));
		checkCorrectEditorOpens("External Entities");
	}

	@Test
	public void testFindNameOfFunction() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("setup", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("setup"));
		// Functions don't open editor, so don't check
	}

	@Test
	public void testFindNameOfPackage() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("Functions", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("Functions"));
		checkCorrectEditorOpens("Functions");
	}

	@Test
	public void testFindNameOfAssociationAndRefAttr() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("R4", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 2,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("R4"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("DoorID (R4.'is accessed via')"));
		checkCorrectEditorOpens("Microwave Oven");
	}

	@Test
	public void testFindNameOfComponent() throws CoreException {
		// Note, in this one we switch things up and search the whole workspace
		SearchUtilities.configureAndRunSearch("Contained", false, false, false,
				false, true, ISearchPageContainer.WORKSPACE_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("Contained"));
		checkCorrectEditorOpens("SearchPackage");
	}

	@Test
	public void testFindKeylettersOfClass() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("MO_D", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 5,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("MO_D", SearchUtilities.ResultType.KEYLETTERS));
	}

	@Test
	public void testFindAssociationRolePhrase() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("provides", false, false, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 1,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
	}

	@Test
	public void testFindNameManyPlaces() throws CoreException {
		selectPackage("MicrowaveOven");
		SearchUtilities.configureAndRunSearch("Door", false, true, false,
				false, true, ISearchPageContainer.SELECTION_SCOPE, "");
		assertEquals("Search did not return expected result count.", 6,
				SearchResult_c.SearchResultInstances(Ooaofooa
						.getDefaultInstance(), null, false).length);
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("CloseDoor"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("OpenDoor"));
		assertNotNull("Search did not produce expected results.", SearchUtilities.findResultInView("DoorID"));
	}

}
