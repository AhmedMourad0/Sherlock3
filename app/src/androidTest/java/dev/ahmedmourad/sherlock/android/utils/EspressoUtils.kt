package dev.ahmedmourad.sherlock.android.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ScrollToAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.TypeSafeMatcher

fun Matcher<View>.childAt(position: Int): Matcher<View> = object : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("with $position child view of type parentMatcher")
    }

    override fun matchesSafely(view: View) = if (view.parent !is ViewGroup)
        this@childAt.matches(view.parent)
    else
        this@childAt.matches(view.parent) && (view.parent as ViewGroup).getChildAt(position) == view
}

fun nestedScrollTo(): ViewAction = ViewActions.actionWithAssertions(NestedScrollToAction())

class NestedScrollToAction private constructor(private val delegate: ScrollToAction) : ViewAction by delegate {

    constructor() : this(ScrollToAction())

    override fun getConstraints(): Matcher<View> = anyOf(
            allOf(
                    withEffectiveVisibility(Visibility.VISIBLE),
                    isDescendantOfA(isAssignableFrom(NestedScrollView::class.java))),
            delegate.constraints
    )
}
