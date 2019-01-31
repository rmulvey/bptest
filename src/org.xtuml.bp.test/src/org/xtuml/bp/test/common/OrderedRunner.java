package org.xtuml.bp.test.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class OrderedRunner extends BlockJUnit4ClassRunner {

	public OrderedRunner(Class aClass) throws InitializationError {
		super(aClass);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		final List<FrameworkMethod> list = super.computeTestMethods();
		try {
			final List<FrameworkMethod> copy = new ArrayList<FrameworkMethod>(list);
			Collections.sort(copy, MethodComparator.getFrameworkMethodComparatorForJUnit4());
			return copy;
		} catch (Throwable throwable) {
			//			logger.fatal("computeTestMethods(): Error while sorting test cases! Using default order (random).",
			//					throwable);
			return list;
		}
	}
}
