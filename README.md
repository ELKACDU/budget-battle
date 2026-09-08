# Budget Battle

A gamified, offline-first Android budgeting simulation for university students, built as an individual prototype for HIT238 Assessment 2 (CDU).

## Overview
Budget Battle helps students rehearse weekly budgeting decisions in a risk-free, 4-week simulation, with real-time stress feedback based on spending choices. See the full report (HIT238_Final.pdf / .docx) for background, persona, and design rationale.

## Tech Stack
- Native Android (Java), API 26+ (compileSdk 34)
- SharedPreferences for local state persistence
- CompletableFuture for async stress calculation
- JUnit 4 (JVM-based unit tests, no emulator required)

## How to Run
1. Open in Android Studio: `File > Open` → select the project root folder
2. Let Gradle sync (Java 17)
3. Run unit tests: right-click `app/src/test/java/com/example/budgetbattle/WeekLogicTest.java` → Run
4. Run the app: start an AVD (Pixel 6, API 34) or connect a device via USB debugging → Run (Shift+F10)
   
   ## AI Usage Disclosure

In line with CDU academic integrity requirements, AI assistance was used during the development of this assessment. All prompts used are disclosed below in full.

**Tools used:** An AI coding agent (used inside Android Studio) for initial code generation; Claude (Anthropic) for iterative edits, debugging Git/GitHub setup, and report drafting assistance.

---

### Prompt 1 — Initial Android project generation

The following prompt was given to an AI coding agent to generate the initial project structure, screens, and core logic:

> # Budget Battle — Android Prototype Build Prompt
> **For use with an AI coding agent (e.g. inside Android Studio / Claude Code / similar)**
> Course: HIT238 Mobile Paradigm — Assessment 2 Individual Coding
>
> ## Context
> Build a small native Android app in **Java** called **Budget Battle** — a low-stakes budgeting simulation game for university students. This is a prototype for **one user story only** — do not build additional features beyond what's specified below.
>
> **User story:** As a student, I want to make weekly spending decisions so I can see how they affect my stress meter, so I understand the consequences of budgeting choices before they hit in real life.
>
> **Client context (for realism, not to be shown in-app):** CDU Student Wellbeing / Financial Counselling service, aiming to teach practical budgeting through simulated consequences rather than lectures or spreadsheets.
>
> ## Explicit scope boundaries
> - No login/authentication screen — out of scope for the assessed user story.
> - No GeoLocation, no NoSQL/cloud database, no mic/camera — not relevant to this user story.
> - No network calls — must work fully offline.
> - Keep it to 3 screens and 2–3 simulated weeks — this is a prototype, not the full game.
>
> ## Screens and flow
> **Screen 1 — Start Screen:** choose income source (RadioGroup/Spinner), set starting weekly budget (SeekBar), "Start Week 1" button.
>
> **Screen 2 — Weekly Decision Screen:** shows current week number and a spending scenario (rent, groceries + textbook curveball, social spending). Uses a SeekBar for spend input with a live-updating TextView. "Confirm" button locks in the decision.
>
> **Screen 3 — Result/Stress Meter Screen:** shows a brief "Calculating your week..." loading state implemented via `CompletableFuture.supplyAsync()`, then updates a stress meter communicating state via both colour and numeric label. "Next Week" button loops back to Screen 2, or shows a final summary after the last week.
>
> ## Technical requirements
> 1. Native Android, Java (no Kotlin/Flutter/React Native)
> 2. Use `java.util.concurrent.CompletableFuture` for the week-outcome calculation, with UI updates posted back to the main thread
> 3. Use `SharedPreferences` to persist budget/income choice, running stress value, and current week
> 4. Use a `SeekBar` with `OnSeekBarChangeListener` for spend input
> 5. Use `ConstraintLayout`/`LinearLayout`; stress meter as a styled visual element, not a plain number
> 6. Package `com.example.budgetbattle`; one Activity per screen plus a separate `WeekLogic.java` helper class for testable calculation logic
>
> ## Testing requirement (mandatory)
> Write at least one JUnit test (in `app/src/test/java/...`) testing the week-outcome calculation logic in isolation from the UI — e.g. overspending increases stress, underspending decreases it. Logic under test must be in a plain Java class so it can run without an emulator.
>
> ## Deliverable
> A working Android Studio project that builds and runs without errors, implements the 3-screen flow, uses `CompletableFuture` for async calculation, persists state via `SharedPreferences`, uses a `SeekBar` for touch input, includes at least one passing JUnit test, and contains no login/GeoLocation/database/mic/camera code.


### Prompt 2 — Post-generation code edits (via Claude)

After initial generation, the following changes were made with Claude's assistance to demonstrate direct engagement with the codebase, each committed separately:

1. **Prompt/change:** "Cap the relief from under-spending — currently a flat -2 no matter how far under budget." → Modified `calculateStressChange()` in `WeekLogic.java` to scale relief proportionally to the underspend amount, capped at -5.
2. **Prompt/change:** "Add a unit test for the new scaled-relief behaviour." → Added `largerUnderspend_givesMoreRelief()` to `WeekLogicTest.java`.
3. **Prompt/change:** "Shorten the async delay and explain why." → Reduced `Thread.sleep(1200)` to `Thread.sleep(900)` in `calculateWeekOutcome()`, with an inline comment explaining the reasoning.

All AI-assisted content was reviewed, tested, and edited by the author before submission. Core design decisions (persona, hypotheses, UI design choices, and the specific edits described above) reflect the author's own judgement.

