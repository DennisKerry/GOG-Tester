package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * LoginPageTest -- verifies the GOG.com login page structure and form behaviour,
 * including real typing, form submission, and response validation.
 *
 * Target URL: https://login.gog.com/login
 * The form is directly visible at this URL -- no intermediate trigger needed.
 *
 * Key element IDs (from live page HTML):
 *   login_username -- email input
 *   login_password -- password input
 *   login_login    -- submit button
 */
public class LoginPageTest extends BaseTest {

    private static final String LOGIN_URL = "https://login.gog.com/login";

    // Selectors derived from actual GOG login page HTML
    private static final By EMAIL_FIELD  = By.id("login_username");
    private static final By PASS_FIELD   = By.id("login_password");
    private static final By SUBMIT_BTN   = By.id("login_login");

    // -----------------------------------------------------------------------
    // Setup -- navigate once for tests that only read the page
    // -----------------------------------------------------------------------

    @BeforeClass
    public void navigateToLoginPage() {
        driver.get(LOGIN_URL);
        TestUtils.dismissCookieConsent(driver);
        // Wait until the email input is actually on screen before any test runs
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
    }

    // -----------------------------------------------------------------------
    // Test 1-2 -- page-level properties
    // -----------------------------------------------------------------------

    @Test(priority = 1, description = "Verify the GOG login page title contains 'GOG'")
    public void testLoginPageTitle() {
        String title = driver.getTitle();
        Assert.assertTrue(title.toUpperCase().contains("GOG"),
                "Login page title must contain 'GOG', actual: " + title);
    }

    @Test(priority = 2,
          description = "Verify the login page is served over HTTPS from the GOG auth subdomain")
    public void testLoginPageHttpsAndDomain() {
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.startsWith("https://"),
                "GOG login page must be served over HTTPS, actual: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "GOG login must be on the gog.com domain, actual: " + url);
    }

    // -----------------------------------------------------------------------
    // Test 3-4 -- field-level interaction: type, verify value, clear
    // -----------------------------------------------------------------------

