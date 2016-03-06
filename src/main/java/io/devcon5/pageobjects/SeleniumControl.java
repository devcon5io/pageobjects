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

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.inkstand.scribble.rules.ExternalResource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

/**
 * Context for running selenium based tests. After initialization, the context is kept as a thread local so that
 * PageObjects may access it to obtain the current state of the driver and test.
 */
public class SeleniumControl extends ExternalResource {

    private static final Logger LOG = getLogger(SeleniumControl.class);

    /**
     * The base URL for the current test execution.
     */
    private String baseUrl;

    /**
     * Action to be performed on Login
     */
    private BiConsumer<User, WebDriver> loginAction;

    /**
     * Action to be performed for logout
     */
    private Consumer<WebDriver> logoutAction;

    /**
     * Flag to indicate if login has been performed
     */
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);

    /**
     * Action to initialize the web driver
     */
    private Optional<Consumer<WebDriver.Options>> driverInit;

    private Instant startTime;

    private Duration testDuration;
    private Supplier<WebDriver> driverProvider;
    private Optional<SeleniumContext> managedContext;

    @Override
    protected void before() throws Throwable {

        this.managedContext = getSeleniumContext();
        this.managedContext.ifPresent(SeleniumContext::init);
        SeleniumContext.currentContext().get().setBaseUrl(baseUrl);
        SeleniumContext.currentDriver().ifPresent(d -> {
            d.get(baseUrl);
            driverInit.ifPresent(di -> di.accept(d.manage()));
        });
        this.startTime = Instant.now();
    }

    @Override
    protected void after() {

        final Instant finishTime = Instant.now();
        this.managedContext.ifPresent(SeleniumContext::destroy);
        this.testDuration = Duration.between(this.startTime, finishTime);
        LOG.info("Test executed in {} s", this.testDuration.getSeconds());
    }

    @Override
    protected void beforeClass() throws Throwable {

        before();
    }

    @Override
    protected void afterClass() {

        after();
    }

    /**
     * Checks, if a context is already present. If that is the case, an empty optional is returned.
     * If no context is initialized, a new context is created using the driver provider of the test rule
     * @return
     *  an optional containing a managed context or the empty optional if a context is already active
     */
    private Optional<SeleniumContext> getSeleniumContext() {

        return Optional.ofNullable((SeleniumContext) SeleniumContext.currentContext()
                                                                    .map(c -> null)
                                                                    .orElse(new SeleniumContext(driverProvider)));
    }

    /**
     * Performs the login action with the specified user
     *
     * @param user
     *         the user to login
     */
    public final void login(User user) {
        currentDriver().ifPresent(d -> {
            loginAction.accept(user, d);
            loggedIn.set(true);
        });
    }

    /**
     * Performs the logout action
     */
    public final void logout() {
        currentDriver().ifPresent(d -> {
            this.logoutAction.accept(d);
            loggedIn.set(false);
        });
    }

    /**
     * Indicates if a user is logged in to the application
     *
     * @return
     */
    public boolean isLoggedIn() {

        return loggedIn.get();
    }

    /**
     * The base URL of the application to test
     *
     * @return the string representing the base URL. All relative URLs (i.e. in the page object model) must be relative
     * to this page
     */
    public String getBaseUrl() {

        return baseUrl;
    }

    /**
     * Returns the driver of this context.
     *
     * @return may be null if the test is not running.
     */
    public Optional<WebDriver> getDriver() {

        return SeleniumContext.currentDriver();
    }

    /**
     * Returns the current context. The context is only available during test execution
     *
     * @return an Optional holding the current context.
     */
    public Optional<SeleniumContext> currentContext() {

        return SeleniumContext.currentContext();
    }

    /**
     * Returns the duration of the test execution.
     *
     * @return the duration of the test execution
     */
    public Duration getTestDuration() {

        return Optional.ofNullable(this.testDuration).orElseThrow(() -> new IllegalStateException("Test not finished"));
    }

    /**
     * Returns the currentContext driver. If this method is invoked outside of a test execution, the returned Optional
     * is empty
     *
     * @return the optional of a driver
     */
    public  Optional<WebDriver> currentDriver() {

        return currentContext().flatMap(ctx -> ctx.getDriver());
    }

    /**
     * Creates a new context builder for fluent setup and instantiation.
     *
     * @return a new builder
     */
    public static SeleniumContextBuilder builder() {

        return new SeleniumContextBuilder();
    }

    /**
     * Builder for creating a Selenium test context
     */
    public static class SeleniumContextBuilder {

        private Supplier<WebDriver> driver;

        private String baseUrl;

        private BiConsumer<User, WebDriver> loginAction;

        private Consumer<WebDriver> logoutAction;

        private Consumer<WebDriver.Options> optionsInitializer;

        SeleniumContextBuilder() {

        }

        public SeleniumContextBuilder driver(Supplier<WebDriver> driver) {

            this.driver = driver;
            return this;
        }

        public SeleniumContextBuilder baseUrl(String baseUrl) {

            this.baseUrl = baseUrl;
            return this;
        }

        public SeleniumContextBuilder loginAction(BiConsumer<User, WebDriver> loginAction) {

            this.loginAction = loginAction;
            return this;
        }

        public SeleniumContextBuilder logoutAction(Consumer<WebDriver> logoutAction) {

            this.logoutAction = logoutAction;
            return this;
        }

        public SeleniumContextBuilder driverOptions(Consumer<WebDriver.Options> optionsInitializer) {

            this.optionsInitializer = optionsInitializer;
            return this;
        }

        public SeleniumControl build() {
            final SeleniumControl ctx = new SeleniumControl();
            ctx.baseUrl = this.baseUrl;
            ctx.driverProvider = this.driver;
            ctx.driverInit = Optional.ofNullable(this.optionsInitializer);
            ctx.loginAction = this.loginAction;
            ctx.logoutAction = this.logoutAction;
            return ctx;

        }
    }
}
