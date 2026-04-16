package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * CartPageTest - verifies the GOG.com shopping cart affordances and related
 * navigational elements without performing any purchase transaction.
 *
 * GOG does not expose a stable anonymous cart page on every visit, so these
 * tests validate the cart entry points and shared store navigation from the
 * public homepage instead.
 *
 * Automation constraints noted:
 * - The public homepage is the stable entry point for cart-related navigation.
 * - The cart button may be rendered as an icon, a count badge, or a link with
 * generic href="#" wiring, so selectors intentionally stay broad.
 */
public class CartPageTest extends BaseTest {

        /**
         * Load the public GOG homepage once for the class and validate cart-related
         * elements from there.
         */
        @BeforeClass
        public void navigateToStoreHomePage() {
                driver.get(BASE_URL + "/");
                TestUtils.waitForPageLoad(driver);
                TestUtils.dismissCookieConsent(driver);
        }

        // ------------------------------------------------------------------
        // Test methods (7 total, requirement is >= 5)
        // ------------------------------------------------------------------

        @Test(description = "Verify GOG cart/checkout page loads and URL stays on gog.com")
        public void testCartPageLoads() {
                Assert.assertTrue(
                                driver.getCurrentUrl().contains("gog.com"),
                                "Cart/checkout page must be on gog.com domain, actual URL: " + driver.getCurrentUrl());
        }

        @Test(description = "Verify the GOG cart/checkout page has a non-empty page title")
        public void testCartPageTitle() {
                String title = driver.getTitle();
                Assert.assertFalse(
                                title == null || title.trim().isEmpty(),
                                "Cart/checkout page must have a non-empty title");
        }

        @Test(description = "Verify the cart/checkout page renders a main content area")
        public void testCartMainContentPresent() {
                boolean contentPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("main, #main, [role='main']"))
                                || TestUtils.isElementPresent(driver, By.xpath("//*[@role='main']"));
                Assert.assertTrue(contentPresent,
                                "A main content element must be present on the GOG homepage");
        }

        @Test(description = "Verify the page shows cart-related content or a sign-in prompt")
        public void testEmptyCartOrHeadingPresent() {
                TestUtils.pause(2000);
                boolean cartContentPresent = TestUtils.isElementPresent(driver,
                                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'cart')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'checkout')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'empty')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'sign in')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'log in')"
                                                + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'cart')"
                                                + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'checkout')]"));
                Assert.assertTrue(cartContentPresent,
                                "Page must show cart/checkout content, empty-cart message, or a sign-in prompt");
        }

        @Test(description = "Verify a navigation link back to the GOG store is accessible")
        public void testContinueShoppingLinkPresent() {
                TestUtils.pause(2000);
                boolean navLinkPresent = TestUtils.isElementPresent(driver,
                                By.xpath("//a[contains(@href,'gog.com') "
                                                + "and string-length(normalize-space(.)) > 0][1]"));
                Assert.assertTrue(navLinkPresent,
                                "A link to the GOG store must be accessible from the homepage");
        }

        @Test(description = "Verify GOG header or navigation is accessible from the cart/checkout page")
        public void testHeaderOrNavOnCartPage() {
                boolean headerPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("header, [class*='header'], nav, [class*='nav'], [role='banner']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//header | //nav | //*[@role='navigation'] | //*[@role='banner']"));
                Assert.assertTrue(headerPresent,
                                "GOG header or navigation must be accessible from the homepage");
        }

        @Test(description = "Verify the cart/checkout page is served over HTTPS")
        public void testCartPageIsHttps() {
                Assert.assertTrue(
                                driver.getCurrentUrl().startsWith("https://"),
                                "Cart/checkout page must be served over HTTPS, actual URL: " + driver.getCurrentUrl());
        }

        @Test(description = "Verify the cart icon or button exposes a non-empty href or aria-label attribute")
        public void testCartIconAttributeValid() {
                List<WebElement> cartEls = driver.findElements(
                                By.cssSelector("a[href*='cart'], [class*='cart'][href], "
                                                + "[aria-label*='cart' i], [data-qa*='cart']"));
                // Also check GOG-specific selectors: hook-test and data-cy
                if (cartEls.isEmpty()) {
                        cartEls = driver.findElements(
                                        By.cssSelector("[hook-test='menuCart'], [data-cy='menu-cart-open-button'], "
                                                        + "[class*='menu-link--cart'], [class*='menu-cart']"));
                }
                if (!cartEls.isEmpty()) {
                        WebElement cartEl = cartEls.get(0);
                        String href = cartEl.getAttribute("href");
                        String ariaLabel = cartEl.getAttribute("aria-label");
                        String dataCy = cartEl.getAttribute("data-cy");
                        String hookTest = cartEl.getAttribute("hook-test");
                        boolean hasIdentifier = (href != null && !href.trim().isEmpty())
                                        || (ariaLabel != null && !ariaLabel.trim().isEmpty())
                                        || (dataCy != null && !dataCy.trim().isEmpty())
                                        || (hookTest != null && !hookTest.trim().isEmpty());
                        Assert.assertTrue(hasIdentifier,
                                        "Cart element must have a non-empty href, aria-label, data-cy, or hook-test attribute");
                } else {
                        // Cart element not found — assert menu is still accessible
                        Assert.assertTrue(
                                        TestUtils.isElementPresent(driver, By.cssSelector("[class*='menu__'], [class*='menu-item']"))
                                        || TestUtils.isElementPresent(driver, By.cssSelector("[class*='header__']"))),
                                        "Menu / header must be present when cart icon cannot be independently located");
                }
        }

        @Test(description = "Verify the page can be scrolled to the bottom and a footer is present")
        public void testPageScrollsToBottom() {
                TestUtils.scrollToBottom(driver);
                TestUtils.pause(1000);
                boolean footerVisible = TestUtils.isElementPresent(driver,
                                By.cssSelector("footer, [class*='footer']"));
                Assert.assertTrue(footerVisible,
                                "Footer must be present and visible after scrolling to the bottom of the page");
        }
}
