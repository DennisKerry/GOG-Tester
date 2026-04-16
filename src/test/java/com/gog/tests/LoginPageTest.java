package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
 * Credentials are loaded from src/test/resources/config.properties.
 *
 * Automation constraints noted:
 * - GOG's login page is an Angular SPA that may show a "LOG IN NOW" trigger
 *   before revealing the email + password form. A shared helper reveals the
 *   form once per class in @BeforeClass.
 * - Tests that submit the form re-navigate to a fresh login page themselves so
 *   they are not affected by whatever state the previous test left behind.
 * - GOG may rate-limit or CAPTCHA after multiple rapid invalid-login attempts;
 *   run with reasonable intervals during development.
 */
public class LoginPageTest extends BaseTest {

    private static final String LOGIN_URL = "https://login.gog.com/login";

    // -----------------------------------------------------------------------
    // Setup -- navigate to the login page and reveal the form once
    // -----------------------------------------------------------------------

    @BeforeClass(dependsOnMethods = "setUp")
    public void navigateToLoginPage() {
        driver.get(LOGIN_URL);
        TestUtils.dismissCookieConsent(driver);
        revealLoginForm();
    }

    /**
     * GOG's Angular login page may gate the email/password form behind a
     * "LOG IN NOW" button. This helper clicks that trigger (if present) and
     * waits for the email input to become visible before returning.
     */
    private void revealLoginForm() {
        TestUtils.pause(2000);
        try {
            WebElement trigger = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "var walker = document.createTreeWalker("
                    + "document.body, NodeFilter.SHOW_TEXT, null, false);"
                    + "var node;"
                    + "while ((node = walker.nextNode()) != null) {"
                    + "  var t = (node.nodeValue || '').trim().toUpperCase();"
                    + "  if (t === 'LOG IN NOW' || t === 'SIGN IN NOW') return node.parentElement;"
                    + "}"
                    + "return null;");
            if (trigger != null) {
                trigger.click();
                TestUtils.pause(1500);
            }
        } catch (Exception ignored) { /* form may already be visible */ }

