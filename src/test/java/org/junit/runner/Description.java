package org.junit.runner;

/**
 * This class, {@linkplain org.junit.rules.TestRule} and
 * {@linkplain org.junit.runners.model.Statement} exist purely so that the testcontainers dependency
 * can run without junit 4 dependencies on the classpath Testcontainers does not actually require
 * these unless we are using junit4 to run the tests, which I am not. But the tests will fail to run
 * without them
 * <p>
 * There is an open issue to resolve this on the testcontainers project, and this solution has been
 * taken from <a
 * href="https://github.com/testcontainers/testcontainers-java/issues/970#issuecomment-625044008">here</a>
 * as a workaround until the maintainers themselves release a 2.x version
 */
public class Description {

}
