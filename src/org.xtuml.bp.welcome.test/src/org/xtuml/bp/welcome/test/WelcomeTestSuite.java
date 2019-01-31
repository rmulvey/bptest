package org.xtuml.bp.welcome.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.TestSuite;

/**
* Test all areas of the core
*/
@RunWith(Suite.class)
@Suite.SuiteClasses({
	
	GlobalTestSetupClass.class,
	WelcomePageTestGPS.class,
	WelcomePageTest.class,
	WelcomePageTestMetamodel.class,	
})
public class WelcomeTestSuite extends TestSuite {


}