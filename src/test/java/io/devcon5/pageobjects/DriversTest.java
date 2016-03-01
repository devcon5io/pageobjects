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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 *
 */
public class DriversTest {

    @Test
    public void testFirefoxDriver() throws Exception {
        assertTrue(Drivers.Firefox.get() instanceof FirefoxDriver);
    }

    @Test
    public void testIEDriver() throws Exception {
        assertTrue(Drivers.IExplorer.get() instanceof InternetExplorerDriver);
    }

    @Test
    public void testChromeDriver() throws Exception {
        assertTrue(Drivers.Chrome.get() instanceof ChromeDriver);
    }

    @Test
    public void testSafariDriver() throws Exception {
        assertTrue(Drivers.Safari.get() instanceof SafariDriver);
    }

    @Test
    public void testHeadlessDriver() throws Exception {
        assertTrue(Drivers.Headless.get() instanceof HtmlUnitDriver);
    }

}
