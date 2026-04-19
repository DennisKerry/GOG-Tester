package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * CatalogBrowseTest – verifies that GOG's URL-parameter-based catalog
 * filtering and sorting actually produces correct, relevant results.
 *
 * This class tests URL manipulation directly (e.g. ?genres=rpg&order=asc:title)
 * rather than clicking interactive filter controls (covered by SearchTest).
 * This validates that GOG's catalog backend correctly interprets query
 * parameters
 * — critical for bookmark/link sharing and direct navigation.
 *
 * Automation constraints noted:
 * - GOG loads game tiles asynchronously via XHR after the initial HTML is
 * delivered; a 2-second settle pause is applied after each navigation.
 * - URL parameters may be normalised by GOG's SPA router; assertions accept
 * both the raw param form and the router-normalised form.
 */
public class CatalogBrowseTest extends E2EBase {

    private static final String GAMES_URL = BASE_URL + "/en/games";
    private static final By PRODUCT_TILE = By.cssSelector("a[selenium-id='productTile']");
    private static final By PRICE_ELEMENT = By.cssSelector(
            "[class*='price'], [selenium-id*='price'], [class*='product-price']");
    private static final By DISCOUNT_BADGE = By.cssSelector(
            "[class*='discount'], [class*='sale-badge'], [class*='label--discount']");

    @BeforeClass(alwaysRun = true)
    public void openCatalogPage() {
        System.out.println("\n========================================");
        System.out.println("[CatalogBrowseTest] Step 7 - Browse GOG catalog via URL parameters");
        System.out.println("========================================");
        driver.get(GAMES_URL);
        TestUtils.waitForPageLoad(driver);
        TestUtils.dismissCookieConsent(driver);
        System.out.println("[CatalogBrowseTest] Base catalog loaded: " + driver.getCurrentUrl());
    }

    // -----------------------------------------------------------------------

    @Test(priority = 1, description = "Verify the base GOG catalog page displays at least 20 game tiles")
    public void testCatalogDisplaysMinimumTileCount() {
        driver.get(GAMES_URL);
        TestUtils.pause(2000);
        List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
        System.out.println("[CatalogBrowseTest] Base catalog tile count: " + tiles.size());
        Assert.assertTrue(tiles.size() >= 20,
                "The GOG games catalog must display at least 20 game tiles on the base page, "
                        + "actual: " + tiles.size());
    }

    @Test(priority = 2, description = "Verify ?genres=rpg URL param filters catalog to RPG titles and retains param in URL")
    public void testRpgGenreUrlParamReturnsResults() {
        driver.get(GAMES_URL + "?genres=rpg");
        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        System.out.println("[CatalogBrowseTest] RPG filter URL: " + url);
        Assert.assertTrue(url.contains("rpg") || url.contains("genres"),
                "URL must retain the genres=rpg parameter after navigation, actual: " + url);
        List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
        System.out.println("[CatalogBrowseTest] RPG genre tile count: " + tiles.size());
        Assert.assertTrue(tiles.size() >= 5,
                "?genres=rpg must return at least 5 game tiles — GOG has an extensive RPG library, "
                        + "actual: " + tiles.size());
    }

    @Test(priority = 3, description = "Verify ?order=asc:title sorts the catalog so the URL reflects the sort and tiles are in A-Z order")
    public void testTitleAscSortOrderApplied() {
        driver.get(GAMES_URL + "?order=asc:title");
        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        System.out.println("[CatalogBrowseTest] Title A-Z sort URL: " + url);
        Assert.assertTrue(url.contains("asc") || url.contains("order"),
                "URL must retain the order=asc:title sort parameter, actual: " + url);

        // Grab the first 3 tile slugs from href and verify A-Z ordering
        List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
        Assert.assertTrue(tiles.size() >= 3,
                "At least 3 tiles needed to verify ascending sort order, found: " + tiles.size());

        String t1 = getTileTitle(tiles.get(0));
        String t2 = getTileTitle(tiles.get(1));
        String t3 = getTileTitle(tiles.get(2));
        System.out.println("[CatalogBrowseTest] First 3 titles (A-Z): ["
                + t1 + "] [" + t2 + "] [" + t3 + "]");

        Assert.assertTrue(t1.compareToIgnoreCase(t2) <= 0,
                "With asc:title sort, tile 1 '" + t1 + "' must come before tile 2 '" + t2 + "'");
        Assert.assertTrue(t2.compareToIgnoreCase(t3) <= 0,
                "With asc:title sort, tile 2 '" + t2 + "' must come before tile 3 '" + t3 + "'");
    }

