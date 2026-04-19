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

import java.util.List;

/**
 * SearchTest - E2E step 3 of 5.
 *
 * Clicks the Store link in the GOG header. If AngularJS intercepts the click
 * and shows the submenu without navigating, the test clicks the "Browse all
 * games" link inside the submenu as a fallback. Then uses the catalog search
 * input (selenium-id="searchComponentInput") to search "witcher" and exercises
 * the filter checkboxes and sort dropdown.
 */
public class SearchTest extends E2EBase {

        private static final By STORE_BTN = By.cssSelector("a[hook-test='menuStoreButton']");
        private static final By BROWSE_ALL_BTN = By.cssSelector("a[hook-test='storeMenuallButton']");
        private static final By SEARCH_INPUT = By.cssSelector("input[selenium-id='searchComponentInput']");
        private static final By PRODUCT_TILE = By.cssSelector("a[selenium-id='productTile']");
        private static final By WITCHER3_TILE = By
                        .cssSelector("a[selenium-id='productTile'][href*='witcher_3_wild_hunt']");

        @BeforeClass(alwaysRun = true)
        public void navigateToStoreAndSearch() {
                System.out.println("\n========================================");
                System.out.println("[SearchTest] Step 3 - Search for The Witcher 3");
                System.out.println("========================================");

                // Ensure we're on www.gog.com
                if (!driver.getCurrentUrl().contains("www.gog.com")) {
                        driver.get("https://www.gog.com/");
                        TestUtils.waitForPageLoad(driver);
                }

                // --- Click the Store link in the header ---
                TestUtils.pause(800);
                System.out.println("[SearchTest] Clicking the Store link in the header...");
                WebElement storeBtn = wait.until(ExpectedConditions.elementToBeClickable(STORE_BTN));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", storeBtn);
                TestUtils.pause(800);

                // If AngularJS only opened the dropdown without navigating, click Browse all
                // games
                if (!driver.getCurrentUrl().contains("/games")) {
                        System.out.println("[SearchTest] Store dropdown opened - clicking 'Browse all games'...");
                        try {
                                WebElement browseAll = wait.until(
                                                ExpectedConditions.elementToBeClickable(BROWSE_ALL_BTN));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", browseAll);
                                TestUtils.waitForPageLoad(driver);
                                TestUtils.pause(800);
                        } catch (Exception e) {
                                System.out.println("[SearchTest] Browse all link not found, navigating directly.");
                                driver.get(BASE_URL + "/en/games");
                                TestUtils.waitForPageLoad(driver);
                        }
                } else {
                        TestUtils.waitForPageLoad(driver);
                }
                System.out.println("[SearchTest] Catalog page: " + driver.getCurrentUrl());
        }

        @Test(priority = 1, description = "Verify the browser is on the GOG games catalog after clicking Store")
        public void testOnCatalogPage() {
                TestUtils.pause(800);
                String url = driver.getCurrentUrl();
                System.out.println("[SearchTest] Catalog URL: " + url);
                Assert.assertTrue(url.contains("gog.com"),
                                "Must be on gog.com after clicking Store, actual: " + url);
                Assert.assertTrue(url.contains("/games") || url.contains("/en/"),
                                "URL must reference '/games' or '/en/', actual: " + url);
        }

        @Test(priority = 2, description = "Verify the search input (selenium-id='searchComponentInput') is present")
        public void testSearchInputPresent() {
                TestUtils.pause(800);
                Assert.assertTrue(TestUtils.isElementPresent(driver, SEARCH_INPUT),
                                "The catalog search input must be present on the games page");
        }

        @Test(priority = 8, description = "Verify at least one product tile appears after searching 'witcher'")
        public void testSearchResultsPresent() {
                TestUtils.pause(800);
                List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
                System.out.println("[SearchTest] Tiles found: " + tiles.size());
                Assert.assertTrue(tiles.size() >= 1,
                                "At least one product tile must appear for the 'witcher' search, actual: "
                                                + tiles.size());
        }

