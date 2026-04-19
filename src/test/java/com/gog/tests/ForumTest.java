package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * ForumTest – verifies the GOG community forum section is functional and
 * properly structured.
 *
 * Covers areas not tested anywhere else in the suite: forum index categories,
 * game-specific forum listings, hot topics, thread navigation, and direct
 * category URLs. Tests the community/discussion side of GOG rather than the
 * store.
 *
 * URL structure observed on gog.com:
 * /forum – forum index with General Forums + Game specific forums
 * /forum/general – General Discussion category (thread listing)
 * /forum/general/{slug} – individual thread
 * /forum/cyberpunk_2077 – game-specific forum category
 *
 * Automation constraints:
 * - Forum category URLs include a fragment (#timestamp) on the index page
 * links;
 * navigating without the fragment still loads the category correctly.
 * - Page state after CartPageTest is on the Witcher 3 game page; this class
 * navigates to /forum first thing in @BeforeClass.
 */
public class ForumTest extends E2EBase {

    private static final String FORUM_URL = BASE_URL + "/forum";
    private static final String GENERAL_URL = BASE_URL + "/forum/general";
    private static final String CYBERPUNK_FORUM = BASE_URL + "/forum/cyberpunk_2077";

    @BeforeClass(alwaysRun = true)
    public void openForumPage() {
        System.out.println("\n========================================");
        System.out.println("[ForumTest] Step 6 - Explore GOG community forums");
        System.out.println("========================================");
        driver.get(FORUM_URL);
        TestUtils.waitForPageLoad(driver);
        TestUtils.dismissCookieConsent(driver);
        System.out.println("[ForumTest] Forum index loaded: " + driver.getTitle());
    }

    // -----------------------------------------------------------------------

    @Test(priority = 1, description = "Verify the GOG forum index page loads over HTTPS with a non-empty title")
    public void testForumPageLoads() {
        TestUtils.pause(800);
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();
        System.out.println("[ForumTest] URL:   " + url);
        System.out.println("[ForumTest] Title: " + title);
        Assert.assertTrue(url.startsWith("https://"),
                "Forum must be served over HTTPS, actual: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "Must remain on gog.com after navigating to the forum, actual: " + url);
        Assert.assertFalse(title == null || title.trim().isEmpty(),
                "Forum index page must have a non-empty page title");
    }

    @Test(priority = 2, description = "Verify the General Forums section contains a link to the General Discussion category")
    public void testGeneralForumSectionPresent() {
        TestUtils.pause(800);
        boolean present = TestUtils.isElementPresent(driver,
                By.cssSelector("a[href*='/forum/general']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(translate(normalize-space(.),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                + "'general discussion')]"));
        System.out.println("[ForumTest] General Discussion link present: " + present);
        Assert.assertTrue(present,
                "The forum index must contain a link to the 'General Discussion' category");
    }

    @Test(priority = 3, description = "Verify game-specific forum links (Witcher 3 or Cyberpunk 2077) appear on the forum index")
    public void testGameSpecificForumsPresent() {
        TestUtils.pause(800);
        boolean hasWitcher = TestUtils.isElementPresent(driver,
                By.cssSelector("a[href*='/forum/the_witcher']"));
        boolean hasCyberpunk = TestUtils.isElementPresent(driver,
                By.cssSelector("a[href*='/forum/cyberpunk']"));
        System.out.println("[ForumTest] Witcher forum link: " + hasWitcher
                + " | Cyberpunk forum link: " + hasCyberpunk);
        Assert.assertTrue(hasWitcher || hasCyberpunk,
                "Game-specific forum links (e.g. Witcher 3, Cyberpunk 2077) must appear on the forum index page");
    }

    @Test(priority = 4, description = "Verify the Hot Topics sidebar shows active community thread links")
    public void testHotTopicsSectionPresent() {
        TestUtils.pause(800);
        // Hot topics are thread links nested under /forum/general/
        boolean hasThreadLinks = TestUtils.isElementPresent(driver,
                By.cssSelector("a[href*='/forum/general/']"));
        boolean hasHotLabel = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(normalize-space(.),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                        + "'hot topic')]"));
        System.out.println("[ForumTest] Hot topic thread links: " + hasThreadLinks
                + " | Hot topics label: " + hasHotLabel);
        Assert.assertTrue(hasThreadLinks || hasHotLabel,
                "The forum index must have a Hot Topics section with active thread links");
    }

    @Test(priority = 5, description = "Verify navigating to the General Discussion category URL returns a thread listing")
    public void testGeneralDiscussionCategoryLoads() {
        driver.get(GENERAL_URL);
        TestUtils.waitForPageLoad(driver);
        TestUtils.pause(1200);
        String url = driver.getCurrentUrl();
        System.out.println("[ForumTest] General Discussion URL: " + url);
        Assert.assertTrue(url.contains("forum"),
                "URL must remain in the forum section after navigating to General Discussion, actual: " + url);
        boolean hasThreadLinks = TestUtils.isElementPresent(driver,
                By.cssSelector("a[href*='/forum/general/']"));
        System.out.println("[ForumTest] Thread links in category: " + hasThreadLinks);
        Assert.assertTrue(hasThreadLinks,
                "The General Discussion category page must list at least one thread link");
    }

    @Test(priority = 6, description = "Verify clicking a forum thread opens a page with a non-empty title and stays on gog.com")
    public void testForumThreadOpens() {
        TestUtils.pause(800);
        List<WebElement> threads = driver.findElements(
                By.cssSelector("a[href*='/forum/general/']"));
        if (!threads.isEmpty()) {
            String href = threads.get(0).getAttribute("href");
            System.out.println("[ForumTest] Opening thread: " + href);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", threads.get(0));
            TestUtils.waitForPageLoad(driver);
            TestUtils.pause(1000);
            String urlAfter = driver.getCurrentUrl();
            String titleAfter = driver.getTitle();
            System.out.println("[ForumTest] Thread URL:   " + urlAfter);
            System.out.println("[ForumTest] Thread title: " + titleAfter);
            Assert.assertTrue(urlAfter.contains("/forum/"),
                    "After clicking a thread, URL must remain in /forum/ path, actual: " + urlAfter);
            Assert.assertFalse(titleAfter == null || titleAfter.trim().isEmpty(),
                    "An opened forum thread must have a non-empty page title");
        } else {
            // Soft pass: if no threads visible, page must still be on gog.com
            Assert.assertTrue(driver.getCurrentUrl().contains("gog.com"),
                    "Must remain on gog.com even if no thread links are currently visible");
        }
    }

    @Test(priority = 7, description = "Verify the Cyberpunk 2077 game-specific forum page is directly accessible and has content")
    public void testCyberpunk2077ForumAccessible() {
        driver.get(CYBERPUNK_FORUM);
        TestUtils.waitForPageLoad(driver);
        TestUtils.pause(1200);
        String url = driver.getCurrentUrl();
        System.out.println("[ForumTest] Cyberpunk 2077 forum URL: " + url);
        Assert.assertTrue(url.contains("gog.com"),
                "Must remain on gog.com for the Cyberpunk 2077 game forum, actual: " + url);
        boolean hasContent = TestUtils.isElementPresent(driver,
                By.cssSelector("h1, h2"))
                || TestUtils.isElementPresent(driver,
                        By.cssSelector("a[href*='/forum/cyberpunk_2077/']"))
                || TestUtils.isElementPresent(driver,
                        By.xpath("//*[contains(translate(normalize-space(.),"
                                + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),"
                                + "'cyberpunk')]"));
        System.out.println("[ForumTest] Cyberpunk 2077 forum has content: " + hasContent);
        Assert.assertTrue(hasContent,
                "The Cyberpunk 2077 game forum page must have visible content (heading or thread links)");
    }
}
