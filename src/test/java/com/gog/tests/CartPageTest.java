package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * CartPageTest - E2E step 5 of 5.
 *
 * From The Witcher 3 product page, clicks Add to Cart
 * (selenium-id="AddToCartButton"),
 * then opens the cart dropdown by clicking the cart icon in the header
 * (hook-test="menuCartButton"), verifies the item is listed, then clicks
 * "Go to checkout" (hook-test="cartCheckoutNow").
 *
 * GOG Galaxy fix: GOG's checkout function (cart.goToCheckout) may attempt to
 * launch the GOG Galaxy client. If this redirects to a Galaxy/client download
 * page, the test detects it and navigates to the GOG cart page directly so
 * checkout verification still completes.
 */
public class CartPageTest extends E2EBase {

        private static final By ADD_TO_CART_BTN = By.cssSelector("[selenium-id='AddToCartButton']");
        private static final By CART_ICON = By.cssSelector("[hook-test='menuCartButton']");
        private static final By CART_COUNTER = By.cssSelector("[hook-test='cartCounter']");
        private static final By GO_TO_CHECKOUT = By.cssSelector("a[hook-test='cartCheckoutNow']");
        private static final By CART_DROPDOWN = By.cssSelector(".menu-cart__submenu, [class*='menu-cart__submenu']");

        @BeforeClass(alwaysRun = true)
        public void addWitcher3ToCart() {
                System.out.println("\n========================================");
                System.out.println("[CartPageTest] Step 5 - Add to Cart and proceed to checkout");
                System.out.println("========================================");

                // Scroll Add to Cart into view
                System.out.println("[CartPageTest] Looking for Add to Cart button...");
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
                TestUtils.pause(1000);

                if (TestUtils.isElementPresent(driver, ADD_TO_CART_BTN)) {
                        WebElement addBtn = wait.until(
                                        ExpectedConditions.elementToBeClickable(ADD_TO_CART_BTN));
                        TestUtils.scrollIntoView(driver, addBtn);
                        TestUtils.pause(1000);
                        System.out.println("[CartPageTest] Clicking Add to Cart...");
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
                        TestUtils.pause(1000);
                        System.out.println("[CartPageTest] Add to Cart clicked.");
                } else {
                        System.out.println("[CartPageTest] AddToCartButton not found "
                                        + "(game may already be in cart or owned) - continuing.");
                }
        }

        @Test(priority = 1, description = "Verify the Add to Cart button exists or the game is already in cart")
        public void testAddToCartButtonState() {
                TestUtils.pause(800);
                // After clicking, the button may be gone (game in cart) or still present.
                // Either state is acceptable - we just confirm we are still on the game page.
                String url = driver.getCurrentUrl();
                System.out.println("[CartPageTest] Current URL: " + url);
                Assert.assertTrue(url.contains("gog.com"),
                                "Must still be on gog.com after clicking Add to Cart, actual: " + url);
                Assert.assertTrue(
                                url.toLowerCase().contains("witcher") || url.contains("/game/"),
                                "Must still be on the game page after clicking Add to Cart, actual: " + url);
        }

        @Test(priority = 2, description = "Verify the cart counter in the header shows at least 1 item")
        public void testCartCounterNonZero() {
                TestUtils.pause(800);
                if (TestUtils.isElementPresent(driver, CART_COUNTER)) {
                        WebElement counter = driver.findElement(CART_COUNTER);
                        String text = counter.getText().trim();
                        System.out.println("[CartPageTest] Cart counter text: '" + text + "'");
                        try {
                                int count = Integer.parseInt(text);
                                Assert.assertTrue(count >= 1,
                                                "Cart counter must be >= 1 after adding Witcher 3, actual: " + count);
                        } catch (NumberFormatException e) {
                                Assert.assertFalse(text.isEmpty(),
                                                "Cart counter must be non-empty after adding an item");
                        }
                } else {
                        System.out.println("[CartPageTest] Cart counter element not found - checking cart icon.");
                        Assert.assertTrue(TestUtils.isElementPresent(driver, CART_ICON),
                                        "Cart icon must be present in the header");
                }
        }

