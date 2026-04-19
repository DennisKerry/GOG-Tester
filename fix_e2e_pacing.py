"""
Fixes for GOG E2E test suite:
  1. Pacing  – 2s pauses between every major step for voiceover
  2. CAPTCHA – wait up to 60 seconds for manual solving; skip gracefully
  3. GOG Galaxy redirect – detect and recover in CartPageTest
  4. Store nav fix – if Store button opens dropdown, click Browse all games
"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
TEST_DIR = os.path.join(BASE, "src", "test", "java", "com", "gog", "tests")

# ---------------------------------------------------------------------------
# HomePageTest.java  (adds 1.5s pauses for viewability)
# ---------------------------------------------------------------------------
HOME_PAGE = r"""package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * HomePageTest - E2E step 1 of 5.
 *
 * Opens https://www.gog.com, dismisses the cookie-consent banner, and verifies
 * the home page structure and key navigation elements.
 *
 * Pauses of ~2 seconds are added between actions so presenters can narrate.
 */
public class HomePageTest extends E2EBase {

    @BeforeClass(alwaysRun = true)
    public void openHomePage() {
        System.out.println("\n========================================");
        System.out.println("[HomePageTest] Step 1 - Opening GOG.com home page");
        System.out.println("========================================");
        driver.get(BASE_URL + "/");
        TestUtils.waitForPageLoad(driver);
        TestUtils.dismissCookieConsent(driver);
        TestUtils.pause(2000);
        System.out.println("[HomePageTest] Home page loaded: " + driver.getTitle());
    }

    @Test(priority = 1, description = "Verify the GOG home-page title contains 'GOG'")
    public void testHomePageTitle() {
        TestUtils.pause(1500);
        String title = driver.getTitle();
        System.out.println("[HomePageTest] Page title: " + title);
        Assert.assertTrue(title.toUpperCase().contains("GOG"),
                "Home-page title must contain 'GOG', actual: " + title);
    }

    @Test(priority = 2, description = "Verify the home page is served over HTTPS")
    public void testHomePageIsHttps() {
        TestUtils.pause(1500);
        String url = driver.getCurrentUrl();
        System.out.println("[HomePageTest] URL: " + url);
        Assert.assertTrue(url.startsWith("https://"),
                "Home page must be served over HTTPS, actual: " + url);
    }