    /** Extract a sortable title string from a product tile element. */
    private String getTileTitle(WebElement tile) {
        try {
            List<WebElement> titleEls = tile.findElements(
                    By.cssSelector("[class*='title'], [selenium-id*='Title']"));
            if (!titleEls.isEmpty()) {
                String text = titleEls.get(0).getText().trim();
                if (!text.isEmpty())
                    return text.toLowerCase();
            }
        } catch (Exception ignored) {
        }
        // Fallback: derive from the href slug (/game/the_witcher_3 → "the witcher 3")
        String href = tile.getAttribute("href");
        if (href != null && href.contains("/game/")) {
            return href.substring(href.lastIndexOf('/') + 1).replace('_', ' ').toLowerCase();
        }
        String title = tile.getAttribute("title");
        return title != null ? title.toLowerCase() : "";
    }

    @Test(priority = 4, description = "Verify ?discounted=1 returns discounted games with visible discount badges")
    public void testDiscountedUrlParamShowsSaleGames() {
        driver.get(GAMES_URL + "?discounted=1");
        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        System.out.println("[CatalogBrowseTest] Discounted filter URL: " + url);
        List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
        System.out.println("[CatalogBrowseTest] Discounted tile count: " + tiles.size());
        Assert.assertTrue(tiles.size() >= 1,
                "?discounted=1 must return at least 1 game — GOG regularly runs sales, actual: "
                        + tiles.size());

        // At least one discount badge (e.g. "-80%") must be visible
        boolean badgeVisible = TestUtils.isElementPresent(driver, DISCOUNT_BADGE)
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(text(),'-') and contains(text(),'%')]"
                                + "[not(ancestor::footer)][not(self::script)]"));
        System.out.println("[CatalogBrowseTest] Discount badge visible: " + badgeVisible);
        Assert.assertTrue(badgeVisible,
                "At least one discount badge (e.g. '-80%') must be visible when filtering by ?discounted=1");
    }

    @Test(priority = 5, description = "Verify ?query=cyberpunk returns Cyberpunk 2077 as a result and reflects query in URL")
    public void testQueryParamReturnsRelevantGame() {
        driver.get(GAMES_URL + "?query=cyberpunk");
        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        System.out.println("[CatalogBrowseTest] Cyberpunk query URL: " + url);
        Assert.assertTrue(url.contains("cyberpunk") || url.contains("query"),
                "URL must retain the query=cyberpunk parameter, actual: " + url);

        boolean cyberpunkTile = TestUtils.isElementPresent(driver,
                By.cssSelector("a[selenium-id='productTile'][href*='cyberpunk']"));
        System.out.println("[CatalogBrowseTest] Cyberpunk 2077 tile found: " + cyberpunkTile);
        Assert.assertTrue(cyberpunkTile,
                "Cyberpunk 2077 product tile must appear in ?query=cyberpunk results "
                        + "— it is sold on GOG.com");
    }

    @Test(priority = 6, description = "Verify catalog game tiles display price information confirming active store functionality")
    public void testCatalogTilesShowPrices() {
        driver.get(GAMES_URL);
        TestUtils.pause(2000);
        boolean priceVisible = TestUtils.isElementPresent(driver, PRICE_ELEMENT)
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(text(),'$') or contains(text(),'€')"
                                + " or contains(translate(text(),'FREE','free'),'free')]"
                                + "[not(ancestor::footer)][not(self::script)]"));
        System.out.println("[CatalogBrowseTest] Price elements present on catalog tiles: " + priceVisible);
        Assert.assertTrue(priceVisible,
                "Product tiles in the GOG catalog must display price information, "
                        + "confirming active store functionality");
    }

    @Test(priority = 7, description = "Verify combined ?genres=rpg&order=asc:title params both appear in URL and return game results")
    public void testCombinedGenreAndSortParams() {
        driver.get(GAMES_URL + "?genres=rpg&order=asc:title");
        TestUtils.pause(2000);
        String url = driver.getCurrentUrl();
        System.out.println("[CatalogBrowseTest] Combined params URL: " + url);
        Assert.assertTrue(url.contains("rpg") || url.contains("genres"),
                "URL must retain genres=rpg parameter in combined filter, actual: " + url);
        Assert.assertTrue(url.contains("asc") || url.contains("order"),
                "URL must retain order=asc:title parameter in combined filter, actual: " + url);
        List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
        System.out.println("[CatalogBrowseTest] Combined params tile count: " + tiles.size());
        Assert.assertTrue(tiles.size() >= 3,
                "Combined genre+sort URL parameters must still return game results, actual: " + tiles.size());
    }
}
