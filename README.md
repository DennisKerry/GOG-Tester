# GOG.com Selenium Testing Suite

## CEN 4072 – Software Testing | Spring 2026 | Group 15

**Team Members:** Robert Benstine · Dennis Kerry · Myles Vinal  
**Instructor:** Deepa Devasenapathy  
**System Under Test:** [GOG.com](https://www.gog.com) (web)

---

## Project Overview

This project applies end-to-end and system-level automated testing to GOG.com's
public-facing web application using **Selenium WebDriver**, **TestNG**, and **Maven**.

GOG (Good Old Games) is a DRM-free digital game store operated by CD PROJEKT RED.
It was chosen because it exposes a rich, publicly accessible UI — including browsing,
searching, product pages, and a shopping cart — without requiring authentication for
most features, making it well-suited for automated browser testing.

The suite contains **8 test classes** (≥ 5 test methods each) covering:

| #   | Test Class        | Needs Login | Methods |
| --- | ----------------- | ----------- | ------- |
| 1   | `HomePageTest`    | No          | 7       |
| 2   | `LoginPageTest`   | No          | 7       |
| 3   | `SignupPageTest`  | No          | 7       |
| 4   | `SearchTest`      | No          | 6       |
| 5   | `NavigationTest`  | No          | 6       |
| 6   | `GamePageTest`    | No          | 7       |
| 7   | `GenreBrowseTest` | No          | 6       |
| 8   | `CartPageTest`    | No          | 7       |

All 8 classes are wired into **`testng.xml`** (the integration runner).

---

## Project Structure

```
GOG-Tester/
├── pom.xml                              # Maven build & dependency config
├── testng.xml                           # TestNG suite (integration runner)
├── src/
│   └── test/
│       ├── java/
│       │   └── com/gog/
│       │       ├── base/
│       │       │   ├── BaseTest.java          # WebDriver setup / teardown
│       │       │   └── AuthenticatedTest.java # Login before test class
│       │       ├── utils/
│       │       │   └── TestUtils.java         # Shared helpers & login logic
│       │       └── tests/
│       │           ├── HomePageTest.java
│       │           ├── LoginPageTest.java
│       │           ├── SignupPageTest.java
│       │           ├── SearchTest.java
│       │           ├── NavigationTest.java
│       │           ├── GamePageTest.java
│       │           ├── GenreBrowseTest.java
│       │           └── CartPageTest.java
│       └── resources/
│           └── config.properties        # GOG credentials (DO NOT COMMIT)
└── doc/
    └── ...                              # Course guidelines
```

---

## Prerequisites

| Tool            | Version                              |
| --------------- | ------------------------------------ |
| Java JDK        | 17 or later                          |
| Maven           | 3.8+                                 |
| Google Chrome   | Latest stable                        |
| Internet access | Required (tests run against gog.com) |

ChromeDriver is downloaded automatically by **WebDriverManager** – no manual
driver installation needed.

---

## Setup Instructions

### 1 – Clone the repository

```bash
git clone https://github.com/DennisKerry/GOG-Tester.git
cd GOG-Tester
```

### 2 – (Optional) Add GOG credentials

Only needed if running tests that extend `AuthenticatedTest`. Currently all
8 classes test public pages and do not require login.

Open `src/test/resources/config.properties` and replace the placeholders:

```properties
gog.username=your_gog_account_email
gog.password=your_gog_account_password
```

> **Security:** `config.properties` is listed in `.gitignore`. Never commit
> real credentials to a public repository.

### 3 – Set JAVA_HOME (Windows)

Ensure Maven uses Java 17+. If you have multiple JDKs installed:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"   # adjust path as needed
```

Or set it permanently via System Properties → Environment Variables.

### 4 – Run the full suite

```bash
mvn test
```

Maven reads `testng.xml` and executes all 8 test classes sequentially.
Chrome windows will open and close automatically for each class.

### 5 – Run a single test class

```bash
mvn test -Dtest=HomePageTest
```

---

## Known Automation Constraints

GOG.com's frontend and bot-protection mechanisms can interfere with automated tests.
The following constraints were identified and addressed:

| Constraint                             | Mitigation                                                                                                                         |
| -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| **GDPR cookie-consent modal**          | `TestUtils.dismissCookieConsent()` auto-clicks the accept button before tests that navigate to a new page.                         |
| **Asynchronous game tile loading**     | `TestUtils.pause()` and `WebDriverWait` with `ExpectedConditions` handle XHR-loaded content.                                       |
| **Dynamic / minified CSS class names** | Tests use `aria-label`, `href`, `data-qa`, `type`, and `placeholder` attributes instead of fragile class selectors where possible. |
| **CAPTCHA on login**                   | Pre-authenticate the test account in a normal Chrome session on the same machine before running authenticated tests.               |
| **Age-gate overlays on game pages**    | Tests assert element presence without depending on gated content; verified against a GOG-published title (The Witcher 3).          |
| **Price changes during sales**         | Tests assert that a price element _exists_, not its specific value.                                                                |
| **Chrome automation detection**        | `ChromeOptions` flags (`--disable-blink-features=AutomationControlled`, `useAutomationExtension=false`) reduce detection signals.  |

---

## Tools Used

- **Java 17+** – primary language
- **Selenium WebDriver 4.18** – browser automation
- **TestNG 7.9** – test framework and assertion library
- **WebDriverManager 5.7** – automatic ChromeDriver management
- **Maven 3** – build and dependency management
- **Git / GitHub** – version control and submission
- **Google Chrome** (latest) – browser under test
- **Windows 11** – OS environment

---

## Running with IDE (IntelliJ IDEA / Eclipse)

1. Import as a **Maven project**.
2. Right-click `testng.xml` → **Run As → TestNG Suite** (Eclipse) or
   **Run 'testng.xml'** (IntelliJ).
3. TestNG HTML reports are generated under `target/surefire-reports/`.

---

## Report Submission Notes

- A ZIP archive of this repository (all `.java` files, `pom.xml`, `testng.xml`,
  screenshots) is attached to the course report submission.
- The GitHub public repository link is provided in Section 1.5 of the report:
  **https://github.com/DennisKerry/GOG-Tester**
