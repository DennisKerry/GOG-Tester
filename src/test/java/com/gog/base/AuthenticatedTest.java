package com.gog.base;

import com.gog.utils.TestUtils;
import org.testng.annotations.BeforeClass;

/**
 * AuthenticatedTest â€“ extends BaseTest and performs a GOG login before any
 * test method runs in the subclass.
 *
 * Credentials are loaded from config.properties (gog.username / gog.password).
 * TestNG guarantees that a parent @BeforeClass runs before a
 * child @BeforeClass,
 * so BaseTest.setUp() always creates the driver before this login() executes.
 *
 * Automation constraint: GOG may display a CAPTCHA or 2FA prompt on first login
 * from a new machine. Pre-authenticate the test account once in a real browser
 * session on the same machine to establish a trusted session history.
 */
public abstract class AuthenticatedTest extends BaseTest {

    @BeforeClass
    public void login() {
        TestUtils.performLogin(driver, wait);
    }
}
