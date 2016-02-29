package io.devcon5.pageobjects;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

import io.inkstand.scribble.rules.ExternalResource;

/**
 * Context for running selenium based tests. After initialization, the context is kept as a thread local so that
 * PageObjects may access it to obtain the current state of the driver and test.
 */
public class SeleniumContext extends ExternalResource {

    private static final Logger LOG = getLogger(SeleniumContext.class);

    private static ThreadLocal<SeleniumContext> CONTEXT = new ThreadLocal<>();

    private WebDriver driver;

    private String baseUrl;

    private BiConsumer<User, WebDriver> loginAction;

    private Consumer<WebDriver> logoutAction;

    private AtomicBoolean loggedIn = new AtomicBoolean(false);

    private Duration loginTime;

    private Consumer<WebDriver.Options> driverInit;

    private long startTime;

    private long finishTime;

    private Duration testDuration;

    @Override
    protected void before() throws Throwable {
        driver.get(baseUrl);
        driverInit.accept(driver.manage());
        CONTEXT.set(this);
        this.startTime = System.nanoTime();
    }

    @Override
    protected void after() {
        this.finishTime = System.nanoTime();
        getDriver().ifPresent(d -> d.quit());
        CONTEXT.set(null);
        this.testDuration = Duration.ofNanos(this.finishTime - this.startTime);
        LOG.info("Test executed in {} s", this.testDuration.getSeconds());
    }

    /**
     * Performs the login action with the specified user
     *
     * @param user
     *         the user to login
     */
    public final void login(User user) {
        this.loginTime = ExecutionStopWatch.measure(() -> currentDriver().ifPresent(d -> {
            loginAction.accept(user, d);
            loggedIn.set(true);
        })).getDuration();
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
     * Returns the duration of the login process
     *
     * @return
     */
    public Duration getLoginTime() {
        return loginTime;
    }

    /**
     * Returns the driver of this context.
     *
     * @return may be null if the test is not running.
     */
    public Optional<WebDriver> getDriver() {
        return Optional.ofNullable(driver);
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
     * Returns the current context. The context is only available during test execution
     *
     * @return an Optional holding the current context.
     */
    public static Optional<SeleniumContext> currentContext() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Returns the duration of the test execution.
     * @return
     *  the duration of the test execution
     */
    public Duration getTestDuration() {
        return Optional.ofNullable(this.testDuration).orElseThrow(() -> new IllegalStateException("Test not finished"));
    }

    /**
     * Resolves the URL path relative to the base URL.
     *
     * @param relativePath
     *         the relative path within the application
     *
     * @return the absolute path of the application's base URL and the relative path
     */
    public static String resolve(String relativePath) {
        return currentContext().map(SeleniumContext::getBaseUrl)
                               .map(base -> {
                                   final StringBuilder buf = new StringBuilder(16);
                                   buf.append(base);
                                   if (base.charAt(base.length() - 1) != '/') {
                                       buf.append('/');
                                   }
                                   if (relativePath.startsWith("(/")) {
                                       buf.append(relativePath.substring(1));
                                   } else {
                                       buf.append(relativePath);
                                   }
                                   return buf.toString();
                               }).orElse(relativePath);
    }

    /**
     * Returns the currentContext driver. If this method is invoked outside of a test execution, the returned Optional
     * is empty
     *
     * @return the optional of a driver
     */
    public static Optional<WebDriver> currentDriver() {
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

        private SeleniumContextBuilder() {
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

        public SeleniumContext build() {
            SeleniumContext ctx = new SeleniumContext();
            ctx.baseUrl = this.baseUrl;
            ctx.driver = this.driver.get();
            ctx.driverInit = this.optionsInitializer;
            ctx.loginAction = this.loginAction;
            ctx.logoutAction = this.logoutAction;
            return ctx;

        }
    }
}
