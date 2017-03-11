package com.github.jordicurto.autochecker;

import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<AutoCheckerApp> {
    public ApplicationTest() {
        super(AutoCheckerApp.class);
        AutoCheckerUtilTest test = new AutoCheckerUtilTest();
        test.runTest();
    }
}