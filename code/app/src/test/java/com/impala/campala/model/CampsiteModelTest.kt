
package com.impala.campala.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests just for the few non-firebase-related behaviours of CampsiteModel
 * Firebase-related tests are in androidTest
 */
class CampsiteModelTest {

    @Test
    fun noRatingsMeansNoAverage() {
        assertNull(CampsiteModel(ratingTotal = 0.0, ratingCount = 0).averageRating)
    }

    @Test
    fun oneRatingIsTheAverage() {
        assertEquals(3.5, CampsiteModel(ratingTotal = 3.5, ratingCount = 1).averageRating!!, 0.0001)
    }

    @Test
    fun averageIsTotalOverCount() {
        assertEquals(13.0 / 3, CampsiteModel(ratingTotal = 13.0, ratingCount = 3).averageRating!!, 0.0001)
    }
}
