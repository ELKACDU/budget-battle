package com.example.budgetbattle;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * WeekLogicTest - JUnit unit tests for the stress-calculation logic.
 *
 * These tests run entirely on the JVM (no Android emulator needed)
 * because WeekLogic contains no Android imports.
 *
 * Run with: ./gradlew test
 * Or right-click this file in Android Studio -> Run 'WeekLogicTest'
 */
public class WeekLogicTest {

    /** Overspending should increase the player's stress level. */
    @Test
    public void overspending_increasesStress() {
        WeekLogic logic = new WeekLogic();
        int result = logic.calculateStressChange(150, 100); // spend $150 on a $100 budget
        assertTrue("Overspending should return a positive stress change", result > 0);
    }

    /** Spending under budget should decrease (or not increase) stress. */
    @Test
    public void underBudget_decreasesStress() {
        WeekLogic logic = new WeekLogic();
        int result = logic.calculateStressChange(80, 100); // spend $80 on a $100 budget
        assertTrue("Spending under budget should return a non-positive stress change", result <= 0);
    }

    /** Spending exactly on budget should not increase stress. */
    @Test
    public void exactBudget_doesNotIncreaseStress() {
        WeekLogic logic = new WeekLogic();
        int result = logic.calculateStressChange(100, 100);
        assertTrue("Spending exactly on budget should not raise stress", result <= 0);
    }

    /** Larger overspend should cause proportionally more stress. */
    @Test
    public void largerOverspend_causesMoreStress() {
        WeekLogic logic = new WeekLogic();
        int stressSmall  = logic.calculateStressChange(110, 100); // $10 over
        int stressLarge  = logic.calculateStressChange(200, 100); // $100 over
        assertTrue("Larger overspend should produce a higher stress value", stressLarge > stressSmall);
    }

    /** Multi-option spending sum should correctly aggregate all allocated categories. */
    @Test
    public void calculateTotalSpend_sumsAllAllocations() {
        WeekLogic logic = new WeekLogic();
        int[] items = { 120, 60, 20 };
        int total = logic.calculateTotalSpend(items);
        assertEquals("Sum of 120 + 60 + 20 should equal 200", 200, total);
    }
}