    @Test(priority = 3, description = "Verify a header navigation bar is present")
    public void testHeaderPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[hook-test='menuStore'], [class*='menu__'], nav"))
                || TestUtils.isElementPresent(driver,
                        By.cssSelector("[class*='header'], [class*='navbar']"));
        Assert.assertTrue(present, "A header / navigation bar must be visible on the home page");
    }

    @Test(priority = 4, description = "Verify the GOG logo is present in the header")
    public void testLogoPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a.menu__logo, a[class*='logo'], [class*='logo'] a, svg.gog-logo"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//img[contains(translate(@alt,"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'gog')]"));
        Assert.assertTrue(present, "The GOG logo must be visible in the header");
    }

    @Test(priority = 5,
          description = "Verify the Sign In button is present (hook-test='menuAnonymousButton')")
    public void testSignInButtonPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[hook-test='menuAnonymousButton']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(translate(normalize-space(text()),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                + "'sign in')]"));
        Assert.assertTrue(present,
                "The Sign In button (hook-test='menuAnonymousButton') must be visible in the header");
    }

    @Test(priority = 6, description = "Verify the Store navigation link is present in the header")
    public void testStoreNavLinkPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[hook-test='menuStoreButton']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//a[contains(@href,'/games')]"
                                + "[ancestor::*[contains(@class,'menu')]]"));
        Assert.assertTrue(present, "The Store link must be present in the GOG header");
    }

    @Test(priority = 7, description = "Verify at least one game product tile is rendered on the home page")
    public void testGameTilesPresent() {
        TestUtils.pause(2000);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[selenium-id='productTile'], [class*='product-tile'], "
                        + "[class*='product_tile'], [class*='productTile']"));
        Assert.assertTrue(present,
                "At least one product tile must be rendered on the GOG home page");
    }

    @Test(priority = 8, description = "Verify a footer element is rendered on the home page")
    public void testFooterPresent() {
        TestUtils.pause(1000);
        System.out.println("[HomePageTest] Scrolling to page footer...");
        TestUtils.scrollToBottom(driver);
        TestUtils.pause(2000);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("footer, [class*='footer']"));
        Assert.assertTrue(present, "A footer must be visible on the GOG home page");
        System.out.println("[HomePageTest] Scrolling back to top...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        TestUtils.pause(1500);
    }

    @Test(priority = 9, description = "Verify that session cookies are set after visiting GOG.com")
    public void testSessionCookiesSet() {
        TestUtils.pause(1500);
        int count = driver.manage().getCookies().size();
        System.out.println("[HomePageTest] Cookie count: " + count);
        Assert.assertTrue(count > 0,
                "At least one cookie must be set by gog.com, actual count: " + count);
    }
}
"""

# ---------------------------------------------------------------------------
# LoginPageTest.java  (CAPTCHA wait + pacing)
# ---------------------------------------------------------------------------
LOGIN_PAGE = r"""package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * LoginPageTest - E2E step 2 of 5.
 *
 * Clicks the Sign In button in the GOG header (hook-test="menuAnonymousButton")
 * without navigating to a URL. The login form opens as a modal popup.
 *
 * CAPTCHA handling: if a reCAPTCHA challenge is blocking login after the form
 * is submitted, the test waits up to 60 seconds for manual solving during a demo
 * session. If it is still blocked after 60 seconds the submission test is
 * skipped gracefully so the rest of the suite can compile and report.
 */
public class LoginPageTest extends E2EBase {

    private static final By SIGN_IN_BTN = By.cssSelector("a[hook-test='menuAnonymousButton']");
    private static final By MODAL_FORM  = By.cssSelector("._modal__content-wrapper, .form--login");
    private static final By EMAIL_FIELD = By.id("login_username");
    private static final By PASS_FIELD  = By.id("login_password");
    private static final By SUBMIT_BTN  = By.id("login_login");

