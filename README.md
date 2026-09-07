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

