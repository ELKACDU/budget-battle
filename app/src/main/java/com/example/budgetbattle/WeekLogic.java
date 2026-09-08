package com.example.budgetbattle;

import java.util.concurrent.CompletableFuture;

/**
 * WeekLogic - pure Java helper class (no Android imports).
 * Keeps calculation logic separate from UI, making it unit-testable
 * without an emulator or instrumentation.
 */
public class WeekLogic {

    // -----------------------------------------------------------------------
    // Week scenario data (3 weeks of content)
    // -----------------------------------------------------------------------

    /** Short titles for each week (used in Activity headers). */
    public static final String[] WEEK_SHORT_TITLES = {
        "Rent Due",
        "Groceries + Textbook",
        "Social Weekend",
        "End-of-Month Bills"
    };

    /** Full scenario description shown on the decision screen. */
    public static final String[] WEEK_DESCRIPTIONS = {
        "Your rent is due this week. Paying it in full avoids late fees, "
            + "but that leaves very little for food and transport.\n\n"
            + "How much will you put towards rent this week?",

        "Weekly groceries are non-negotiable, but your lecturer just announced "
            + "a required textbook ($85). You can't find it second-hand in time.\n\n"
            + "How much will you spend on essentials this week?",

        "A big social weekend is coming up. Friends are planning dinners, "
            + "activities, and a group outing. FOMO is real, but so is your "
            + "bank balance.\n\n"
            + "How much will you spend on social activities this week?",

        "End of month! Subscription renewals, phone bill, internet, and "
            + "transport card top-up are all due at once.\n\n"
            + "How much will you allocate to cover your monthly bills?"
    };

    /** Spending category names per week (3 selectable options per week) */
    public static final String[][] WEEK_CATEGORIES = {
        { "🏠 Rent", "🛒 Groceries", "🚌 Transport" },
        { "🛒 Groceries", "📚 Textbook ($85)", "💡 Phone & Bills" },
        { "🍽️ Dinners Out", "🎮 Activities", "☕ Cafes & Snacks" },
        { "📱 Phone Bill", "🌐 Internet", "🚌 Transport Card" }
    };

    /** Default allocations per category for each week (in dollars) */
    public static final int[][] WEEK_DEFAULT_ALLOCATIONS = {
        { 120, 60, 20 },
        { 70, 85, 30 },
        { 50, 40, 20 },
        { 45, 60, 30 }
    };

    /** Maximum spend limit for each category slider ($500 for all) */
    public static final int[][] WEEK_CATEGORY_MAX = {
        { 500, 500, 500 },
        { 500, 500, 500 },
        { 500, 500, 500 },
        { 500, 500, 500 }
    };

    /** Maximum spend slider value per week (in dollars). */
    public static final int[] WEEK_MAX_SPEND = { 500, 500, 500, 500 };

    /**
     * Calculates the sum of all allocated categories for the week.
     *
     * @param allocations Array of spending amounts per category
     * @return Total sum of spending
     */
    public int calculateTotalSpend(int[] allocations) {
        int total = 0;
        if (allocations != null) {
            for (int amount : allocations) {
                total += amount;
            }
        }
        return total;
    }

    // -----------------------------------------------------------------------
    // Core stress calculation - pure Java, no Android dependencies
    // -----------------------------------------------------------------------

    /**
     * Calculates the change in stress for a given week's spending decision.
     *
     * @param spendAmount  Amount the player chose to spend this week
     * @param budget       Player's weekly budget
     * @return             Positive value = stress increases; negative = stress decreases
     */
    public int calculateStressChange(int spendAmount, int budget) {
    int overspend = spendAmount - budget;
    if (overspend > 0) {
        // Overspending raises stress proportionally to how far over budget
        return overspend / 10;
    }
    // Staying under budget gives relief proportional to the buffer saved,
    // capped at -5 so saving a huge amount doesn't dominate the stress score
    int underspend = Math.abs(overspend);
    return Math.max(-5, -(underspend / 20) - 1);
}
    }

    // -----------------------------------------------------------------------
    // Async version - used by ResultActivity
    // -----------------------------------------------------------------------

    /**
     * Runs the week outcome calculation asynchronously using CompletableFuture.
     * Simulates a short processing delay (1200ms) to make the async behaviour visible.
     *
     * @param spendAmount  Amount the player chose to spend
     * @param budget       Player's weekly budget
     * @return             CompletableFuture that resolves to the stress change value
     */
    public CompletableFuture<Integer> calculateWeekOutcome(int spendAmount, int budget) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate processing delay - makes async behaviour visible in the UI
                // Reduced from 1200ms after testing on a mid-range device — 900ms still
            // reads as "calculating" without feeling sluggish on repeated weekly taps
            Thread.sleep(900);
            } catch (InterruptedException ignored) {}
            return calculateStressChange(spendAmount, budget);
        });
    }
}
