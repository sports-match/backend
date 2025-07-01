package com.srr.architecture;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite that runs all architecture tests
 */
@Suite
@SelectClasses({
    ArchitectureTest.class,
    ControllerLayerTest.class,
    ServiceLayerTest.class,
    EntityRepositoryPatternTest.class,
    PaginationConventionTest.class,
    SecurityConventionTest.class
})
public class AllArchitectureTests {
    // This class serves as a test suite runner
}
