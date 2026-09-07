package com.example.budgetbattle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * WeeklyDecisionActivity - Screen 2: Weekly Decision Screen
 *
 * Shows the current week's scenario and allows the player to allocate spending
 * across realistic expense categories/options using a touch SeekBar.
 *
 * The player can tap different category cards to adjust their budgets individually.
 * The sum of all chosen options is dynamically calculated and displayed
 * as the total weekly spending.
 */
public class WeeklyDecisionActivity extends AppCompatActivity {

    // Shared preferences keys from MainActivity
    private static final String PREFS_NAME = MainActivity.PREFS_NAME;

    // Views - Header & Scenario
    private TextView tvWeekTitle;
    private TextView tvWeekDescription;
    private TextView tvBudgetHint;

    // Views - Category Selection Cards
    private CardView cardCat1, cardCat2, cardCat3;
    private TextView tvCatName1, tvCatName2, tvCatName3;
    private TextView tvCatAmount1, tvCatAmount2, tvCatAmount3;

    // Views - Active Slider Controls
    private TextView tvActiveCategoryTitle;
    private TextView tvSpendAmount;
    private SeekBar  seekBarSpend;
    private TextView tvMaxSpend;

    // Views - Total Sum & Status
    private TextView tvTotalWeeklySpend;
    private TextView tvBreakdownSummary;
    private TextView tvBudgetStatus;
    private Button   btnConfirm;

    // State loaded from SharedPreferences
    private int currentWeek;
    private int budget;

    // Multi-option state
    private int currentCategoryIndex = 0;
    private String[] categoryNames = new String[3];
    private int[] categoryAllocations = new int[3];
    private int[] categoryMaxes = new int[3];

    // Flag to prevent programmatic SeekBar updates from overwriting allocations
    private boolean isSwitchingCategory = false;

