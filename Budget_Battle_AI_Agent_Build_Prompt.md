# Budget Battle — Android Prototype Build Prompt
**For use with an AI coding agent (e.g. inside Android Studio / Claude Code / similar)**
Course: HIT238 Mobile Paradigm — Assessment 2 Individual Coding

---

## Context

Build a small native Android app in **Java** called **Budget Battle** — a low-stakes budgeting simulation game for university students. This is a prototype for **one user story only** — do not build additional features beyond what's specified below.

**User story:**
> As a student, I want to make weekly spending decisions so I can see how they affect my stress meter, so I understand the consequences of budgeting choices before they hit in real life.

**Client context (for realism, not to be shown in-app):** CDU Student Wellbeing / Financial Counselling service, aiming to teach practical budgeting through simulated consequences rather than lectures or spreadsheets.

---

## Explicit scope boundaries

- **No login/authentication screen.** This is out of scope for the assessed user story.
- **No GeoLocation, no NoSQL/cloud database, no mic/camera.** Not relevant to this user story — do not add them.
- **No network calls.** The app must work fully offline, matching the project's offline-first justification.
- Keep it to **3 screens** and **2–3 simulated weeks** — this is a prototype, not the full game.

---

## Screens and flow

### Screen 1 — Start Screen
- Let the player choose an income source: `Part-time job`, `Family support`, `Centrelink` (RadioGroup or Spinner)
- Let the player set a starting weekly budget (EditText, numeric input, or a SeekBar)
- A "Start Week 1" button to proceed

### Screen 2 — Weekly Decision Screen
- Shows the current week number (e.g. "Week 1 of 3")
- Presents a spending scenario per week, e.g.:
  - Week 1: Rent due — choose how much to pay towards rent vs. save
  - Week 2: Groceries + a curveball (unexpected textbook cost)
  - Week 3: Socialising (a mate's birthday dinner) vs. saving
- Use a **SeekBar or draggable slider** (touch event) for the player to choose how much to spend, with a live-updating TextView showing the chosen amount
- A "Confirm" button to lock in the decision for that week

### Screen 3 — Result / Stress Meter Screen
- On confirming a week's decision, show a brief **"Calculating your week..."** loading state
  - Implement this using `CompletableFuture.supplyAsync()` to run the outcome calculation off the main thread, then `.thenAccept()` (or post back to the UI thread) to update the UI once done — simulate a short delay (e.g. 800ms–1.5s) to make the async behaviour visible
- After the delay, update a **stress meter** — a custom view or styled ProgressBar — showing the player's stress level
  - Stress meter must communicate state via **both colour and a numeric/text label** (not colour alone), for accessibility
- A "Next Week" button advances to the next week's decision screen, looping back to Screen 2 until all weeks are complete
- After the final week, show a simple summary (final stress level + a short takeaway message)

---

## Technical requirements

1. **Language/platform:** Native Android, Java (matches HIT238 workshops — no Kotlin, no Flutter/React Native)
2. **Async requirement:** Use `java.util.concurrent.CompletableFuture` for the week-outcome calculation, per the pattern below (adapt exact logic to the scenario):

```java
public CompletableFuture<Integer> calculateWeekOutcome(int spendAmount, int budget) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            Thread.sleep(1000); // simulate processing delay
        } catch (InterruptedException ignored) {}
        int overspend = spendAmount - budget;
        int stressChange = overspend > 0 ? overspend / 10 : -2;
        return stressChange;
    }).thenApplyAsync(result -> result, runnable -> runOnUiThread(runnable));
}
```
Ensure any UI update after the async call happens back on the main thread (use `runOnUiThread` or a Handler).

3. **Local storage requirement:** Use `SharedPreferences` to persist:
   - The player's current budget/income choice
   - The running stress meter value
   - Which week they're on
   This should allow the app to resume state if reopened mid-session (offline-friendly, no cloud dependency).

4. **Touch/motion event requirement:** Use a `SeekBar` (or equivalent draggable control) with an `OnSeekBarChangeListener` for spending-amount input on the Weekly Decision Screen — this is the app's primary touch interaction, not just button taps.

5. **UI/Layout requirement:**
   - Use `ConstraintLayout` or `LinearLayout` appropriately for each screen
   - Stress meter should be a clear, styled visual element (custom View or a coloured/labelled ProgressBar) — not just a plain number
   - Follow the class code style already used in this project (see `MainActivity.java` reference pattern: class-scope View fields, `findViewById` in an initializer or `onCreate`, `Toast` for brief feedback)

6. **Code style / structure:**
   - Package: `com.example.budgetbattle`
   - Reasonable separation: an `Activity` per screen (or Fragments if the agent prefers, but Activities are consistent with what's taught), plus a small helper class (e.g. `WeekLogic.java`) holding the `CompletableFuture` calculation logic — keep it testable and separate from UI code
   - Comment key sections briefly, matching the teaching style seen in `MainActivity.java` (short explanatory comments, not verbose)

---

## Testing requirement (mandatory — required for a passing "Excellent" grade)

Write **at least one unit test** (JUnit, in `app/src/test/java/...`) that tests the week-outcome calculation logic in isolation from the UI — e.g.:
- Given a spend amount over budget, `calculateWeekOutcome` should return a positive stress increase
- Given a spend amount under budget, it should return a stress decrease (or zero/negative)

Keep the logic under test in a plain Java class (not inside an Activity) so it can be tested without an Android emulator/instrumentation, e.g.:

```java
public class WeekLogicTest {
    @Test
    public void overspending_increasesStress() {
        WeekLogic logic = new WeekLogic();
        int result = logic.calculateStressChange(150, 100); // spend, budget
        assertTrue(result > 0);
    }

    @Test
    public void underBudget_decreasesStress() {
        WeekLogic logic = new WeekLogic();
        int result = logic.calculateStressChange(80, 100);
        assertTrue(result <= 0);
    }
}
```

---

## Deliverable

A working Android Studio project (Gradle-based, matching the structure already used in this course's Week 4 project zip) that:
- Builds and runs without errors
- Implements the 3-screen flow above
- Demonstrably uses `CompletableFuture` for the async week-calculation
- Persists state via `SharedPreferences`
- Uses a `SeekBar` for touch-based spend input
- Includes at least one passing JUnit test on the core stress-calculation logic
- Contains no login screen, no GeoLocation, no database, no mic/camera code
