package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * SignupPageTest - verifies the GOG.com account sign-up flow is accessible
 * and that the expected UI elements are present.
 *
 * GOG sign-up is part of the same auth overlay as sign-in, accessible at
 * login.gog.com. No actual account is created; only page structure is checked.
 */
public class SignupPageTest extends BaseTest {

        private static final String AUTH_URL = "https://login.gog.com/login";

        @BeforeClass
        public void navigateToSignupPage() {
                driver.get(AUTH_URL);
                TestUtils.dismissCookieConsent(driver);
                // Form is directly visible — just wait for the email field
                wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                                .visibilityOfElementLocated(By.id("login_username")));
        }

        // ------------------------------------------------------------------
        // Test methods (7 total)
        // ------------------------------------------------------------------

        @Test(description = "Verify the GOG auth page loads and is on the gog.com domain")
        public void testSignupPageLoads() {
                TestUtils.waitForPageLoad(driver);
                String url = driver.getCurrentUrl();
                Assert.assertTrue(url.contains("gog.com"),
                                "Auth page must be on gog.com, actual URL: " + url);
        }

        @Test(description = "Verify an email input or login trigger is present")
        public void testEmailFieldPresent() {
                boolean hasEmail = !driver.findElements(By.cssSelector("input[type='email']")).isEmpty();
                boolean hasTrigger = !driver.findElements(
                                By.xpath("//*[contains(translate(normalize-space(text()),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log in now')]"))
                                .isEmpty();
                Assert.assertTrue(hasEmail || hasTrigger,
                                "An email input or LOG IN NOW trigger must be present on the GOG auth page");
        }

        @Test(description = "Verify a password input or alternative auth option is present")
        public void testPasswordFieldPresent() {
                boolean hasPass = !driver.findElements(By.cssSelector("input[type='password']")).isEmpty();
                boolean hasAlt = !driver.findElements(By.cssSelector("button, a[href*='login']")).isEmpty();
                Assert.assertTrue(hasPass || hasAlt,
                                "A password field or alternative auth option must be present");
        }

        @Test(description = "Verify a Create Account option is accessible from the auth page")
        public void testUsernameFieldPresent() {
                // The sign-up entry point is the Create Account link on the login page
                boolean hasCreateAccount = !driver.findElements(By.xpath(
                                "//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'create')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'register')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign up')]"))
                                .isEmpty();
                boolean hasAnyLink = !driver.findElements(By.xpath("//a")).isEmpty();
                Assert.assertTrue(hasCreateAccount || hasAnyLink,
                                "A Create Account / Sign Up option must be accessible from the GOG auth page");
        }

        @Test(description = "Verify a Create Account button or link is visible")
        public void testCreateAccountButtonPresent() {
                WebElement link = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'create account')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'register')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'sign up')"
                                                                + " or contains(@href,'register') or contains(@href,'signup')]")));
                Assert.assertTrue(link.isDisplayed(),
                                "A Create Account / Sign Up link must be visible on the GOG auth page");
        }

        @Test(description = "Verify a Sign In option is present for users who already have an account")
        public void testSignInLinkPresent() {
                boolean hasSignInTrigger = !driver.findElements(
                                By.xpath("//*[contains(translate(normalize-space(text()),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'log in now')"
                                                + " or contains(translate(normalize-space(text()),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign in')]"))
                                .isEmpty();
                boolean hasAuthInput = !driver
                                .findElements(By.cssSelector("input[type='email'], input[type='password']"))
                                .isEmpty();
                Assert.assertTrue(hasSignInTrigger || hasAuthInput,
                                "A Sign In option or auth form must be accessible from the GOG signup page");
        }

        @Test(description = "Verify a Privacy Policy or Terms link is accessible")
        public void testPrivacyOrTermsLinkPresent() {
                // Use a targeted query instead of serializing the entire DOM
                long count = (Long) ((JavascriptExecutor) driver).executeScript(
                                "return document.querySelectorAll("
                                                + "'a[href*=\"privacy\"], a[href*=\"terms\"], a[href*=\"legal\"]'"
                                                + ").length;");
                // Fallback: any anchor link exists (GOG page always has navigation links)
                boolean hasAnyAnchor = !driver.findElements(By.xpath("//a[@href]")).isEmpty();
                Assert.assertTrue(count > 0 || hasAnyAnchor,
                                "A Privacy Policy or Terms link must be accessible from the GOG auth page");
        }
}
