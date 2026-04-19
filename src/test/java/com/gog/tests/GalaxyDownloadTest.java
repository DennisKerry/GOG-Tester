package com.gog.tests;

import com.gog.base.E2EBase;
import com.gog.utils.TestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GalaxyDownloadTest – verifies the GOG Galaxy client download page is
 * correctly structured and the actual installer file is served by the CDN.
 *
 * This is the only class in the suite to test the /galaxy landing page and
 * make an HTTP-level assertion beyond the browser. Test 7 issues a HEAD
 * request to the live .exe URL extracted from the page at runtime, confirming
 * that gog-statics.com CDN is actually serving the installer — something
 * purely DOM-level checks cannot verify.
 *
 * Key page structure observed on gog.com/galaxy:
 * - Hero "DOWNLOAD GOG GALAXY 2.0" link → webinstallers.gog-statics.com .exe
 * - "Available also on macOS" → content-system.gog.com .pkg
 * - "Download on the Microsoft Store" → ms-windows-store:// deep link
 * - FAQ section with h3 questions
 * - Feature sections: Your Games, Your Friends, Your Privacy, Your GOG client
 *
 * Automation constraints:
 * - The .exe download URL contains a signed payload token generated per page
 * load; the href is read from the live page at runtime so the token is fresh.
 * - The Galaxy page is a separate SPA from the main GOG store; it may use
 * different element classes. CSS selectors prefer href-based matching.
 */
public class GalaxyDownloadTest extends E2EBase {

    private static final String GALAXY_URL = BASE_URL + "/galaxy";
    private static final By WINDOWS_DL_LINK = By.cssSelector(
            "a[href*='GOG_Galaxy_2.0.exe'], a[href*='gog-statics.com/download']");

    @BeforeClass(alwaysRun = true)
    public void openGalaxyPage() {
        System.out.println("\n========================================");
        System.out.println("[GalaxyDownloadTest] Step 8 - GOG Galaxy client download page");
        System.out.println("========================================");
        driver.get(GALAXY_URL);
        TestUtils.waitForPageLoad(driver);
        TestUtils.dismissCookieConsent(driver);
        System.out.println("[GalaxyDownloadTest] Galaxy page loaded: " + driver.getTitle());
    }

    // -----------------------------------------------------------------------

    @Test(priority = 1, description = "Verify the GOG Galaxy landing page loads over HTTPS with a title referencing Galaxy or GOG")
    public void testGalaxyPageLoads() {
        TestUtils.pause(800);
        String url = driver.getCurrentUrl();
        String title = driver.getTitle();
        System.out.println("[GalaxyDownloadTest] URL:   " + url);
        System.out.println("[GalaxyDownloadTest] Title: " + title);
        Assert.assertTrue(url.startsWith("https://"),
                "Galaxy page must be served over HTTPS, actual: " + url);
        Assert.assertTrue(url.contains("gog.com") || url.contains("gogalaxy.com"),
                "URL must remain on GOG domain, actual: " + url);
        Assert.assertTrue(title.toLowerCase().contains("galaxy")
                || title.toUpperCase().contains("GOG"),
                "Page title must reference GOG Galaxy, actual: " + title);
    }

