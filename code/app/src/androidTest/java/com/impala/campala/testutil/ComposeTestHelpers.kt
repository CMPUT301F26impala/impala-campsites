// DISCLOSURE:
// I wasn't entirely sure how to do UI tests that integrated with Firestore, so I worked with Claude Opus 5.5 in the Claude Code harness
// After it helped me set up the first UI test (CreateReservationFlowTest), I prompted it with
// "Is there any work we can do to extract the boilerplate stuff to another file so that our test files themselves dont have to duplicate it"
// It generated the below scaffolding code for the common UI test steps (waiting for text, clicking, picking dates), so that the tests I go on to write can be my own code, just using these helper functions

package com.impala.campala.testutil

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.impala.campala.CampalaApp
import com.impala.campala.ui.theme.AppTheme
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Shared steps for UI tests, written as extensions on the compose rule so a test can just call
// compose.clickText("Lotteries") etc.
//
// Use createComposeRule() (not createAndroidComposeRule<MainActivity>) for the rule, because the
// activity rule launches the app before @Before runs, which would be before the test data exists

/** How long to wait for something to show up before failing the test */
const val UI_TIMEOUT_MS = 10_000L

/** Renders the whole app, call this after the test data has been seeded */
fun ComposeContentTestRule.launchApp() {
    setContent { AppTheme { CampalaApp() } }
}

/**
 * Waits until some text is on screen
 *
 * Compose can't tell when Firestore calls finish, so anything that waits on data has to wait for it to show up
 *
 * @param text Exact text to look for
 * @param useUnmergedTree Needed for text inside some components (eg: the ExtendedFloatingActionButton), which isn't in the merged tree
 *
 * @throws androidx.compose.ui.test.ComposeTimeoutException if it doesn't show up within [UI_TIMEOUT_MS]
 */
fun ComposeContentTestRule.waitForText(text: String, useUnmergedTree: Boolean = false) {
    waitUntil(UI_TIMEOUT_MS) {
        onAllNodesWithText(text, useUnmergedTree = useUnmergedTree).fetchSemanticsNodes().isNotEmpty()
    }
}

/**
 * Waits for some text to show up and then clicks it
 *
 * @param text Exact text to click
 * @param useUnmergedTree See [waitForText]
 *
 * @throws androidx.compose.ui.test.ComposeTimeoutException if it doesn't show up within [UI_TIMEOUT_MS]
 * @throws AssertionError if more than one thing on screen has that text
 */
fun ComposeContentTestRule.clickText(text: String, useUnmergedTree: Boolean = false) {
    waitForText(text, useUnmergedTree)
    onNodeWithText(text, useUnmergedTree = useUnmergedTree).performClick()
}

/**
 * Opens a date field's picker, taps a day, and presses OK
 *
 * Day cells in the Material date picker are labelled with the full date (eg: "Thursday, December 10, 2026"),
 * and the screen behind the dialog also has day numbers on it, so this only looks inside the dialog
 *
 * The picker has to already be showing the month [date] is in
 *
 * @param fieldLabel Label of the date field to open
 * @param date Day to pick
 *
 * @throws androidx.compose.ui.test.ComposeTimeoutException if the day can't be found or the field doesn't update
 */
fun ComposeContentTestRule.pickDate(fieldLabel: String, date: LocalDate) {
    onNodeWithText(fieldLabel).performScrollTo().performClick()
    val dayLabel = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US))
    val dayCell = hasText(dayLabel, substring = true) and hasClickAction() and hasAnyAncestor(isDialog())
    waitUntil(UI_TIMEOUT_MS) { onAllNodes(dayCell).fetchSemanticsNodes().isNotEmpty() }
    onNode(dayCell).performClick()
    onNodeWithText("OK").performClick()

    // The field should now show the picked date
    waitForText(date.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")))
}

/**
 * Same idea as [pickDate], but for date pickers that open in text input mode (eg: the lottery form),
 * where you type the date instead of tapping a day
 *
 * If several fields have the same label (the lottery form has three "Start Date"s), this fills in the
 * first one that still shows its label, so call it in the order the fields are on screen
 *
 * @param fieldLabel Label of the date field to open
 * @param date Date to type in
 *
 * @throws androidx.compose.ui.test.ComposeTimeoutException if the field doesn't update
 */
fun ComposeContentTestRule.typeDate(fieldLabel: String, date: LocalDate) {
    // The field might be further down a list, and lists only create the rows that are on screen
    onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasText(fieldLabel))
    onAllNodesWithText(fieldLabel).onFirst().performClick()

    // The picker's text box takes just the digits, it adds the slashes itself
    val dateBox = hasSetTextAction() and hasAnyAncestor(isDialog())
    waitUntil(UI_TIMEOUT_MS) { onAllNodes(dateBox).fetchSemanticsNodes().isNotEmpty() }
    onNode(dateBox).performTextInput(date.format(DateTimeFormatter.ofPattern("MMdduuuu")))
    onNodeWithText("OK").performClick()

    // The field should now show the typed date
    waitForText(date.format(DateTimeFormatter.ofPattern("MM/dd/uuuu")))
}

/**
 * Replaces whatever is in a text field with new text
 *
 * @param fieldLabel Label of the text field
 * @param text What to type into it
 */
fun ComposeContentTestRule.typeInto(fieldLabel: String, text: String) {
    waitForText(fieldLabel)
    onNodeWithText(fieldLabel).performTextClearance()
    onNodeWithText(fieldLabel).performTextInput(text)
}

/**
 * Waits for an icon button (or anything else with a content description) and clicks it
 *
 * @param description The content description, eg: "Edit Icon"
 *
 * @throws androidx.compose.ui.test.ComposeTimeoutException if it doesn't show up within [UI_TIMEOUT_MS]
 */
fun ComposeContentTestRule.clickContentDescription(description: String) {
    waitUntil(UI_TIMEOUT_MS) {
        onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()
    }
    onNodeWithContentDescription(description).performClick()
}

/**
 * Checks that some text does NOT show up, by watching the screen for a while
 *
 * Compose can't tell when Firestore is done loading, so the only way to check that something
 * isn't there is to give it time to show up and make sure it doesn't
 *
 * @param text Text that shouldn't be on screen
 * @param substring Whether a partial match counts
 * @param forMs How long to watch for
 *
 * @throws AssertionError if the text shows up
 */
fun ComposeContentTestRule.assertTextStaysHidden(text: String, substring: Boolean = false, forMs: Long = 2_000L) {
    val showedUp = try {
        waitUntil(forMs) {
            onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
        }
        true
    } catch (e: ComposeTimeoutException) {
        false
    }
    if (showedUp) {
        throw AssertionError("Expected \"$text\" not to be on screen, but it showed up")
    }
}

/**
 * Waits until something is true in the database, eg: after pressing a button that saves in the background
 *
 * @param condition Checked over and over until it returns true, it can call repository functions
 *
 * @throws androidx.compose.ui.test.ComposeTimeoutException if it's still false after [UI_TIMEOUT_MS]
 */
fun ComposeContentTestRule.waitForDatabase(condition: suspend () -> Boolean) {
    waitUntil(UI_TIMEOUT_MS) { runBlocking { condition() } }
}

/** Uses the switch user mode button in the top bar to go to the campsite owner screens */
fun ComposeContentTestRule.switchToOwner() {
    clickContentDescription("Switch user mode")
    clickText("Use as a Campsite Owner")
}

/** Uses the switch user mode button in the top bar to go to the entrant (camper) screens */
fun ComposeContentTestRule.switchToEntrant() {
    clickContentDescription("Switch user mode")
    clickText("Use as a Camper")
}
