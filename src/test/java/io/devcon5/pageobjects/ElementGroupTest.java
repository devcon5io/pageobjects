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
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.SearchContext;
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

    @Rule
    public SeleniumTestContext selenium = new SeleniumTestContext();


    @Test(expected = IllegalStateException.class)
    public void testGetSearchContext_noTest_exception() throws Exception {

        subject.getSearchContext();
    }

    @Test
    public void testGetSearchContext_insideTest_driver() throws Throwable {

        SearchContext ctx = selenium.execute(() -> subject.getSearchContext());
        assertEquals(selenium.getMockDriver(), ctx);
    }

    @Test
    public void testGet_unqualified() throws Throwable {

        TestGroup group = selenium.execute(() -> subject.get(TestGroup.class));
        assertNotNull(group);
        assertEquals("unqualified", group.id);
    }

    @Test
    public void testGet_qualified() throws Throwable {

        TestGroup group = selenium.execute(() -> subject.get(TestGroup.class, TestQualifier.class));
        assertNotNull(group);
        assertEquals("qualified", group.id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_unusedGroup_exception() throws Throwable {

        selenium.execute(() -> subject.get(UnusedGroup.class));
    }

    @Test
    public void testLocateElements() throws Throwable {
        selenium.execute(() -> {
            subject.locateElements();
            return null;
        });

        assertNotNull(subject.get(TestGroup.class));
        assertEquals("generated", subject.get(TestGroup.class).id);
    }

    //// test page objects

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
}
