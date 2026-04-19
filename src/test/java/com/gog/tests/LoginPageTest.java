package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * LoginPageTest - E2E step 2 of 5.
 *
 * Clicks the Sign In button in the GOG header (hook-test="menuAnonymousButton")
 * without navigating to a URL. The login form opens as a modal popup.
 *
 * CAPTCHA handling: if a reCAPTCHA challenge is blocking login after the form
 * is submitted, the test waits up to 60 seconds for manual solving during a
 * demo
 * session. If it is still blocked after 60 seconds the submission test is
 * skipped gracefully so the rest of the suite can compile and report.
 */
public class LoginPageTest extends E2EBase {

        private static final By SIGN_IN_BTN = By.cssSelector("a[hook-test='menuAnonymousButton']");
        private static final By MODAL_FORM = By.cssSelector("._modal__content-wrapper, .form--login");
        private static final By EMAIL_FIELD = By.id("login_username");
        private static final By PASS_FIELD = By.id("login_password");
        private static final By SUBMIT_BTN = By.id("login_login");
        // Set to true once login is confirmed; SearchTest checks this before proceeding
        static volatile boolean loginConfirmed = false;

        @BeforeClass(alwaysRun = true)
        public void openLoginPopup() {
                System.out.println("\n========================================");
                System.out.println("[LoginPageTest] Step 2 - Opening Sign In popup");
                System.out.println("========================================");

                // Ensure we are on www.gog.com before clicking Sign In
                if (!driver.getCurrentUrl().startsWith("https://www.gog.com")) {
                        driver.get("https://www.gog.com/");
                        TestUtils.waitForPageLoad(driver);
                        TestUtils.dismissCookieConsent(driver);
                }

                TestUtils.pause(1500);
                System.out.println("[LoginPageTest] Clicking the Sign In button in the header...");
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BTN));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

                // Give GOG's AngularJS time to animate the login modal open.
                // We do NOT poll for specific selectors here because the login form may
                // render inside a cross-origin iframe (login.gog.com) that Selenium cannot
                // inspect from the parent page context. The individual @Test methods and
                // testValidLogin handle their own waits.
                TestUtils.pause(4000);
                System.out.println("[LoginPageTest] Sign In button clicked — login popup should now be open.");
                System.out.println(
                                "[LoginPageTest] >>> If the CAPTCHA is visible, solve it now before the test fills in credentials. <<<");
        }

        @Test(priority = 1, description = "Verify the login popup / form is visible after clicking Sign In")
        public void testLoginPopupVisible() {
                TestUtils.pause(1500);
                boolean visible = TestUtils.isElementPresent(driver, MODAL_FORM)
                                || TestUtils.isElementPresent(driver, EMAIL_FIELD);
                Assert.assertTrue(visible, "The login popup must be visible after clicking Sign In");
        }

        @Test(priority = 2, description = "Verify the email / username field is present in the popup")
        public void testEmailFieldPresent() {
                TestUtils.pause(1500);
                WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
                Assert.assertTrue(field.isDisplayed(),
                                "Email field (#login_username) must be visible in the login popup");
        }

        @Test(priority = 3, description = "Verify the email field has type='email'")
        public void testEmailFieldType() {
                TestUtils.pause(1500);
                WebElement field = driver.findElement(EMAIL_FIELD);
                String type = field.getAttribute("type");
                Assert.assertTrue("email".equals(type) || "text".equals(type),
                                "Email field type must be 'email' or 'text', actual: " + type);
        }

        @Test(priority = 4, description = "Verify the password field is present in the popup")
        public void testPasswordFieldPresent() {
                TestUtils.pause(1500);
                WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(PASS_FIELD));
                Assert.assertTrue(field.isDisplayed(),
                                "Password field (#login_password) must be visible in the login popup");
        }

        @Test(priority = 5, description = "Verify the password field has type='password' so input is masked")
        public void testPasswordFieldMasked() {
                TestUtils.pause(1500);
                WebElement field = driver.findElement(PASS_FIELD);
                Assert.assertEquals(field.getAttribute("type"), "password",
                                "Password field must have type='password' to mask the user's credentials");
        }

        @Test(priority = 6, description = "Verify the 'Log in now' submit button is present and enabled")
        public void testSubmitButtonPresent() {
                TestUtils.pause(1500);
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN));
                Assert.assertTrue(btn.isDisplayed(), "Submit button (#login_login) must be visible");
                Assert.assertTrue(btn.isEnabled(), "Submit button must be enabled");
        }

        @Test(priority = 7, description = "Verify a Forgot-Password / Password-Reset link is present")
        public void testForgotPasswordLinkPresent() {
                TestUtils.pause(1500);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("a[data-content-type='requestPasswordForm'], a[href*='password']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//a[contains(translate(normalize-space(.),"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                + "'password')]"));
                Assert.assertTrue(present,
                                "A password-reset link must be present in the login popup");
        }

        @Test(priority = 8, description = "Verify a Sign Up / Create Account link is present in the popup")
        public void testSignUpLinkPresent() {
                TestUtils.pause(1500);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("a[data-content-type='registerForm']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//a[contains(translate(normalize-space(.),"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                + "'sign up')]"));
                Assert.assertTrue(present,
                                "A Sign Up / Create Account link must be present in the login popup");
        }

        @Test(priority = 20, description = "Submit valid credentials through the popup and confirm the user is logged in")
        public void testValidLogin() {
                System.out.println("==================================================");
                System.out.println("[LoginPageTest] testValidLogin starting.");
                System.out.println("==================================================");

                // ----------------------------------------------------------------
                // Step 1: Fill in email and password.
                // ----------------------------------------------------------------
                System.out.println("[LoginPageTest] Entering credentials...");

                WebElement email = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_FIELD));
                email.clear();
                TestUtils.pause(400);
                email.sendKeys(TestUtils.getUsername());
                TestUtils.pause(600);

                WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(PASS_FIELD));
                pass.clear();
                TestUtils.pause(400);
                pass.sendKeys(TestUtils.getPassword());
                TestUtils.pause(600);

                // ----------------------------------------------------------------
                // Step 2: Wait for the CAPTCHA to be solved before submitting.
                //
                // reCAPTCHA tokens expire after ~2 minutes, so the CAPTCHA must be
                // solved immediately before submission. We cannot reliably detect
                // the token from the parent-page JS context, so we give the presenter
                // a 45-second countdown to solve the checkbox, then submit.
                // ----------------------------------------------------------------
                System.out.println("==================================================");
                System.out.println("[LoginPageTest] >>> CAPTCHA: If the checkbox is visible, SOLVE IT NOW! <<<");
                System.out.println("[LoginPageTest] Credentials are filled. Submitting in 45 seconds...");
                System.out.println("==================================================");
                for (int countdown = 45; countdown > 0; countdown -= 5) {
                        System.out.println("[LoginPageTest] Submitting in " + countdown + " seconds...");
                        TestUtils.pause(5000);
                }

                // ----------------------------------------------------------------
                // Step 3: Submit the form.
                // ----------------------------------------------------------------
                System.out.println("[LoginPageTest] Clicking 'Log in now'...");
                driver.findElement(SUBMIT_BTN).click();
                TestUtils.pause(2000);

                // ----------------------------------------------------------------
                // Step 4: Confirm login.
                //
                // a[hook-test='menuAccountButton'] has ng-show="account.isUserLoggedIn"
                // on GOG and only becomes visible after a successful login.
                // ----------------------------------------------------------------
                System.out.println("[LoginPageTest] Waiting for login confirmation...");
                boolean loggedIn = false;
                long loginDeadline = System.currentTimeMillis() + 60_000L; // 60 seconds

                while (System.currentTimeMillis() < loginDeadline) {
                        try {
                                WebElement acct = driver
                                                .findElement(By.cssSelector("a[hook-test='menuAccountButton']"));
                                if (acct.isDisplayed()) {
                                        loggedIn = true;
                                        break;
                                }
                        } catch (Exception ignored) {
                        }
                        TestUtils.pause(1000);
                }

                if (!loggedIn) {
                        throw new RuntimeException(
                                        "[LoginPageTest] Login was not confirmed within 60 seconds after submission. "
                                                        + "Check that the CAPTCHA was solved and credentials are correct.");
                }

                loginConfirmed = true;
                System.out.println("[LoginPageTest] Login confirmed! User is now logged in.");
                TestUtils.pause(2000);

                String url = driver.getCurrentUrl();
                System.out.println("[LoginPageTest] Post-login URL: " + url);
                Assert.assertTrue(url.contains("gog.com"),
                                "After login the browser must be on a gog.com page, actual: " + url);
        }
}