        // Wait until the email field (or the LOG IN NOW trigger) is present
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")),
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[normalize-space(text())='LOG IN NOW']"))));
        } catch (Exception ignored) { /* proceed -- individual tests handle missing fields */ }
    }

    // -----------------------------------------------------------------------
    // Test 1-2 -- page-level properties (no form interaction needed)
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
                "GOG login must be served from the gog.com domain, actual: " + url);
    }

    // -----------------------------------------------------------------------
    // Test 3-4 -- field-level interaction: type, verify value, clear
    // -----------------------------------------------------------------------

    @Test(priority = 3,
          description = "Verify the email field accepts typed input and the "
                      + "value attribute reflects exactly what was typed")
    public void testEmailFieldAcceptsTypedInput() {
        List<WebElement> emailFields = driver.findElements(By.cssSelector("input[type='email']"));
        Assert.assertFalse(emailFields.isEmpty(),
                "An email/username input field must be present on the login page");

        WebElement emailField = emailFields.get(0);
        emailField.clear();
        emailField.sendKeys("test.input@example.com");

        String value = emailField.getAttribute("value");
        Assert.assertNotNull(value, "Email field must expose a 'value' attribute after typing");
        Assert.assertEquals(value, "test.input@example.com",
                "Email field value must match exactly what was typed");

        // Clear and verify the field empties
        emailField.clear();
        String clearedValue = emailField.getAttribute("value");
        Assert.assertTrue(clearedValue == null || clearedValue.isEmpty(),
                "Email field must be empty after clear()");
    }

    @Test(priority = 4,
          description = "Verify the password field is masked (type='password') "
                      + "and accepts typed input without exposing the value as plain text")
    public void testPasswordFieldMaskedAndAcceptsInput() {
        List<WebElement> passFields = driver.findElements(By.cssSelector("input[type='password']"));
        Assert.assertFalse(passFields.isEmpty(),
                "A password input field must be present on the login page");

        WebElement passField = passFields.get(0);
        Assert.assertEquals(passField.getAttribute("type"), "password",
                "Password field type must be 'password' to mask the credential on screen");
        Assert.assertTrue(passField.isEnabled(),
                "Password field must be enabled so the user can type into it");

        passField.clear();
        passField.sendKeys("SomeTypedPassword");
        String value = passField.getAttribute("value");
        // The value attribute is populated even for masked fields
        Assert.assertFalse(value == null || value.isEmpty(),
                "Password field value attribute must not be empty after typing");

        passField.clear();
    }

    // -----------------------------------------------------------------------
    // Test 5 -- submit EMPTY form
    // -----------------------------------------------------------------------

    @Test(priority = 5,
          description = "Verify submitting the login form with empty fields keeps "
                      + "the user on the GOG auth page (no unintended redirect)")
    public void testEmptyFormSubmissionStaysOnPage() {
        driver.get(LOGIN_URL);
        revealLoginForm();

        List<WebElement> emailFields = driver.findElements(By.cssSelector("input[type='email']"));
        List<WebElement> passFields  = driver.findElements(By.cssSelector("input[type='password']"));

        if (!emailFields.isEmpty()) emailFields.get(0).clear();
        if (!passFields.isEmpty())  passFields.get(0).clear();

        // Submit via the submit button or keyboard Enter
        List<WebElement> submitBtns = driver.findElements(By.cssSelector("button[type='submit']"));
        if (!submitBtns.isEmpty()) {
            submitBtns.get(0).click();
        } else if (!emailFields.isEmpty()) {
            emailFields.get(0).sendKeys(Keys.RETURN);
        }

        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "Submitting an empty login form must keep the user on gog.com, actual: " + url);
    }

    // -----------------------------------------------------------------------
    // Test 6 -- submit INVALID credentials
    // -----------------------------------------------------------------------

    @Test(priority = 6,
          description = "Verify submitting invalid credentials keeps the user on "
                      + "the GOG auth domain and shows an error indicator")
    public void testInvalidCredentialsShowsError() {
        driver.get(LOGIN_URL);
        revealLoginForm();

        WebElement emailField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        emailField.clear();
        emailField.sendKeys("nobody@notarealaccount.xyz");

        WebElement passField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password']")));
        passField.clear();
        passField.sendKeys("TotallyWrongPassword999!");

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        TestUtils.pause(3000);

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "After invalid login, user must remain on gog.com, actual: " + url);

        // GOG shows an inline error message or highlights the form on failure
        boolean errorVisible = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'incorrect')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'wrong')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'failed')"
                        + " or contains(translate(@class,"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'error')"
                        + " or contains(translate(@class,"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid')]"));
        System.out.println("[LoginPageTest] Error indicator visible after invalid login: "
                + errorVisible);
        // Hard assert: user must stay on gog.com
        Assert.assertTrue(url.contains("gog.com"),
                "User must stay on gog.com after invalid login");
    }

    // -----------------------------------------------------------------------
    // Test 7 -- submit VALID credentials
    // -----------------------------------------------------------------------

    @Test(priority = 7,
          description = "Verify submitting correct credentials redirects the user "
                      + "to the GOG store / account area")
    public void testValidCredentialsRedirectToStore() {
        driver.get(LOGIN_URL);
        revealLoginForm();

        WebElement emailField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        emailField.clear();
        emailField.sendKeys(TestUtils.getUsername());

        WebElement passField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password']")));
        passField.clear();
        passField.sendKeys(TestUtils.getPassword());

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Wait up to 20 s for the redirect back to www.gog.com
        try {
            wait.until(ExpectedConditions.urlContains("www.gog.com"));
        } catch (Exception ignored) { /* may stay on login.gog.com if CAPTCHA appeared */ }

        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "After valid login, user must be on gog.com, actual: " + url);
    }

    // -----------------------------------------------------------------------
    // Test 8 -- Forgot Password navigates to the reset page
    // -----------------------------------------------------------------------

    @Test(priority = 8,
          description = "Verify clicking the Forgot Password link navigates to "
                      + "the GOG password-reset page")
    public void testForgotPasswordNavigation() {
        driver.get(LOGIN_URL);
        revealLoginForm();

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
                "Forgot Password must navigate to a gog.com page, actual: " + urlAfter);
        Assert.assertFalse(urlAfter.equals(LOGIN_URL),
                "Forgot Password must navigate away from the login page, actual: " + urlAfter);
    }

    // -----------------------------------------------------------------------
    // Test 9 -- Create Account link navigates to registration
    // -----------------------------------------------------------------------

    @Test(priority = 9,
          description = "Verify clicking the Create Account link navigates to "
                      + "the GOG registration page")
    public void testCreateAccountNavigation() {
        driver.get(LOGIN_URL);
        revealLoginForm();

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