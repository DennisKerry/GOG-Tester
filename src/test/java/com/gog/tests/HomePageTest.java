package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * HomePageTest â€“ verifies the structure and key elements of the GOG.com home
 * page.
 *
 * No authentication is required; all features under test are publicly
 * accessible.
 *
 * Automation constraints noted:
 * - GOG loads featured game banners and tiles via asynchronous XHR requests.
 * A short settle pause is applied before asserting dynamic content.
 * - A GDPR cookie-consent modal may appear on the first visit from a new
 * browser profile; TestUtils.dismissCookieConsent() handles this.
 */
public class HomePageTest extends BaseTest {

        // ------------------------------------------------------------------
        // Test methods (7 total, requirement is >= 5)
        // ------------------------------------------------------------------

        @Test(description = "Verify the GOG home page title contains 'GOG'")
        public void testHomePageTitle() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                String title = driver.getTitle();
                Assert.assertTrue(
                                title.toUpperCase().contains("GOG"),
                                "Home page title should contain 'GOG', actual: " + title);
        }

        @Test(description = "Verify a header element is present on the GOG home page")
        public void testHeaderPresent() {
                driver.get(BASE_URL + "/");
                WebElement header = wait.until(
                                ExpectedConditions.presenceOfElementLocated(
                                                By.cssSelector("[class*='menu__'], [class*='menu-item'], [class*='header__']")));
                Assert.assertNotNull(header, "A <header> element must be present on the GOG home page");
        }

        @Test(description = "Verify the GOG logo or brand link is present in the header")
        public void testLogoPresent() {
                driver.get(BASE_URL + "/");
                boolean logoPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("a.header__logo, a[class*='logo'], [class*='logo'] a, [class*='brand'] a"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath(
                                                                "//img[contains(translate(@alt,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'gog')]"));
                Assert.assertTrue(logoPresent, "GOG logo or brand link must be visible in the header");
        }

        @Test(description = "Verify at least one game product tile is rendered on the home page")
        public void testGameTilesPresent() {
                driver.get(BASE_URL + "/");
                TestUtils.pause(3000); // allow async XHR tiles to render
                boolean tilesPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("[class*='product-tile'], [class*='product_tile'], "
                                                + "[class*='product-card'], [class*='game-card']"));
                Assert.assertTrue(tilesPresent,
                                "At least one product tile must be rendered on the GOG home page");
        }

        @Test(description = "Verify a Sign In link or button is accessible from the home page")
        public void testSignInPresent() {
                driver.get(BASE_URL + "/");
                boolean signInPresent = TestUtils.isElementPresent(driver,
                                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'sign in')]"
                                                + " | //*[@data-qa='header-login-button']"
                                                + " | //*[contains(@class,'login') and (self::a or self::button)]"));
                Assert.assertTrue(signInPresent,
                                "A Sign In link or button must be accessible on the GOG home page");
        }

        @Test(description = "Verify a footer is rendered on the GOG home page")
        public void testFooterPresent() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                boolean footerPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("footer, [class*='footer']"));
                Assert.assertTrue(footerPresent, "A footer element must be present on the GOG home page");
        }

        @Test(description = "Verify the GOG home page is served over HTTPS")
        public void testHomePageIsHttps() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                String url = driver.getCurrentUrl();
                Assert.assertTrue(
                                url.startsWith("https://"),
                                "GOG home page must be served over HTTPS, actual URL: " + url);
        }

        @Test(description = "Verify a full-page screenshot of the GOG home page can be captured and is non-empty")
        public void testScreenshotCapture() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                File screenshot = TestUtils.takeScreenshot(driver, "home_page");
                Assert.assertNotNull(screenshot, "Screenshot file must be created successfully");
                Assert.assertTrue(screenshot.exists(), "Screenshot file must exist on disk");
                Assert.assertTrue(screenshot.length() > 0,
                                "Screenshot file must not be empty — expected image data");
        }

        @Test(description = "Verify at least one session cookie is set after visiting GOG.com")
        public void testSessionCookiesSet() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                int cookieCount = driver.manage().getCookies().size();
                Assert.assertTrue(cookieCount > 0,
                                "At least one cookie must be set by gog.com, actual count: " + cookieCount);
        }

        @Test(description = "Verify the GOG home page includes a viewport meta tag for mobile responsiveness")
        public void testViewportMetaTagPresent() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                String content = (String) ((JavascriptExecutor) driver).executeScript(
                                "var m = document.querySelector('meta[name=\"viewport\"]');"
                                                + "return m ? m.getAttribute('content') : null;");
                Assert.assertNotNull(content,
                                "A viewport meta tag must be present on the GOG home page for mobile responsiveness");
                Assert.assertTrue(content.contains("width"),
                                "Viewport meta content must specify 'width', actual: " + content);
        }
}
