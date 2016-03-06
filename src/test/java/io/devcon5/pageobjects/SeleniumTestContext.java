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

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Test Rule to simulate the selenium context rule
 */
public class SeleniumTestContext implements TestRule {

    private SeleniumControl ctx;
    private WebDriver mockDriver;
    private Description mockDescription;

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
    public <T> T execute(Callable<T> run) throws Throwable {

        AtomicReference<T> result = new AtomicReference<>();
        ctx.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                result.set(run.call());
            }
        }, mockDescription).evaluate();
        return result.get();
    }

    /**
     * Executes the callable in the context of the selenium context provided by the test
     *
     * @param run
     *
     * @return
     *
     * @throws Throwable
     */
    public void execute(Runnable run) throws Throwable {

        ctx.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                run.run();
            }
        }, mockDescription).evaluate();
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                try {
                    mockDriver = mock(WebDriver.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS)
                                                                     .extraInterfaces(JavascriptExecutor.class));
                    mockDescription = mock(Description.class);
                    ctx = SeleniumControl.builder().driver(() -> mockDriver).baseUrl("http://localhost").build();
                    statement.evaluate();
                } finally {

                }
            }
        };
    }

    public WebDriver getMockDriver() {

        return mockDriver;
    }

    public Description getMockDescription() {

        return mockDescription;
    }
}