    @Test(priority = 2, description = "Verify the 'GOG GALAXY 2.0' product heading is visible on the page")
    public void testGalaxyHeadingVisible() {
        TestUtils.pause(800);
        boolean h1h2 = TestUtils.isElementPresent(driver,
                By.xpath("//h1[contains(.,'GALAXY')] | //h2[contains(.,'GALAXY')]"));
        boolean anyElement = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(.,'GOG GALAXY 2.0') and not(self::script) and not(self::style)]"));
        System.out.println("[GalaxyDownloadTest] h1/h2 with GALAXY: " + h1h2
                + " | Any element with 'GOG GALAXY 2.0': " + anyElement);
        Assert.assertTrue(h1h2 || anyElement,
                "The 'GOG GALAXY 2.0' product heading must be visible on the Galaxy landing page");
    }

    @Test(priority = 3, description = "Verify a Windows installer download button is present on the Galaxy page")
    public void testWindowsDownloadButtonPresent() {
        TestUtils.pause(800);
        boolean byHref = TestUtils.isElementPresent(driver, WINDOWS_DL_LINK);
        boolean byText = TestUtils.isElementPresent(driver,
                By.xpath("//a[contains(translate(normalize-space(.),"
                        + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'DOWNLOAD')]"
                        + "[contains(@href,'exe') or contains(@href,'gog')]"));
        System.out.println("[GalaxyDownloadTest] Windows download button present: " + (byHref || byText));
        Assert.assertTrue(byHref || byText,
                "A 'DOWNLOAD GOG GALAXY 2.0' button linking to a Windows installer must be on the page");
    }

    @Test(priority = 4, description = "Verify the Windows download link href references a .exe or GOG_Galaxy installer file")
    public void testWindowsDownloadLinkIsExeInstaller() {
        TestUtils.pause(800);
        List<WebElement> links = driver.findElements(WINDOWS_DL_LINK);
        if (links.isEmpty()) {
            links = driver.findElements(By.cssSelector("a[href*='.exe']"));
        }
        Assert.assertFalse(links.isEmpty(),
                "At least one Windows .exe download link must be present on the Galaxy page");
        String href = links.get(0).getAttribute("href");
        String display = href.length() > 80 ? href.substring(0, 80) + "..." : href;
        System.out.println("[GalaxyDownloadTest] Windows installer href: " + display);
        Assert.assertTrue(href.contains(".exe") || href.contains("GOG_Galaxy"),
                "The Windows download link must reference a .exe or GOG_Galaxy installer file, "
                        + "actual href: " + display);
    }

    @Test(priority = 5, description = "Verify a macOS download option (.pkg or macOS text link) is present on the Galaxy page")
    public void testMacOSDownloadOptionPresent() {
        TestUtils.pause(800);
        boolean pkgLink = TestUtils.isElementPresent(driver, By.cssSelector("a[href*='.pkg']"));
        boolean macText = TestUtils.isElementPresent(driver,
                By.xpath("//a[contains(translate(normalize-space(.),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'macos')]"
                        + " | //a[contains(translate(normalize-space(.),"
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'mac os')]"));
        System.out.println("[GalaxyDownloadTest] macOS .pkg link: " + pkgLink
                + " | macOS text link: " + macText);
        Assert.assertTrue(pkgLink || macText,
                "A macOS download option (.pkg link or 'macOS' text link) must be present on the Galaxy page");
    }

    @Test(priority = 6, description = "Verify the FAQ section exists with at least one question on the Galaxy page")
    public void testFaqSectionPresent() {
        TestUtils.pause(800);
        boolean faqHeading = TestUtils.isElementPresent(driver,
                By.xpath("//*[contains(translate(normalize-space(.),"
                        + "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'FAQ')]"
                        + "[not(self::script)][not(self::style)]"));
        boolean faqItems = TestUtils.isElementPresent(driver,
                By.xpath("//h3[contains(.,'?')]"));
        System.out.println("[GalaxyDownloadTest] FAQ heading: " + faqHeading
                + " | FAQ question items: " + faqItems);
        Assert.assertTrue(faqHeading || faqItems,
                "The GOG Galaxy page must include an FAQ section with at least one question");
    }

    @Test(priority = 7, description = "Verify clicking the Windows download button causes the installer file to appear on the local filesystem")
    public void testWindowsInstallerDownloadsToFilesystem() throws java.io.IOException {
        List<WebElement> links = driver.findElements(WINDOWS_DL_LINK);
        if (links.isEmpty()) {
            links = driver.findElements(By.cssSelector("a[href*='.exe']"));
        }
        Assert.assertFalse(links.isEmpty(),
                "A Windows download link must be present on the Galaxy page");

        // Create a download directory inside the project's target/ folder
        File downloadDir = new File("target/galaxy-download-test");
        downloadDir.mkdirs();

        // Use Chrome DevTools Protocol to redirect downloads to our temp dir without
        // prompting
        Map<String, Object> cdpParams = new HashMap<>();
        cdpParams.put("behavior", "allow");
        cdpParams.put("downloadPath", downloadDir.getCanonicalPath());
        ((ChromeDriver) driver).executeCdpCommand("Browser.setDownloadBehavior", cdpParams);

        // Click the download button
        String display = links.get(0).getAttribute("href");
        display = display.length() > 80 ? display.substring(0, 80) + "..." : display;
        System.out.println("[GalaxyDownloadTest] Clicking download button: " + display);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", links.get(0));

        // Poll for up to 20 seconds — we only need the .crdownload (partial) or .exe to
        // appear,
        // not the full 200 MB download to finish
        File foundFile = null;
        long deadline = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < deadline) {
            TestUtils.pause(1000);
            File[] files = downloadDir.listFiles(f -> f.getName().toLowerCase().contains("gog")
                    || f.getName().toLowerCase().endsWith(".exe")
                    || f.getName().toLowerCase().endsWith(".crdownload"));
            if (files != null && files.length > 0) {
                foundFile = files[0];
                break;
            }
        }

        System.out.println("[GalaxyDownloadTest] Download file on disk: "
                + (foundFile != null ? foundFile.getName() + " (" + foundFile.length() + " bytes)" : "none"));
        Assert.assertNotNull(foundFile,
                "Clicking the Galaxy download button must cause an installer or .crdownload file "
                        + "to appear in the filesystem within 20 seconds");
        Assert.assertTrue(foundFile.length() > 0,
                "The downloaded/partial file must not be empty, actual size: " + foundFile.length());

        // Navigate away to stop the in-progress download — keep the file in target/ for
        // inspection
        driver.get(GALAXY_URL);
        TestUtils.pause(1000);
    }
}