    private final WeekLogic weekLogic = new WeekLogic();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_decision);

        initViews();
        loadWeekData();
        setupCategoryClickListeners();
        setupSpendSeekBar();
        setupConfirmButton();
    }

    /** Bind all views from the layout. */
    private void initViews() {
        tvWeekTitle            = findViewById(R.id.tvWeekTitle);
        tvWeekDescription      = findViewById(R.id.tvWeekDescription);
        tvBudgetHint           = findViewById(R.id.tvBudgetHint);

        cardCat1               = findViewById(R.id.cardCat1);
        cardCat2               = findViewById(R.id.cardCat2);
        cardCat3               = findViewById(R.id.cardCat3);
        tvCatName1             = findViewById(R.id.tvCatName1);
        tvCatName2             = findViewById(R.id.tvCatName2);
        tvCatName3             = findViewById(R.id.tvCatName3);
        tvCatAmount1           = findViewById(R.id.tvCatAmount1);
        tvCatAmount2           = findViewById(R.id.tvCatAmount2);
        tvCatAmount3           = findViewById(R.id.tvCatAmount3);

        tvActiveCategoryTitle  = findViewById(R.id.tvActiveCategoryTitle);
        tvSpendAmount          = findViewById(R.id.tvSpendAmount);
        seekBarSpend           = findViewById(R.id.seekBarSpend);
        tvMaxSpend             = findViewById(R.id.tvMaxSpend);

        tvTotalWeeklySpend     = findViewById(R.id.tvTotalWeeklySpend);
        tvBreakdownSummary     = findViewById(R.id.tvBreakdownSummary);
        tvBudgetStatus         = findViewById(R.id.tvBudgetStatus);
        btnConfirm             = findViewById(R.id.btnConfirm);
    }

    /**
     * Load week number and budget from SharedPreferences,
     * then populate the UI with 3 expense options for this week.
     */
    private void loadWeekData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentWeek = prefs.getInt(MainActivity.KEY_WEEK, 1);
        budget      = prefs.getInt(MainActivity.KEY_BUDGET, 200);

        int weekIndex = Math.max(0, Math.min(currentWeek - 1, WeekLogic.WEEK_SHORT_TITLES.length - 1));

        // Load 3 categories and defaults for this week
        categoryNames       = WeekLogic.WEEK_CATEGORIES[weekIndex].clone();
        categoryAllocations = WeekLogic.WEEK_DEFAULT_ALLOCATIONS[weekIndex].clone();
        categoryMaxes       = WeekLogic.WEEK_CATEGORY_MAX[weekIndex].clone();

        // Update header and scenario text
        tvWeekTitle.setText("Week " + currentWeek + " of 4");
        tvWeekDescription.setText(WeekLogic.WEEK_DESCRIPTIONS[weekIndex]);
        tvBudgetHint.setText("Your weekly budget: $" + budget);

        // Populate category labels
        tvCatName1.setText(categoryNames[0]);
        tvCatName2.setText(categoryNames[1]);
        tvCatName3.setText(categoryNames[2]);

        updateCategoryCardAmounts();

        // Select the first category by default
        selectCategory(0);
        updateTotalSummary();
    }

    /** Set click listeners for the 3 category cards so the user can choose which to edit. */
    private void setupCategoryClickListeners() {
        cardCat1.setOnClickListener(v -> selectCategory(0));
        cardCat2.setOnClickListener(v -> selectCategory(1));
        cardCat3.setOnClickListener(v -> selectCategory(2));
    }

    /**
     * Highlights the selected category and safely points the SeekBar slider to it
     * without triggering progress changes that corrupt the allocated value.
     */
    private void selectCategory(int index) {
        isSwitchingCategory = true;
        currentCategoryIndex = index;

        int activeBgColor   = getResources().getColor(R.color.accent_purple, getTheme());
        int inactiveBgColor = getResources().getColor(R.color.card_bg_alt, getTheme());

        cardCat1.setCardBackgroundColor(index == 0 ? activeBgColor : inactiveBgColor);
        cardCat2.setCardBackgroundColor(index == 1 ? activeBgColor : inactiveBgColor);
        cardCat3.setCardBackgroundColor(index == 2 ? activeBgColor : inactiveBgColor);

        tvCatAmount1.setTextColor(index == 0 ? getResources().getColor(R.color.white, getTheme()) : getResources().getColor(R.color.accent_gold, getTheme()));
        tvCatAmount2.setTextColor(index == 1 ? getResources().getColor(R.color.white, getTheme()) : getResources().getColor(R.color.accent_gold, getTheme()));
        tvCatAmount3.setTextColor(index == 2 ? getResources().getColor(R.color.white, getTheme()) : getResources().getColor(R.color.accent_gold, getTheme()));

        // Bind slider to this category safely
        tvActiveCategoryTitle.setText("Adjusting: " + categoryNames[index]);
        tvMaxSpend.setText("$" + categoryMaxes[index]);
        seekBarSpend.setMax(categoryMaxes[index]);
        seekBarSpend.setProgress(categoryAllocations[index]);
        tvSpendAmount.setText("$" + categoryAllocations[index]);

        isSwitchingCategory = false;
    }

    /**
     * Configure the SeekBar for interactive spending allocation.
     * Only updates allocation when user actively touches/drags the slider.
     */
    private void setupSpendSeekBar() {
        seekBarSpend.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Ignore programmatic events (e.g. setMax / setProgress during category switch)
                if (!fromUser || isSwitchingCategory) {
                    return;
                }

                // Update currently active category's allocation
                categoryAllocations[currentCategoryIndex] = progress;
                tvSpendAmount.setText("$" + progress);

                // Update the category card label
                updateCategoryCardAmounts();

                // Recalculate and update the live total sum
                updateTotalSummary();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /** Updates the dollar amounts shown on each category card. */
    private void updateCategoryCardAmounts() {
        tvCatAmount1.setText("$" + categoryAllocations[0]);
        tvCatAmount2.setText("$" + categoryAllocations[1]);
        tvCatAmount3.setText("$" + categoryAllocations[2]);
    }

    /**
     * Calculates the sum of all chosen categories, updates the total display,
     * breakdown formula, budget status, and confirm button.
     */
    private void updateTotalSummary() {
        int totalSpend = weekLogic.calculateTotalSpend(categoryAllocations);
        tvTotalWeeklySpend.setText("$" + totalSpend);

        // Highlight total and status: red if over budget, green if within budget
        if (totalSpend > budget) {
            int overspend = totalSpend - budget;
            tvTotalWeeklySpend.setTextColor(getResources().getColor(R.color.stress_high, getTheme()));
            tvBudgetStatus.setText("⚠️ Over budget by $" + overspend + " (raises stress)");
            tvBudgetStatus.setTextColor(getResources().getColor(R.color.stress_high, getTheme()));
        } else {
            int remaining = budget - totalSpend;
            tvTotalWeeklySpend.setTextColor(getResources().getColor(R.color.stress_low, getTheme()));
            tvBudgetStatus.setText("✅ Within budget ($" + remaining + " buffer)");
            tvBudgetStatus.setTextColor(getResources().getColor(R.color.stress_low, getTheme()));
        }

        // Show formula breakdown
        tvBreakdownSummary.setText(categoryNames[0] + " ($" + categoryAllocations[0] + ") + "
                + categoryNames[1] + " ($" + categoryAllocations[1] + ") + "
                + categoryNames[2] + " ($" + categoryAllocations[2] + ")");

        // Dynamic Confirm Button text showing total
        btnConfirm.setText("✅  CONFIRM TOTAL: $" + totalSpend);
    }

    /** Lock in the player's total decision and move to the Result screen. */
    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            int totalSpend = weekLogic.calculateTotalSpend(categoryAllocations);

            // Persist total spend decision for ResultActivity to calculate outcome
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putInt("spend_amount", totalSpend);
            editor.apply();

            Toast.makeText(this, "Total $" + totalSpend + " locked in - calculating outcome...", Toast.LENGTH_SHORT).show();

            // Navigate to result screen and finish this activity
            startActivity(new Intent(this, ResultActivity.class));
            finish();
        });
    }
}
