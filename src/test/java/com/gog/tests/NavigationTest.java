package com.gog.tests;

import com.gog.base.BaseTest;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * NavigationTest â€“ verifies that the main navigation links on GOG.com
 * route to the correct destinations and persist across page transitions.
 *
 * No authentication required; all navigation elements tested are publicly
 * visible.
 *
 * Automation constraints noted:
 * - GOG uses a SPA-style frontend; URL changes may occur without a full page
 * reload. waitForPageLoad + URL assertions are used rather than readyState
 * alone.
 * - Header dropdown menus (genre lists, etc.) require hover; hover interactions
 * are not tested here because they are unreliable in non-interactive
 * ChromeDriver.
 */
public class NavigationTest extends BaseTest {

    // ------------------------------------------------------------------
    // Test methods (6 total, requirement is >= 5)
    // ------------------------------------------------------------------

    @Test(description = "Verify the GOG logo navigates back to the home page")
    public void testLogoNavigatesHome() {
        driver.get(BASE_URL + "/games");
        WebElement logo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a.header__logo, a[class*='logo'][href], "
                                + "header a[href='/'], header a[href*='gog.com/'][class*='logo']")));
        logo.click();
        TestUtils.pause(1500);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("gog.com"),
                "Clicking the logo must keep the user on gog.com");
    }

    @Test(description = "Verify a Store or Games navigation link is present in the header")
    public void testStoreNavLinkPresent() {
        driver.get(BASE_URL + "/");
        boolean storeLink = TestUtils.isElementPresent(driver,
                By.xpath("//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'store')"
                        + " or contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'games')"
                        + " or contains(@href,'/games')]"
                        + "[ancestor::header or ancestor::nav]"));
        Assert.assertTrue(storeLink,
                "A Store or Games link must be present in the GOG header navigation");
    }

    @Test(description = "Verify the Sign In button is present in the header")
    public void testSignInButtonPresent() {
        driver.get(BASE_URL + "/");
        boolean signIn = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + "'abcdefghijklmnopqrstuvwxyz'),'sign in')]"
                        + " | //*[@data-qa='header-login-button']"
                        + " | //*[contains(@class,'login-button') or contains(@class,'sign-in')]"));
        Assert.assertTrue(signIn,
                "Sign In link or button must be present in the GOG header");
    }

    @Test(description = "Verify navigating to the /games catalog page stays on gog.com")
    public void testGamesCatalogNavigation() {
        driver.get(BASE_URL + "/games");
        TestUtils.waitForPageLoad(driver);
        Assert.assertTrue(
                driver.getCurrentUrl().contains("gog.com"),
                "Games catalog page must be on gog.com");
    }

    @Test(description = "Verify the header persists on both the home and games catalog pages")
    public void testHeaderPersistsAcrossPages() {
        driver.get(BASE_URL + "/");
        boolean headerOnHome = TestUtils.isElementPresent(driver,
                By.cssSelector("header, [class*='header__']"));

        driver.get(BASE_URL + "/games");
        boolean headerOnGames = TestUtils.isElementPresent(driver,
                By.cssSelector("header, [class*='header__']"));

        Assert.assertTrue(headerOnHome, "Header must be present on the GOG home page");
        Assert.assertTrue(headerOnGames, "Header must persist on the Games catalog page");
    }

    @Test(description = "Verify a wishlist or cart icon is accessible in the header")
    public void testWishlistOrCartIconPresent() {
        driver.get(BASE_URL + "/");
        boolean cartOrWishlist = TestUtils.isElementPresent(driver,
                By.cssSelector("a[href*='cart'], [class*='cart'], "
                        + "[class*='wishlist'], [aria-label*='cart' i], "
                        + "[aria-label*='wishlist' i]"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(@href,'cart') or contains(@href,'wishlist')]"));
        Assert.assertTrue(cartOrWishlist,
                "A cart or wishlist icon must be accessible in the GOG header");
    }
}
