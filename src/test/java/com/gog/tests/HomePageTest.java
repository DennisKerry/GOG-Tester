package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * HomePageTest - E2E step 1 of 5.
 *
 * Opens https://www.gog.com, dismisses the cookie-consent banner, and verifies
 * the home page structure and key navigation elements.
 *
 * Pauses of ~2 seconds are added between actions so presenters can narrate.
 */
public class HomePageTest extends E2EBase {

        @BeforeClass(alwaysRun = true)
        public void openHomePage() {
                System.out.println("\n========================================");
                System.out.println("[HomePageTest] Step 1 - Opening GOG.com home page");
                System.out.println("========================================");
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                TestUtils.dismissCookieConsent(driver);
                TestUtils.pause(1000);
                System.out.println("[HomePageTest] Home page loaded: " + driver.getTitle());
        }

        @Test(priority = 1, description = "Verify the GOG home-page title contains 'GOG'")
        public void testHomePageTitle() {
                TestUtils.pause(800);
                String title = driver.getTitle();
                System.out.println("[HomePageTest] Page title: " + title);
                Assert.assertTrue(title.toUpperCase().contains("GOG"),
                                "Home-page title must contain 'GOG', actual: " + title);
        }

        @Test(priority = 2, description = "Verify the home page is served over HTTPS")
        public void testHomePageIsHttps() {
                TestUtils.pause(800);
                String url = driver.getCurrentUrl();
                System.out.println("[HomePageTest] URL: " + url);
                Assert.assertTrue(url.startsWith("https://"),
                                "Home page must be served over HTTPS, actual: " + url);
        }

        @Test(priority = 3, description = "Verify a header navigation bar is present")
        public void testHeaderPresent() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("[hook-test='menuStore'], [class*='menu__'], nav"))
                                || TestUtils.isElementPresent(driver,
                                                By.cssSelector("[class*='header'], [class*='navbar']"));
                Assert.assertTrue(present, "A header / navigation bar must be visible on the home page");
        }

        @Test(priority = 4, description = "Verify the GOG logo is present in the header")
        public void testLogoPresent() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("a.menu__logo, a[class*='logo'], [class*='logo'] a, svg.gog-logo"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//img[contains(translate(@alt,"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'gog')]"));
                Assert.assertTrue(present, "The GOG logo must be visible in the header");
        }

        @Test(priority = 5, description = "Verify the Sign In button is present (hook-test='menuAnonymousButton')")
        public void testSignInButtonPresent() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("a[hook-test='menuAnonymousButton']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(translate(normalize-space(text()),"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                + "'sign in')]"));
                Assert.assertTrue(present,
                                "The Sign In button (hook-test='menuAnonymousButton') must be visible in the header");
        }

        @Test(priority = 6, description = "Verify the Store navigation link is present in the header")
        public void testStoreNavLinkPresent() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("a[hook-test='menuStoreButton']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//a[contains(@href,'/games')]"
                                                                + "[ancestor::*[contains(@class,'menu')]]"));
                Assert.assertTrue(present, "The Store link must be present in the GOG header");
        }

        @Test(priority = 7, description = "Verify at least one game product tile is rendered on the home page")
        public void testGameTilesPresent() {
                TestUtils.pause(1000);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("a[selenium-id='productTile'], [class*='product-tile'], "
                                                + "[class*='product_tile'], [class*='productTile']"));
                Assert.assertTrue(present,
                                "At least one product tile must be rendered on the GOG home page");
        }

        @Test(priority = 8, description = "Verify a footer element is rendered on the home page")
        public void testFooterPresent() {
                TestUtils.pause(1000);
                System.out.println("[HomePageTest] Scrolling to page footer...");
                TestUtils.scrollToBottom(driver);
                TestUtils.pause(1000);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("footer, [class*='footer']"));
                Assert.assertTrue(present, "A footer must be visible on the GOG home page");
                System.out.println("[HomePageTest] Scrolling back to top...");
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
                TestUtils.pause(800);
        }

        @Test(priority = 9, description = "Verify that session cookies are set after visiting GOG.com")
        public void testSessionCookiesSet() {
                TestUtils.pause(800);
                int count = driver.manage().getCookies().size();
                System.out.println("[HomePageTest] Cookie count: " + count);
                Assert.assertTrue(count > 0,
                                "At least one cookie must be set by gog.com, actual count: " + count);
        }

        @Test(priority = 10, description = "Verify the home page renders more than 3 game product tiles (catalog richness)")
        public void testMultipleGameTilesPresent() {
                TestUtils.pause(1000);
                List<WebElement> tiles = driver.findElements(
                                By.cssSelector("a[selenium-id='productTile'], [class*='product-tile'], [class*='productTile']"));
                System.out.println("[HomePageTest] Home page game tile count: " + tiles.size());
                Assert.assertTrue(tiles.size() > 3,
                                "The GOG home page must display more than 3 game tiles, actual: " + tiles.size());
        }

        @Test(priority = 11, description = "Verify the footer contains legal or informational links (privacy, terms, cookies, or refund)")
        public void testFooterContainsLegalLinks() {
                TestUtils.pause(800);
                TestUtils.scrollToBottom(driver);
                TestUtils.pause(1000);
                // Match by link TEXT (GOG footer text may be 'Privacy Policy', 'User
                // Agreement', 'Refund Policy', etc.)
                boolean present = TestUtils.isElementPresent(driver,
                                By.xpath("//footer//a[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'privacy')"
                                                + " or contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'terms')"
                                                + " or contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'cookies')"
                                                + " or contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'refund')"
                                                + " or contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'legal')"
                                                + " or contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'agreement')]"));
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
                Assert.assertTrue(present,
                                "The footer must contain at least one legal/informational link "
                                                + "(privacy, terms, cookies, refund, legal, or agreement)");
        }
}
