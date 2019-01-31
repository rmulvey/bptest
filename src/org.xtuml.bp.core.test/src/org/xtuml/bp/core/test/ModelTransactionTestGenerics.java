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

package org.xtuml.bp.core.test;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.tools.AbstractTool;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.xtuml.bp.core.Attribute_c;
import org.xtuml.bp.core.ClassIdentifier_c;
import org.xtuml.bp.core.EventIgnored_c;
import org.xtuml.bp.core.ModelClass_c;
import org.xtuml.bp.core.Ooaofooa;
import org.xtuml.bp.core.Package_c;
import org.xtuml.bp.core.common.ClassQueryInterface_c;
import org.xtuml.bp.core.common.IdAssigner;
import org.xtuml.bp.core.ui.DeleteAction;
import org.xtuml.bp.core.ui.RemoveFromIdentifierOnO_ATTRWizard;
import org.xtuml.bp.core.ui.Selection;
import org.xtuml.bp.test.TestUtil;
import org.xtuml.bp.test.common.BaseTest;
import org.xtuml.bp.test.common.CanvasTestUtils;
import org.xtuml.bp.test.common.OrderedRunner;
import org.xtuml.bp.test.common.TransactionListener;
import org.xtuml.bp.test.common.UITestingUtilities;
import org.xtuml.bp.ui.canvas.CanvasTransactionListener;
import org.xtuml.bp.ui.properties.EventIgnoredSM_EIGNPropertySource;

@RunWith(OrderedRunner.class)
public class ModelTransactionTestGenerics extends BaseTest {
	
	@Rule public TestName name = new TestName();

	private static String result_folder;
	private Ooaofooa thisModelRoot;
	private static boolean initialized = false;

	public ModelTransactionTestGenerics(){
		super(null, null);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		BaseTest.dispatchEvents(0);
		if (!initialized) {
			loadProject("testTransaction");
			initialized = true;
		}
		IdAssigner.setSeedOfAllInstances(name.getMethodName().hashCode());

		thisModelRoot = modelRoot;
		result_folder = new String(m_workspace_path
				+ "/expected_results/TransactionTest/"); //$NON-NLS-1$			

		CanvasTransactionListener.disableReconciler();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		CanvasTransactionListener.enableReconciler();
	}
	
	@Test
	public void testEarlyReturnExitTransactionWizard() {
		ModelClass_c mclass = getModelClassByName("EarlyExitA");

		assertNotNull(mclass);
		Attribute_c attr = Attribute_c.getOneO_ATTROnR102(mclass);
		assertNotNull(attr);

		Selection.getInstance().clear();
		StructuredSelection sel = new StructuredSelection(attr);

		RemoveFromIdentifierOnO_ATTRWizard wizard = new RemoveFromIdentifierOnO_ATTRWizard();
		wizard.v_Identifier = ClassIdentifier_c.getOneO_IDOnR104(mclass);
		wizard.init(PlatformUI.getWorkbench(), sel, m_bp_tree);
		// close the warning dialog
		TestUtil.dismissDialog(500);
		wizard.performFinish();

		// verify that the active transaction is null, meaning that
		// the transaction was cancelled
		assertNull("The transaction was not cancelled after an early exit.",
				getSystemModel().getTransactionManager().getActiveTransaction());
	}

	@Test
	public void testEarlyReturnExitTransaction() {
		DeleteAction action = new DeleteAction(null);

		ModelClass_c mclass = getModelClassByName("EarlyExitA");

		// assumes that the first attribute for the above class
		// is used to formalize an association
		Attribute_c attribute = Attribute_c.getOneO_ATTROnR102(mclass);

		Selection.getInstance().clear();
		Selection.getInstance().addToSelection(attribute);

		// close the warning dialog
		TestUtil.dismissDialog(500);

		action.run();

		assertNull("Transaction was not cancelled after an early return.",
				getSystemModel().getTransactionManager().getActiveTransaction());

	}

