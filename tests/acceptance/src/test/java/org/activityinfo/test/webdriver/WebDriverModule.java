package org.activityinfo.test.webdriver;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;


public class WebDriverModule extends AbstractModule {
    @Override
    protected void configure() {


        if("chrome".equals(System.getProperty("webdriver"))) {
            bind(WebDriverProvider.class).to(ChromeWebDriverProvider.class);
            bind(SessionReporter.class).to(SimpleReporter.class);
        } else if(SauceLabsDriverProvider.isEnabled()) {
            System.out.println("Using SauceLabs as WebDriver");
            bind(WebDriverProvider.class).to(SauceLabsDriverProvider.class);
            bind(SessionReporter.class).to(SauceReporter.class);
        } else {
            System.out.println("Using PhantomJS as WebDriver");
            bind(WebDriverProvider.class).to(PhantomJsProvider.class);
            bind(SessionReporter.class).to(SimpleReporter.class);
        }

    }

    @ScenarioScoped 
    @Provides
    public WebDriver provideDriver(WebDriverSession session) {
        return session.getDriver();
    }
}