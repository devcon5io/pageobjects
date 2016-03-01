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
