package com.example.budgetbattle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ResultActivity - Screen 3: Result / Stress Meter Screen
 *
 * Displays a "Calculating..." loading state while running the week outcome
 * calculation asynchronously via CompletableFuture (WeekLogic.calculateWeekOutcome).
 * After the delay (≈1.2 s), updates the stress meter ProgressBar + text label
 * on the main thread using runOnUiThread.
 *
 * After Week 3, shows a final summary instead of "Next Week".
 */
public class ResultActivity extends AppCompatActivity {

    private static final String PREFS_NAME = MainActivity.PREFS_NAME;

    // Views
    private TextView    tvCalculating;
    private ProgressBar progressBarStress;
    private TextView    tvStressPercent;
    private TextView    tvStressLabel;
    private TextView    tvOutcomeMessage;
    private Button      btnNext;

    // State loaded from SharedPreferences
    private int currentWeek;
    private int budget;
    private int spendAmount;
    private int currentStress;

    private final WeekLogic weekLogic = new WeekLogic();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initViews();
        loadState();
        startAsyncCalculation();
    }

    /** Bind all views and hide result UI until calculation completes. */
    private void initViews() {
        tvCalculating     = findViewById(R.id.tvCalculating);
        progressBarStress = findViewById(R.id.progressBarStress);
        tvStressPercent   = findViewById(R.id.tvStressPercent);
        tvStressLabel     = findViewById(R.id.tvStressLabel);
        tvOutcomeMessage  = findViewById(R.id.tvOutcomeMessage);
        btnNext           = findViewById(R.id.btnNext);

        // Show only loading text initially
        progressBarStress.setVisibility(View.INVISIBLE);
        tvStressPercent.setVisibility(View.INVISIBLE);
        tvStressLabel.setVisibility(View.INVISIBLE);
        tvOutcomeMessage.setVisibility(View.INVISIBLE);
        btnNext.setVisibility(View.INVISIBLE);
    }

    /** Load current game state from SharedPreferences. */
    private void loadState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentWeek   = prefs.getInt(MainActivity.KEY_WEEK, 1);
        budget        = prefs.getInt(MainActivity.KEY_BUDGET, 200);
        spendAmount   = prefs.getInt("spend_amount", 100);
        currentStress = prefs.getInt(MainActivity.KEY_STRESS, 20);
    }

    /**
     * Kick off the async week calculation using CompletableFuture.
     * The result is applied back on the main thread via runOnUiThread.
     */
    private void startAsyncCalculation() {
        tvCalculating.setText("⚙️ Calculating your week...");

        // Run calculation off the main thread; post result back to UI thread
        weekLogic.calculateWeekOutcome(spendAmount, budget)
                .thenAccept(stressChange -> runOnUiThread(() -> showResult(stressChange)));
    }

    /**
     * Called on the main thread after the async calculation completes.
     * Updates stress meter, saves new stress value, and shows outcome message.
     */
    private void showResult(int stressChange) {
        // Clamp stress between 0 and 100
        currentStress = Math.max(0, Math.min(100, currentStress + stressChange));

        // Persist updated stress value
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(MainActivity.KEY_STRESS, currentStress);
        editor.apply();

        // Reveal result views
        tvCalculating.setVisibility(View.GONE);
        progressBarStress.setVisibility(View.VISIBLE);
        tvStressPercent.setVisibility(View.VISIBLE);
        tvStressLabel.setVisibility(View.VISIBLE);
        tvOutcomeMessage.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);

        // Update stress ProgressBar and numeric label (accessible: colour + text)
        progressBarStress.setProgress(currentStress);
        tvStressPercent.setText(currentStress + "%");

        // Apply colour + text label based on stress band
        if (currentStress <= 30) {
            setStressBand(R.color.stress_low, "😊 Low Stress: You're doing great!", getPositiveMessage());
        } else if (currentStress <= 60) {
            setStressBand(R.color.stress_medium, "😐 Moderate Stress: Watch your spending", getMediumMessage());
        } else {
            setStressBand(R.color.stress_high, "😰 High Stress: You're overspending!", getNegativeMessage());
        }

        // Multimedia feedback: play short audio tone reflecting the outcome (rubric criterion 3)
        playOutcomeTone(stressChange);

        // Configure the Next/Finish button
        if (currentWeek >= 4) {
            btnNext.setText("🏁  See Final Summary");
            btnNext.setOnClickListener(v -> showFinalSummary());
        } else {
            btnNext.setText("Next Week →");
            btnNext.setOnClickListener(v -> advanceToNextWeek());
        }
    }

    /**
     * Play a brief audio tone based on the week's outcome.
     * Demonstrates multimedia usage as requested in the Assessment 2 rubric.
     */
    private void playOutcomeTone(int stressChange) {
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80);
            if (stressChange > 0) {
                // Warning tone when spending causes stress increase
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 250);
            } else {
                // Pleasant confirmation tone when staying within budget
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 200);
            }
        } catch (Exception ignored) {
            // Gracefully ignore if audio device is unavailable (e.g. headless emulator)
        }
    }

    /** Apply colour tint and label text for the current stress band. */
    private void setStressBand(int colorRes, String label, String message) {
        int color = getResources().getColor(colorRes, getTheme());
        progressBarStress.setProgressTintList(ColorStateList.valueOf(color));
        tvStressPercent.setTextColor(color);
        tvStressLabel.setText(label);
        tvOutcomeMessage.setText(message);
    }

    /** Advance to the next week's decision screen. */
    private void advanceToNextWeek() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(MainActivity.KEY_WEEK, currentWeek + 1);
        editor.apply();

        startActivity(new Intent(this, WeeklyDecisionActivity.class));
        finish();
    }

    /** Show the end-of-game summary and reset saved session. */
    private void showFinalSummary() {
        // Reset week so resuming starts fresh
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(MainActivity.KEY_WEEK, 0);
        editor.apply();

        // Build final summary string
        String grade;
        String takeaway;
        if (currentStress <= 30) {
            grade    = "⭐ Excellent Budgeter!";
            takeaway = "You stayed calm and in control all 4 weeks. "
                     + "Real-life budgeting success starts with habits like these!";
        } else if (currentStress <= 60) {
            grade    = "👍 Decent Effort";
            takeaway = "Some weeks were tough, but you managed. "
                     + "Next time, build a small buffer for unexpected costs.";
        } else {
            grade    = "💸 Overspending Hit Hard";
            takeaway = "High stress can impact your studies and wellbeing. "
                     + "Prioritise rent and food first, social costs can be trimmed.";
        }

        // Update UI to display summary
        tvStressLabel.setText("Final Stress: " + currentStress + "%");
        tvOutcomeMessage.setText(
            "🎮 Game Over!\n\n" + grade + "\n\n" + takeaway + "\n\n"
            + "📊 Final Overview:\n"
            + "• Final Stress Level: " + currentStress + "%"
        );
        btnNext.setText("🔄  Play Again");
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    // -----------------------------------------------------------------------
    // Outcome messages per week
    // -----------------------------------------------------------------------

    private String getPositiveMessage() {
        String[] msgs = {
            "Smart choice! Paying rent on time avoids fees and keeps you stress-free.",
            "You handled the textbook surprise without breaking the budget. Well done!",
            "You balanced social life and essentials like a pro. Your future self thanks you.",
            "All bills paid on time! You've mastered the end-of-month crunch."
        };
        return msgs[Math.min(currentWeek - 1, 3)];
    }

    private String getMediumMessage() {
        String[] msgs = {
            "Rent took a big chunk. Try to negotiate a cheaper share-house next semester.",
            "Multiple expenses at once can pile up fast. A small emergency fund helps!",
            "FOMO is real but expensive. Suggest a potluck next time: same fun, less cost.",
            "Bills add up at end of month. Automate payments to avoid surprises next time."
        };
        return msgs[Math.min(currentWeek - 1, 3)];
    }

    private String getNegativeMessage() {
        String[] msgs = {
            "Overpaying on rent squeezes everything else. Consider a cheaper option ASAP.",
            "Unexpected expenses happen, that is why a buffer each week matters.",
            "Dining out adds up quickly. Cooking together is cheaper AND more fun!",
            "End-of-month bills hit hard when you haven't saved a buffer. Plan ahead!"
        };
        return msgs[Math.min(currentWeek - 1, 3)];
    }
}
