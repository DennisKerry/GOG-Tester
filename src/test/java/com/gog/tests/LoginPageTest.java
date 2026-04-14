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
 * LoginPageTest â€“ verifies the structure and behaviour of the GOG.com login
 * page
 * WITHOUT requiring a pre-authenticated session.
 *
 * GOG uses a dedicated authentication subdomain: https://login.gog.com/login
 *
 * Automation constraints noted:
 * - GOG may trigger a CAPTCHA or rate-limit after repeated failed login
 * attempts
 * from the same IP. Space out test runs or use a fresh browser profile.
 * - The error message displayed on invalid credentials may vary by locale;
 * a broad XPath matching common error indicators is used.
 */
public class LoginPageTest extends BaseTest {

        private static final String LOGIN_URL = "https://login.gog.com/login";

        @BeforeMethod
        public void navigateToLoginPage() {
                driver.get(LOGIN_URL);
                TestUtils.dismissCookieConsent(driver);
                // GOG login page shows a "LOG IN NOW" button first; click it to reveal
                // the email + password form fields.
                try {
                        WebElement loginNowBtn = wait.until(
                                        ExpectedConditions.elementToBeClickable(
                                                        By.xpath("//*[contains(translate(normalize-space(.),"
                                                                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                        + "'log in now') and (self::button or self::a)]")));
                        loginNowBtn.click();
                        TestUtils.waitForPageLoad(driver);
                } catch (Exception e) {
                        // Button not present – the form may already be visible
                }
        }

        // ------------------------------------------------------------------
        // Test methods (7 total, requirement is >= 5)
        // ------------------------------------------------------------------

        @Test(description = "Verify the GOG login page title contains 'GOG'")
        public void testLoginPageTitle() {
                String title = driver.getTitle();
                Assert.assertTrue(
                                title.toUpperCase().contains("GOG"),
                                "Login page title should contain 'GOG', actual: " + title);
        }

        @Test(description = "Verify the username / email input field is visible and enabled")
        public void testUsernameFieldPresent() {
                WebElement field = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("input[type='email'], input[name*='username'], "
                                                                + "input[id*='username'], input[id*='login']")));
                Assert.assertTrue(field.isDisplayed(), "Username/email field must be visible");
                Assert.assertTrue(field.isEnabled(), "Username/email field must be enabled");
        }

        @Test(description = "Verify the password input field is visible and masks input")
        public void testPasswordFieldPresent() {
                WebElement field = wait.until(
                                ExpectedConditions
                                                .visibilityOfElementLocated(By.cssSelector("input[type='password']")));
                Assert.assertTrue(field.isDisplayed(), "Password field must be visible");
                Assert.assertEquals(
                                field.getAttribute("type"), "password",
                                "Password field must be of type 'password' to mask input");
        }

        @Test(description = "Verify the Sign In submit button is visible")
        public void testSignInButtonPresent() {
                WebElement btn = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[type='submit']")));
                Assert.assertTrue(btn.isDisplayed(), "Sign In button must be visible");
        }

        @Test(description = "Verify a Forgot Password link is present on the login page")
        public void testForgotPasswordLinkPresent() {
                WebElement link = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'forgot')"
                                                                + " or contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'forgot')"
                                                                + " or contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'password')"
                                                                + " or contains(translate(@href,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'reset')]")));
                Assert.assertTrue(link.isDisplayed(), "Forgot password link must be present on the login page");
                Assert.assertNotNull(link.getAttribute("href"),
                                "Forgot password link must have a valid href attribute");
        }

        @Test(description = "Verify a Create Account / Sign Up link is present on the login page")
        public void testCreateAccountLinkPresent() {
                WebElement link = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'create account')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'register')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'sign up')"
                                                                + " or contains(@href,'register') or contains(@href,'signup')]")));
                Assert.assertTrue(link.isDisplayed(), "A Create Account / Sign Up link must be present");
        }

        @Test(description = "Verify submitting invalid credentials shows an error message")
        public void testInvalidLoginShowsError() {
                WebElement usernameField = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.cssSelector("input[type='email'], input[name*='username'], "
                                                                + "input[id*='username'], input[id*='login']")));
                usernameField.sendKeys("invalid_test_user_xyz99@example.invalid");

                WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
                passwordField.sendKeys("wrongPassword_xyz_999!");

                driver.findElement(By.cssSelector("button[type='submit']")).click();

                WebElement error = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'incorrect')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'invalid')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'wrong')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'not found')"
                                                                + " or contains(@class,'error') or contains(@class,'alert')"
                                                                + " or contains(@class,'form__error')]")));
                Assert.assertNotNull(error,
                                "An error message must appear after submitting invalid credentials");
        }
}
