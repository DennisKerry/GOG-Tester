package com.gog.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
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

        // Wait for redirect back to gog.com
        try {
            wait.until(ExpectedConditions.urlContains("gog.com"));
            Thread.sleep(2000); // allow SPA to settle after login redirect
        } catch (InterruptedException | TimeoutException ignored) {
            Thread.currentThread().interrupt();
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
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                    + "'abcdefghijklmnopqrstuvwxyz'),'accept')"
                                    + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                    + "'abcdefghijklmnopqrstuvwxyz'),'agree')"
                                    + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                    + "'abcdefghijklmnopqrstuvwxyz'),'ok')"
                                    + " or @id='onetrust-accept-btn-handler'"
                                    + " or contains(@class,'accept')]")));
            btn.click();
        } catch (TimeoutException | NoSuchElementException ignored) {
            // No consent dialog present â€“ continue
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
}
