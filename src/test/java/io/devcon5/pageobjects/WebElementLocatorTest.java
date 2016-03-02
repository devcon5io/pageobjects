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
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebElementLocatorTest {

    @Mock
    private WebDriver driver;

    @Mock
    private Description description;

    @Mock
    private SearchContext searchContext;

    @Mock
    private WebElement webElement;

    @Mock
    private Locator locator;

    private SeleniumContext ctx;

    @Before
    public void setUp() throws Exception {

        this.ctx = SeleniumContext.builder().driver(() -> driver).baseUrl("http://localhost").build();
    }

    /**
     * Executes the callable in the context of the selenium context provided by the test
     * @param run
     * @param <T>
     * @return
     * @throws Throwable
     */
    public <T> T execute(Callable<T> run) throws Throwable {

        AtomicReference<T> result = new AtomicReference<>();
        ctx.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                result.set(run.call());
            }
        }, description).evaluate();
        return result.get();
    }


    @Test
    public void testLocate() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(30);
        when(driver.findElement(By.id("testId"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        //act
        WebElement element = execute(() -> WebElementLocator.locate(locator));

        //assert
        assertNotNull(element);
        assertEquals(webElement, element);
    }

    @Test
    public void testLocate_context() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(30);
        when(webElement.isDisplayed()).thenReturn(true);
        when(searchContext.findElement(By.id("testId"))).thenReturn(webElement);

        //act
        WebElement element = WebElementLocator.locate(searchContext, locator);

        //assert
        assertNotNull(element);
        assertEquals(webElement, element);
    }

    @Test(expected = TimeoutException.class)
    public void testLocate_context_timeout() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(1);
        when(webElement.isDisplayed()).thenReturn(false);
        when(searchContext.findElement(By.id("testId"))).thenReturn(webElement);

        //act
        Instant start = Instant.now();
        try {
            WebElementLocator.locate(searchContext, locator);
        } finally {
            Duration dur = Duration.between(start, Instant.now());
            assertTrue(dur.compareTo(Duration.ofMillis(950)) > 0);
        }
    }


    @Test
    public void testWaitForElement_context() throws Throwable {

        when(searchContext.findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertNotNull(WebElementLocator.waitForElement(searchContext, By.id("test"), 10));
    }

    @Test
    public void testWaitForElement_noDriver_noElement() throws Throwable {

        when(driver.findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertFalse(WebElementLocator.waitForElement(By.id("test"), 10).isPresent());
    }

    @Test
    public void testWaitForElement_noContext() throws Throwable {

        when(driver.findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        execute(() -> {
            assertTrue(WebElementLocator.waitForElement(By.id("test"), 10).isPresent());
            return null;
        });
    }

    @Test(expected = TimeoutException.class)
    public void testWaitForElement_noContext_timeout() throws Throwable {

        when(driver.findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        execute(() -> {
            assertTrue(WebElementLocator.waitForElement(By.id("test"), 1).isPresent());
            return null;
        });
    }


}
