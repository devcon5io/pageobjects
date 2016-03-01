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
