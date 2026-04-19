package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * GamePageTest - E2E step 4 of 5.
 *
 * Clicks The Witcher 3: Wild Hunt product tile from the search results page
 * left by SearchTest. No driver.get() shortcut.
 * Handles optional age-gate dialogs, then verifies the product page structure.
 */
public class GamePageTest extends E2EBase {

    private static final By WITCHER3_TILE =
            By.cssSelector("a[selenium-id='productTile'][href*='witcher_3_wild_hunt']");
    private static final By SEARCH_INPUT =
            By.cssSelector("input[selenium-id='searchComponentInput']");
    private static final By ADD_TO_CART  =
            By.cssSelector("[selenium-id='AddToCartButton']");

    @BeforeClass(alwaysRun = true)
    public void openWitcher3Page() {
        System.out.println("\n========================================");
        System.out.println("[GamePageTest] Step 4 - Navigate to The Witcher 3 page");
        System.out.println("========================================");

        // If the tile is not visible (filters were active), re-search
        if (!TestUtils.isElementPresent(driver, WITCHER3_TILE)) {
            System.out.println("[GamePageTest] Witcher 3 tile not visible - re-searching...");
            try {
                WebElement box = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                box.click();
                box.clear();
                box.sendKeys("witcher");
                box.sendKeys(Keys.ENTER);
                TestUtils.pause(1500);
            } catch (Exception e) {
                System.out.println("[GamePageTest] Re-search failed: " + e.getMessage());
            }
        }

        TestUtils.pause(800);
        System.out.println("[GamePageTest] Clicking The Witcher 3 product tile...");
        WebElement tile = wait.until(ExpectedConditions.elementToBeClickable(WITCHER3_TILE));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tile);
        TestUtils.waitForPageLoad(driver);
        TestUtils.pause(1000);

        dismissAgeGate();
        TestUtils.dismissCookieConsent(driver);
        TestUtils.pause(1000);
        System.out.println("[GamePageTest] Game page loaded: " + driver.getTitle());
    }

    private void dismissAgeGate() {
        if (!driver.findElements(
                By.cssSelector("[class*='age-gate'], [class*='age_gate']")).isEmpty()) {
            System.out.println("[GamePageTest] Age gate detected - dismissing...");
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("[class*='age-gate'] button,"
                                        + " [class*='age_gate'] button,"
                                        + " [class*='age-gate'] a")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                TestUtils.waitForPageLoad(driver);
                TestUtils.pause(800);
                System.out.println("[GamePageTest] Age gate dismissed.");
            } catch (Exception e) {
                System.out.println("[GamePageTest] Age gate dismiss failed: " + e.getMessage());
            }
        }
    }

    @Test(priority = 1, description = "Verify the browser navigated to The Witcher 3 page on gog.com")
    public void testOnWitcher3Page() {
        TestUtils.pause(800);
        String url = driver.getCurrentUrl();
        System.out.println("[GamePageTest] URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "Game page must be on gog.com, actual: " + url);
        Assert.assertTrue(url.toLowerCase().contains("witcher"),
                "URL must reference 'witcher' after clicking the tile, actual: " + url);
    }

    @Test(priority = 2, description = "Verify the game page is served over HTTPS")
    public void testGamePageHttps() {
        TestUtils.pause(800);
        Assert.assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Game page must be served over HTTPS, actual: " + driver.getCurrentUrl());
    }

    @Test(priority = 3, description = "Verify the page <title> contains 'Witcher'")
    public void testGamePageTitle() {
        TestUtils.pause(800);
        String title = driver.getTitle();
        System.out.println("[GamePageTest] Title: " + title);
        Assert.assertTrue(title.toLowerCase().contains("witcher"),
                "Game page title must contain 'Witcher', actual: " + title);
    }

    @Test(priority = 4, description = "Verify a H1 game-title heading is visible on the product page")
    public void testGameTitleHeadingPresent() {
        TestUtils.pause(800);
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        String text = h1.getAttribute("textContent");
        if (text == null || text.trim().isEmpty()) text = h1.getText();
        System.out.println("[GamePageTest] H1: " + text.trim());
        Assert.assertFalse(text.trim().isEmpty(),
                "The H1 game-title heading on the product page must not be empty");
    }

    @Test(priority = 5, description = "Verify the Add to Cart button is present "
            + "(selenium-id='AddToCartButton')")
    public void testBuyButtonPresent() {
        TestUtils.pause(800);
        // Scroll to top to make sure the button is reachable
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        TestUtils.pause(500);
        boolean present = TestUtils.isElementPresent(driver, ADD_TO_CART)
                || TestUtils.isElementPresent(driver,
                        By.cssSelector("button[class*='cart-button']"));
        Assert.assertTrue(present,
                "AddToCartButton (selenium-id) or cart-button must be present on the game page");
    }

    @Test(priority = 6, description = "Verify a price or ownership indicator is visible")
    public void testPricePresent() {
        TestUtils.pause(800);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[class*='price'], [class*='buy__price']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(text(),'$') or contains(text(),'EUR')"
                                + " or contains(translate(text(),'FREE','free'),'free')]"
                                + "[not(ancestor::footer)][not(self::script)]"));
        Assert.assertTrue(present,
                "A price or ownership indicator must be visible on the game page");
    }

    @Test(priority = 7, description = "Verify the game description or overview section is present")
    public void testGameDescriptionPresent() {
        TestUtils.pause(800);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[class*='description'], [class*='overview'], [class*='about']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(@class,'description') or contains(@class,'summary')]"
                                + "[string-length(normalize-space(.)) > 50]"));
        Assert.assertTrue(present,
                "A game description or overview section must be present on the product page");
    }

    @Test(priority = 8, description = "Verify at least one screenshot or media image is present")
    public void testScreenshotsPresent() {
        TestUtils.pause(1000);
        System.out.println("[GamePageTest] Scrolling to screenshots section...");
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        TestUtils.pause(1000);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[class*='screenshot'], [class*='gallery'],"
                        + " [class*='slider'], [class*='media-player']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//img[contains(@src,'.jpg') or contains(@src,'.png')"
                                + " or contains(@src,'.webp')]"
                                + "[not(ancestor::header)][not(ancestor::footer)]"));
        Assert.assertTrue(present,
                "At least one screenshot, gallery image, or media element must be present");
        System.out.println("[GamePageTest] Scrolling back to top for Add to Cart...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        TestUtils.pause(800);
    }

    @Test(priority = 9, description = "Verify the system requirements section is present")
    public void testSystemRequirementsPresent() {
        TestUtils.pause(1000);
        System.out.println("[GamePageTest] Scrolling to system requirements...");
        TestUtils.scrollToBottom(driver);
        TestUtils.pause(1000);
        boolean present = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'system req')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'minimum')]"));
        System.out.println("[GamePageTest] Scrolling back to top for CartPageTest...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        TestUtils.pause(800);
        Assert.assertTrue(present,
                "A system requirements section must be present on The Witcher 3 product page");
    }
}
