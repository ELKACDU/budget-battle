package com.example.budgetbattle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - Screen 1: Start Screen
 *
 * Allows the player to:
 *  - Select their income source (RadioGroup)
 *  - Set their weekly budget (SeekBar)
 *  - Start the 4-week simulation
 *
 * Also resumes any saved mid-session state via SharedPreferences.
 */
public class MainActivity extends AppCompatActivity {

    // SharedPreferences constants (shared across all activities)
    public static final String PREFS_NAME   = "BudgetBattlePrefs";
    public static final String KEY_BUDGET   = "budget";
    public static final String KEY_INCOME   = "income";
    public static final String KEY_STRESS   = "stress";
    public static final String KEY_WEEK     = "week";

    // Views
    private RadioGroup rgIncome;
    private SeekBar    seekBarBudget;
    private TextView   tvBudgetAmount;
    private Button     btnStartWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Resume a saved session if one exists
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedWeek = prefs.getInt(KEY_WEEK, 0);
        if (savedWeek >= 1 && savedWeek <= 4) {
            Toast.makeText(this, "Resuming saved session - Week " + savedWeek + " of 4", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, WeeklyDecisionActivity.class));
            finish();
            return;
        }

        initViews();
        setupBudgetSeekBar();
        setupStartButton();
    }

    /** Bind all views from the layout. */
    private void initViews() {
        rgIncome      = findViewById(R.id.rgIncome);
        seekBarBudget = findViewById(R.id.seekBarBudget);
        tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
        btnStartWeek  = findViewById(R.id.btnStartWeek);
    }

    /**
     * Configure the budget SeekBar.
     * Range: $50 to $500 (offset by 50 so progress=0 -> $50).
     */
    private void setupBudgetSeekBar() {
        seekBarBudget.setMax(450);     // max progress = $500 (450 + 50)
        seekBarBudget.setProgress(150); // default = $200

        tvBudgetAmount.setText("$200");

        seekBarBudget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Live-update the displayed budget amount
                int budget = progress + 50;
                tvBudgetAmount.setText("$" + budget);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /** Wire up the Start Week 1 button. */
    private void setupStartButton() {
        btnStartWeek.setOnClickListener(v -> {
            // Validate income selection
            if (rgIncome.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select your income source first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine selected income label
            String income;
            int selectedId = rgIncome.getCheckedRadioButtonId();
            if (selectedId == R.id.rbFamily)      income = "Family Support";
            else if (selectedId == R.id.rbCentrelink) income = "Centrelink";
            else                                   income = "Part-time Job";

            // Read budget from SeekBar
            int budget = seekBarBudget.getProgress() + 50;

            // Persist game state to SharedPreferences (offline-first)
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(KEY_INCOME, income);
            editor.putInt(KEY_BUDGET, budget);
            editor.putInt(KEY_STRESS, 20);  // starting stress level
            editor.putInt(KEY_WEEK, 1);     // start at week 1
            editor.apply();

            // Navigate to the first weekly decision screen
            startActivity(new Intent(this, WeeklyDecisionActivity.class));
            finish();
        });
    }
}
