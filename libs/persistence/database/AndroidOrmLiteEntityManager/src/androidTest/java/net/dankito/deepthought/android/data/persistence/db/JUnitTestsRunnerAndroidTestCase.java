package net.dankito.deepthought.android.data.persistence.db;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Log;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import net.dankito.deepthought.data.model.DeepThoughtTestBase;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 10/12/14.
 */
public class JUnitTestsRunnerAndroidTestCase extends AndroidTestCase {

  protected JUnitTestsRunnerAndroidTestSuite testSuite = null;

  public static Test suite() {
    Log.e("Tests", "Test suite()");
    return new JUnitTestsRunnerAndroidTestSuite(DeepThoughtTestBase.class);
  }


  public JUnitTestsRunnerAndroidTestCase() {
    this.testSuite = new JUnitTestsRunnerAndroidTestSuite(DeepThoughtTestBase.class);
  }

  @Override
  public void testAndroidTestCaseSetupProperly() {
    super.testAndroidTestCaseSetupProperly();
  }

  @Override
  public void setContext(Context context) {
    super.setContext(context);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public int countTestCases() {
//    return super.countTestCases();
    return testSuite.countTestCases();
  }

  @Override
  public void run(TestResult result) {
    super.run(result);
  }

  @Override
  public TestResult run() {
    return super.run();
  }

  @Override
  protected TestResult createResult() {
    return super.createResult();
  }


  public static class JUnitTestsRunnerAndroidTestSuite extends TestSuite {

    protected List<Test> testCases = new ArrayList<>();


    public JUnitTestsRunnerAndroidTestSuite(Class<?> theClass) {
      super();
      Log.e("Tests", "Called constructor");

      int countTestCases = countTestCases();
      if(countTestCases == 0)
        findTestCases(theClass);
    }

    protected void findTestCases(Class<?> theClass) {
      Method[] debug = theClass.getMethods();

      for(Method method : theClass.getMethods()) {
        if(isTestMethod(method))
          testCases.add(createTest(theClass, method.getName()));
      }
    }

    protected boolean isTestMethod(Method method) {
      return method.isAnnotationPresent(org.junit.Test.class) || method.getName().startsWith("test");
    }

    @Override
    public void addTest(Test test) {
      super.addTest(test);
    }

    @Override
    public void runTest(Test test, TestResult result) {
      super.runTest(test, result);
    }

    @Override
    public int countTestCases() {
      if(testCases.size() != 0)
        return testCases.size();

      return super.countTestCases();
    }
  }
}
