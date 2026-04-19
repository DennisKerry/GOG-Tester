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

                // --- Login guard: do not proceed if LoginPageTest did not confirm login ---
                if (!LoginPageTest.loginConfirmed) {
                        throw new RuntimeException(
                                        "[SearchTest] BLOCKED: LoginPageTest.loginConfirmed is false. "
                                                        + "Login must complete before the search tests can run.");
                }

                // Ensure we're on www.gog.com
                if (!driver.getCurrentUrl().contains("www.gog.com")) {
                        driver.get("https://www.gog.com/");
                        TestUtils.waitForPageLoad(driver);
                }

                // --- Click the Store link in the header ---
                TestUtils.pause(1500);
                System.out.println("[SearchTest] Clicking the Store link in the header...");
                WebElement storeBtn = wait.until(ExpectedConditions.elementToBeClickable(STORE_BTN));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", storeBtn);
                TestUtils.pause(1500);

                // If AngularJS only opened the dropdown without navigating, click Browse all
                // games
                if (!driver.getCurrentUrl().contains("/games")) {
                        System.out.println("[SearchTest] Store dropdown opened - clicking 'Browse all games'...");
                        try {
                                WebElement browseAll = wait.until(
                                                ExpectedConditions.elementToBeClickable(BROWSE_ALL_BTN));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", browseAll);
                                TestUtils.waitForPageLoad(driver);
                                TestUtils.pause(1500);
                        } catch (Exception e) {
                                System.out.println("[SearchTest] Browse all link not found, navigating directly.");
                                driver.get(BASE_URL + "/en/games");
                                TestUtils.waitForPageLoad(driver);
                        }
                } else {
                        TestUtils.waitForPageLoad(driver);
                }
                System.out.println("[SearchTest] Catalog page: " + driver.getCurrentUrl());
                TestUtils.pause(2000);

                // --- Type "witcher" in the catalog search box ---
                System.out.println("[SearchTest] Searching for 'witcher' using the catalog search box...");
                WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                searchBox.click();
                TestUtils.pause(500);
                searchBox.clear();
                // Type character by character so viewers can see the text being entered
                for (char c : "witcher".toCharArray()) {
                        searchBox.sendKeys(String.valueOf(c));
                        TestUtils.pause(80);
                }
                TestUtils.pause(800);
                searchBox.sendKeys(Keys.ENTER);
                TestUtils.pause(3000);
                System.out.println("[SearchTest] Searched for 'witcher'. URL: " + driver.getCurrentUrl());
        }

        @Test(priority = 1, description = "Verify the browser is on the GOG games catalog after clicking Store")
        public void testOnCatalogPage() {
                TestUtils.pause(1500);
                String url = driver.getCurrentUrl();
                System.out.println("[SearchTest] Catalog URL: " + url);
                Assert.assertTrue(url.contains("gog.com"),
                                "Must be on gog.com after clicking Store, actual: " + url);
                Assert.assertTrue(url.contains("/games") || url.contains("/en/"),
                                "URL must reference '/games' or '/en/', actual: " + url);
        }

        @Test(priority = 2, description = "Verify the search input (selenium-id='searchComponentInput') is present")
        public void testSearchInputPresent() {
                TestUtils.pause(1500);
                Assert.assertTrue(TestUtils.isElementPresent(driver, SEARCH_INPUT),
                                "The catalog search input must be present on the games page");
        }

        @Test(priority = 3, description = "Verify at least one product tile appears after searching 'witcher'")
        public void testSearchResultsPresent() {
                TestUtils.pause(1500);
                List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
                System.out.println("[SearchTest] Tiles found: " + tiles.size());
                Assert.assertTrue(tiles.size() >= 1,
                                "At least one product tile must appear for the 'witcher' search, actual: "
                                                + tiles.size());
        }

        @Test(priority = 4, description = "Verify The Witcher 3: Wild Hunt tile is visible in the results")
        public void testWitcher3TileVisible() {
                TestUtils.pause(1500);
                boolean present = TestUtils.isElementPresent(driver, WITCHER3_TILE);
                System.out.println("[SearchTest] Witcher 3 tile present: " + present);
                Assert.assertTrue(present,
                                "The Witcher 3: Wild Hunt product tile must appear in the 'witcher' search results");
        }

        @Test(priority = 5, description = "Verify the filters panel is present on the search-results page")
        public void testFiltersPanelPresent() {
                TestUtils.pause(1500);
                boolean present = TestUtils.isElementPresent(driver,
                                By.cssSelector("[selenium-id='filtersWrapper']"))
                                || TestUtils.isElementPresent(driver, By.cssSelector(".filters"));
                Assert.assertTrue(present, "The filters panel must be present on the search-results page");
        }

        @Test(priority = 6, description = "Verify the 'Show only discounted' filter checkbox can be toggled")
        public void testDiscountedFilterToggle() {
                TestUtils.pause(1500);
                By cb = By.cssSelector("[selenium-id='filterDiscountedCheckbox'] input,"
                                + " input[name='discounted']");
                if (TestUtils.isElementPresent(driver, cb)) {
                        System.out.println("[SearchTest] Toggling 'Show only discounted' filter...");
                        WebElement checkbox = driver.findElement(cb);
                        boolean before = checkbox.isSelected();
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                        TestUtils.pause(2000);
                        boolean after = driver.findElement(cb).isSelected();
                        System.out.println("[SearchTest] Filter toggled: " + before + " -> " + after);
                        Assert.assertNotEquals(after, before,
                                        "Clicking 'Show only discounted' must toggle the checkbox state");
                        // Restore to unfiltered state
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                        driver.findElement(cb));
                        TestUtils.pause(1500);
                } else {
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must be present when the discount filter is unavailable");
                }
        }

        @Test(priority = 7, description = "Verify the Role-playing genre filter checkbox can be toggled")
        public void testRpgGenreFilter() {
                TestUtils.pause(1500);
                By cb = By.cssSelector("[selenium-id='filterGenresCheckboxrpg'] input,"
                                + " input[name='genres-rpg']");
                if (TestUtils.isElementPresent(driver, cb)) {
                        System.out.println("[SearchTest] Applying Role-playing genre filter...");
                        WebElement checkbox = driver.findElement(cb);
                        boolean before = checkbox.isSelected();
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                        TestUtils.pause(2000);
                        boolean after = driver.findElement(cb).isSelected();
                        System.out.println("[SearchTest] RPG filter toggled: " + before + " -> " + after);
                        Assert.assertNotEquals(after, before,
                                        "Clicking the Role-playing genre filter must toggle the checkbox state");
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must remain after applying the RPG genre filter");
                        // Restore
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                        driver.findElement(cb));
                        TestUtils.pause(1500);
                } else {
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must be present when the RPG filter is unavailable");
                }
        }

        @Test(priority = 8, description = "Verify the sort dropdown can be opened and a sort option selected")
        public void testSortDropdown() {
                TestUtils.pause(1500);
                By sortBy = By.cssSelector("[selenium-id='sort']");
                if (TestUtils.isElementPresent(driver, sortBy)) {
                        System.out.println("[SearchTest] Opening sort dropdown...");
                        WebElement sortEl = driver.findElement(sortBy);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sortEl);
                        TestUtils.pause(1500);

                        boolean optionsVisible = TestUtils.isElementPresent(driver,
                                        By.cssSelector("[selenium-id='sortOptionTitleAsc'],"
                                                        + " [selenium-id='sortOptionPriceAsc'],"
                                                        + " [selenium-id='sortOptionDiscountDesc']"));
                        Assert.assertTrue(optionsVisible,
                                        "Sort options must be visible after opening the sort dropdown");
                        TestUtils.pause(1000);

                        // Select Title A-Z
                        By titleAz = By.cssSelector("[selenium-id='sortOptionTitleAsc']");
                        if (TestUtils.isElementPresent(driver, titleAz)) {
                                System.out.println("[SearchTest] Selecting 'Title (A to Z)' sort...");
                                ((JavascriptExecutor) driver).executeScript(
                                                "arguments[0].click();", driver.findElement(titleAz));
                                TestUtils.pause(2000);
                                System.out.println("[SearchTest] Applied Title A-Z sort.");
                        }
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must remain visible after applying a sort option");
                } else {
                        Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                                        "Product tiles must be present when the sort control is unavailable");
                }
        }

        @Test(priority = 9, description = "Verify The Witcher 3 tile is present and ready to click at end of SearchTest")
        public void testWitcher3TileClickable() {
                TestUtils.pause(1500);
                // Re-search if filters/sort are hiding the Witcher 3 tile
                if (!TestUtils.isElementPresent(driver, WITCHER3_TILE)) {
                        System.out.println("[SearchTest] Witcher 3 not visible - re-searching...");
                        try {
                                WebElement box = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                                box.click();
                                box.clear();
                                box.sendKeys("witcher");
                                box.sendKeys(Keys.ENTER);
                                TestUtils.pause(3000);
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
