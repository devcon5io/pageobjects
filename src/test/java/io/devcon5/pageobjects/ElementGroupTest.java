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

import static io.devcon5.pageobjects.Locator.ByLocator.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ElementGroupTest {

    /**
     * The class under test
     */
    private ElementGroup subject = new ElementGroup() {

        @Locator(by = ID,
                 value = "testId")
        Supplier<WebElement> element;

        TestGroup group = new TestGroup("unqualified");

        @TestQualifier
        TestGroup unqualifiedGroup = new TestGroup("qualified");

    };

    public static class TestGroup implements ElementGroup {

        String id;

        public TestGroup() {

            id = "generated";
        }

        public TestGroup(final String id) {

            this.id = id;
        }
    }

    public static class UnusedGroup implements ElementGroup {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Qualifier
    public @interface TestQualifier {

    }

    @Mock
    private WebDriver driver;

    @Mock
    private Description description;

    private SeleniumContext ctx;

    @Before
    public void setUp() throws Exception {

        this.ctx = SeleniumContext.builder().driver(() -> driver).baseUrl("http://localhost").build();
    }

    /**
     * Executes the callable in the context of the selenium context provided by the test
     *
     * @param run
     * @param <T>
     *
     * @return
     *
     * @throws Throwable
     */
    private <T> T execute(Callable<T> run) throws Throwable {

        AtomicReference<T> result = new AtomicReference<>();
        ctx.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                result.set(run.call());
            }
        }, description).evaluate();
        return result.get();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSearchContext_noTest_exception() throws Exception {

        subject.getSearchContext();
    }

    @Test
    public void testGetSearchContext_insideTest_driver() throws Throwable {

        SearchContext ctx = execute(() -> subject.getSearchContext());
        assertEquals(driver, ctx);
    }

    @Test
    public void testGet_unqualified() throws Throwable {

        TestGroup group = execute(() -> subject.get(TestGroup.class));
        assertNotNull(group);
        assertEquals("unqualified", group.id);
    }

    @Test
    public void testGet_qualified() throws Throwable {

        TestGroup group = execute(() -> subject.get(TestGroup.class, TestQualifier.class));
        assertNotNull(group);
        assertEquals("qualified", group.id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_unusedGroup_exception() throws Throwable {

        execute(() -> subject.get(UnusedGroup.class));
    }

    @Test
    public void testLocateElements() throws Throwable {
        execute(() -> {
            subject.locateElements();
            return null;
        });

        assertNotNull(subject.get(TestGroup.class));
        assertEquals("generated", subject.get(TestGroup.class).id);
    }
}
