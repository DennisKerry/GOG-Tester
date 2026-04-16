package com.gog.utils;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates doc/project-report.html after every TestNG suite run.
 */
public class HtmlProjectReportListener implements IReporter {

    private static final Path REPORT_PATH = Path.of("doc", "project-report.html");

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        SuiteStats suiteStats = collectSuiteStats(suites);
        List<ClassStats> classStats = collectClassStats(suites);
        String html = buildHtml(suiteStats, classStats);

        try {
            Files.createDirectories(REPORT_PATH.getParent());
            Files.writeString(REPORT_PATH, html, StandardCharsets.UTF_8);
            System.out.println("[HtmlProjectReportListener] Report generated: " + REPORT_PATH.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[HtmlProjectReportListener] Failed to write report: " + e.getMessage());
        }
    }

    private SuiteStats collectSuiteStats(List<ISuite> suites) {
        int passed = 0;
        int failed = 0;
        int skipped = 0;
        long durationMs = 0;
        String suiteName = suites.isEmpty() ? "TestNG Suite" : suites.get(0).getName();
        long firstStart = Long.MAX_VALUE;
        long lastEnd = 0;

        for (ISuite suite : suites) {
            Map<String, ISuiteResult> results = suite.getResults();
            for (ISuiteResult suiteResult : results.values()) {
                ITestContext ctx = suiteResult.getTestContext();
                passed += ctx.getPassedTests().size();
                failed += ctx.getFailedTests().size();
                skipped += ctx.getSkippedTests().size();

                long start = ctx.getStartDate().getTime();
                long end = ctx.getEndDate().getTime();
                firstStart = Math.min(firstStart, start);
                lastEnd = Math.max(lastEnd, end);
                durationMs += Math.max(0, end - start);
            }
        }

        if (firstStart == Long.MAX_VALUE) {
            firstStart = System.currentTimeMillis();
            lastEnd = firstStart;
        }

        return new SuiteStats(suiteName, passed + failed + skipped, passed, failed, skipped, durationMs, firstStart, lastEnd);
    }

    private List<ClassStats> collectClassStats(List<ISuite> suites) {
        Map<String, ClassStats> byClass = new LinkedHashMap<>();

        for (ISuite suite : suites) {
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                ITestContext ctx = suiteResult.getTestContext();
                collectFromResultMap(byClass, ctx.getPassedTests().getAllResults(), Status.PASS);
                collectFromResultMap(byClass, ctx.getFailedTests().getAllResults(), Status.FAIL);
                collectFromResultMap(byClass, ctx.getSkippedTests().getAllResults(), Status.SKIP);
            }
        }

