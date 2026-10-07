# OrangeHRM Employee Lifecycle Automation

End-to-end UI + API automation of the OrangeHRM employee lifecycle: authentication, employee creation,
role-based access, employee update, backend API verification and employee deletion.

**Stack:** Java 21 (LTS bytecode, builds on JDK 21+) · Selenium 4.50 · Cucumber 7 (BDD) + PicoContainer ·
TestNG · REST Assured + JSON-schema validation · Allure + Cucumber reports · SLF4J/Logback ·
Checkstyle + SpotBugs · Maven · GitHub Actions · Selenium Grid (Docker)

## Demo

A recording of a passing run (MP4), captured by the framework's own video recorder, is in
[`docs/demo/`](docs/demo/).

---

## Contents

1. [Quick start](#1-quick-start)
2. [Execution guide](#2-execution-guide)
3. [Architecture](#3-architecture)
4. [Test coverage](#4-test-coverage)
5. [Tagging strategy](#5-tagging-strategy)
6. [Configuration and environments](#6-configuration-and-environments)
7. [Test data management](#7-test-data-management)
8. [Stability: waits, retries, flaky tests](#8-stability-waits-retries-flaky-tests)
9. [Reporting and observability](#9-reporting-and-observability)
10. [CI/CD pipeline](#10-cicd-pipeline)
11. [Key design decisions](#11-key-design-decisions)
12. [Extending the framework](#12-extending-the-framework)
13. [Verified results](#13-verified-results)
14. [Troubleshooting](#14-troubleshooting)

---

## 1. Quick start

**Prerequisites:** JDK 21+ and Maven 3.9+. A browser (Chrome by default); Selenium Manager downloads the
driver automatically. Optional: `ffmpeg` (for MP4 failure videos; GIF is used without it), the Allure CLI,
and Docker (for Selenium Grid).

```bash
git clone https://github.com/BalajiG555/OrangeHRM-Employee-Lifecycle-Automation-v2.git
cd OrangeHRM-Employee-Lifecycle-Automation-v2

# 1. Provide credentials locally (this file is git-ignored)
cp src/test/resources/config/config.properties.example src/test/resources/config/config.properties
#    then set: username=Admin  /  password=admin123   (public demo credentials)

# 2. Run the framework self-tests (no browser, ~5 s)
mvn test -Dtestng.suite=testng-unit.xml

# 3. Run the E2E suite against QA
mvn clean test -Denv=qa

# 4. Open the HTML report
mvn allure:serve
```

Credentials can alternatively be supplied as environment variables (`ORANGEHRM_USERNAME`,
`ORANGEHRM_PASSWORD`) or system properties (`-Dusername=... -Dpassword=...`).

> `config.properties` is git-ignored, so it is **not** part of a fresh clone or zip. Create it again
> after every new checkout, or the run fails fast with *"Required configuration 'username' is missing"*.

### Running from IntelliJ IDEA

1. **File → Project Structure → SDK:** JDK 21 or newer.
2. **Maven tool window → 🔄 Reload All Maven Projects.**
3. **Maven tool window → Execute Maven Goal**, then type the goal **without** the `mvn` prefix, for example
   `clean test -Denv=qa`. This uses IntelliJ's bundled Maven, so no separate Maven install is needed.
4. Pass single tags **without quotes**: `-Dcucumber.filter.tags=@auth`. IntelliJ forwards quotes
   literally, so `"@auth"` would match nothing.
5. Do **not** right-click `TestRunner.java` → Run. That bypasses `testng.xml`, so the retry, flaky
   detection and parallel listeners are not active. Use Maven, or right-click `testng.xml` → Run.

---

## 2. Execution guide

| Goal | Command |
|---|---|
| Full regression on QA | `mvn clean test -Denv=qa` |
| Smoke only | `mvn clean test -Denv=qa -Dcucumber.filter.tags="@smoke"` |
| One domain | `mvn clean test -Dcucumber.filter.tags="@rbac"` |
| Read-only prod smoke | `mvn clean test -Denv=prod -Dcucumber.filter.tags="@smoke and not @destructive"` |
| More parallel threads | `mvn clean test -Denv=qa -Dparallel.threads=4` |
| Another browser | `mvn clean test -Dbrowser=firefox` (chrome, firefox, edge) |
| Headed browser | `mvn clean test -Dheadless=false` |
| Selenium Grid | `docker compose up -d` then `mvn clean test -Dgrid.url=http://localhost:4444` |
| Disable retries | `mvn clean test -Dretry.max.count=0` |
| Record every scenario | `mvn clean test -Dvideo.mode=always` |
| Framework self-tests | `mvn test -Dtestng.suite=testng-unit.xml` |
| Static analysis only | `mvn verify -DskipTests` |
| Allure HTML report | `mvn allure:serve` or `mvn allure:report` |

Any configuration key can be overridden the same way, for example `-Dexplicit.wait.seconds=25`.

**Zero-scenario guard:** if a tag expression matches no scenarios, for example because of a typo, the
build **fails** with `No tests were executed!` instead of reporting a misleading success. Add
`-DfailIfNoTests=false` when an empty selection is intentional. CI does this per shard, and its report
job fails if *no* shard ran anything.

**IntelliJ tip:** in *Run Anything* / *Execute Maven Goal*, pass single tags **without quotes**
(`-Dcucumber.filter.tags=@auth`); IntelliJ forwards the quotes literally. Use the Terminal tab for
expressions with spaces (`"@smoke and not @destructive"`).

### Output locations

| Output | Path |
|---|---|
| Allure results | `target/allure-results/` |
| Cucumber HTML / JSON / JUnit XML | `target/cucumber-reports/` |
| Failed-scenario rerun list | `target/cucumber-reports/rerun.txt` |
| Screenshots and videos | `target/test-artifacts/{screenshots,videos}/` |
| Run log (thread + scenario on each line) | `target/logs/test-run.log` |
| Flaky-test report | `target/flaky-report/flaky-tests.md` |

---

## 3. Architecture

The code is split into a **reusable framework core** and an **application layer**, so another
application can be onboarded without touching the core.

```text
src/main/java
├── com/framework/                 <- application-agnostic core
│   ├── api/          ApiClient (base), AuthStrategy, ApiLoggingFilter, ApiAssertions
│   ├── config/       ConfigReader (layered, env-aware), ConfigKeys
│   ├── data/         CleanupRegistry (LIFO, failure-isolated teardown)
│   ├── driver/       DriverFactory (ThreadLocal, local/Grid), BrowserType
│   ├── exceptions/   FrameworkException, ConfigurationException, ApiException
│   ├── listeners/    RetryAnalyzer, RetryListener, FlakyTestListener,
│   │                 ParallelExecutionListener, ReportEnvironmentListener
│   ├── pages/        BasePage
│   ├── reporting/    ScreenshotUtils, ArtifactPaths, AllureEnvironmentWriter,
│   │                 video/ (VideoRecorder, Ffmpeg/Gif encoders)
│   └── utils/        WaitUtils, JsonDataReader, RandomDataUtils, ResourceUtils, XPathUtils
│
└── com/orangehrm/                 <- application layer (OrangeHRM)
    ├── api/          ApiEndpoints, EmployeeApiClient, UserApiClient, ApiServices
    │   └── auth/     FormLogin / SessionCookie / OAuth strategies + factory
    ├── context/      TestContext (scenario-scoped state)
    ├── data/         TestDataGenerator, CredentialsProvider
    ├── models/       Employee, Credentials, UserRole, SystemUser, ApiEmployee, ApiJobDetails
    ├── pages/        OrangeHrmPage (app base), Login, Dashboard, EmployeeList, AddEmployee,
    │                 EmployeeDetails, EmployeeJob, SystemUsers, PageManager, OxdLocators, AppRoutes
    │   └── components/ SideMenu
    └── services/     EmployeeService, TestDataService

src/test/java
├── com/orangehrm/
│   ├── hooks/            Hooks (guards, browser, evidence, cleanup)
│   ├── runners/          TestRunner (parallel DataProvider)
│   └── stepdefinitions/  Authentication, Employee, ApiVerification, RoleBasedAccess, ParameterTypes
└── com/framework/unit/   Framework self-tests (13 tests, no browser)

src/test/resources
├── features/   authentication, employee_management, employee_api_verification, role_based_access
├── config/     config-default + config-{local,dev,qa,prod} + config.properties.example
├── testdata/   employee.json, profile.png (+ optional testdata/<env>/ overrides)
├── schemas/    JSON schemas for API contract validation
├── allure/     categories.json (failure classification)
└── logback-test.xml, allure.properties, cucumber.properties
```

### Layers and flow

```text
Feature (Gherkin)  ->  Step definitions (thin, assertions only)
                          |                    |
                    PageManager           ApiServices / TestDataService
                          |                    |
                    Page objects          Services -> API clients -> AuthStrategy
                          |                    |
                    BasePage + WaitUtils  ApiClient (logging + Allure filters)
                          |                    |
                    Selenium WebDriver    REST Assured
```

Each layer has one responsibility:

* **Step definitions** orchestrate and assert. They never contain locators or HTTP calls.
* **Page objects** describe screens. They never assert and never wait manually.
* **Services** turn raw API responses into domain objects.
* **Hooks** own the scenario lifecycle.

---

## 4. Test coverage

| Area | Scenarios | Assertions |
|---|---|---|
| Authentication | valid login, invalid login, logout | dashboard route + header, exact error message, login page after logout |
| Employee creation | UI add with profile picture | first/last name on profile, empNumber from URL, searchable by Employee Id |
| Employee update | Job Title + Employment Status via UI | both values re-read from the Job tab (soft assertions) |
| Employee deletion | delete via Employee List | not listed + "No Records Found" |
| API verification | create / update / delete persisted in backend | HTTP status, **JSON schema**, firstName, lastName, employeeId, empNumber, **jobTitle, employmentStatus**, absence after delete |
| Role-based access | menu visibility (Admin vs ESS); direct URL to System Users | ESS: Admin/PIM hidden, My Info visible, System Users denied. Admin rows act as positive controls. |

Each scenario is independent. Preconditions such as "an employee exists" are seeded through the API, so
scenarios never depend on each other or on execution order. Every scenario cleans up its own data.

---

## 5. Tagging strategy

| Dimension | Tags | Used for |
|---|---|---|
| Suite | `@smoke`, `@regression` | Selecting depth: PR/prod smoke vs nightly/regression |
| Domain | `@auth`, `@employee`, `@api`, `@rbac` | CI sharding and focused runs |
| Layer | `@ui`, `@api` | Knowing which layers a scenario touches |
| Safety | `@destructive` | Scenarios that create/modify/delete data. Excluded in prod by CI and by a hook guard. |
| Lifecycle | `@quarantine`, `@wip` | Quarantine runs in a non-blocking CI lane. `@wip` is excluded everywhere. |
| Reporting | `@severity=blocker/critical/normal` | Allure severity labels |

Tags combine freely, for example `-Dcucumber.filter.tags="@regression and @api and not @quarantine"`.

---

## 6. Configuration and environments

Configuration resolution, highest priority first:

```text
-Dkey=value                         (JVM system property)
ORANGEHRM_KEY=value                 (environment variable, e.g. ORANGEHRM_PARALLEL_THREADS)
config/config.properties            (local, git-ignored, secrets)
config/config-<env>.properties      (committed, per environment)
config/config-default.properties    (committed, shared defaults)
```

Select the environment with `-Denv=<name>` or `ORANGEHRM_ENV`. The default is `local`. An unknown
environment fails immediately; it never silently falls back to defaults.

### What differs per environment

| Setting | local | dev | qa | prod |
|---|---|---|---|---|
| headless | false | true | true | true |
| parallel.threads | 1 | 2 | 3 | 2 |
| retry.max.count | 0 | 1 | 1 | 1 |
| explicit.wait.seconds | 15 | 20 | 15 | 10 |
| video.mode | off | always | on-failure | on-failure |
| tests.destructive.enabled | true | true | true | **false** |
| api.validation.enabled | true | true | true | false |
| Employee Id prefix | LC | DV | QA | PR |

All environments currently point at the public OrangeHRM demo, because it is the only instance
available. Pointing an environment at its own host is a one-line change (`app.base.url`) in its file or
via `ORANGEHRM_APP_BASE_URL`.

**Secrets** (`username`, `password`, optional OAuth values) are never committed. They come from the
local `config.properties` or from GitHub secrets.

---

## 7. Test data management

* **Dynamic generation.** `TestDataGenerator` builds employees from `testdata/employee.json` with a
  random name suffix and an Employee Id made of a per-environment prefix plus random digits (max 10
  characters, OrangeHRM's limit). It uses `SecureRandom`, not timestamps, so parallel scenarios never
  collide. A unit test generates 2,000 ids in parallel and asserts uniqueness.
* **API-driven setup.** Preconditions are created through the API (`testdata.setup.mode=api`; `ui` is
  available). This is faster and keeps update/delete scenarios from re-testing the Add Employee screen.
* **On-demand role accounts.** ESS users (employee + system user) are created per scenario through the
  API, so no shared ESS account or secret is needed and nothing is shared between parallel runs.
* **Guaranteed cleanup.** `TestDataService` registers an idempotent API cleanup action *before* data is
  created. `CleanupRegistry` runs the actions in reverse order (user before employee). Cleanup failures
  are logged and attached to Allure; they never override the scenario result.
* **Environment independence.** `testdata/<env>/employee.json` overrides the shared template for
  environments with different reference data, with no code change.

---

## 8. Stability: waits, retries, flaky tests

### Smart waits

* There is no `Thread.sleep` anywhere, and a Checkstyle rule fails the build if one is introduced.
  Implicit wait is zero; every wait is explicit and condition-based (`WaitUtils`).
* `clickWhenUnobstructed` handles OrangeHRM's re-appearing form loader by retrying the
  "overlay gone → element ready → click" sequence as one unit.
* Async form values are awaited explicitly, for example the auto-generated Employee Id before it is
  overwritten.
* Negative checks (an element should be absent) first wait for the container to render, then check
  immediately. This keeps them fast without making them meaningless.
* **Two kinds of timeout.** Waits that span a server round-trip (login, redirects, page loads) use
  `page.load.timeout.seconds`. Waits for elements on an already-loaded page use
  `explicit.wait.seconds`. A slow server response is therefore never mistaken for a missing element.
  This came from a real flaky failure found by the framework itself: a blank page after login under
  parallel load.
* **Login fails fast and explains why.** It reports either "rejected: <application message>" or
  "no response within N s", and logs how long each login took, so slowness trends show up in the logs.

### Retry logic

`RetryAnalyzer` retries a failed scenario up to `retry.max.count` times, configured per environment.
Configuration errors are never retried. The retry is wired through `testng.xml`, because TestNG ignores
annotation transformers declared through `@Listeners`.

### Flaky test detection

1. **In every run:** `FlakyTestListener` classifies scenarios that *failed then passed on retry* as
   **FLAKY** and scenarios that failed every attempt as **CONSISTENT FAILURE**. It writes
   `target/flaky-report/flaky-tests.md` and adds that table to the GitHub job summary. Allure also shows
   retries and puts flaky tests in a dedicated category.
2. **Scheduled:** the `flaky-detection.yml` workflow runs the suite N times (default 5) with retries
   *disabled*. `scripts/flaky_report.py` then labels each scenario STABLE, FLAKY (mixed results) or
   ALWAYS FAILING, with a failure rate.

### Flaky test mitigation

* **Prevention:** condition-based waits only, unique data per scenario, independent scenarios, one
  browser per thread, API-based setup instead of long UI preconditions.
* **Containment:** a bounded retry (1 in CI, 0 locally so developers see real failures). Every retry
  is reported, so retries never hide flakiness.
* **Quarantine:** tag a flaky scenario `@quarantine`. It moves to a non-blocking CI lane, so it is
  still tracked but no longer blocks merges.
* **Fix and verify:** diagnose with the evidence (screenshot, video, page source, logs). Remove the tag
  once the scheduled detection report shows the scenario as STABLE again.

---

## 9. Reporting and observability

* **HTML reports:**
  * **Allure** for local runs. In CI, all shards are merged into one report with trend history,
    uploaded as an artifact and published to GitHub Pages.
  * **Cucumber HTML** per shard.
  * **JUnit XML**, shown as PR checks.
* **Screenshots:** captured at the failing step, embedded in both Allure and the Cucumber HTML report,
  and saved to disk. The page URL and page source are attached on failure.
* **Videos:** per-scenario recording through the Chrome DevTools screencast.
  * Works headless, in parallel and on Grid.
  * Encoded to MP4 with ffmpeg, with a pure-Java GIF fallback.
  * `video.mode` = `off`, `on-failure` or `always`.
* **API traces:** every API request and response is attached to Allure. Login requests are deliberately
  excluded so credentials never appear in reports.
* **Environment widget:** Allure shows environment, URL, browser, Grid, threads, retry count, auth mode,
  tag expression, Java and OS, plus a link to the CI run.
* **Failure categories:** product defects, test defects, timeouts, API failures, configuration
  problems, flaky tests, and tests skipped by an environment guard.
* **Logs:** one line per event with thread and scenario name (readable in parallel), written to the
  console and to `target/logs/test-run.log`.

### Execution recordings

There are two kinds of recording:

**1. Demo recording (reference only, committed once).** A short recording of a **passed** run, produced
by the framework's own video capture, is kept in [`docs/demo/`](docs/demo/). It is static documentation:
the framework never writes to this folder.

**2. Recordings generated by every run (automatic).** No manual step is needed. During each run the
framework records each scenario and saves the result automatically:

| Where | What |
|---|---|
| `target/test-artifacts/videos/` | One file per kept scenario, named `<scenario>_<timestamp>_<thread>.mp4` (or `.gif` when ffmpeg is not installed) |
| Allure report | Embedded as **"Scenario recording"** under the scenario's After hook |
| GitHub Actions | Uploaded with the `test-evidence-<shard>` artifact of each run |

Which recordings are kept depends on `video.mode`:

| `video.mode` | Behaviour | Default for |
|---|---|---|
| `off` | No recording | local |
| `on-failure` | Records every scenario, keeps the video only if it fails | qa, prod |
| `always` | Records and keeps every scenario | dev |

To keep a video of every scenario in any environment:

```bash
mvn clean test -Denv=qa -Dvideo.mode=always
```

`target/` is cleaned on every `mvn clean` and is git-ignored. Copy a video out of it if you want to keep
it permanently. Install ffmpeg to get MP4 files, which are much smaller than the GIF fallback.

---

## 10. CI/CD pipeline

`.github/workflows/ci.yml` runs on push and PR to `main`, nightly, and on manual dispatch (choose
environment, suite, browser and thread count).

```text
quality ──► e2e (4 parallel shards: auth | employee-ui | employee-api | rbac) ──► report ──► deploy-report
   │              each shard runs scenarios on N parallel threads                (Allure)     (GitHub Pages)
   └──────► quarantine (non-blocking)
```

| Stage | What it does |
|---|---|
| quality | Compile, framework unit tests, Checkstyle, SpotBugs |
| e2e | Install dependencies (Maven cache), run tests, publish JUnit checks, upload Allure results, Cucumber report and evidence (screenshots, videos, logs, flaky report) |
| quarantine | Runs `@quarantine` scenarios; never fails the pipeline |
| report | Merges shard results, restores trend history, generates the Allure HTML, writes a run summary, uploads the `allure-report` artifact |
| deploy-report | Publishes the report to GitHub Pages (main branch) |

Parallelism works at two levels: four shard jobs run concurrently, and each shard runs scenarios on
`parallel.threads` threads inside the JVM.

**Setup** (do this *before* the first push, because the push triggers the pipeline):

1. **Settings → Secrets and variables → Actions:** add `ORANGEHRM_USERNAME` and `ORANGEHRM_PASSWORD`.
2. **Settings → Pages → Source:** select **GitHub Actions**.
3. Push to `main`, then open the **Actions** tab.

Where the results appear:

* **Run summary page:** pass/fail counts and the flaky-test table.
* **Artifacts:** `allure-report`, plus `cucumber-report-*` and `test-evidence-*` (screenshots, videos,
  logs) per shard.
* **GitHub Pages:** the hosted Allure report at [balajig555.github.io/OrangeHRM-Employee-Lifecycle-Automation-v2](https://balajig555.github.io/OrangeHRM-Employee-Lifecycle-Automation-v2/), with
  trend history across runs.

If you rename the repository, also update the link patterns in `src/test/resources/allure.properties`.

The OAuth secrets are optional (only for `api.auth.mode=oauth`). The old ESS and API-base-URL secrets
are no longer needed.

`.github/workflows/flaky-detection.yml` runs weekly, or on demand, as described in section 8.

---

## 11. Key design decisions

| Decision | Why |
|---|---|
| Core framework vs application packages | New applications reuse config, driver, waits, API base, reporting and listeners without modification. |
| PicoContainer dependency injection | Step classes stay small and focused. One `TestContext` per scenario means no static state and safe parallelism. |
| ThreadLocal WebDriver + parallel DataProvider | One browser per thread. The thread count is a configuration value, not code. |
| API auth strategy pattern, default `form` | Needs only the admin credentials, works before a browser exists and after logout. OAuth authorization codes are single-use and unsuitable for CI, so they are optional. |
| API-seeded preconditions and API cleanup | Smaller, faster, independent scenarios that don't depend on UI state. |
| Positive-control rows in role-based tests | Prove that "hidden"/"denied" checks would fail if access were actually granted. |
| Guards that skip, not return | A disabled API check or a destructive test in prod shows as *skipped*, never as a false pass. |
| CDP screencast for video | No X display, no cross-talk between parallel tests, works on Grid, no extra dependency. |
| Bounded, reported retries + quarantine + repeat-run detection | Retries reduce noise without hiding flakiness. |
| Java 21 LTS bytecode | A long-term-support baseline; still builds with JDK 23. |
| Quality gates (Checkstyle, SpotBugs, unit tests) before E2E | Fast feedback and enforced coding standards. |

---

## 12. Extending the framework

* **New page:** extend `OrangeHrmPage` (or `BasePage` for another application), declare locators as
  `private static final By`, and expose it through `PageManager`.
* **New API endpoint:** add the path to `ApiEndpoints` and a method to an `ApiClient` subclass. Map the
  response in a service.
* **New authentication scheme:** implement `AuthStrategy` and register it in `AuthStrategyFactory`.
* **New browser:** add an enum constant to `BrowserType`.
* **New environment:** add `config-<env>.properties` (and optionally `testdata/<env>/`).
* **New application:** create a sibling package to `com.orangehrm` that reuses `com.framework.*`.

---

## 13. Verified results

Results from runs against the live OrangeHRM demo (Windows 11, Chrome 154, JDK 23, Selenium 4.50):

| Check | Command | Result |
|---|---|---|
| Framework unit tests | `mvn verify -Dtestng.suite=testng-unit.xml` | 13 run, 0 failures (the MP4 test skips without ffmpeg) |
| Coding standards | (part of `verify`) | Checkstyle: 0 violations; SpotBugs: 0 bugs |
| Zero-scenario guard | `mvn test -Dcucumber.filter.tags=@typo` | Build fails with "No tests were executed!" |
| Full suite, sequential | `mvn clean test` | 16/16 passed, 49/49 steps, about 6m 45s |
| Full suite, parallel (3 threads) | `mvn clean test -Denv=qa` | 16/16 passed in about 3 min (2.3× faster), no retries needed |
| Test data hygiene | every run | All seeded employees and ESS users removed (`Cleanup done`, no failures) |

**Flakiness found and fixed by the framework itself.** During parallel runs, `FlakyTestListener`
flagged a scenario that passed only on retry. The failure screenshot showed a blank page after login,
which pointed to waits for server round-trips being bounded by the shorter element timeout. Navigation
waits were separated from element waits across all pages.
Occasional slowness of the shared public demo itself, such as a login page taking more than 30 s to
load, is absorbed by the single retry and still reported as FLAKY, never hidden.

---

## 14. Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Required configuration 'username' is missing` | No `config.properties` (it is git-ignored, so a fresh clone or zip does not have it) | Create it from `config.properties.example`, or set `ORANGEHRM_USERNAME` / `ORANGEHRM_PASSWORD` |
| `Tests run: 0` / `No tests were executed!` | The tag filter matched nothing, usually because IntelliJ passed quotes literally (`"@auth"`) | Remove the quotes: `-Dcucumber.filter.tags=@auth` |
| `Error: -classpath requires class path specification` when running `mvn` in a terminal | Broken or incomplete system Maven install (source zip, or wrong `MAVEN_HOME`) | Use **Execute Maven Goal** (bundled Maven), or reinstall Maven from the *binary* zip and fix `PATH` |
| `Unable to find an exact match for CDP version …` | Chrome auto-updated past the DevTools versions bundled with Selenium | Bump `selenium.version` in `pom.xml`. Only video capture uses DevTools; tests are unaffected. |
| `Timed out receiving message from renderer` | The demo site did not deliver a page within `page.load.timeout.seconds` | Usually transient on the shared demo, and absorbed by the retry. If persistent, raise `page.load.timeout.seconds`. |
| `Tests run: 17, Skipped: 1` with BUILD SUCCESS | One scenario failed once and passed on retry | See `target/flaky-report/flaky-tests.md` and the `Retrying … after:` line in `target/logs/test-run.log` |
| `VideoEncoderTest.ffmpegEncoderProducesMp4` skipped | ffmpeg is not installed | Optional. Videos fall back to GIF; install ffmpeg for MP4. |
| Chrome never visible | QA, dev and prod run headless | Add `-Dheadless=false`, or use the default `local` environment |
