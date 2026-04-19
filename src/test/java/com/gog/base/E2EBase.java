package com.gog.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.time.Duration;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * E2EBase – suite-scoped base class for the end-to-end test flow.
 *
 * A single Chrome window is created once before the entire suite and kept open
 * across all E2E test classes (HomePageTest → LoginPageTest → SearchTest →
 * GamePageTest → CartPageTest). Each test class picks up the browser state
 * left by the previous class, mirroring a real user journey.
 *
 * Standalone test classes (SignupPageTest, NavigationTest, GenreBrowseTest)
 * extend BaseTest instead and manage their own per-class driver.
 *
 * Guard flags (suiteStarted / suiteStopped) prevent double-initialisation when
 * TestNG discovers this @BeforeSuite annotation on each inheriting class.
 */
public abstract class E2EBase {

    protected static WebDriver driver;
    protected static WebDriverWait wait;

    protected static final String BASE_URL = "https://www.gog.com";
    protected static final Duration TIMEOUT = Duration.ofSeconds(20);

    private static volatile boolean suiteStarted = false;
    private static volatile boolean suiteStopped = false;

    @BeforeSuite(alwaysRun = true)
    public synchronized void initDriver() {
        if (suiteStarted)
            return;
        suiteStarted = true;

        Logger.getLogger("org.openqa.selenium.devtools").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.chromium").setLevel(Level.OFF);
        Logger.getLogger("org.openqa.selenium.remote").setLevel(Level.SEVERE);

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        wait = new WebDriverWait(driver, TIMEOUT);

        System.out.println("[E2EBase] Shared browser started for E2E suite.");
    }

    @AfterSuite(alwaysRun = true)
    public synchronized void quitDriver() {
        if (suiteStopped)
            return;
        suiteStopped = true;
        if (driver != null) {
            driver.quit();
            driver = null;
            System.out.println("[E2EBase] Shared browser closed after E2E suite.");
        }
    }
}
