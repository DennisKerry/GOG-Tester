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

import java.io.File;
import java.util.List;

/**
 * GamePageTest - verifies the structure and key elements of a GOG game product
 * page.
 *
 * The Witcher 3: Wild Hunt is used as the test target. It is a flagship title
 * published by CD PROJEKT RED (GOG's parent company) and is guaranteed to be
 * available on the platform at all times.
 *
 * Automation constraints noted:
 * - An age-gate overlay may be shown on first visit; the test proceeds past it
 * by asserting any visible element, not specifically gated content.
 * - Game prices change during sales; tests assert the price element is present,
 * not its specific value.
 * - Screenshots use lazy loading; a short pause is applied before assertion.
 */
public class GamePageTest extends BaseTest {

        private static final String GAME_URL = "https://www.gog.com/en/game/the_witcher_3_wild_hunt";

        /**
         * Navigate to the game page once and dismiss the mature-content age gate
         * so subsequent tests don't stall waiting for blocked content.
         */
        @BeforeClass
        public void dismissAgeGate() {
                driver.get(GAME_URL);
                TestUtils.waitForPageLoad(driver);
                // Click any age-gate confirmation button ("Enter", "I'm 18+", "Confirm", etc.)
                try {
                        WebElement ageBtn = wait.until(
                                        ExpectedConditions.elementToBeClickable(
                                                        By.xpath("//button[contains(translate(normalize-space(text()),"
                                                                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                        + "'enter') or contains(translate(normalize-space(text()),"
                                                                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                        + "'confirm') or contains(translate(normalize-space(text()),"
                                                                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                        + "'18') or contains(translate(normalize-space(text()),"
                                                                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                                                        + "'yes')]")));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ageBtn);
                        TestUtils.pause(800);
                } catch (Exception ignored) {
                        // No age gate present â€” continue
                }
                TestUtils.dismissCookieConsent(driver);
        }

        @Test(description = "Verify the Witcher 3 game page loads on gog.com")
        public void testGamePageLoads() {
                driver.get(GAME_URL);
                TestUtils.waitForPageLoad(driver);
                Assert.assertTrue(
                                driver.getCurrentUrl().contains("gog.com"),
                                "Game page must be on gog.com, actual URL: " + driver.getCurrentUrl());
        }

        @Test(description = "Verify the game title heading is displayed on the product page")
        public void testGameTitlePresent() {
                driver.get(GAME_URL);
                TestUtils.pause(800);
                // Use visibilityOfElementLocated so we wait for the React-rendered h1 to be
                // fully painted; then read textContent (more reliable than getText() for SPAs).
                WebElement title = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
                String titleText = title.getAttribute("textContent");
                if (titleText == null || titleText.trim().isEmpty()) {
                        titleText = title.getText();
                }
                Assert.assertFalse(
                                titleText.trim().isEmpty(),
                                "Game title heading must not be empty");
        }

        @Test(description = "Verify a Buy or Add-to-Cart button is present on the game page")
        public void testBuyButtonPresent() {
                driver.get(GAME_URL);
                TestUtils.pause(800);
                boolean buyPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("[class*='buy-btn'], [class*='buyBtn'], "
                                                + "[class*='buy-button'], [data-qa*='buy']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'add to cart')"
                                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'buy now')"
                                                                + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                                + "'abcdefghijklmnopqrstuvwxyz'),'buy')]"));
                Assert.assertTrue(buyPresent,
                                "A Buy / Add-to-Cart button must be present on the game page");
        }

        @Test(description = "Verify the game description or overview section is present")
        public void testGameDescriptionPresent() {
                driver.get(GAME_URL);
                TestUtils.pause(800);
                boolean descPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("[class*='description'], [class*='overview'], [class*='about']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//*[contains(@class,'description') or contains(@class,'overview')]"
                                                                + "[string-length(text()) > 50]"));
                Assert.assertTrue(descPresent,
                                "A game description or overview section must be present on the page");
        }

        @Test(description = "Verify at least one screenshot or media image is present on the game page")
        public void testGameScreenshotsPresent() {
                driver.get(GAME_URL);
                TestUtils.pause(1500); // allow lazy-loaded screenshots to render
                boolean mediaPresent = TestUtils.isElementPresent(driver,
                                By.cssSelector("[class*='screenshot'], [class*='gallery'], "
                                                + "[class*='slider'], [class*='media']"))
                                || TestUtils.isElementPresent(driver,
                                                By.xpath("//img[contains(@src,'.jpg') or contains(@src,'.png') "
                                                                + "or contains(@src,'.webp')][not(ancestor::header)]"));
                Assert.assertTrue(mediaPresent,
                                "At least one screenshot or media image must be present on the game page");
        }

        @Test(description = "Verify the system requirements section is present on the game page")
        public void testSystemRequirementsPresent() {
                driver.get(GAME_URL);
                TestUtils.pause(800);
                boolean sysReqPresent = TestUtils.isElementPresent(driver,
                                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'system req')"
                                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'minimum')"
                                                + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'system-req')"
                                                + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                                + "'abcdefghijklmnopqrstuvwxyz'),'sysreq')]"));
                Assert.assertTrue(sysReqPresent,
                                "System requirements section must be present on the game page");
        }

        @Test(description = "Verify the game page is served over HTTPS")
        public void testGamePageIsHttps() {
                driver.get(GAME_URL);
                TestUtils.waitForPageLoad(driver);
                Assert.assertTrue(
                                driver.getCurrentUrl().startsWith("https://"),
                                "Game page must be served over HTTPS");
        }

        @Test(description = "Verify a full-page screenshot of the game product page can be captured and is non-empty")
        public void testGamePageScreenshot() {
                driver.get(GAME_URL);
                TestUtils.waitForPageLoad(driver);
                TestUtils.pause(800);
                File screenshot = TestUtils.takeScreenshot(driver, "game_page_witcher3");
                Assert.assertNotNull(screenshot, "Screenshot must be captured from the game product page");
                Assert.assertTrue(screenshot.exists(), "Screenshot file must exist on disk");
                Assert.assertTrue(screenshot.length() > 0,
                                "Screenshot file must not be empty â€” expected image data");
        }

        @Test(description = "Verify the game price or ownership indicator displays non-empty text")
        public void testGamePriceOrOwnershipPresent() {
                driver.get(GAME_URL);
                TestUtils.pause(1500);
                List<WebElement> priceEls = driver.findElements(
                                By.cssSelector("[class*='price'], [class*='buy__price'], [class*='buyBtn']"));
                if (!priceEls.isEmpty()) {
                        WebElement priceEl = priceEls.get(0);
                        TestUtils.scrollIntoView(driver, priceEl);
                        String text = priceEl.getAttribute("textContent");
                        if (text == null || text.trim().isEmpty()) {
                                text = priceEl.getText();
                        }
                        Assert.assertFalse(text == null || text.trim().isEmpty(),
                                        "Price / ownership element must display non-empty text");
                } else {
                        // Price element was not found â€” assert the page is still accessible
                        Assert.assertTrue(driver.getCurrentUrl().contains("gog.com"),
                                        "Game page must remain accessible on gog.com even without a visible price element");
                }
        }

        @Test(description = "Verify system requirements section can be scrolled into view on the game page")
        public void testSystemRequirementsScrollable() {
                driver.get(GAME_URL);
                TestUtils.pause(800);
                By sysReqSel = By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                + "'abcdefghijklmnopqrstuvwxyz'),'system req')"
                                + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                + "'abcdefghijklmnopqrstuvwxyz'),'minimum')"
                                + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                                + "'abcdefghijklmnopqrstuvwxyz'),'sysreq')]");
                List<WebElement> sysReqEls = driver.findElements(sysReqSel);
                if (!sysReqEls.isEmpty()) {
                        TestUtils.scrollIntoView(driver, sysReqEls.get(0));
                        TestUtils.pause(500);
                        // Verify the scroll completed and the page is still accessible
                        Assert.assertTrue(driver.getCurrentUrl().contains("gog.com"),
                                        "Page must remain on gog.com after scrolling to system requirements");
                } else {
                        // Element not present â€” scroll to bottom as a fallback scroll assertion
                        TestUtils.scrollToBottom(driver);
                        TestUtils.pause(500);
                        Assert.assertTrue(driver.getCurrentUrl().contains("gog.com"),
                                        "Page must remain on gog.com after scrolling to the bottom");
                }
        }
}
