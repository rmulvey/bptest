//=====================================================================
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
//=====================================================================
package org.xtuml.bp.model.compare.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestSuite;

/**
* Test all areas of compare and merge
*/
@RunWith(Suite.class)
@Suite.SuiteClasses({
	GlobalTestSetupClass.class,
// See issue 8612 (https://support.onefact.net/issues/8612#note-3)	
//	ModelComparisonTests.class,
	ElementOrderingTests.class,
	ModelMergeTests.class,
	ModelMergeTests2.class,
})
public class ModelCompareTestSuite extends TestSuite {


}