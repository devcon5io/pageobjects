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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PageObjectsInjectorTest {

    @Mock
    private Description description;

    @Mock
    private WebElement element;

    @Test
    public void testInjectMethods() throws Exception {
        //prepare
        ChildMethodInjectTestGroup group = new ChildMethodInjectTestGroup();

        //act
        PageObjectsInjector.injectMethods(group);

        //assert
        //direct injection
        assertNotNull(group.child);
        //superclass injection
        assertNotNull(group.field);
    }

    @Test
    public void testInjectFields() throws Throwable {

        //prepare
        ChildFieldInjectTestGroup group = new ChildFieldInjectTestGroup();
        SeleniumContext ctx = SeleniumContext.builder().driver(() -> mock(WebDriver.class)).baseUrl("http://localhost")
                                             .build();
        when(ctx.getDriver().get().findElement(By.id("subgroup"))).thenReturn(element);
        //act
        ctx.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {

                PageObjectsInjector.injectFields(group);

            }
        }, description).evaluate();


        //assert
        //direct injection
        assertNotNull(group.child);
        //superclass injection
        assertNotNull(group.field);
        //element group injection
        assertNotNull(group.group);
        assertNotNull(group.group.field);
        assertNotNull(group.ctxGroup.field);
        assertNotNull(group.ctxGroup.field);

    }

    //// Test Page Object classes

    public static class FieldInjectTestGroup implements ElementGroup {

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;

        SubGroup group;

        @Locator(by = ID, value="subgroup")
        ContextualSubGroup ctxGroup;

    }

    public static class ChildFieldInjectTestGroup extends FieldInjectTestGroup {

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> child;

    }

    public static class SubGroup implements ElementGroup {
        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;
    }

    public static class ContextualSubGroup implements ElementGroup {

        private final SearchContext searchContext;

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;

        public ContextualSubGroup(SearchContext ctx){
            this.searchContext = ctx;
        }
    }

    public static class MethodInjectTestGroup implements ElementGroup {

        Supplier<WebElement> field;

        @Locator(by = ID, value = "testId")
        void setField(final Supplier<WebElement> field) {

            this.field = field;
        }
    }

    public static class ChildMethodInjectTestGroup extends MethodInjectTestGroup {

        Supplier<WebElement> child;

        @Locator(by = ID, value = "testId")
        void setChild(final Supplier<WebElement> child) {
            this.child = child;
        }
    }


}
