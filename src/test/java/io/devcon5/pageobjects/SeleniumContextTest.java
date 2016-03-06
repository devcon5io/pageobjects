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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumContextTest {

    @Mock
    private Description description;

    @Mock
    private WebDriver webDriver;

    /**
     * The class under test
     */
    private SeleniumContext subject;

    @Before
    public void setUp() throws Exception {
        subject = new SeleniumContext(() -> webDriver);
    }

    @Test
    public void testInit_destroy() throws Exception {

        subject.init();
        try {
            assertTrue(subject.getDriver().isPresent());
            assertTrue(SeleniumContext.currentContext().isPresent());
            assertTrue(SeleniumContext.currentDriver().isPresent());
        } finally {
            subject.destroy();
            assertFalse(subject.getDriver().isPresent());
            assertFalse(SeleniumContext.currentContext().isPresent());
            assertFalse(SeleniumContext.currentDriver().isPresent());
        }
    }


    @Test
    public void testCurrentContext_outsideTest_empty() throws Exception {
        assertFalse(SeleniumContext.currentContext().isPresent());
    }

    @Test
    public void testCurrentDriver_outsideTest_empty() throws Exception {
        assertFalse(SeleniumContext.currentDriver().isPresent());
    }

    @Test
    public void testGetDriver_outsideTest_empty() throws Exception {
        assertFalse(subject.getDriver().isPresent());
    }

    @Test
    public void testSetGetBaseUrl() throws Exception {
        subject.setBaseUrl("http://localhost");
        assertEquals("http://localhost", subject.getBaseUrl());
    }

    @Test(expected = NullPointerException.class)
    public void testSetBaseUrl_null() throws Exception {
        subject.setBaseUrl(null);
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
