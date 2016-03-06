# pageobjects
Pageobjects is a helper library for defining a [Page Object Model](http://martinfowler.com/bliki/PageObject.html)
to be used with Selenium. Pages and ElementGroups can be defined as classes, where all the single elements
of the page or element group can be declaratively defined using an annotation.
The framework helps you locating the elements and injecting a default getter (Supplier) for that element, 
including waiting for the presence of the element.

On top of that, it includes support for declaring user transactions for the execution time can be measured. 
All measured times can be collected and process, i.e. for performance analysis.

## Usage

Add the following dependency to your project

    <dependency>
        <groupId>io.devcon5</groupId>
        <artifactId>pageobjects</artifactId>
        <version>0.3</version>
    </dependency>

This library requires a Java 8 JRE to run. If you write code for Java 7 production environments, but
 and your build and test execution environment supports Java 8 - Selenium tests can be executed in a 
 different JVM than the system under test - you may put your Selenium based tests in a separate
 Maven module, and configure only that module to compile for Java 8. I.e. by adding the compiler
 plugin configuration.

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
# Defining a Page Object Model

The library contains two base types for declaring your page objects:

- `ElementGroup` defines an arbitrary set of elements that are logically bound together
- `Page` is a special element group declaring a page of your application. 

Both are interfaces that may be implemented by your page object model and define default methods providing
some logic for navigating and element location.
The single elements of the page or element group have to be fields of the class of type 
`java.util.function.Supplier<WebElement>` and must be annotated with `Locator`.

The `Locator` can contain a simple URL as String value which can be used to annotate a Page type in
 order to define on which url the page can be found. For all elements on a page, the type of locator
 has to be defined, which map directly to selenium By locators.

## Define a simple page

As example, take a login page:

    @Locator("login.jsp")
    public class LoginPage implements Page {

        @Locator(by = ID, value = "username")
        Supplier<WebElement> username;
        
        @Locator(by = ID, value = "password")
        Supplier<WebElement> password;
                
        @Locator(by = ID, value = "login")
        Supplier<WebElement> loginButton;
        
        public LoginPage enterUsername(String username){
            username.get().sendKeys(username);
        }
        public LoginPage enterPassword(String password){
            password.get().sendKeys(username);
        }
        public void pressLogin(){
            loginButton.get().click();
        }
    }

## Running Tests with Page Objects
The parts that load the page and locate the elements on the page require a Selenium driver. The default
case is to run a test with a single driver in one test execution thread. In order to initialize a driver
for the current test, the library provides a test rule that has to be added to the test:

    public class PageObjectTest {
    
        @Rule
        public SeleniumControl selenium = SeleniumControl.builder()
                                                         .baseUrl(basePath)
                                                         .driver(() -> new FirefoxDriver())
                                                         .build();
                                                         
         //...
    
    }

The basePath in the example has to be the absolute URL required to resolve all relative URLs of 
locators defined of your model. For driver a `java.util.function.Supplier` is passed. The library
provides a set of default suppliers in the `Drivers` enum.

## Using the Page Objects in your test
When using the page object in your tests, you have to navigate to the page first and then may invoke
the methods on the objects.

    @Test
    public void testLogin() {
        LoginPage login = Page.navigateTo(LoginPage.class);
        login.enterUsername("username")
             .enterPassword("password")
             .pressLogin();
    }

If the mechanism to load a page - either by navigating to the URL or by clicking on an element on the page - 
should be different, the `loadPage()` method can be implemented.
