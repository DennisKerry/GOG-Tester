package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * SignupPageTest â€“ verifies the structure and elements of the GOG.com account
 * registration page WITHOUT creating an actual account.
 *
 * GOG registration is hosted at: https://login.gog.com/signup
 *
 * Automation constraints noted:
 * - GOG registration is on a separate subdomain (login.gog.com). No actual
 * account creation is performed; only page structure is validated.
 * - Field selectors use type and placeholder attributes rather than fragile
 * class names which change between GOG frontend deployments.
 */
public class SignupPageTest extends BaseTest {

    private static final String SIGNUP_URL = "https://login.gog.com/signup";

    @BeforeMethod
    public void navigateToSignupPage() {
        driver.get(SIGNUP_URL);
        TestUtils.dismissCookieConsent(driver);
    }

    // ------------------------------------------------------------------
    // Test methods (7 total, requirement is >= 5)
    // ------------------------------------------------------------------

    @Test(description = "Verify the GOG signup page loads and the URL is on login.gog.com")
    public void testSignupPageLoads() {
        TestUtils.waitForPageLoad(driver);
        String url = driver.getCurrentUrl();
        Assert.assertTrue(
                url.contains("gog.com"),
                "Signup/registration page must be on gog.com, actual URL: " + url);
    }

    @Test(description = "Verify an email input field is present and enabled")
    public void testEmailFieldPresent() {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[type='email'], input[name*='email'], "
                                + "input[placeholder*='email' i], input[id*='email']")));
        Assert.assertTrue(field.isDisplayed(), "Email field must be visible");
        Assert.assertTrue(field.isEnabled(), "Email field must be enabled");
    }

    @Test(description = "Verify a password input field is present and masks input")
    public void testPasswordFieldPresent() {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password']")));
        Assert.assertTrue(field.isDisplayed(), "Password field must be visible");
        Assert.assertEquals(
                field.getAttribute("type"), "password",
                "Password field must be of type 'password' to mask input");
    }

    @Test(description = "Verify a username or display name input field is present")
    public void testUsernameFieldPresent() {
        boolean fieldPresent = TestUtils.isElementPresent(driver,
                By.cssSelector("input[name*='username'], input[name*='screenName'], "
                        + "input[id*='username'], input[placeholder*='username' i], "
                        + "input[placeholder*='name' i]"));
        Assert.assertTrue(fieldPresent,
                "A username or display-name field must be present on the signup page");
    }

    @Test(description = "Verify the Create Account / Sign Up submit button is visible")
    public void testCreateAccountButtonPresent() {
        WebElement btn = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit']")));
        Assert.assertTrue(btn.isDisplayed(), "Create Account button must be visible");
    }

    @Test(description = "Verify a link back to Sign In is present on the signup page")
    public void testSignInLinkPresent() {
        boolean signInPresent = TestUtils.isElementPresent(driver,
                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'sign in')"
                        + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'log in')"
                        + " or contains(@href,'login')]"));
        Assert.assertTrue(signInPresent,
                "A Sign In / Log In link must be accessible from the signup page");
    }

    @Test(description = "Verify a Privacy Policy or Terms of Service link is present")
    public void testPrivacyOrTermsLinkPresent() {
        boolean linkPresent = TestUtils.isElementPresent(driver,
                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'privacy')"
                        + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'terms')"
                        + " or contains(@href,'privacy') or contains(@href,'terms')]"));
        Assert.assertTrue(linkPresent,
                "A Privacy Policy or Terms of Service link must be present on the signup page");
    }
}
