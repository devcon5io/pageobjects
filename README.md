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

## Grouping Elements
Typically, elements of a web page are grouped into logical units. A common example is a Form element containing input
 elements. Further, Selenium supports element location in a specific search context. The default search context is 
 the driver, but every other element of a page can be a search context itself, i.e. the mentioned form element being 
 the search context for locating the input elements.
 
the pageobjects library provides the `ElementGroup` interface for that. The interface defines the `getSearchContext` 
method defaulting to the current driver. If an ElementGropu requires a different search context, this method must be
implemented. 
Further the `ElementGroup` defines a generic getter method for obtaining sub-element groups and a method that is
 invoked to locate the elements of the page, defaulting to the injection mechanism of the pageobjects library.

The `Page` interface is a specialization of the `ElementGroup`.

Any page or element group can nest further element groups. Those are automatically injected, when the test control 
flow navigates to the page and thereby creating the page instance. 

Example:

    public class LoginForm implements ElementGroup {
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
    
    @Locator("login.jsp")
    public class LoginPage implements Page {
        LoginForm form;
    }
    

# Measure User Transactions
Usually, the response times for certain interactions on the user interface are part of the non-functional requirements
and should be part of a specification, the tests. First step to measure response times, is to declare user transactions
for which the begin and end timestamp should be recorded. For this purpose, the pageobjects library provides the 
`Transaction` annotation. ElementGroup, including Pages, and Methods can be marked as transaction. The name of the
user transaction can be specified as value of the annotation or it is dynamically derived from the method or
declaring class. 

## Add Transaction Support to Page Object Model
To enable the transaction support, the ElementGroup or Page has to implement `TransactionSupport`. When the 
instance of the element group or page is created, it is automatically enhanced to record the repsonse times
of methods annotated with `Transaction` as well as the time it takes to load the Page, if the Page implementation 
is annotated as well. For all other ElementGroups annotated with `Transaction`, the annotation is only used to 
derive the name of all transaction of that group.

Example for Declaring annoation:

    public class LoginForm implements ElementGroup, TransactionSupport {
         // ...
         
        @Transaction("Login")
        public void pressLogin(){
            loginButton.get().click();
        }
    }
    
Alternatively, you may declare the start and stop point for the response time measurement imperatively invoking
the `txBegin` and `txEnd` method defined in the `TransactionSupport` interface.
 
    public class LoginForm implements ElementGroup, TransactionSupport {
          // ...
          
         public void pressLogin(){
             txBegin("Login");
             loginButton.get().click();
             txEnd("Login");
         }
    }
 

## Enable Response Time Recording
To collect the response times of the declared transactions of your Page Object model in your test, you have to 
add the `ResponseTimeRecording` rule to your tests:

    @Rule
    public ResponseTimeRecording rt = new ResponseTimeRecording();
    
This will activate the automatic recording of all response times of performed transactions of your model.

The test rule uses a response time collector that is bound to the current thread. The ResponseTime recording mechanism
can also be used outside of a unit test, if needed, using the `ResponseTimeCollector`

    ResponseTimeCollector collector = new ResponseTimeCollector();
    collector.startRecording();
    // do something
    collector.stopRecording();
    
This will bind the collector instance to the current thread, making it accessible using a static getter:

    ResponseTimeCollector.current().ifPresent(rtc -> rtc.startTx("customTx");
    //do transaction
    ResponseTimeCollector.current().ifPresent(rtc -> rtc.stopTx("customTx");
    
## Accessing Response Times
All started and completed collection are passed to the global collector `ResponseTimes`. The default setting for that
global collector is to record all captured response times and making them accessible using the `getResponesTimes`, 
providing a map, mapping all transactions to recorded response times for that transaction. As being a shared collector,
the collection of recorded response times fills up over time and needs to be cleaned up to prevent memory leaking.
Therefore the `clear` method should be invoked if the captured response times are processed.

This default behavior can be changed by overriding the handlers for 

- `onMeasureStart`
- `onMeasureEnd`

Typical use cases for changing that behavior are to pass the measures into a central database or to another processing
system.
