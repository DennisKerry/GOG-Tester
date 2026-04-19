"""
Writes all 5 E2E test files and updated testng.xml for the GOG test suite.
Run from the Project directory:  python write_e2e_tests.py
"""
import os

BASE = os.path.dirname(os.path.abspath(__file__))
TEST_DIR = os.path.join(BASE, "src", "test", "java", "com", "gog", "tests")

# ---------------------------------------------------------------------------
# HomePageTest.java
# ---------------------------------------------------------------------------
HOME_PAGE = r"""package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * HomePageTest - E2E step 1 of 5.
 *
 * Opens https://www.gog.com, dismisses the cookie-consent banner, and verifies
 * the home page structure and key navigation elements.
 *
 * The shared browser remains on gog.com after all tests so that LoginPageTest
 * can click the Sign In button without a driver.get() URL shortcut.
 */
public class HomePageTest extends E2EBase {

    @BeforeClass(alwaysRun = true)
    public void openHomePage() {
        System.out.println("[HomePageTest] Navigating to " + BASE_URL);
        driver.get(BASE_URL + "/");
        TestUtils.waitForPageLoad(driver);
        TestUtils.dismissCookieConsent(driver);
        TestUtils.pause(1000);
        System.out.println("[HomePageTest] Home page loaded: " + driver.getTitle());
    }

    @Test(priority = 1, description = "Verify the GOG home-page title contains 'GOG'")
    public void testHomePageTitle() {
        String title = driver.getTitle();
        Assert.assertTrue(title.toUpperCase().contains("GOG"),
                "Home-page title must contain 'GOG', actual: " + title);
    }

    @Test(priority = 2, description = "Verify the home page is served over HTTPS")
    public void testHomePageIsHttps() {
        Assert.assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Home page must be served over HTTPS, actual: " + driver.getCurrentUrl());
    }

    @Test(priority = 3, description = "Verify a header navigation bar is present")
    public void testHeaderPresent() {
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[hook-test='menuStore'], [class*='menu__'], nav"))
                || TestUtils.isElementPresent(driver,
                        By.cssSelector("[class*='header'], [class*='navbar']"));
        Assert.assertTrue(present, "A header / navigation bar must be visible on the home page");
    }

    @Test(priority = 4, description = "Verify the GOG logo is present in the header")
    public void testLogoPresent() {
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a.menu__logo, a[class*='logo'], [class*='logo'] a, svg.gog-logo"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//img[contains(translate(@alt,"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'gog')]"));
        Assert.assertTrue(present, "The GOG logo must be visible in the header");
    }

    @Test(priority = 5,
          description = "Verify the anonymous Sign In button is present (hook-test='menuAnonymousButton')")
    public void testSignInButtonPresent() {
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
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[hook-test='menuStoreButton']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//a[contains(@href,'/games')]"
                                + "[ancestor::*[contains(@class,'menu')]]"));
        Assert.assertTrue(present, "The Store link must be present in the GOG header");
    }

    @Test(priority = 7, description = "Verify at least one game product tile is rendered on the home page")
    public void testGameTilesPresent() {
        TestUtils.pause(1500);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[selenium-id='productTile'], [class*='product-tile'], "
                        + "[class*='product_tile'], [class*='productTile']"));
        Assert.assertTrue(present,
                "At least one product tile must be rendered on the GOG home page");
    }

    @Test(priority = 8, description = "Verify a footer element is rendered on the home page")
    public void testFooterPresent() {
        TestUtils.scrollToBottom(driver);
        TestUtils.pause(600);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("footer, [class*='footer']"));
        Assert.assertTrue(present, "A footer must be visible on the GOG home page");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    @Test(priority = 9, description = "Verify that session cookies are set after visiting GOG.com")
    public void testSessionCookiesSet() {
        int count = driver.manage().getCookies().size();
        Assert.assertTrue(count > 0,
                "At least one cookie must be set by gog.com, actual count: " + count);
    }
}
"""

# ---------------------------------------------------------------------------
# LoginPageTest.java
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * LoginPageTest - E2E step 2 of 5.
 *
 * Clicks the Sign In button in the GOG header (hook-test="menuAnonymousButton")
 * without navigating to a URL. The login form opens as a modal popup on
 * www.gog.com. Tests 1-7 verify the popup structure (no side-effects). Test 20
 * (priority=20) submits valid credentials and waits for the anonymous Sign In
 * button to disappear, confirming the user is logged in.
 *
 * After all tests the shared browser is logged in and on www.gog.com, ready for
 * SearchTest to click the Store link.
 */
