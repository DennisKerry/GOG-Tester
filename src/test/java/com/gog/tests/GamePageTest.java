package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * GamePageTest - verifies the structure and key elements of a GOG game product page.
 *
 * The Witcher 3: Wild Hunt is used as the test target. It is a flagship title
 * published by CD PROJEKT RED (GOG's parent company) and is guaranteed to be
 * available on the platform at all times.
 *
 * Automation constraints noted:
 *  - An age-gate overlay may be shown on first visit; the test proceeds past it
 *    by asserting any visible element, not specifically gated content.
 *  - Game prices change during sales; tests assert the price element is present,
 *    not its specific value.
 *  - Screenshots use lazy loading; a short pause is applied before assertion.
 */
public class GamePageTest extends BaseTest {

    private static final String GAME_URL =
        "https://www.gog.com/en/game/the_witcher_3_wild_hunt";

    // ------------------------------------------------------------------
    // Test methods (7 total, requirement is >= 5)
    // ------------------------------------------------------------------

    @Test(description = "Verify the Witcher 3 game page loads on gog.com")
    public void testGamePageLoads() {
        driver.get(GAME_URL);
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
            driver.getCurrentUrl().contains("gog.com"),
            "Game page must be on gog.com, actual URL: " + driver.getCurrentUrl()
        );
    }

    @Test(description = "Verify the game title heading is displayed on the product page")
    public void testGameTitlePresent() {
        driver.get(GAME_URL);
        WebElement title = wait.until(
            ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("h1, [class*='title'], [data-qa*='title']")
            )
        );
        Assert.assertFalse(
            title.getText().trim().isEmpty(),
            "Game title heading must not be empty"
        );
    }

    @Test(description = "Verify a Buy or Add-to-Cart button is present on the game page")
    public void testBuyButtonPresent() {
        driver.get(GAME_URL);
        TestUtils.pause(2000);
        boolean buyPresent = TestUtils.isElementPresent(driver,
            By.cssSelector("[class*='buy-btn'], [class*='buyBtn'], "
                         + "[class*='buy-button'], [data-qa*='buy']")
        ) || TestUtils.isElementPresent(driver,
            By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'add to cart')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'buy now')"
                   + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'buy')]")
        );
        Assert.assertTrue(buyPresent,
            "A Buy / Add-to-Cart button must be present on the game page");
    }

    @Test(description = "Verify the game description or overview section is present")
    public void testGameDescriptionPresent() {
        driver.get(GAME_URL);
        TestUtils.pause(2000);
        boolean descPresent = TestUtils.isElementPresent(driver,
            By.cssSelector("[class*='description'], [class*='overview'], [class*='about']")
        ) || TestUtils.isElementPresent(driver,
            By.xpath("//*[contains(@class,'description') or contains(@class,'overview')]"
                   + "[string-length(text()) > 50]")
        );
        Assert.assertTrue(descPresent,
            "A game description or overview section must be present on the page");
    }

    @Test(description = "Verify at least one screenshot or media image is present on the game page")
    public void testGameScreenshotsPresent() {
        driver.get(GAME_URL);
        TestUtils.pause(3000); // allow lazy-loaded screenshots to render
        boolean mediaPresent = TestUtils.isElementPresent(driver,
            By.cssSelector("[class*='screenshot'], [class*='gallery'], "
                         + "[class*='slider'], [class*='media']")
        ) || TestUtils.isElementPresent(driver,
            By.xpath("//img[contains(@src,'.jpg') or contains(@src,'.png') "
                   + "or contains(@src,'.webp')][not(ancestor::header)]")
        );
        Assert.assertTrue(mediaPresent,
            "At least one screenshot or media image must be present on the game page");
    }

    @Test(description = "Verify the system requirements section is present on the game page")
    public void testSystemRequirementsPresent() {
        driver.get(GAME_URL);
        TestUtils.pause(2000);
        boolean sysReqPresent = TestUtils.isElementPresent(driver,
            By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'system req')"
                   + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'minimum')"
                   + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'system-req')"
                   + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                   + "'abcdefghijklmnopqrstuvwxyz'),'sysreq')]")
        );
        Assert.assertTrue(sysReqPresent,
            "System requirements section must be present on the game page");
    }

    @Test(description = "Verify the game page is served over HTTPS")
    public void testGamePageIsHttps() {
        driver.get(GAME_URL);
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
            driver.getCurrentUrl().startsWith("https://"),
            "Game page must be served over HTTPS"
        );
    }
}
