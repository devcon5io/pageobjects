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

import java.util.function.Supplier;

import org.junit.Test;
import org.openqa.selenium.WebElement;

/**
 *
 */
public class PageObjectsInjectorTest {

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
    public void testInjectFields() throws Exception {

        //prepare
        ChildFieldInjectTestGroup group = new ChildFieldInjectTestGroup();

        //act
        PageObjectsInjector.injectFields(group);

        //assert
        //direct injection
        assertNotNull(group.child);
        //superclass injection
        assertNotNull(group.field);
        //element group injection
        assertNotNull(group.group);
        assertNotNull(group.group.field);

    }

    public static class FieldInjectTestGroup implements ElementGroup {

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;

        SubGroup group;

    }

    public static class ChildFieldInjectTestGroup extends FieldInjectTestGroup {

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> child;

    }

    public static class SubGroup implements ElementGroup {
        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;
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