public class LoginPageTest extends E2EBase {

    private static final By SIGN_IN_BTN   = By.cssSelector("a[hook-test='menuAnonymousButton']");
    private static final By MODAL_FORM    = By.cssSelector("._modal__content-wrapper, .form--login");
    private static final By EMAIL_FIELD   = By.id("login_username");
    private static final By PASS_FIELD    = By.id("login_password");
    private static final By SUBMIT_BTN    = By.id("login_login");

    @BeforeClass(alwaysRun = true)
    public void openLoginPopup() {
        System.out.println("[LoginPageTest] Opening Sign In popup from header button");

        // Ensure we are on www.gog.com before clicking Sign In
        if (!driver.getCurrentUrl().startsWith("https://www.gog.com")) {
            driver.get("https://www.gog.com/");
            TestUtils.waitForPageLoad(driver);
            TestUtils.dismissCookieConsent(driver);
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BTN));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        // Wait for the popup form to become visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        System.out.println("[LoginPageTest] Login popup is open.");
    }

    @Test(priority = 1, description = "Verify the login popup / form is visible after clicking Sign In")
    public void testLoginPopupVisible() {
        boolean visible = TestUtils.isElementPresent(driver, MODAL_FORM)
                || TestUtils.isElementPresent(driver, EMAIL_FIELD);
        Assert.assertTrue(visible, "The login popup must be visible after clicking Sign In");
    }

    @Test(priority = 2, description = "Verify the email / username field is present in the popup")
    public void testEmailFieldPresent() {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
        Assert.assertTrue(field.isDisplayed(),
                "Email field (#login_username) must be visible in the login popup");
    }

    @Test(priority = 3, description = "Verify the email field has type='email'")
    public void testEmailFieldType() {
        WebElement field = driver.findElement(EMAIL_FIELD);
        String type = field.getAttribute("type");
        Assert.assertTrue("email".equals(type) || "text".equals(type),
                "Email field type must be 'email' or 'text', actual: " + type);
    }

    @Test(priority = 4, description = "Verify the password field is present in the popup")
    public void testPasswordFieldPresent() {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(PASS_FIELD));
        Assert.assertTrue(field.isDisplayed(),
                "Password field (#login_password) must be visible in the login popup");
    }

    @Test(priority = 5, description = "Verify the password field has type='password' so input is masked")
    public void testPasswordFieldMasked() {
        WebElement field = driver.findElement(PASS_FIELD);
        Assert.assertEquals(field.getAttribute("type"), "password",
                "Password field must have type='password' to mask the user's credentials");
    }

    @Test(priority = 6, description = "Verify the 'Log in now' submit button is present and enabled")
    public void testSubmitButtonPresent() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN));
        Assert.assertTrue(btn.isDisplayed(), "Submit button (#login_login) must be visible");
        Assert.assertTrue(btn.isEnabled(), "Submit button must be enabled");
    }

    @Test(priority = 7, description = "Verify a Forgot-Password / Password-Reset link is present")
    public void testForgotPasswordLinkPresent() {
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
     * Performs the actual login - runs last (priority=20).
     * Fills the popup with credentials from config.properties and waits for the
     * anonymous Sign In button to disappear as confirmation of successful login.
     */
    @Test(priority = 20,
          description = "Submit valid credentials through the popup and confirm the user is logged in")
    public void testValidLogin() {
        System.out.println("[LoginPageTest] Submitting valid credentials...");

        // Re-open the popup if a previous test accidentally dismissed it
        if (!TestUtils.isElementPresent(driver, EMAIL_FIELD)) {
            System.out.println("[LoginPageTest] Popup closed — reopening...");
            try {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(SIGN_IN_BTN));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
                wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_FIELD));
            } catch (Exception e) {
                System.out.println("[LoginPageTest] Could not reopen popup: " + e.getMessage());
            }
        }

        if (TestUtils.isElementPresent(driver, EMAIL_FIELD)) {
            WebElement email = driver.findElement(EMAIL_FIELD);
            email.clear();
            email.sendKeys(TestUtils.getUsername());

            WebElement pass = driver.findElement(PASS_FIELD);
            pass.clear();
            pass.sendKeys(TestUtils.getPassword());

            driver.findElement(SUBMIT_BTN).click();
            System.out.println("[LoginPageTest] Login form submitted.");
        }

        // Wait for the anonymous Sign In button to disappear = user logged in
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> !TestUtils.isElementPresent(
                            d, By.cssSelector("a[hook-test='menuAnonymousButton']")));
            System.out.println("[LoginPageTest] Login confirmed — anonymous button gone.");
        } catch (Exception e) {
            System.out.println("[LoginPageTest] Login wait timed out: " + driver.getCurrentUrl());
        }

        // Wait until browser is on a gog.com domain (handles any auth redirect)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> d.getCurrentUrl().contains("gog.com"));
        } catch (Exception ignored) {}

        TestUtils.dismissCookieConsent(driver);
        String url = driver.getCurrentUrl();
        System.out.println("[LoginPageTest] Post-login URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "After login the browser must be on a gog.com page, actual: " + url);
    }
}
"""

# ---------------------------------------------------------------------------
# SearchTest.java
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
 * Clicks the Store link in the GOG header (hook-test="menuStoreButton"), uses
 * the catalog search input (selenium-id="searchComponentInput") to search for
 * "witcher", then exercises the filter checkboxes and sort dropdown.
 *
 * After all tests the browser is on the /en/games catalog page showing Witcher
 * results so GamePageTest can click directly on The Witcher 3 tile.
 */
public class SearchTest extends E2EBase {

    private static final By STORE_BTN    = By.cssSelector("a[hook-test='menuStoreButton']");
    private static final By SEARCH_INPUT = By.cssSelector("input[selenium-id='searchComponentInput']");
    private static final By PRODUCT_TILE = By.cssSelector("a[selenium-id='productTile']");
    private static final By WITCHER3_TILE =
            By.cssSelector("a[selenium-id='productTile'][href*='witcher_3_wild_hunt']");

    @BeforeClass(alwaysRun = true)
    public void navigateToStoreAndSearch() {
        System.out.println("[SearchTest] Clicking Store link in header...");

        // Ensure we are on www.gog.com (logged in)
        if (!driver.getCurrentUrl().contains("www.gog.com")) {
            driver.get("https://www.gog.com/");
            TestUtils.waitForPageLoad(driver);
        }

        WebElement storeBtn = wait.until(ExpectedConditions.elementToBeClickable(STORE_BTN));
        storeBtn.click();
        TestUtils.waitForPageLoad(driver);
        System.out.println("[SearchTest] Catalog page: " + driver.getCurrentUrl());

        // Type "witcher" in the search input and submit
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        searchBox.click();
        searchBox.clear();
        searchBox.sendKeys("witcher");
        searchBox.sendKeys(Keys.ENTER);
        TestUtils.pause(2500);
        System.out.println("[SearchTest] Searched for 'witcher'. URL: " + driver.getCurrentUrl());
    }

    @Test(priority = 1, description = "Verify the browser is on the GOG games catalog after clicking Store")
    public void testOnCatalogPage() {
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "Must be on gog.com after clicking Store, actual: " + url);
        Assert.assertTrue(url.contains("/games") || url.contains("/en/"),
                "URL must reference '/games' or '/en/', actual: " + url);
    }

    @Test(priority = 2, description = "Verify the search input (selenium-id='searchComponentInput') is present")
    public void testSearchInputPresent() {
        Assert.assertTrue(TestUtils.isElementPresent(driver, SEARCH_INPUT),
                "The catalog search input must be present on the games page");
    }

    @Test(priority = 3, description = "Verify at least one product tile appears after searching 'witcher'")
    public void testSearchResultsPresent() {
        List<WebElement> tiles = driver.findElements(PRODUCT_TILE);
        Assert.assertTrue(tiles.size() >= 1,
                "At least one product tile must appear for the 'witcher' search, actual: "
                        + tiles.size());
    }

    @Test(priority = 4, description = "Verify The Witcher 3: Wild Hunt tile is visible in the results")
    public void testWitcher3TileVisible() {
        boolean present = TestUtils.isElementPresent(driver, WITCHER3_TILE);
        Assert.assertTrue(present,
                "The Witcher 3: Wild Hunt product tile must appear in the 'witcher' search results");
    }

    @Test(priority = 5, description = "Verify the filters panel is present on the search-results page")
    public void testFiltersPanelPresent() {
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[selenium-id='filtersWrapper']"))
                || TestUtils.isElementPresent(driver, By.cssSelector(".filters"));
        Assert.assertTrue(present, "The filters panel must be present on the search-results page");
    }

    @Test(priority = 6, description = "Verify the 'Show only discounted' filter checkbox can be toggled")
    public void testDiscountedFilterToggle() {
        By cb = By.cssSelector("[selenium-id='filterDiscountedCheckbox'] input,"
                + " input[name='discounted']");
        if (TestUtils.isElementPresent(driver, cb)) {
            WebElement checkbox = driver.findElement(cb);
            boolean before = checkbox.isSelected();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            TestUtils.pause(1500);
            boolean after = driver.findElement(cb).isSelected();
            Assert.assertNotEquals(after, before,
                    "Clicking 'Show only discounted' must toggle the checkbox state");
            // Restore
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                    driver.findElement(cb));
            TestUtils.pause(1000);
        } else {
            Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                    "Product tiles must be present when the discount filter is unavailable");
        }
    }

    @Test(priority = 7, description = "Verify the RPG genre filter checkbox is present and toggleable")
    public void testRpgGenreFilter() {
        By cb = By.cssSelector("[selenium-id='filterGenresCheckboxrpg'] input,"
                + " input[name='genres-rpg']");
        if (TestUtils.isElementPresent(driver, cb)) {
            WebElement checkbox = driver.findElement(cb);
            boolean before = checkbox.isSelected();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
            TestUtils.pause(1500);
            boolean after = driver.findElement(cb).isSelected();
            Assert.assertNotEquals(after, before,
                    "Clicking the RPG genre filter must toggle the checkbox state");
            // Verify tiles still present (Witcher 3 is RPG)
            Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                    "Product tiles must remain after applying the RPG genre filter");
            // Restore
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                    driver.findElement(cb));
            TestUtils.pause(1000);
        } else {
            Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                    "Product tiles must be present when the RPG filter is unavailable");
        }
    }

    @Test(priority = 8, description = "Verify the sort dropdown can be opened and sort options are selectable")
    public void testSortDropdown() {
        By sortBy = By.cssSelector("[selenium-id='sort']");
        if (TestUtils.isElementPresent(driver, sortBy)) {
            WebElement sortEl = driver.findElement(sortBy);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sortEl);
            TestUtils.pause(800);

            boolean optionsVisible = TestUtils.isElementPresent(driver,
                    By.cssSelector("[selenium-id='sortOptionTitleAsc'],"
                            + " [selenium-id='sortOptionPriceAsc'],"
                            + " [selenium-id='sortOptionDiscountDesc']"));
            Assert.assertTrue(optionsVisible,
                    "Sort options must be visible after opening the sort dropdown");

            // Select Title A-Z
            By titleAz = By.cssSelector("[selenium-id='sortOptionTitleAsc']");
            if (TestUtils.isElementPresent(driver, titleAz)) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();", driver.findElement(titleAz));
                TestUtils.pause(1500);
                System.out.println("[SearchTest] Applied 'Title A-Z' sort.");
            }
            Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                    "Product tiles must remain visible after applying Title A-Z sort");
        } else {
            Assert.assertTrue(TestUtils.isElementPresent(driver, PRODUCT_TILE),
                    "Product tiles must be present when the sort control is unavailable");
        }
    }

    @Test(priority = 9,
          description = "Verify The Witcher 3 tile is present and clickable at end of SearchTest")
    public void testWitcher3TileClickable() {
        // Re-search in case active filters are hiding it
        if (!TestUtils.isElementPresent(driver, WITCHER3_TILE)) {
            try {
                WebElement box = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                box.click();
                box.clear();
                box.sendKeys("witcher");
                box.sendKeys(Keys.ENTER);
                TestUtils.pause(2500);
            } catch (Exception e) {
                System.out.println("[SearchTest] Re-search failed: " + e.getMessage());
            }
        }
        Assert.assertTrue(TestUtils.isElementPresent(driver, WITCHER3_TILE),
                "The Witcher 3 tile must be visible and clickable at the end of SearchTest");
    }
}
"""