        List<ClassStats> stats = new ArrayList<>(byClass.values());
        stats.sort(Comparator.comparing(ClassStats::className));
        return stats;
    }

    private void collectFromResultMap(Map<String, ClassStats> byClass, Collection<ITestResult> results, Status status) {
        for (ITestResult result : results) {
            String className = result.getTestClass().getName();
            ClassStats current = byClass.getOrDefault(className, new ClassStats(className));

            current.tests += 1;
            current.durationMs += Math.max(0, result.getEndMillis() - result.getStartMillis());

            if (status == Status.PASS) {
                current.passed += 1;
            } else if (status == Status.FAIL) {
                current.failures += 1;
            } else {
                current.skipped += 1;
            }

            byClass.put(className, current);
        }
    }

    private String buildHtml(SuiteStats suite, List<ClassStats> classes) {
        int totalClasses = classes.size();
        String generatedDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String startAt = formatEpoch(suite.startedAtMs);
        String finishedAt = formatEpoch(suite.finishedAtMs);

        StringBuilder rows = new StringBuilder();
        for (ClassStats c : classes) {
            String badge = c.failures > 0
                    ? "<span class=\"badge bad\">FAIL</span>"
                    : c.skipped > 0 ? "<span class=\"badge warn\">PARTIAL</span>" : "<span class=\"badge ok\">PASS</span>";

            rows.append("          <tr>\n")
                    .append("            <td>").append(escapeHtml(c.className)).append("</td>\n")
                    .append("            <td>").append(c.tests).append("</td>\n")
                    .append("            <td>").append(c.failures).append("</td>\n")
                    .append("            <td>0</td>\n")
                    .append("            <td>").append(c.skipped).append("</td>\n")
                    .append("            <td>").append(formatSeconds(c.durationMs)).append("</td>\n")
                    .append("            <td>").append(badge).append("</td>\n")
                    .append("          </tr>\n");
        }

        String suiteDurationSeconds = formatSeconds(suite.durationMs);

        return "<!doctype html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\">\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <title>GOG Tester - Project Report</title>\n"
                + "  <style>\n"
                + "    :root {\n"
                + "      --bg: #f4f6f8;\n"
                + "      --card: #ffffff;\n"
                + "      --ink: #0f172a;\n"
                + "      --muted: #475569;\n"
                + "      --line: #d9e2ec;\n"
                + "      --accent: #0b7285;\n"
                + "      --accent-soft: #e6f4f7;\n"
                + "      --ok: #2f9e44;\n"
                + "      --warn: #e67700;\n"
                + "      --bad: #d9480f;\n"
                + "      --shadow: 0 10px 24px rgba(15, 23, 42, 0.08);\n"
                + "    }\n"
                + "\n"
                + "    * { box-sizing: border-box; }\n"
                + "\n"
                + "    body {\n"
                + "      margin: 0;\n"
                + "      font-family: \"Segoe UI\", \"Trebuchet MS\", sans-serif;\n"
                + "      color: var(--ink);\n"
                + "      background: radial-gradient(circle at 10% -10%, #d0ebff 0%, transparent 35%),\n"
                + "                  radial-gradient(circle at 90% 0%, #d3f9d8 0%, transparent 30%),\n"
                + "                  var(--bg);\n"
                + "      line-height: 1.5;\n"
                + "    }\n"
                + "\n"
                + "    .wrap {\n"
                + "      max-width: 1100px;\n"
                + "      margin: 0 auto;\n"
                + "      padding: 28px 18px 48px;\n"
                + "    }\n"
                + "\n"
                + "    .hero {\n"
                + "      background: linear-gradient(135deg, #0b7285, #1864ab);\n"
                + "      color: #fff;\n"
                + "      border-radius: 16px;\n"
                + "      padding: 28px;\n"
                + "      box-shadow: var(--shadow);\n"
                + "      margin-bottom: 18px;\n"
                + "    }\n"
                + "\n"
                + "    .hero h1 {\n"
                + "      margin: 0 0 8px;\n"
                + "      font-size: 1.9rem;\n"
                + "      letter-spacing: 0.3px;\n"
                + "    }\n"
                + "\n"
                + "    .hero p {\n"
                + "      margin: 4px 0;\n"
                + "      color: rgba(255, 255, 255, 0.9);\n"
                + "    }\n"
                + "\n"
                + "    .grid {\n"
                + "      display: grid;\n"
                + "      grid-template-columns: repeat(4, minmax(0, 1fr));\n"
                + "      gap: 12px;\n"
                + "      margin: 14px 0 20px;\n"
                + "    }\n"
                + "\n"
                + "    .stat {\n"
                + "      background: var(--card);\n"
                + "      border: 1px solid var(--line);\n"
                + "      border-left: 5px solid var(--accent);\n"
                + "      border-radius: 12px;\n"
                + "      padding: 12px;\n"
                + "      box-shadow: var(--shadow);\n"
                + "    }\n"
                + "\n"
                + "    .stat .label {\n"
                + "      font-size: 0.85rem;\n"
                + "      color: var(--muted);\n"
                + "      margin-bottom: 4px;\n"
                + "    }\n"
                + "\n"
                + "    .stat .value {\n"
                + "      font-size: 1.5rem;\n"
                + "      font-weight: 700;\n"
                + "    }\n"
                + "\n"
                + "    .card {\n"
                + "      background: var(--card);\n"
                + "      border: 1px solid var(--line);\n"
                + "      border-radius: 14px;\n"
                + "      box-shadow: var(--shadow);\n"
                + "      padding: 18px;\n"
                + "      margin-bottom: 14px;\n"
                + "    }\n"
                + "\n"
                + "    .card h2 {\n"
                + "      margin-top: 0;\n"
                + "      font-size: 1.15rem;\n"
                + "      border-bottom: 2px solid var(--accent-soft);\n"
                + "      padding-bottom: 8px;\n"
                + "    }\n"
                + "\n"
                + "    table {\n"
                + "      width: 100%;\n"
                + "      border-collapse: collapse;\n"
                + "      font-size: 0.95rem;\n"
                + "    }\n"
                + "\n"
                + "    th, td {\n"
                + "      text-align: left;\n"
                + "      border-bottom: 1px solid var(--line);\n"
                + "      padding: 9px 8px;\n"
                + "      vertical-align: top;\n"
                + "    }\n"
                + "\n"
                + "    th {\n"
                + "      background: #f8fafc;\n"
                + "      font-weight: 600;\n"
                + "    }\n"
                + "\n"
                + "    .badge {\n"
                + "      display: inline-block;\n"
                + "      padding: 2px 8px;\n"
                + "      border-radius: 999px;\n"
                + "      font-size: 0.8rem;\n"
                + "      font-weight: 600;\n"
                + "      border: 1px solid transparent;\n"
                + "    }\n"
                + "\n"
                + "    .ok {\n"
                + "      background: #ebfbee;\n"
                + "      color: var(--ok);\n"
                + "      border-color: #c3e6cb;\n"
                + "    }\n"
                + "\n"
                + "    .warn {\n"
                + "      background: #fff4e6;\n"
                + "      color: var(--warn);\n"
                + "      border-color: #ffd8a8;\n"
                + "    }\n"
                + "\n"
                + "    .bad {\n"
                + "      background: #fff5f5;\n"
                + "      color: var(--bad);\n"
                + "      border-color: #ffc9c9;\n"
                + "    }\n"
                + "\n"
                + "    ul {\n"
                + "      margin: 8px 0;\n"
                + "      padding-left: 20px;\n"
                + "    }\n"
                + "\n"
                + "    code {\n"
                + "      background: #f1f3f5;\n"
                + "      border: 1px solid #dee2e6;\n"
                + "      border-radius: 6px;\n"
                + "      padding: 1px 6px;\n"
                + "      font-size: 0.92em;\n"
                + "    }\n"
                + "\n"
                + "    .footer {\n"
                + "      margin-top: 12px;\n"
                + "      font-size: 0.86rem;\n"
                + "      color: #5c677d;\n"
                + "      text-align: center;\n"
                + "    }\n"
                + "\n"
                + "    @media (max-width: 900px) {\n"
                + "      .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }\n"
                + "    }\n"
                + "\n"
                + "    @media (max-width: 560px) {\n"
                + "      .wrap { padding: 16px 10px 28px; }\n"
                + "      .hero { padding: 18px; }\n"
                + "      .grid { grid-template-columns: 1fr; }\n"
                + "      table { font-size: 0.9rem; }\n"
                + "    }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <main class=\"wrap\">\n"
                + "    <section class=\"hero\">\n"
                + "      <h1>GOG Tester - HTML Project Report</h1>\n"
                + "      <p><strong>Project:</strong> Automated end-to-end testing suite for GOG.com</p>\n"
                + "      <p><strong>Course:</strong> CEN 4072 Software Testing (Spring 2026), Group 15</p>\n"
                + "      <p><strong>Generated:</strong> " + generatedDate + "</p>\n"
                + "    </section>\n"
                + "\n"
                + "    <section class=\"grid\" aria-label=\"Summary statistics\">\n"
                + "      <article class=\"stat\">\n"
                + "        <div class=\"label\">Total Test Classes</div>\n"
                + "        <div class=\"value\">" + totalClasses + "</div>\n"
                + "      </article>\n"
                + "      <article class=\"stat\">\n"
                + "        <div class=\"label\">Total Test Methods</div>\n"
                + "        <div class=\"value\">" + suite.total + "</div>\n"
                + "      </article>\n"
                + "      <article class=\"stat\">\n"
                + "        <div class=\"label\">Passed / Failed</div>\n"
                + "        <div class=\"value\">" + suite.passed + " / " + suite.failed + "</div>\n"
                + "      </article>\n"
                + "      <article class=\"stat\">\n"
                + "        <div class=\"label\">Suite Duration</div>\n"
                + "        <div class=\"value\">" + suiteDurationSeconds + "s</div>\n"
                + "      </article>\n"
                + "    </section>\n"
                + "\n"
                + "    <section class=\"card\">\n"
                + "      <h2>1. Project Scope</h2>\n"
                + "      <p>This automation suite validates major user flows on <strong>GOG.com</strong> using Selenium WebDriver with TestNG and Maven.</p>\n"
                + "      <ul>\n"
                + "        <li>Target system: public GOG storefront and product pages.</li>\n"
                + "        <li>Execution model: UI-level browser automation with reusable base and utility layers.</li>\n"
                + "        <li>Run trigger: TestNG suite execution via Maven Surefire.</li>\n"
                + "      </ul>\n"
                + "    </section>\n"
                + "\n"
                + "    <section class=\"card\">\n"
                + "      <h2>2. Suite Execution Metadata</h2>\n"
                + "      <p><strong>Suite name:</strong> " + escapeHtml(suite.suiteName) + "</p>\n"
                + "      <p><strong>Started:</strong> " + startAt + "</p>\n"
                + "      <p><strong>Finished:</strong> " + finishedAt + "</p>\n"
                + "      <p><strong>Skipped tests:</strong> " + suite.skipped + "</p>\n"
                + "    </section>\n"
                + "\n"
                + "    <section class=\"card\">\n"
                + "      <h2>3. Class-Level Results</h2>\n"
                + "      <table>\n"
                + "        <thead>\n"
                + "          <tr>\n"
                + "            <th>Test Class</th>\n"
                + "            <th>Tests</th>\n"
                + "            <th>Failures</th>\n"
                + "            <th>Errors</th>\n"
                + "            <th>Skipped</th>\n"
                + "            <th>Time (s)</th>\n"
                + "            <th>Status</th>\n"
                + "          </tr>\n"
                + "        </thead>\n"
                + "        <tbody>\n"
                + rows
                + "        </tbody>\n"
                + "      </table>\n"
                + "    </section>\n"
                + "\n"
                + "    <section class=\"card\">\n"
                + "      <h2>4. Regeneration Behavior</h2>\n"
                + "      <ul>\n"
                + "        <li>This file is regenerated automatically at the end of each TestNG suite run.</li>\n"
                + "        <li>No manual editing is required to refresh result metrics.</li>\n"
                + "      </ul>\n"
                + "    </section>\n"
                + "\n"
                + "    <p class=\"footer\">End of report - generated by HtmlProjectReportListener.</p>\n"
                + "  </main>\n"
                + "</body>\n"
                + "</html>\n";
    }

    private String formatSeconds(long ms) {
        return String.format(Locale.US, "%.3f", ms / 1000.0);
    }

    private String formatEpoch(long epochMs) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(java.time.Instant.ofEpochMilli(epochMs).atZone(java.time.ZoneId.systemDefault()));
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private enum Status {
        PASS,
        FAIL,
        SKIP
    }

    private static final class SuiteStats {
        private final String suiteName;
        private final int total;
        private final int passed;
        private final int failed;
        private final int skipped;
        private final long durationMs;
        private final long startedAtMs;
        private final long finishedAtMs;

        private SuiteStats(String suiteName, int total, int passed, int failed, int skipped,
                           long durationMs, long startedAtMs, long finishedAtMs) {
            this.suiteName = suiteName;
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
            this.durationMs = durationMs;
            this.startedAtMs = startedAtMs;
            this.finishedAtMs = finishedAtMs;
        }
    }

    private static final class ClassStats {
        private final String className;
        private int tests;
        private int passed;
        private int failures;
        private int skipped;
        private long durationMs;

        private ClassStats(String className) {
            this.className = className;
        }

        private String className() {
            return className;
        }
    }
}