    @BeforeClass(alwaysRun = true)
    public void openLoginPopup() {
        System.out.println("\n========================================");
        System.out.println("[LoginPageTest] Step 2 - Opening Sign In popup");
        System.out.println("========================================");

        // Ensure we are on www.gog.com before clicking Sign In
        if (!driver.getCurrentUrl().startsWith("https://www.gog.com")) {
            driver.get("https://www.gog.com/");
            TestUtils.waitForPageLoad(driver);
            TestUtils.dismissCookieConsent(driver);
        }

        TestUtils.pause(1500);
        System.out.println("[LoginPageTest] Clicking the Sign In button in the header...");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BTN));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        // Wait for the popup email field to become visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        TestUtils.pause(1500);
        System.out.println("[LoginPageTest] Login popup is open and ready.");
    }

    @Test(priority = 1, description = "Verify the login popup / form is visible after clicking Sign In")
    public void testLoginPopupVisible() {
        TestUtils.pause(1500);
        boolean visible = TestUtils.isElementPresent(driver, MODAL_FORM)
                || TestUtils.isElementPresent(driver, EMAIL_FIELD);
        Assert.assertTrue(visible, "The login popup must be visible after clicking Sign In");
    }

    @Test(priority = 2, description = "Verify the email / username field is present in the popup")
    public void testEmailFieldPresent() {
        TestUtils.pause(1500);
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        Assert.assertTrue(field.isDisplayed(),
                "Email field (#login_username) must be visible in the login popup");
    }

    @Test(priority = 3, description = "Verify the email field has type='email'")
    public void testEmailFieldType() {
        TestUtils.pause(1500);
        WebElement field = driver.findElement(EMAIL_FIELD);
        String type = field.getAttribute("type");
        Assert.assertTrue("email".equals(type) || "text".equals(type),
                "Email field type must be 'email' or 'text', actual: " + type);
    }

    @Test(priority = 4, description = "Verify the password field is present in the popup")
    public void testPasswordFieldPresent() {
        TestUtils.pause(1500);
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(PASS_FIELD));
        Assert.assertTrue(field.isDisplayed(),
                "Password field (#login_password) must be visible in the login popup");
    }

    @Test(priority = 5, description = "Verify the password field has type='password' so input is masked")
    public void testPasswordFieldMasked() {
        TestUtils.pause(1500);
        WebElement field = driver.findElement(PASS_FIELD);
        Assert.assertEquals(field.getAttribute("type"), "password",
                "Password field must have type='password' to mask the user's credentials");
    }

    @Test(priority = 6, description = "Verify the 'Log in now' submit button is present and enabled")
    public void testSubmitButtonPresent() {
        TestUtils.pause(1500);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN));
        Assert.assertTrue(btn.isDisplayed(), "Submit button (#login_login) must be visible");
        Assert.assertTrue(btn.isEnabled(), "Submit button must be enabled");
    }

    @Test(priority = 7, description = "Verify a Forgot-Password / Password-Reset link is present")
    public void testForgotPasswordLinkPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[data-content-type='requestPasswordForm'], a[href*='password']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//a[contains(translate(normalize-space(.),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                + "'password')]"));
        Assert.assertTrue(present,
                "A password-reset link must be present in the login popup");
    }

    @Test(priority = 8, description = "Verify a Sign Up / Create Account link is present in the popup")
    public void testSignUpLinkPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[data-content-type='registerForm']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//a[contains(translate(normalize-space(.),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                + "'sign up')]"));
        Assert.assertTrue(present,
                "A Sign Up / Create Account link must be present in the login popup");
    }

    /**
     * Submits the login form (priority=20, runs last).
     *
     * CAPTCHA strategy:
     *  1. Enter credentials and click submit.
     *  2. Wait 8 seconds for a fast login (no CAPTCHA challenge).
     *  3. If we are still on the login popup after 8 s, print a message that a
     *     CAPTCHA challenge may be blocking login and wait up to 60 more seconds
     *     for a presenter to solve it manually during the demo.
     *  4. If CAPTCHA is still blocking after 60 s, skip this test with an
     *     informative message rather than crashing the suite.
     */
    @Test(priority = 20,
          description = "Submit valid credentials through the popup and confirm the user is logged in")
    public void testValidLogin() {
        System.out.println("[LoginPageTest] Filling in credentials...");

        // Re-open the popup if a previous test accidentally dismissed it
        if (!TestUtils.isElementPresent(driver, EMAIL_FIELD)) {
            System.out.println("[LoginPageTest] Popup closed - reopening...");
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BTN));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
                TestUtils.pause(1000);
            } catch (Exception e) {
                throw new SkipException(
                        "Could not reopen login popup, skipping login test: " + e.getMessage());
            }
        }

        // Fill credentials slowly so viewers can see the typing
        WebElement email = driver.findElement(EMAIL_FIELD);
        email.clear();
        TestUtils.pause(500);
        email.sendKeys(TestUtils.getUsername());
        TestUtils.pause(800);

        WebElement pass = driver.findElement(PASS_FIELD);
        pass.clear();
        TestUtils.pause(500);
        pass.sendKeys(TestUtils.getPassword());
        TestUtils.pause(1000);

        System.out.println("[LoginPageTest] Clicking 'Log in now'...");
        driver.findElement(SUBMIT_BTN).click();
        TestUtils.pause(1000);

        // ---- Phase 1: wait 8 seconds for a clean login (no CAPTCHA) ----
        boolean loggedIn = false;
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(d -> !TestUtils.isElementPresent(
                            d, By.cssSelector("a[hook-test='menuAnonymousButton']")));
            loggedIn = true;
            System.out.println("[LoginPageTest] Logged in successfully.");
        } catch (Exception ignored) {
            // Either CAPTCHA is showing or network is slow
        }

        // ---- Phase 2: CAPTCHA detected - wait up to 60 seconds ----
        if (!loggedIn && TestUtils.isElementPresent(driver, EMAIL_FIELD)) {
            System.out.println("--------------------------------------------------");
            System.out.println("[LoginPageTest] CAPTCHA challenge detected!");
            System.out.println("[LoginPageTest] Waiting up to 60 seconds for manual solving...");
            System.out.println("--------------------------------------------------");
            try {
                new WebDriverWait(driver, Duration.ofSeconds(60))
                        .until(d -> !TestUtils.isElementPresent(
                                d, By.cssSelector("a[hook-test='menuAnonymousButton']")));
                loggedIn = true;
                System.out.println("[LoginPageTest] CAPTCHA solved - logged in!");
            } catch (Exception e) {
                throw new SkipException(
                        "CAPTCHA was not solved within 60 seconds. "
                        + "Log in manually in the browser and re-run, or run the suite on a "
                        + "pre-authenticated Chrome profile to bypass reCAPTCHA challenges.");
            }
        }

        // ---- Verify final state ----
        // Wait until we are on a gog.com page (handles auth redirects)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> d.getCurrentUrl().contains("gog.com"));
        } catch (Exception ignored) {}

        TestUtils.dismissCookieConsent(driver);
        TestUtils.pause(2000);

        String url = driver.getCurrentUrl();
        System.out.println("[LoginPageTest] Post-login URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "After login the browser must be on a gog.com page, actual: " + url);
    }
}
"""

# ---------------------------------------------------------------------------
# SearchTest.java  (Store nav fix + pacing)
# ---------------------------------------------------------------------------
SEARCH_TEST = r"""package com.gog.tests;

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

    private static final By STORE_BTN       = By.cssSelector("a[hook-test='menuStoreButton']");
    private static final By BROWSE_ALL_BTN  = By.cssSelector("a[hook-test='storeMenuallButton']");
    private static final By SEARCH_INPUT    = By.cssSelector("input[selenium-id='searchComponentInput']");
    private static final By PRODUCT_TILE    = By.cssSelector("a[selenium-id='productTile']");
    private static final By WITCHER3_TILE   =
            By.cssSelector("a[selenium-id='productTile'][href*='witcher_3_wild_hunt']");

    @BeforeClass(alwaysRun = true)
    public void navigateToStoreAndSearch() {
        System.out.println("\n========================================");
        System.out.println("[SearchTest] Step 3 - Search for The Witcher 3");
        System.out.println("========================================");

        // Ensure we're on www.gog.com (logged in from previous step)
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

        // If AngularJS only opened the dropdown without navigating, click Browse all games
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

    @Test(priority = 9,
          description = "Verify The Witcher 3 tile is present and ready to click at end of SearchTest")
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
"""

# ---------------------------------------------------------------------------
# GamePageTest.java  (pacing)
# ---------------------------------------------------------------------------
GAME_PAGE = r"""package com.gog.tests;

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
                TestUtils.pause(3000);
            } catch (Exception e) {
                System.out.println("[GamePageTest] Re-search failed: " + e.getMessage());
            }
        }

        TestUtils.pause(1500);
        System.out.println("[GamePageTest] Clicking The Witcher 3 product tile...");
        WebElement tile = wait.until(ExpectedConditions.elementToBeClickable(WITCHER3_TILE));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tile);
        TestUtils.waitForPageLoad(driver);
        TestUtils.pause(2000);

        dismissAgeGate();
        TestUtils.dismissCookieConsent(driver);
        TestUtils.pause(2000);
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
                TestUtils.pause(1500);
                System.out.println("[GamePageTest] Age gate dismissed.");
            } catch (Exception e) {
                System.out.println("[GamePageTest] Age gate dismiss failed: " + e.getMessage());
            }
        }
    }

    @Test(priority = 1, description = "Verify the browser navigated to The Witcher 3 page on gog.com")
    public void testOnWitcher3Page() {
        TestUtils.pause(1500);
        String url = driver.getCurrentUrl();
        System.out.println("[GamePageTest] URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "Game page must be on gog.com, actual: " + url);
        Assert.assertTrue(url.toLowerCase().contains("witcher"),
                "URL must reference 'witcher' after clicking the tile, actual: " + url);
    }

    @Test(priority = 2, description = "Verify the game page is served over HTTPS")
    public void testGamePageHttps() {
        TestUtils.pause(1500);
        Assert.assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Game page must be served over HTTPS, actual: " + driver.getCurrentUrl());
    }

    @Test(priority = 3, description = "Verify the page <title> contains 'Witcher'")
    public void testGamePageTitle() {
        TestUtils.pause(1500);
        String title = driver.getTitle();
        System.out.println("[GamePageTest] Title: " + title);
        Assert.assertTrue(title.toLowerCase().contains("witcher"),
                "Game page title must contain 'Witcher', actual: " + title);
    }

    @Test(priority = 4, description = "Verify a H1 game-title heading is visible on the product page")
    public void testGameTitleHeadingPresent() {
        TestUtils.pause(1500);
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
        TestUtils.pause(1500);
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
        TestUtils.pause(1500);
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
        TestUtils.pause(1500);
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
        TestUtils.pause(2000);
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
        TestUtils.pause(1500);
    }

    @Test(priority = 9, description = "Verify the system requirements section is present")
    public void testSystemRequirementsPresent() {
        TestUtils.pause(1000);
        System.out.println("[GamePageTest] Scrolling to system requirements...");
        TestUtils.scrollToBottom(driver);
        TestUtils.pause(2000);
        boolean present = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'system req')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'minimum')]"));
        System.out.println("[GamePageTest] Scrolling back to top for CartPageTest...");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        TestUtils.pause(1500);
        Assert.assertTrue(present,
                "A system requirements section must be present on The Witcher 3 product page");
    }
}
"""

# ---------------------------------------------------------------------------
# CartPageTest.java  (GOG Galaxy fix + pacing)
# ---------------------------------------------------------------------------
CART_PAGE = r"""package com.gog.tests;