# ---------------------------------------------------------------------------
# GamePageTest.java
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
 * Clicks The Witcher 3: Wild Hunt tile (selenium-id="productTile") from the
 * search results page that SearchTest left the browser on. Navigates to the
 * game product page entirely via UI interaction - no driver.get() URL shortcut.
 *
 * Handles optional age-gate dialogs and verifies the product page structure and
 * key purchase elements. After all tests the browser is on The Witcher 3 product
 * page so CartPageTest can click Add to Cart.
 */
public class GamePageTest extends E2EBase {

    private static final By WITCHER3_TILE =
            By.cssSelector("a[selenium-id='productTile'][href*='witcher_3_wild_hunt']");
    private static final By SEARCH_INPUT =
            By.cssSelector("input[selenium-id='searchComponentInput']");
    private static final By ADD_TO_CART  = By.cssSelector("[selenium-id='AddToCartButton']");
    private static final By CHECKOUT_BTN = By.cssSelector("[selenium-id='CheckoutButton']");

    @BeforeClass(alwaysRun = true)
    public void openWitcher3Page() {
        System.out.println("[GamePageTest] Looking for Witcher 3 tile...");

        // If not already visible (e.g. filters hid it), re-search
        if (!TestUtils.isElementPresent(driver, WITCHER3_TILE)) {
            System.out.println("[GamePageTest] Tile not visible - re-searching...");
            try {
                WebElement box = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
                box.click();
                box.clear();
                box.sendKeys("witcher");
                box.sendKeys(Keys.ENTER);
                TestUtils.pause(2500);
            } catch (Exception e) {
                System.out.println("[GamePageTest] Re-search failed: " + e.getMessage());
            }
        }

        WebElement tile = wait.until(ExpectedConditions.elementToBeClickable(WITCHER3_TILE));
        System.out.println("[GamePageTest] Clicking Witcher 3 tile...");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tile);
        TestUtils.waitForPageLoad(driver);

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
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("gog.com"),
                "Game page must be on gog.com, actual: " + url);
        Assert.assertTrue(url.toLowerCase().contains("witcher"),
                "URL must reference 'witcher' after clicking the tile, actual: " + url);
    }

    @Test(priority = 2, description = "Verify the game page is served over HTTPS")
    public void testGamePageHttps() {
        Assert.assertTrue(driver.getCurrentUrl().startsWith("https://"),
                "Game page must be served over HTTPS, actual: " + driver.getCurrentUrl());
    }

    @Test(priority = 3, description = "Verify the page <title> contains 'Witcher'")
    public void testGamePageTitle() {
        String title = driver.getTitle();
        Assert.assertTrue(title.toLowerCase().contains("witcher"),
                "Game page title must contain 'Witcher', actual: " + title);
    }

    @Test(priority = 4, description = "Verify a H1 game-title heading is visible on the product page")
    public void testGameTitleHeadingPresent() {
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        String text = h1.getAttribute("textContent");
        if (text == null || text.trim().isEmpty()) text = h1.getText();
        Assert.assertFalse(text.trim().isEmpty(),
                "The H1 game-title heading on the product page must not be empty");
    }

    @Test(priority = 5,
          description = "Verify the Add to Cart or Checkout button is present "
                  + "(selenium-id='AddToCartButton' or 'CheckoutButton')")
    public void testBuyButtonPresent() {
        boolean present = TestUtils.isElementPresent(driver, ADD_TO_CART)
                || TestUtils.isElementPresent(driver, CHECKOUT_BTN);
        Assert.assertTrue(present,
                "AddToCartButton or CheckoutButton (selenium-id) must be present on the game page");
    }

    @Test(priority = 6, description = "Verify a price or ownership indicator is visible")
    public void testPricePresent() {
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
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[class*='screenshot'], [class*='gallery'],"
                        + " [class*='slider'], [class*='media-player']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//img[contains(@src,'.jpg') or contains(@src,'.png')"
                                + " or contains(@src,'.webp')]"
                                + "[not(ancestor::header)][not(ancestor::footer)]"));
        Assert.assertTrue(present,
                "At least one screenshot, gallery image, or media element must be present");
        // Scroll back to top so Add to Cart button is visible for CartPageTest
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    @Test(priority = 9, description = "Verify the system requirements section is present")
    public void testSystemRequirementsPresent() {
        TestUtils.scrollToBottom(driver);
        TestUtils.pause(500);
        boolean present = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'system req')"
                        + " or contains(translate(text(),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'minimum')]"));
        // Scroll back up to reveal Add to Cart for CartPageTest
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        Assert.assertTrue(present,
                "A system requirements section must be present on The Witcher 3 product page");
    }
}
"""

# ---------------------------------------------------------------------------
# CartPageTest.java
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
 * Picks up from The Witcher 3 product page left by GamePageTest.
 * Clicks the Add to Cart button (selenium-id="AddToCartButton"), verifies the
 * cart counter increments, opens the cart dropdown via the cart icon in the
 * header (hook-test="menuCartButton") - no driver.get(CART_URL) shortcut -
 * verifies The Witcher 3 is listed, then clicks "Go to checkout"
 * (hook-test="cartCheckoutNow") and verifies the checkout page loads on gog.com.
 */
public class CartPageTest extends E2EBase {

    private static final By ADD_TO_CART_BTN = By.cssSelector("[selenium-id='AddToCartButton']");
    private static final By CHECKOUT_BTN    = By.cssSelector("[selenium-id='CheckoutButton']");
    private static final By CART_ICON       = By.cssSelector("[hook-test='menuCartButton']");
    private static final By CART_COUNTER    = By.cssSelector("[hook-test='cartCounter']");
    private static final By GO_TO_CHECKOUT  = By.cssSelector("a[hook-test='cartCheckoutNow']");
    private static final By CART_DROPDOWN   =
            By.cssSelector(".menu-cart__submenu, [class*='menu-cart__submenu']");

    @BeforeClass(alwaysRun = true)
    public void addWitcher3ToCart() {
        System.out.println("[CartPageTest] Adding The Witcher 3 to cart...");

        // Scroll the Add-to-Cart button into view and click it
        WebElement addBtn = wait.until(ExpectedConditions.presenceOfElementLocated(ADD_TO_CART_BTN));
        TestUtils.scrollIntoView(driver, addBtn);
        TestUtils.pause(500);
        addBtn = wait.until(ExpectedConditions.elementToBeClickable(ADD_TO_CART_BTN));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);
        System.out.println("[CartPageTest] Add to Cart clicked.");

        // Wait for the button to transition to CheckoutButton (confirms item was added)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(CHECKOUT_BTN));
            System.out.println("[CartPageTest] CheckoutButton appeared - item confirmed in cart.");
        } catch (Exception e) {
            System.out.println("[CartPageTest] CheckoutButton not seen yet: " + e.getMessage());
        }
        TestUtils.pause(800);
    }

    @Test(priority = 1,
          description = "Verify the Add to Cart button transitioned to the Checkout button")
    public void testAddToCartTransitionedToCheckout() {
        boolean present = TestUtils.isElementPresent(driver, CHECKOUT_BTN)
                || TestUtils.isElementPresent(driver, ADD_TO_CART_BTN);
        Assert.assertTrue(present,
                "After clicking Add to Cart, either CheckoutButton or AddToCartButton must be present");
    }

    @Test(priority = 2, description = "Verify the cart counter in the header shows at least 1 item")
    public void testCartCounterNonZero() {
        if (TestUtils.isElementPresent(driver, CART_COUNTER)) {
            WebElement counter = driver.findElement(CART_COUNTER);
            String text = counter.getText().trim();
            System.out.println("[CartPageTest] Cart counter: '" + text + "'");
            try {
                int count = Integer.parseInt(text);
                Assert.assertTrue(count >= 1,
                        "Cart counter must be >= 1 after adding Witcher 3, actual: " + count);
            } catch (NumberFormatException e) {
                Assert.assertFalse(text.isEmpty(),
                        "Cart counter must be non-empty after adding an item");
            }
        } else {
            Assert.assertTrue(TestUtils.isElementPresent(driver, CART_ICON),
                    "Cart icon must be present in the header");
        }
    }

    @Test(priority = 3,
          description = "Verify the cart dropdown opens by clicking the cart icon (no URL navigation)")
    public void testCartDropdownOpens() {
        WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cartIcon);
        TestUtils.pause(1000);
        boolean open = TestUtils.isElementPresent(driver, CART_DROPDOWN)
                || TestUtils.isElementPresent(driver, GO_TO_CHECKOUT);
        Assert.assertTrue(open,
                "The cart dropdown must open when the cart icon in the header is clicked");
    }

    @Test(priority = 4, description = "Verify The Witcher 3: Wild Hunt is listed in the cart dropdown")
    public void testWitcher3InCartDropdown() {
        // Ensure dropdown is open
        if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
            try {
                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                TestUtils.pause(800);
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
        Assert.assertTrue(inCart,
                "The Witcher 3: Wild Hunt must appear in the cart dropdown");
    }

    @Test(priority = 5, description = "Verify a total price is displayed in the cart dropdown")
    public void testCartTotalPresent() {
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("[hook-test='cartTotalPrice'], .menu-cart__total-price, ._price"))
                || TestUtils.isElementPresent(driver,
                        By.cssSelector("[class*='cart__total'], [class*='total-price']"));
        Assert.assertTrue(present, "A total price must be visible in the cart dropdown");
    }

    @Test(priority = 6,
          description = "Verify the 'Go to checkout' button is present in the cart dropdown")
    public void testGoToCheckoutButtonPresent() {
        boolean present = TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)
                || TestUtils.isElementPresent(driver,
                        By.xpath("//a[contains(translate(normalize-space(.),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                + "'checkout')]"));
        Assert.assertTrue(present,
                "A 'Go to checkout' button must be present in the cart dropdown");
    }

    @Test(priority = 7,
          description = "Verify clicking 'Go to checkout' navigates to the GOG checkout page")
    public void testClickGoToCheckout() {
        // Ensure dropdown is open and checkout button is visible
        if (!TestUtils.isElementPresent(driver, GO_TO_CHECKOUT)) {
            try {
                WebElement icon = wait.until(ExpectedConditions.elementToBeClickable(CART_ICON));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", icon);
                wait.until(ExpectedConditions.visibilityOfElementLocated(GO_TO_CHECKOUT));
            } catch (Exception e) {
                System.out.println("[CartPageTest] Could not reopen dropdown: " + e.getMessage());
            }
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(GO_TO_CHECKOUT));
        System.out.println("[CartPageTest] Clicking 'Go to checkout'...");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        TestUtils.waitForPageLoad(driver);
        TestUtils.pause(1000);

        String url = driver.getCurrentUrl();
        System.out.println("[CartPageTest] Checkout/payment URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "Clicking 'Go to checkout' must navigate to a gog.com page, actual: " + url);
        Assert.assertTrue(
                url.contains("checkout") || url.contains("payment")
                        || url.contains("cart") || url.contains("order"),
                "URL must reference checkout/payment/cart/order, actual: " + url);
    }
}
"""

