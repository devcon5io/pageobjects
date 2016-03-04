/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.devcon5.pageobjects;

import static org.junit.Assert.*;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumControlTest {

    /**
     * The class under test
     */
    @InjectMocks
    private SeleniumControl subject;

    @Mock
    private Description description;

    @Mock
    private WebDriver webDriver;

    private String basePath = "http://testBaseUrl";

    @Before
    public void setUp() throws Exception {

        subject = SeleniumControl.builder().baseUrl(basePath).driver(() -> webDriver).build();
    }

    @Test
    public void testLogin() throws Throwable {
        //prepare
        final AtomicReference<User> user = new AtomicReference<>();
        final AtomicBoolean loggedIn = new AtomicBoolean();
        final SeleniumControl ctrl = SeleniumControl.builder()
                                                   .baseUrl(basePath)
                                                   .driver(() -> webDriver)
                                                   .loginAction((u, d) -> {
                                                       user.set(u);
                                                       try {
                                                           Thread.sleep(50);
                                                       } catch (InterruptedException e) {
                                                           //
                                                       }
                                                   })
                                                   .build();
        final Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                SeleniumContext.currentContext().ifPresent(ctx -> {
                    ctrl.login(new User("test", "pw"));
                    loggedIn.set(ctrl.isLoggedIn());
                });
            }
        };
        //act
        ctrl.apply(stmt, description).evaluate();

        //assert
        assertTrue(loggedIn.get());
        assertEquals("test", user.get().getUsername());
        assertEquals("pw", user.get().getPassword());
    }

    @Test
    public void testLogout() throws Throwable {
        //prepare
        final AtomicBoolean loggedIn = new AtomicBoolean();
        final SeleniumControl ctrl = SeleniumControl.builder()
                                                   .baseUrl(basePath)
                                                   .driver(() -> webDriver)
                                                   .loginAction((u, d) -> {
                                                   })
                                                   .logoutAction((d) -> {
                                                   })
                                                   .build();
        final Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                SeleniumContext.currentContext().ifPresent(ctx -> {
                    ctrl.login(new User("test", "pw"));
                    ctrl.logout();
                    loggedIn.set(ctrl.isLoggedIn());
                });
            }
        };
        //act
        ctrl.apply(stmt, description).evaluate();

        //assert
        assertFalse(loggedIn.get());
    }

    @Test
    public void testIsLoggedIn_noLogin_false() throws Exception {

        assertFalse(subject.isLoggedIn());
    }

    @Test
    public void testGetDriver_outsideTest() throws Exception {

        //assert
        assertFalse(subject.getDriver().isPresent());
    }

    @Test
    public void testGetDriver_insideTest() throws Throwable {

        //prepare
        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                assertEquals(webDriver, subject.getDriver().get());

            }
        };
        subject.apply(stmt, description).evaluate();

        //assert


    }

    @Test
    public void testGetBaseUrl() throws Exception {

        //assert
        assertEquals("http://testBaseUrl", subject.getBaseUrl());
    }

    @Test
    public void testCurrentContext_outsideTest_notSet() throws Exception {

        assertFalse(subject.currentContext().isPresent());
    }

    @Test
    public void testCurrentContext_insideTest_set() throws Throwable {
        //prepare
        AtomicReference<SeleniumContext> ctx = new AtomicReference<>();

        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                ctx.set(subject.currentContext().get());
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertNotNull(ctx.get());
    }

    @Test
    public void testCurrentDriver_outsideTest_notSet() throws Exception {

        assertNotNull(subject.currentDriver());
        assertFalse(subject.currentDriver().isPresent());
    }

    @Test
    public void testCurrentDriver_insideTest_set() throws Throwable {
        //prepare
        AtomicReference<WebDriver> driver = new AtomicReference<>();

        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                driver.set(subject.currentDriver().get());
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertNotNull(driver.get());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetTestDuration_beforeTest() throws Exception {

        subject.getTestDuration();
    }

    @Test
    public void testGetTestDuration_afterTest() throws Throwable {
        //prepare
        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                Thread.sleep(50);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        Duration dur = subject.getTestDuration();
        assertTrue(dur.getNano() >= 50);
    }

    @Test
    public void testResolve_outsideTest_equals() throws Exception {
        //prepare
        //act
        String path = SeleniumContext.resolve("relativePath");
        //assert
        assertEquals("relativePath", path);
    }

    @Test
    public void testResolve_insideTest_baseUrlTrailingSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl/";
        String relPath = "relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    @Test
    public void testResolve_insideTest_relPathLeadingSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl";
        String relPath = "/relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    @Test
    public void testResolve_insideTest_baseUrlTrailing_and_relPathLeadingSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl/";
        String relPath = "/relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    @Test
    public void testResolve_insideTest_noSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl";
        String relPath = "relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    private void testResolvePathInsideTest(final String basePath, final String relPath, final String expected)
            throws Throwable {

        SeleniumControl ctx = SeleniumControl.builder().baseUrl(basePath).driver(() -> webDriver).build();
        AtomicReference<String> path = new AtomicReference<>();

        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                path.set(SeleniumContext.resolve(relPath));
            }
        };
        //act
        ctx.apply(stmt, description).evaluate();

        //assert
        assertEquals(expected, path.get());
    }

}
