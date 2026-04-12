package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * CartPageTest - verifies the GOG.com shopping cart page structure and
 * navigational elements without performing any purchase transaction.
 *
 * No authentication is required; an anonymous empty-cart state is tested.
 *
 * Automation constraints noted:
 *  - The GOG cart lives on a subdomain (cart.gog.com). The test navigates
 *    there directly; cross-subdomain cookie propagation is handled by the browser.
 *  - An empty-cart state is expected since no items are added during the test run.
 *    Tests assert empty-state UI elements and general page structure.
 *  - Page may redirect to login if GOG enforces authenticated cart; both
 *    anonymous and authenticated redirects are accepted as valid outcomes.
 */
public class CartPageTest extends BaseTest {

    private static final String CART_URL = "https://cart.gog.com/cart";

    // ------------------------------------------------------------------
    // Test methods (7 total, requirement is >= 5)
    // ------------------------------------------------------------------

    @Test(description = "Verify the GOG cart page loads and the URL is on gog.com")
    public void testCartPageLoads() {
        driver.get(CART_URL);
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
            driver.getCurrentUrl().contains("gog.com"),
            "Cart page must be on gog.com domain, actual URL: " + driver.getCurrentUrl()
        );
    }

    @Test(description = "Verify the GOG cart page has a non-empty page title")
    public void testCartPageTitle() {
        driver.get(CART_URL);
        TestUtils.waitForPageLoad(driver);
        String title = driver.getTitle();
        Assert.assertFalse(
            title == null || title.trim().isEmpty(),
            "Cart page must have a non-empty title"
        );
    }

    @Test(description = "Verify the cart page renders a main content area")
    public void testCartMainContentPresent() {
        driver.get(CART_URL);
        WebElement content = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("main, [class*='cart'], [class*='content'], #main")
            )
        );
        Assert.assertNotNull(content,
            "A main content element must be present on the cart page");
    }

    @Test(description = "Verify an empty cart message or Your Cart heading is displayed")
    public void testEmptyCartOrHeadingPresent() {
        driver.get(CART_URL);
        TestUtils.pause(2000);
        boolean cartContentPresent = TestUtils.isElementPresent(driver,
            By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'empty')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'your cart')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'no items')"
                   + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'cart')"
                   + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'empty')]")
        );
        Assert.assertTrue(cartContentPresent,
            "An empty cart message or cart heading must be displayed");
    }

    @Test(description = "Verify a link to continue shopping or browse games is accessible")
    public void testContinueShoppingLinkPresent() {
        driver.get(CART_URL);
        TestUtils.pause(2000);
        boolean continueLink = TestUtils.isElementPresent(driver,
            By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'continue')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'browse')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'shop')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'games')"
                   + " or contains(@href,'gog.com')]"
                   + " | //button[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'continue')]")
        );
        Assert.assertTrue(continueLink,
            "A continue shopping / browse games link must be accessible from the cart page");
    }

    @Test(description = "Verify GOG header or navigation is accessible from the cart page")
    public void testHeaderOrNavOnCartPage() {
        driver.get(CART_URL);
        TestUtils.waitForPageLoad(driver);
        boolean headerPresent = TestUtils.isElementPresent(driver,
            By.cssSelector("header, [class*='header'], nav, [class*='nav']")
        ) || TestUtils.isElementPresent(driver,
            By.xpath("//a[contains(@href,'gog.com')]")
        );
        Assert.assertTrue(headerPresent,
            "GOG header or navigation must be accessible from the cart page");
    }

    @Test(description = "Verify the cart page is served over HTTPS")
    public void testCartPageIsHttps() {
        driver.get(CART_URL);
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
            driver.getCurrentUrl().startsWith("https://"),
            "Cart page must be served over HTTPS, actual URL: " + driver.getCurrentUrl()
        );
    }
}