	@Test
	public void testEarlyExceptionExitTransactionInProperties() {
		// pass a null property value so that an exception will occur
		EventIgnoredSM_EIGNPropertySource source = new EventIgnoredSM_EIGNPropertySource(
				new EventIgnored_c(modelRoot));
		source.setPropertyValue("Descrip", null);

		assertTrue("Log file was not present as expected.", logFileExists());

		assertNull(
				"Transaction was not cancelled after an early exit due to an exception.",
				getSystemModel().getTransactionManager().getActiveTransaction());
		
		// clear the log view as we expect a NPE 
		BaseTest.clearErrorLogView();
	}

	// Testing action/transaction that will result in both ooaofooa and ooaofgraphics changes
	@Test
	public void testShapeCreationTransactionThruCanvas() throws Exception {
		// make the log is clear before starting the tests
		BaseTest.clearResultLogger();
		TransactionListener listener = new TransactionListener();
		getSystemModel().getTransactionManager().addTransactionListener(
				listener);

		Package_c[] pkgs = Package_c.PackageInstances(modelRoot);
		Package_c dom = null;
		for (Package_c pkg : pkgs) {
			if (pkg.getName().equalsIgnoreCase("A Subsystem"))
				dom = pkg;
		}
		CanvasTestUtils.openCanvasEditor(dom);
		
		SequenceTestsGenerics.resizeMainWindow();

		TestUtil.okToDialog(100);
		
		// Create Subsystem in domain, as this forces 
		// execution of ModelSpecification_c.Elementcreated() (the one native operation that
		// generates model change events)
		AbstractTool tool = UITestingUtilities.getTool("Package"); //$NON-NLS-1$
		Ooaofooa.setPersistEnabled(true);
		UITestingUtilities.activateTool(tool);
		CanvasTestUtils.createMouseEvent(100, 100, "MouseDown"); //$NON-NLS-1$
		CanvasTestUtils.createMouseEvent(200, 200, "MouseMove"); //$NON-NLS-1$
		CanvasTestUtils.createMouseEvent(200, 200, "MouseUp"); //$NON-NLS-1$
		UITestingUtilities.deactivateTool(tool);

		listener.WaitForTransactionUnderReview();
		getSystemModel().getTransactionManager().removeTransactionListener(
				listener);
		// See issue 9505, for now just ignore this test
//		BaseTest.compareAndOutputResults(result_folder
//				+ "shape_creation_transaction_generics.exp"); //$NON-NLS-1$
	}

	public void setGenerateResults() throws Exception {
		BaseTest.doCreateResults = true;

		testShapeCreationTransactionThruCanvas();

		setUp();

		testShapeCreationTransactionThruCanvas();

		BaseTest.doCreateResults = false;
	}

	private boolean logFileExists() {
		IPath logfile_path = new Path(m_logfile_path);
		File logfile = logfile_path.toFile();
		if (logfile.exists()) {
			logfile.delete();
			return true;
		} else {
			return false;
		}
	}

	private ModelClass_c getModelClassByName(final String name) {
		ModelClass_c mclass = ModelClass_c.ModelClassInstance(modelRoot,
				new ClassQueryInterface_c() {

					public boolean evaluate(Object candidate) {
						ModelClass_c mclass = (ModelClass_c) candidate;
						if (mclass.getName().equals(name)) {
							return true;
						}
						return false;
					}

				});
		assertNotNull(mclass);

		return mclass;
	}

	private Package_c getPackageByName(final String name) {
		Package_c pkg = Package_c.PackageInstance(modelRoot,
				new ClassQueryInterface_c() {

					public boolean evaluate(Object candidate) {
						Package_c mclass = (Package_c) candidate;
						if (mclass.getName().equals(name)) {
							return true;
						}
						return false;
					}

				});
		assertNotNull(pkg);

		return pkg;
	}

}
