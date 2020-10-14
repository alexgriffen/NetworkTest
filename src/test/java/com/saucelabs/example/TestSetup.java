package com.saucelabs.example;

import com.google.common.collect.ImmutableMap;
import com.saucelabs.example.util.ResultsReporter;
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TestSetup {
    private ResultsReporter reporter;
    private ThreadLocal<IOSDriver> driver = new ThreadLocal<IOSDriver>();
    /**
     * DataProvider that explicitly sets the browser combinations to be used.
     *
     * @param testMethod
     * @return
     */
    @DataProvider(name = "devices", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
        return new Object[][]{
                //Verify that your account has access to the devices below
                new Object[]{"iOS", "iPhone.*", "13"},
                new Object[]{"iOS", "iPhone.*", "13"}
        };
    }
    private IOSDriver createDriver(String platformName, String platformVersion, String deviceName, String methodName) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("testobject_api_key", "TESTOBJECT_PROJECTKEY_HERE");
        capabilities.setCapability("deviceName", deviceName);
        capabilities.setCapability("platformVersion", platformVersion);
        capabilities.setCapability("platformName", platformName);
        capabilities.setCapability("name",  methodName);
        // capabilities.setCapability("appiumVersion", "1.7.2");
        driver.set(new IOSDriver<WebElement>(
                new URL(System.getenv("APPIUM_URL")),
                capabilities));
        return driver.get();
    }
    /* A simple addition, it expects the correct result to appear in the result field. */
    @Test(dataProvider = "devices")
    public void testNetworkSpeed(String platformName, String deviceName, String platformVersion, Method method) throws MalformedURLException {
        IOSDriver driver = createDriver(platformName, platformVersion, deviceName, method.getName());
        MobileElement testButton = (MobileElement)(driver.findElement(By.xpath("//XCUIElementTypeButton[@name='testButton']")));
        testButton.click();

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.getScreenshotAs(OutputType.FILE);
        driver.executeScript("mobile: launchApp", ImmutableMap.of("bundleId", "com.apple.Preferences"));
        scrollToCell((MobileElement)driver.findElement(By.xpath("//XCUIElementTypeCell[@name=\"Developer\"]")), driver);
        scrollToCell((MobileElement)driver.findElement(By.xpath("//XCUIElementTypeCell[@name=\"Network Link Conditioner\"]")), driver);
        scrollToCell((MobileElement)driver.findElement(By.xpath("//XCUIElementTypeCell[@name=\"Enable\"]")), driver);
        scrollToCell((MobileElement)driver.findElement(By.xpath("//XCUIElementTypeCell[@name=\"100% Loss\"]")), driver);
        driver.getScreenshotAs(OutputType.FILE);
        driver.executeScript("mobile: launchApp", ImmutableMap.of("bundleId", "com.saucelabs.NetworkSpeed"));
        testButton.click();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.getScreenshotAs(OutputType.FILE);
        driver.executeScript("mobile: launchApp", ImmutableMap.of("bundleId", "com.apple.Preferences"));
        scrollToCell((MobileElement)driver.findElement(By.xpath("//XCUIElementTypeCell[@name=\"Enable\"]")), driver);
        driver.getScreenshotAs(OutputType.FILE);
        // String targetCell = "//UIATableCell[@name='Developer']]";
        //  MobileElement cellWithText = (MobileElement)driver.findElement(By.xpath(targetCell));
        //  swipeToDirection_iOS_XCTest(cellWithText, "d");
    }
    private void scrollToCell(MobileElement element, IOSDriver driver) {
        HashMap<String, String> swipeObject = new HashMap<String, String>();
        swipeObject.put("direction", "down");
        while (!element.isDisplayed()){
            driver.executeScript("mobile: scroll", swipeObject);
        }
        element.click();
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        IOSDriver driver = getWebDriver();
        reporter = new ResultsReporter();
        boolean success = result.isSuccess();
        String sessionId = driver.getSessionId().toString();
        reporter.saveTestStatus(sessionId, success);
        driver.quit();
    }
    /**
     * @return the {@link WebDriver} for the current thread
     */
    public IOSDriver getWebDriver() {
        return driver.get();
    }
}