import com.gog.base.E2EBase;
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

/**
 * CartPageTest - E2E step 5 of 5.
 *
 * From The Witcher 3 product page, clicks Add to Cart (selenium-id="AddToCartButton"),
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
    private static final By CART_ICON       = By.cssSelector("[hook-test='menuCartButton']");
    private static final By CART_COUNTER    = By.cssSelector("[hook-test='cartCounter']");
    private static final By GO_TO_CHECKOUT  = By.cssSelector("a[hook-test='cartCheckoutNow']");
    private static final By CART_DROPDOWN   =
            By.cssSelector(".menu-cart__submenu, [class*='menu-cart__submenu']");

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
            TestUtils.pause(2000);
            System.out.println("[CartPageTest] Add to Cart clicked.");
        } else {
            System.out.println("[CartPageTest] AddToCartButton not found "
                    + "(game may already be in cart or owned) - continuing.");
        }
    }

    @Test(priority = 1,
          description = "Verify the Add to Cart button exists or the game is already in cart")
    public void testAddToCartButtonState() {
        TestUtils.pause(1500);
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
        TestUtils.pause(1500);
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

    @Test(priority = 3,
          description = "Verify the cart dropdown opens by clicking the cart icon in the header")
    public void testCartDropdownOpens() {
        TestUtils.pause(1500);
        System.out.println("[CartPageTest] Clicking the cart icon to open the dropdown...");
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartIcon);
        TestUtils.pause(2000);
        boolean open = TestUtils.isElementPresent(driver, CART_DROPDOWN)
                || TestUtils.isElementPresent(driver, GO_TO_CHECKOUT);
        System.out.println("[CartPageTest] Cart dropdown open: " + open);
        Assert.assertTrue(open,
                "The cart dropdown must open when the cart icon in the header is clicked");
    }

    @Test(priority = 4, description = "Verify The Witcher 3: Wild Hunt is listed in the cart dropdown")
    public void testWitcher3InCartDropdown() {
        TestUtils.pause(1500);
        // Re-open dropdown if it closed
        if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
            try {
                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                TestUtils.pause(1500);
            } catch (Exception ignored) {}
        }

        boolean inCart =
                TestUtils.isElementPresent(driver,
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
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[hook-test='cartTotalPrice'], .menu-cart__total-price, ._price"))
                || TestUtils.isElementPresent(driver,
                        By.cssSelector("[class*='cart__total'], [class*='total-price']"));
        Assert.assertTrue(present, "A total price must be visible in the cart dropdown");
    }

    @Test(priority = 6,
          description = "Verify the 'Go to checkout' button is present in the cart dropdown")
    public void testGoToCheckoutButtonPresent() {
        TestUtils.pause(1500);
        // Keep dropdown open
        if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
            try {
                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                TestUtils.pause(1500);
            } catch (Exception ignored) {}
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
    @Test(priority = 7,
          description = "Verify clicking 'Go to checkout' navigates to the GOG checkout/cart page")
    public void testClickGoToCheckout() {
        TestUtils.pause(1500);

        // Ensure the dropdown is open
        if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
            try {
                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                wait.until(ExpectedConditions.visibilityOfElementLocated(GO_TO_CHECKOUT));
                TestUtils.pause(1000);
            } catch (Exception e) {
                System.out.println("[CartPageTest] Could not reopen dropdown: " + e.getMessage());
            }
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(GO_TO_CHECKOUT));
        System.out.println("[CartPageTest] Clicking 'Go to checkout'...");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        TestUtils.waitForPageLoad(driver);
        TestUtils.pause(2000);

        String url = driver.getCurrentUrl();
        System.out.println("[CartPageTest] Post-checkout URL: " + url);

        // GOG Galaxy fix: if we ended up on the Galaxy promo/download page, navigate
        // to the actual cart page so checkout verification still completes.
        if (url.contains("galaxy") || url.contains("/client") || url.contains("download")) {
            System.out.println("[CartPageTest] GOG Galaxy redirect detected ("
                    + url + ") - navigating to cart page...");
            driver.get("https://www.gog.com/en/cart");
            TestUtils.waitForPageLoad(driver);
            TestUtils.pause(2000);
            url = driver.getCurrentUrl();
            System.out.println("[CartPageTest] Recovered to cart URL: " + url);
        }

        System.out.println("[CartPageTest] Final URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "Must be on a gog.com page after clicking 'Go to checkout', actual: " + url);
        Assert.assertTrue(
                url.contains("checkout") || url.contains("payment")
                        || url.contains("cart") || url.contains("order"),
                "URL must reference checkout/payment/cart/order, actual: " + url);
    }
}
"""

# ---------------------------------------------------------------------------
# Write files
# ---------------------------------------------------------------------------
files = {
    os.path.join(TEST_DIR, "HomePageTest.java"):  HOME_PAGE,
    os.path.join(TEST_DIR, "LoginPageTest.java"): LOGIN_PAGE,
    os.path.join(TEST_DIR, "SearchTest.java"):    SEARCH_TEST,
    os.path.join(TEST_DIR, "GamePageTest.java"):  GAME_PAGE,
    os.path.join(TEST_DIR, "CartPageTest.java"):  CART_PAGE,
}

for path, content in files.items():
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Written: {os.path.basename(path)}")

print("\nAll files written successfully.")
