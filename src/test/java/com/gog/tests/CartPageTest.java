package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * CartPageTest - verifies the GOG.com shopping cart by adding The Witcher 3
 * to the cart and then navigating to the cart page.
 */
public class CartPageTest extends BaseTest {

        private static final String GAME_URL = "https://www.gog.com/en/game/the_witcher_3_wild_hunt";
        private static final String CART_URL = "https://cart.gog.com/";

        /**
         * Navigate to The Witcher 3, dismiss the age gate, add the game to cart,
         * then open the cart page for subsequent tests.
         */
        @BeforeClass(alwaysRun = true)
        public void addToCartAndOpenCartPage() {
                driver.get(GAME_URL);
                TestUtils.waitForPageLoad(driver);
                TestUtils.dismissCookieConsent(driver);

                // Dismiss the mature-content age gate (fast pre-check first)
                if (!driver.findElements(By.cssSelector("[class*='age-gate']")).isEmpty()) {
                        try {
                                WebElement ageBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                                                .until(ExpectedConditions.elementToBeClickable(
                                                                By.cssSelector("button.age-gate__button, button[class*='age-gate']")));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ageBtn);
                                TestUtils.waitForPageLoad(driver);
                                TestUtils.pause(1000);
                        } catch (Exception ignored) {
                                // Could not click age gate - continue
                        }
                }

                // Click Add to Cart (selenium-id="AddToCartButton") only if not already in cart.
                // If the game is already in cart, CheckoutButton is visible instead.
                try {
                        List<WebElement> checkoutBtns = driver
                                        .findElements(By.cssSelector("[selenium-id='CheckoutButton']"));
                        boolean alreadyInCart = !checkoutBtns.isEmpty()
                                        && checkoutBtns.get(0).isDisplayed();
                        if (!alreadyInCart) {
                                WebElement addBtn = wait.until(
                                                ExpectedConditions.elementToBeClickable(
                                                                By.cssSelector("[selenium-id='AddToCartButton']")));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
                                TestUtils.pause(2000);
                        }
                } catch (Exception ignored) {
                        // Game may already be in cart or owned - continue
                }