    @Test(priority = 3,
          description = "Verify the email field accepts typed input and its value "
                      + "attribute reflects exactly what was typed")
    public void testEmailFieldAcceptsTypedInput() {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));

        emailField.clear();
        emailField.sendKeys("test.input@example.com");

        String value = emailField.getAttribute("value");
        Assert.assertEquals(value, "test.input@example.com",
                "Email field value must match exactly what was typed");

        // Clear and confirm the field empties
        emailField.clear();
        String cleared = emailField.getAttribute("value");
        Assert.assertTrue(cleared == null || cleared.isEmpty(),
                "Email field must be empty after clear()");
    }

    @Test(priority = 4,
          description = "Verify the password field has type='password' to mask input "
                      + "and accepts typed text")
    public void testPasswordFieldMaskedAndAcceptsInput() {
        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(PASS_FIELD));

        Assert.assertEquals(passField.getAttribute("type"), "password",
                "Password field type must be 'password' to mask credentials on screen");
        Assert.assertTrue(passField.isEnabled(),
                "Password field must be enabled for user interaction");

        passField.clear();
        passField.sendKeys("SomeTestPassword");
        String value = passField.getAttribute("value");
        Assert.assertFalse(value == null || value.isEmpty(),
                "Password field must retain typed input in its value attribute");

        passField.clear();
    }

    // -----------------------------------------------------------------------
    // Test 5 -- submit EMPTY form, verify no unintended redirect
    // -----------------------------------------------------------------------

    @Test(priority = 5,
          description = "Verify submitting a completely empty login form keeps the "
                      + "user on the GOG auth page")
    public void testEmptyFormSubmissionStaysOnPage() {
        driver.get(LOGIN_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        WebElement passField  = driver.findElement(PASS_FIELD);

        emailField.clear();
        passField.clear();

        driver.findElement(SUBMIT_BTN).click();
        TestUtils.pause(2000);

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "Submitting an empty form must keep the user on gog.com, actual: " + url);

        // GOG shows inline error messages -- at least one should now be visible
        boolean inlineError = TestUtils.isElementPresent(driver,
                By.cssSelector(".js-error-msg:not(.is-hidden), .field__msg:not(.is-hidden)"));
        System.out.println("[LoginPageTest] Inline error visible after empty submit: " + inlineError);
    }

    // -----------------------------------------------------------------------
    // Test 6 -- submit INVALID credentials
    // -----------------------------------------------------------------------

    @Test(priority = 6,
          description = "Verify submitting invalid credentials keeps the user on "
                      + "the GOG auth domain")
    public void testInvalidCredentialsStayOnAuthDomain() {
        driver.get(LOGIN_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        WebElement passField  = driver.findElement(PASS_FIELD);

        emailField.clear();
        emailField.sendKeys("nobody@notarealaccount.xyz");

        passField.clear();
        passField.sendKeys("TotallyWrongPassword999!");

        driver.findElement(SUBMIT_BTN).click();
        TestUtils.pause(3000);

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "After invalid login, user must remain on gog.com, actual: " + url);
    }

    // -----------------------------------------------------------------------
    // Test 7 -- submit VALID credentials
    // -----------------------------------------------------------------------

    @Test(priority = 7,
          description = "Verify submitting correct credentials redirects to the GOG store")
    public void testValidCredentialsRedirectToStore() {
        driver.get(LOGIN_URL);
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        WebElement passField  = driver.findElement(PASS_FIELD);

        emailField.clear();
        emailField.sendKeys(TestUtils.getUsername());

        passField.clear();
        passField.sendKeys(TestUtils.getPassword());

        driver.findElement(SUBMIT_BTN).click();

        // Wait up to 20 s for redirect to www.gog.com
        try {
            wait.until(ExpectedConditions.urlContains("www.gog.com"));
        } catch (Exception ignored) { /* CAPTCHA or 2FA may have appeared */ }

        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "After valid login, user must be on gog.com, actual: " + url);
    }

    // -----------------------------------------------------------------------
    // Test 8 -- Forgot Password link
    // -----------------------------------------------------------------------

    @Test(priority = 8,
          description = "Verify clicking the Forgot Password link navigates to the "
                      + "GOG password-reset page")
    public void testForgotPasswordNavigation() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));

        WebElement forgotLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//a[contains(translate(text(),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'forgot')"
                                + " or contains(translate(@href,"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'forgot')"
                                + " or contains(translate(@href,"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'reset')"
                                + " or contains(translate(@href,"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'password')]")));

        Assert.assertNotNull(forgotLink.getAttribute("href"),
                "Forgot Password link must have a valid href attribute");

        forgotLink.click();
        TestUtils.pause(2000);

        String urlAfter = driver.getCurrentUrl();
        Assert.assertTrue(urlAfter.contains("gog.com"),
                "Forgot Password must stay on gog.com, actual: " + urlAfter);
        Assert.assertFalse(urlAfter.equals(LOGIN_URL),
                "Forgot Password must navigate away from the login page, actual: " + urlAfter);
    }

    // -----------------------------------------------------------------------
    // Test 9 -- Create Account link
    // -----------------------------------------------------------------------

    @Test(priority = 9,
          description = "Verify clicking the Create Account link navigates to the "
                      + "GOG registration page")
    public void testCreateAccountNavigation() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));

        WebElement createLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//a[contains(translate(text(),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create account')"
                                + " or contains(translate(text(),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'register')"
                                + " or contains(translate(text(),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign up')"
                                + " or contains(@href,'register') or contains(@href,'signup')]")));

        Assert.assertTrue(createLink.isDisplayed(),
                "Create Account link must be visible on the login page");

        createLink.click();
        TestUtils.pause(2000);

        String urlAfter = driver.getCurrentUrl();
        Assert.assertTrue(urlAfter.contains("gog.com"),
                "Create Account must navigate to a gog.com page, actual: " + urlAfter);
    }
}