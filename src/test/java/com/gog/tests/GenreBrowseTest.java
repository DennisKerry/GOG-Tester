package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * GenreBrowseTest - verifies the GOG game catalog and genre-based browsing.
 *
 * The RPG genre is used as the primary test target because GOG has an extensive
 * RPG library (Witcher series, Cyberpunk 2077, Baldur's Gate, etc.)
 * guaranteeing
 * results at all times.
 *
 * Automation constraints noted:
 * - GOG's catalog page loads game tiles via asynchronous XHR after the initial
 * HTML is delivered. Pause durations are applied to allow results to appear.
 * - Filter and sort controls may be rendered inside a collapsible sidebar;
 * tests check for element presence without requiring the sidebar to be open.
 */
public class GenreBrowseTest extends BaseTest {

    private static final String GAMES_URL = BASE_URL + "/games";
    private static final String RPG_URL = BASE_URL + "/games?genres=rpg";

    // ------------------------------------------------------------------
    // Test methods (6 total, requirement is >= 5)
    // ------------------------------------------------------------------

    @Test(description = "Verify the GOG games catalog page loads and stays on gog.com")
    public void testCatalogPageLoads() {
        driver.get(GAMES_URL);
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("gog.com"),
                "Games catalog must be on gog.com, actual URL: " + driver.getCurrentUrl());
    }

    @Test(description = "Verify the RPG genre browse page loads and stays on gog.com")
    public void testGenrePageLoads() {
        driver.get(RPG_URL);
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("gog.com"),
                "RPG genre page must stay on gog.com, actual URL: " + driver.getCurrentUrl());
    }

    @Test(description = "Verify game product tiles are rendered in the RPG genre listing")
    public void testGameTilesPresentInGenre() {
        driver.get(RPG_URL);
        TestUtils.pause(1500); // allow XHR tile results to render
        boolean tilesPresent = TestUtils.isElementPresent(driver,
                By.cssSelector("[class*='product-tile'], [class*='product_tile'], "
                        + "[class*='product-card'], [class*='productcell']"));
        Assert.assertTrue(tilesPresent,
                "At least one product tile must appear in the RPG genre listing");
    }

    @Test(description = "Verify game tile cover images are present in the genre listing")
    public void testGameTileImagesPresent() {
        driver.get(RPG_URL);
        TestUtils.pause(1500);
        boolean imagesPresent = TestUtils.isElementPresent(driver,
                By.cssSelector("[class*='product-tile'] img, [class*='product_tile'] img, "
                        + "[class*='product-card'] img, [class*='productcell'] img"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(@class,'product') and @href]//img"));
        Assert.assertTrue(imagesPresent,
                "Game tile cover images must be present in the genre listing");
    }

    @Test(description = "Verify sort or filter controls are present on the catalog page")
    public void testSortFilterControlsPresent() {
        driver.get(GAMES_URL);
        TestUtils.waitForPageLoad(driver);
        boolean controlsPresent = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'sort')"
                        + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'filter')"
                        + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'filter')"
                        + " or contains(translate(@class,'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'sort')]"
                        + "[not(ancestor::footer)]"));
        Assert.assertTrue(controlsPresent,
                "Sort or filter controls must be visible on the games catalog page");
    }

    @Test(description = "Verify the catalog page title or heading references Games or GOG")
    public void testCatalogPageTitleOrHeading() {
        driver.get(GAMES_URL);
        TestUtils.waitForPageLoad(driver);
        String title = driver.getTitle();
        boolean titleOk = title.toUpperCase().contains("GOG")
                || title.toLowerCase().contains("game");
        boolean headingPresent = TestUtils.isElementPresent(driver,
                By.xpath("//*[self::h1 or self::h2][contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'game')]"));
        Assert.assertTrue(titleOk || headingPresent,
                "Catalog page title or heading must reference 'GOG' or 'Game', actual title: " + title);
    }

    @Test(description = "Verify the RPG genre browse URL contains the expected 'rpg' or 'genres' query parameter")
    public void testRpgGenreUrlParameter() {
        driver.get(RPG_URL);
        TestUtils.waitForPageLoad(driver);
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(
                currentUrl.contains("rpg") || currentUrl.contains("genres"),
                "RPG genre URL must contain 'rpg' or 'genres' parameter, actual: " + currentUrl);
    }

    @Test(description = "Verify the RPG genre listing contains more than one game tile (count-based assertion)")
    public void testRpgGenreHasMultipleTiles() {
        driver.get(RPG_URL);
        TestUtils.pause(1500);
        List<WebElement> tiles = driver.findElements(
                By.cssSelector("[class*='product-tile'], [class*='product_tile'], "
                        + "[class*='product-card'], [class*='productcell']"));
        Assert.assertTrue(tiles.size() > 1,
                "RPG genre must have more than 1 game tile, actual count: " + tiles.size());
    }
}