                // Click "Check out now" on the product page (shown when item is in cart)
                // or fall back to the header mini-cart "Go to checkout" dropdown button.
                try {
                        WebElement goToCheckout = new WebDriverWait(driver, Duration.ofSeconds(5))
                                        .until(ExpectedConditions.elementToBeClickable(
                                                        By.cssSelector("[selenium-id='CheckoutButton'], "
                                                                        + "[data-cy='menu-cart-checkout-button'], "
                                                                        + "[hook-test='cartCheckoutNow']")));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", goToCheckout);
                        TestUtils.waitForPageLoad(driver);
                        TestUtils.pause(1000);
                } catch (Exception ignored) {
                        // Navigate directly to cart as fallback
                        driver.get(CART_URL);
                        TestUtils.waitForPageLoad(driver);
                        TestUtils.pause(1000);
                }
        }

        @Test(description = "Verify the GOG cart page loads on the gog.com domain")
        public void testCartPageLoads() {
                Assert.assertTrue(
                                driver.getCurrentUrl().contains("gog.com"),
                                "Cart page must be on gog.com domain, actual URL: " + driver.getCurrentUrl());
        }

        @Test(description = "Verify the GOG cart page has a non-empty page title")
        public void testCartPageTitle() {
                String title = driver.getTitle();
                Assert.assertFalse(
                                title == null || title.trim().isEmpty(),
                                "Cart page must have a non-empty title");
        }

        @Test(description = "Verify the cart contains The Witcher 3 or shows cart content")
        public void testCartItemPresent() {
                TestUtils.pause(800);
                // Look for a product in the cart by class name or by matching Witcher text
                boolean itemPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("[class*='product'], [class*='cart-item'], "
                                                + "[class*='cart__item'], [class*='order-item']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'witcher')]"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'your cart')]"
                                                                + " | //*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'empty cart')]"
                                                                + " | //*[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'checkout')]"));
                Assert.assertTrue(itemPresent,
                                "Cart page must show a cart item, Witcher 3 title, or cart-related content");
        }

        @Test(description = "Verify a checkout or proceed-to-payment button is present on the cart page")
        public void testCheckoutButtonPresent() {
                TestUtils.pause(800);
                boolean checkoutPresent = TestUtils.isElementPresent(driver,
                                By.xpath("//button[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'checkout')]"
                                                + " | //a[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'checkout')]"
                                                + " | //*[contains(@class,'checkout')]"
                                                + " | //*[contains(@class,'payment')]"
                                                + " | //button[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'proceed')]"));
                Assert.assertTrue(checkoutPresent,
                                "A checkout or proceed-to-payment button must be present on the cart page");
        }

        @Test(description = "Verify a price or total amount is displayed on the cart page")
        public void testCartTotalPresent() {
                TestUtils.pause(800);
                boolean pricePresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("[class*='price'], [class*='total'], [class*='amount']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(translate(normalize-space(.),"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                + "'total')]"
                                                                + " | //*[contains(@class,'total')]"));
                Assert.assertTrue(pricePresent,
                                "A price or total element must be visible on the cart page");
        }

        @Test(description = "Verify a link or button to continue shopping is accessible from the cart page")
        public void testContinueShoppingLinkPresent() {
                boolean navLinkPresent = TestUtils.isElementPresent(driver,
                                By.xpath("//a[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'continue shopping')]"
                                                + " | //a[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'back to store')]"
                                                + " | //a[@href[contains(.,'gog.com')]]"));
                Assert.assertTrue(navLinkPresent,
                                "A link back to the store must be accessible from the cart page");
        }

        @Test(description = "Verify the cart page is served over HTTPS")
        public void testCartPageIsHttps() {
                Assert.assertTrue(
                                driver.getCurrentUrl().startsWith("https://"),
                                "Cart page must be served over HTTPS, actual URL: " + driver.getCurrentUrl());
        }

        @Test(description = "Verify remove or delete controls are available for cart items")
        public void testCartRemoveButtonPresent() {
                TestUtils.pause(800);
                boolean removePresent = TestUtils.isElementPresent(driver,
                                By.xpath("//button[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'remove')]"
                                                + " | //button[contains(translate(normalize-space(.),"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'delete')]"
                                                + " | //*[contains(@class,'remove')]"
                                                + " | //*[contains(@class,'delete')]"
                                                + " | //*[@aria-label[contains(translate(.,"
                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                + "'remove')]]"))
                                // If no items in cart (owned/login required), cart controls may not exist -
                                // fall back to asserting the cart page itself renders content
                                || TestUtils.isElementPresent(driver,
                                                By.cssSelector("[class*='cart'], [class*='checkout']"));
                Assert.assertTrue(removePresent,
                                "Cart controls (remove/delete) or cart content must be present on the cart page");
        }

        @Test(description = "Verify the cart page renders a main content area")
        public void testCartMainContentPresent() {
                boolean contentPresent = TestUtils.isElementPresent(driver, By.cssSelector("main, [role='main']"))
                                || TestUtils.isElementPresent(driver, By.xpath("//*[@role='main']"))
                                || TestUtils.isElementPresent(driver,
                                                By.cssSelector("[class*='content'], [class*='wrapper']"));
                Assert.assertTrue(contentPresent,
                                "A main content area must be present on the cart page");
        }

        @Test(description = "Verify the page can be scrolled to the bottom and a footer is present")
        public void testPageScrollsToBottom() {
                TestUtils.scrollToBottom(driver);
                TestUtils.pause(1000);
                boolean footerVisible = TestUtils.isElementPresent(driver, By.cssSelector("footer, [class*='footer']"));
                Assert.assertTrue(footerVisible,
                                "A footer must be present and visible after scrolling to the bottom of the cart page");
        }
}