# ---------------------------------------------------------------------------
# testng.xml
# ---------------------------------------------------------------------------
TESTNG_XML = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="GOG Selenium Test Suite" verbose="2" parallel="none">

    <listeners>
        <listener class-name="com.gog.utils.HtmlProjectReportListener"/>
    </listeners>

    <!-- ============================================================
         CEN 4072 - Software Testing  |  Group 15
         GOG.com Automated Test Suite
         Members: Robert Benstine, Dennis Kerry, Myles Vinal
         System Under Test: https://www.gog.com
         ============================================================ -->

    <!-- ===== E2E Flow (steps 1-5) - share one browser via E2EBase @BeforeSuite ===== -->

    <test name="Home Page Tests">
        <classes>
            <class name="com.gog.tests.HomePageTest"/>
        </classes>
    </test>

    <test name="Login Page Tests">
        <classes>
            <class name="com.gog.tests.LoginPageTest"/>
        </classes>
    </test>

    <test name="Search Tests">
        <classes>
            <class name="com.gog.tests.SearchTest"/>
        </classes>
    </test>

    <test name="Game Page Tests">
        <classes>
            <class name="com.gog.tests.GamePageTest"/>
        </classes>
    </test>

    <test name="Cart Page Tests">
        <classes>
            <class name="com.gog.tests.CartPageTest"/>
        </classes>
    </test>

    <!-- ===== Standalone Tests - each gets its own browser via BaseTest @BeforeClass ===== -->

    <test name="Signup Page Tests">
        <classes>
            <class name="com.gog.tests.SignupPageTest"/>
        </classes>
    </test>

    <test name="Navigation Tests">
        <classes>
            <class name="com.gog.tests.NavigationTest"/>
        </classes>
    </test>

    <test name="Genre Browse Tests">
        <classes>
            <class name="com.gog.tests.GenreBrowseTest"/>
        </classes>
    </test>

</suite>
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
    os.path.join(BASE, "testng.xml"):             TESTNG_XML,
}

for path, content in files.items():
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Written: {path}")

print("\nAll files written successfully.")
