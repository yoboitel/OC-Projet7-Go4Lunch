package com.yohan.go4lunch;

import android.content.Context;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.yohan.go4lunch.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.yohan.go4lunch", appContext.getPackageName());
    }


    @Test
    public void verifyMapIsFirstFragment() {
        //Verify map is shown, so fragmentMap is the first one to be displayed after app launch
        onView(ViewMatchers.withId(R.id.map)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void verifyListFragmentDisplayed() {
        onView(ViewMatchers.withId(R.id.navigation_list)).perform(click());
        onView(ViewMatchers.withId(R.id.rcRestaurants)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void verifyWorkmatesFragmentDisplayed() {
        onView(ViewMatchers.withId(R.id.navigation_workmates)).perform(click());
        onView(ViewMatchers.withId(R.id.rcWorkmates)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.activity_main_toolbar)).check(matches(hasDescendant(withText("Available workmates"))));
    }

    @Test
    public void verifyChatFragmentDisplayed() {
        onView(ViewMatchers.withId(R.id.navigation_chat)).perform(click());
        onView(ViewMatchers.withId(R.id.rcChat)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.activity_main_toolbar)).check(matches(hasDescendant(withText("Chat"))));
    }

    @Test
    public void verifyShowSettingsWorks() {
        onView(withContentDescription(R.string.navigation_drawer_open)).perform(click());
        onView(ViewMatchers.withId(R.id.drawer_settings)).perform(click());
        onView(ViewMatchers.withId(R.id.switchNotif)).check(matches(ViewMatchers.isDisplayed()));
    }

}