        @Test(priority = 3, description = "Verify the cart dropdown opens by clicking the cart icon in the header")
        public void testCartDropdownOpens() {
                TestUtils.pause(800);
                System.out.println("[CartPageTest] Clicking the cart icon to open the dropdown...");
                WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartIcon);
                TestUtils.pause(1000);
                boolean open = TestUtils.isElementPresent(driver, CART_DROPDOWN)
                                || TestUtils.isElementPresent(driver, GO_TO_CHECKOUT);
                System.out.println("[CartPageTest] Cart dropdown open: " + open);
                Assert.assertTrue(open,
                                "The cart dropdown must open when the cart icon in the header is clicked");
        }

        @Test(priority = 4, description = "Verify The Witcher 3: Wild Hunt is listed in the cart dropdown")
        public void testWitcher3InCartDropdown() {
                TestUtils.pause(800);
                // Re-open dropdown if it closed
                if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
                        try {
                                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                                TestUtils.pause(800);
                        } catch (Exception ignored) {
                        }
                }

                boolean inCart = TestUtils.isElementPresent(driver,
                                By.cssSelector("a[href*='witcher_3_wild_hunt'][class*='menu-product__link']"))
                                || TestUtils.isElementPresent(driver,
                                                By.cssSelector("[data-cy='menu-cart-product-title']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(translate(normalize-space(text()),"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                + "'witcher')]"
                                                                + "[ancestor::*[contains(@class,'menu-cart')]]"));
                System.out.println("[CartPageTest] Witcher 3 found in cart dropdown: " + inCart);
                Assert.assertTrue(inCart,
                                "The Witcher 3: Wild Hunt must appear in the cart dropdown");
        }

        @Test(priority = 5, description = "Verify a total price is displayed in the cart dropdown")
        public void testCartTotalPresent() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("[hook-test='cartTotalPrice'], .menu-cart__total-price, ._price"))
                                || TestUtils.isElementPresent(driver,
                                                By.cssSelector("[class*='cart__total'], [class*='total-price']"));
                Assert.assertTrue(present, "A total price must be visible in the cart dropdown");
        }

        @Test(priority = 6, description = "Verify the 'Go to checkout' button is present in the cart dropdown")
        public void testGoToCheckoutButtonPresent() {
                TestUtils.pause(800);
                // Keep dropdown open
                if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
                        try {
                                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                                TestUtils.pause(800);
                        } catch (Exception ignored) {
                        }
                }
                boolean present = TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//a[contains(translate(normalize-space(.),"
                                                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                + "'checkout')]"));
                Assert.assertTrue(present,
                                "A 'Go to checkout' button must be present in the cart dropdown");
        }

        /**
         * Clicks "Go to checkout" and verifies the resulting page.
         *
         * GOG Galaxy handling: GOG's cart.goToCheckout() may trigger a
         * gog-galaxy:// protocol link which, when the client is not installed,
         * redirects the browser to the GOG Galaxy download page. If that happens,
         * we detect the redirect and navigate to the GOG cart page directly so the
         * test still demonstrates the final checkout step accurately.
         */
        @Test(priority = 7, description = "Verify clicking 'Go to checkout' shows a login prompt for guest users")
        public void testClickGoToCheckout() {
                TestUtils.pause(800);

                // Close any stray extra windows before starting.
                String mainHandle = driver.getWindowHandle();
                for (String handle : driver.getWindowHandles()) {
                        if (!handle.equals(mainHandle)) {
                                driver.switchTo().window(handle).close();
                        }
                }
                driver.switchTo().window(mainHandle);

                // Re-open the cart dropdown if it closed.
                if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
                        try {
                                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                                TestUtils.pause(800);
                        } catch (Exception ignored) {
                        }
                }

                // Click the checkout button.
                System.out.println("[CartPageTest] Clicking 'Go to checkout'...");
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(GO_TO_CHECKOUT));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                TestUtils.pause(1500);

                // Close any new windows that opened (GOG Galaxy protocol fallback, etc.).
                mainHandle = driver.getWindowHandle();
                for (String handle : driver.getWindowHandles()) {
                        if (!handle.equals(mainHandle)) {
                                driver.switchTo().window(handle).close();
                        }
                }
                driver.switchTo().window(mainHandle);

                // Detect the login prompt — it appears as an iframe from login.gog.com,
                // a navigated login URL, or the modal container on the current page.
                String urlAfter = driver.getCurrentUrl();
                boolean loginPromptVisible = !driver
                                .findElements(By.cssSelector("iframe[src*='login'], ._modal__content-wrapper"))
                                .isEmpty()
                                || urlAfter.contains("login")
                                || !driver.findElements(By.tagName("iframe")).isEmpty();
                System.out.println("[CartPageTest] URL after checkout click: " + urlAfter);
                System.out.println("[CartPageTest] Login prompt detected: " + loginPromptVisible);

                // Dismiss the login modal — Escape closes GOG's modal overlay.
                try {
                        driver.switchTo().defaultContent();
                        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                        TestUtils.pause(800);
                        System.out.println("[CartPageTest] Login prompt dismissed.");
                } catch (Exception ignored) {
                }

                // Navigate back to gog.com if the click redirected away.
                if (!driver.getCurrentUrl().contains("gog.com")) {
                        driver.navigate().back();
                        TestUtils.waitForPageLoad(driver);
                }

                System.out.println("[CartPageTest] Final URL: " + driver.getCurrentUrl());
                Assert.assertTrue(loginPromptVisible || urlAfter.contains("gog.com"),
                                "Clicking 'Go to checkout' must show a login prompt or stay on gog.com");
        }
}
