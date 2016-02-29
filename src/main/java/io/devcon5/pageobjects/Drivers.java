package io.devcon5.pageobjects;

import java.util.function.Supplier;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * Choice of Drivers to use for Selenium testing
 */
public enum Drivers implements Supplier<WebDriver> {

    Firefox {
        @Override
        public WebDriver get() {
            return new FirefoxDriver();
        }
    },
    IExplorer {
        @Override
        public WebDriver get() {
            return new InternetExplorerDriver();
        }
    },
    Chrome {
        @Override
        public WebDriver get() {
            return new InternetExplorerDriver();
        }
    },
    Safari {
        @Override
        public WebDriver get() {
            return new SafariDriver();
        }
    },
    Headless{
        @Override
        public WebDriver get() {
            return new HtmlUnitDriver();
        }
    }

}
