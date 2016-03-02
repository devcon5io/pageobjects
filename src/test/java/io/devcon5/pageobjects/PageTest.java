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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PageTest {

    @Rule
    public SeleniumTestContext selenium = new SeleniumTestContext();

    @Mock
    private WebElement webElement;

    @Test
    public void testNavigateTo_noDriver() throws Exception {
        //prepare
        Page page = new Page(){};

        //act
        Optional<WebElement> element = page.navigateTo();

        //assert
        assertFalse(element.isPresent());

    }

    @Test
    public void testNavigateTo_noLocation() throws Throwable {
        //prepare
        Page page = new Page(){};

        //act
        Optional<WebElement> element = selenium.execute(() -> page.navigateTo());

        //assert
        assertFalse(element.isPresent());
    }

    @Test
    public void testNavigateTo_withUrlLocation() throws Throwable {
        //prepare
        Page page = new TestUrlPage();

        //act
        Optional<WebElement> element = selenium.execute(() -> page.navigateTo());

        //assert
        assertFalse(element.isPresent());
        verify(selenium.getMockDriver().navigate()).to("http://localhost/contextRoot");
    }

    @Test
    public void testNavigateTo_withNavElementLocation() throws Throwable {
        //prepare
        Page page = new TestElementPage();
        when(selenium.getMockDriver().findElement(By.id("testId"))).thenReturn(webElement);

        //act
        Optional<WebElement> element = selenium.execute(() -> page.navigateTo());

        //assert
        assertTrue(element.isPresent());
        assertEquals(webElement, element.get());
    }

    @Test
    public void testNavigateTo_page_byUrl() throws Throwable {
        //prepare
        when(((JavascriptExecutor)selenium.getMockDriver()).executeScript(anyString())).thenReturn("complete");
        //act
        TestUrlPage page = selenium.execute(() -> Page.navigateTo(TestUrlPage.class));

        //assert
        assertNotNull(page);
        verify(selenium.getMockDriver().navigate()).to("http://localhost/contextRoot");
    }

    @Test
    public void testNavigateTo_page_byElement() throws Throwable {
        //prepare
        when(selenium.getMockDriver().findElement(By.id("testId"))).thenReturn(webElement);
        when(((JavascriptExecutor)selenium.getMockDriver()).executeScript(anyString())).thenReturn("complete");

        //act
        TestElementPage page = selenium.execute(() -> Page.navigateTo(TestElementPage.class));

        //assert
        assertNotNull(page);
        verify(webElement).click();
    }

    @Locator("contextRoot")
    public static class TestUrlPage implements Page {

    }

    @Locator(by = Locator.ByLocator.ID, value="testId")
    public static class TestElementPage implements Page {

    }

}
