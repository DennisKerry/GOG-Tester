package com.gog.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * TestUtils â€“ shared helpers used across all GOG.com test classes.
 *
 * Credentials are loaded from src/test/resources/config.properties so they
 * are never hard-coded in source files.
 */
public class TestUtils {

    private static final String CONFIG_FILE = "config.properties";
    private static String username;
    private static String password;

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    static {
        loadCredentials();
    }

    // -----------------------------------------------------------------------
    // Credential loading
    // -----------------------------------------------------------------------

    private static void loadCredentials() {
        Properties props = new Properties();
        try (InputStream input = TestUtils.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                username = props.getProperty("gog.username", "");
                password = props.getProperty("gog.password", "");
            } else {
                System.err.println("[TestUtils] config.properties not found on classpath. "
                        + "Add src/test/resources/config.properties with your GOG credentials.");
            }
        } catch (IOException e) {
            System.err.println("[TestUtils] Failed to load config.properties: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Login helper
    // -----------------------------------------------------------------------

    /**
     * Navigates to the GOG login page (login.gog.com/login), enters credentials
     * from config.properties, and submits the form.
     *
     * Known automation constraints:
     * - GOG may display a CAPTCHA on first login from a new IP. Pre-authenticate
     * the test account once in a normal browser session on the same machine.
     * - GOG's login subdomain (login.gog.com) sets cookies scoped to .gog.com,
     * so the session is recognised on www.gog.com after redirect.
     * - A 2-second settle pause follows redirect to allow the SPA to initialise.
     */
    public static void performLogin(WebDriver driver, WebDriverWait wait) {
        driver.get("https://login.gog.com/login");

        dismissCookieConsent(driver);

        // Enter email / username
        WebElement loginField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[type='email'], input[name*='username'], "
                                + "input[id*='username'], input[id*='login_username']")));
        loginField.clear();
        loginField.sendKeys(username);

        // Enter password
        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[type='password']")));
        passwordField.clear();
        passwordField.sendKeys(password);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait until Chrome leaves the login page (fast – fires on any gog.com redirect)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(d -> !d.getCurrentUrl().startsWith("https://login.gog.com/login"));
        } catch (TimeoutException ignored) {
            // CAPTCHA or network delay — continue
        }
    }

    // -----------------------------------------------------------------------
    // Dialog / popup helpers
    // -----------------------------------------------------------------------

    /**
     * Dismisses any cookie-consent / GDPR banner on the current page.
     * Silently ignored when no such dialog is present.
     *
     * Automation constraint: GOG displays a GDPR consent modal on first visit
     * from a new browser profile. This helper handles both the GOG-native modal
     * and the OneTrust consent SDK that GOG may embed.
     */
    public static void dismissCookieConsent(WebDriver driver) {
        // Use JavaScript — avoids translate() XPath on large DOMs that crashes Chrome 147+.
        try {
            ((JavascriptExecutor) driver).executeScript(
                "var btns=document.querySelectorAll('button[id*=accept],button[class*=accept]');"
                + "if(!btns.length){btns=document.querySelectorAll('button')}"
                + "for(var i=0;i<btns.length;i++){"
                + "  var t=(btns[i].textContent||'').trim().toLowerCase();"
                + "  if(t==='accept all'||t==='i accept'||t==='accept'||t==='agree'){"
                + "    btns[i].click();break;"
                + "  }"
                + "}"
            );
        } catch (Exception ignored) {
            // No consent dialog — continue
        }
    }

        // -----------------------------------------------------------------------
    // General utilities
    // -----------------------------------------------------------------------

    /** Blocks until the browser reports document.readyState == 'complete'. */
    public static void waitForPageLoad(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(
                d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState")
                        .equals("complete"));
    }

    /**
     * Returns true if at least one element matching {@code locator} exists in
     * the current DOM; false otherwise. Never throws.
     */
    public static boolean isElementPresent(WebDriver driver, By locator) {
        try {
            return !driver.findElements(locator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** Pauses the calling thread for {@code millis} milliseconds. */
    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -----------------------------------------------------------------------
    // Screenshot and scroll helpers
    // -----------------------------------------------------------------------

    /**
     * Captures a full-page screenshot and saves it under target/screenshots/.
     * Returns the saved {@link File}, or {@code null} if the capture failed.
     */
    public static File takeScreenshot(WebDriver driver, String testName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path screenshotDir = Paths.get("target", "screenshots");
            Files.createDirectories(screenshotDir);
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dest = screenshotDir.resolve(testName + "_" + timestamp + ".png");
            Files.copy(srcFile.toPath(), dest);
            return dest.toFile();
        } catch (IOException e) {
            System.err.println("[TestUtils] Failed to save screenshot: " + e.getMessage());
            return null;
        }
    }

    /** Scrolls the page to the very bottom using JavaScript. */
    public static void scrollToBottom(WebDriver driver) {
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    /** Scrolls {@code element} into the visible viewport using JavaScript. */
    public static void scrollIntoView(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
