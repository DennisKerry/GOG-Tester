package com.gog.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BaseTest â€“ sets up and tears down the Chrome WebDriver once per test class.
 *
 * Anti-detection flags are applied to reduce the chance of GOG or its CDN
 * triggering bot-protection challenges during automated runs.
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final String BASE_URL = "https://www.gog.com";
    protected static final Duration TIMEOUT = Duration.ofSeconds(20);

    @BeforeClass
    public void setUp() {
        // Suppress Selenium CDP version-mismatch warnings (Chrome 147 vs Selenium 4.18)
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
        // Remove the "Chrome is being controlled by automated software" banner
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);  // explicit waits used everywhere
        wait = new WebDriverWait(driver, TIMEOUT);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