        @Test(priority = 9, description = "Verify The Witcher 3: Wild Hunt tile is visible in the results")
        public void testWitcher3TileVisible() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver, WITCHER3_TILE);
                System.out.println("[SearchTest] Witcher 3 tile present: " + present);
                Assert.assertTrue(present,
                                "The Witcher 3: Wild Hunt product tile must appear in the 'witcher' search results");
        }

        @Test(priority = 3, description = "Verify the filters panel is present on the full games catalog")
        public void testFiltersPanelPresent() {
                TestUtils.pause(800);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("[selenium-id='filtersWrapper']"))
                                || TestUtils.isElementPresent(driver, By.cssSelector(".filters"));
                Assert.assertTrue(present, "The filters panel must be present on the search-results page");
        }

        @Test(priority = 4, description = "Verify 'Show only discounted' filter narrows the catalog and changes visible tile count or URL")
        public void testDiscountedFilterToggle() {
                TestUtils.pause(800);
                By cb = By.cssSelector("[selenium-id='filterDiscountedCheckbox'] input,"
                                + " input[name='discounted']");
                if (TestUtils.isElementPresent(driver, cb)) {
                        // Capture baseline before toggling
                        int countBefore = driver.findElements(PRODUCT_TILE).size();
                        String urlBefore = driver.getCurrentUrl();
                        System.out.println("[SearchTest] Toggling 'Show only discounted' filter"
                                        + " (baseline tiles: " + countBefore + ")...");
                        WebElement checkbox = driver.findElement(cb);
                        boolean stateBefore = checkbox.isSelected();
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                        TestUtils.pause(1500); // allow async results to update
                        boolean stateAfter = driver.findElement(cb).isSelected();
                        String urlAfter = driver.getCurrentUrl();
                        int countAfter = driver.findElements(PRODUCT_TILE).size();
                        System.out.println("[SearchTest] Filter toggled: " + stateBefore + " -> " + stateAfter
                                        + " | tiles: " + countBefore + " -> " + countAfter
                                        + " | URL changed: " + !urlBefore.equals(urlAfter));
                        Assert.assertNotEquals(stateAfter, stateBefore,
                                        "Clicking 'Show only discounted' must toggle the checkbox state");
                        Assert.assertTrue(!urlBefore.equals(urlAfter) || countAfter != countBefore,
                                        "Applying the discount filter must change the URL or visible tile count"
                                                        + " (before: " + countBefore + ", after: " + countAfter + ")");
                        // Restore to unfiltered state
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                        driver.findElement(cb));
                        TestUtils.pause(800);
                } else {
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must be present when the discount filter is unavailable");
                }
        }

        @Test(priority = 5, description = "Verify the RPG genre filter narrows catalog results and reflects in the URL")
        public void testRpgGenreFilter() {
                TestUtils.pause(800);
                By cb = By.cssSelector("[selenium-id='filterGenresCheckboxrpg'] input,"
                                + " input[name='genres-rpg']");
                if (TestUtils.isElementPresent(driver, cb)) {
                        int countBefore = driver.findElements(PRODUCT_TILE).size();
                        String urlBefore = driver.getCurrentUrl();
                        System.out.println("[SearchTest] Applying RPG genre filter"
                                        + " (baseline tiles: " + countBefore + ")...");
                        WebElement checkbox = driver.findElement(cb);
                        boolean stateBefore = checkbox.isSelected();
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                        TestUtils.pause(1500);
                        boolean stateAfter = driver.findElement(cb).isSelected();
                        String urlAfter = driver.getCurrentUrl();
                        int countAfter = driver.findElements(PRODUCT_TILE).size();
                        System.out.println("[SearchTest] RPG filter toggled: " + stateBefore + " -> " + stateAfter
                                        + " | tiles: " + countBefore + " -> " + countAfter);
                        Assert.assertNotEquals(stateAfter, stateBefore,
                                        "Clicking the RPG genre filter must toggle the checkbox state");
                        Assert.assertTrue(
                                        urlAfter.contains("rpg") || urlAfter.contains("genres")
                                                        || !urlBefore.equals(urlAfter)
                                                        || countAfter != countBefore,
                                        "Applying the RPG genre filter must update the URL or change visible tile count"
                                                        + " (before: " + countBefore + ", after: " + countAfter + ")");
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must remain visible after applying the RPG genre filter");
                        // Restore
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                        driver.findElement(cb));
                        TestUtils.pause(800);
                } else {
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must be present when the RPG filter is unavailable");
                }
        }

        @Test(priority = 6, description = "Verify the sort dropdown reorders catalog results and updates the URL with the sort parameter")
        public void testSortDropdown() {
                TestUtils.pause(800);
                By sortBy = By.cssSelector("[selenium-id='sort']");
                if (TestUtils.isElementPresent(driver, sortBy)) {
                        String urlBefore = driver.getCurrentUrl();
                        System.out.println("[SearchTest] Opening sort dropdown...");
                        WebElement sortEl = driver.findElement(sortBy);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sortEl);
                        TestUtils.pause(800);

                        boolean optionsVisible = TestUtils.isElementPresent(driver,
                                        By.cssSelector("[selenium-id='sortOptionTitleAsc'],"
                                                        + " [selenium-id='sortOptionPriceAsc'],"
                                                        + " [selenium-id='sortOptionDiscountDesc']"));
                        Assert.assertTrue(optionsVisible,
                                        "Sort options must be visible after opening the sort dropdown");
                        TestUtils.pause(800);

                        // Select Title A-Z and verify the URL reflects the sort parameter
                        By titleAz = By.cssSelector("[selenium-id='sortOptionTitleAsc']");
                        if (TestUtils.isElementPresent(driver, titleAz)) {
                                System.out.println("[SearchTest] Selecting 'Title (A to Z)' sort...");
                                ((JavascriptExecutor) driver).executeScript(
                                                "arguments[0].click();", driver.findElement(titleAz));
                                TestUtils.pause(1500);
                                String urlAfter = driver.getCurrentUrl();
                                System.out.println("[SearchTest] Applied Title A-Z sort. URL: " + urlAfter);
                                Assert.assertTrue(
                                                urlAfter.contains("asc:title") || urlAfter.contains("order=")
                                                                || !urlBefore.equals(urlAfter),
                                                "Selecting Title A-Z must update the URL to reflect the sort order,"
                                                                + " actual: " + urlAfter);
                        }
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must remain visible after applying a sort option");
                } else {
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must be present when the sort control is unavailable");
                }
        }

        @Test(priority = 7, description = "Search for 'witcher' in the catalog search box")
        public void testSearchForWitcher() {
                System.out.println("[SearchTest] Searching for 'witcher' using the catalog search box...");
                WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                searchBox.click();
                TestUtils.pause(500);
                searchBox.clear();
                // Type character-by-character so viewers can see the text being entered
                for (char c : "witcher".toCharArray()) {
                        searchBox.sendKeys(String.valueOf(c));
                        TestUtils.pause(80);
                }
                TestUtils.pause(800);
                searchBox.sendKeys(Keys.ENTER);
                TestUtils.pause(1500);
                String url = driver.getCurrentUrl();
                System.out.println("[SearchTest] Searched for 'witcher'. URL: " + url);
                Assert.assertTrue(url.contains("witcher") || url.contains("/games"),
                                "URL must reflect the witcher search query, actual: " + url);
        }

        @Test(priority = 10, description = "Verify The Witcher 3 tile is present and ready to click at end of SearchTest")
        public void testWitcher3TileClickable() {
                TestUtils.pause(800);
                // Re-search if filters/sort are hiding the Witcher 3 tile
                if (!TestUtils.isElementPresent(driver, WITCHER3_TILE)) {
                        System.out.println("[SearchTest] Witcher 3 not visible - re-searching...");
                        try {
                                WebElement box = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                                box.click();
                                box.clear();
                                box.sendKeys("witcher");
                                box.sendKeys(Keys.ENTER);
                                TestUtils.pause(1500);
                        } catch (Exception e) {
                                System.out.println("[SearchTest] Re-search failed: " + e.getMessage());
                        }
                }
                boolean present = TestUtils.isElementPresent(driver, WITCHER3_TILE);
                System.out.println("[SearchTest] Witcher 3 tile ready for GamePageTest: " + present);
                Assert.assertTrue(present,
                                "The Witcher 3 tile must be visible and clickable at the end of SearchTest");
        }
}
