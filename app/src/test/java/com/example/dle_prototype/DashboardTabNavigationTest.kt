package com.example.dle_prototype

import com.example.dle_prototype.ui.navigation.AppRoute
import com.example.dle_prototype.ui.screens.DashboardTabRoutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardTabNavigationTest {

    @Test
    fun testAppRouteItemsAndConstants() {
        assertEquals("feed", AppRoute.FEED_ROUTE)
        assertEquals("practice", AppRoute.PRACTICE_ROUTE)
        assertEquals("compete", AppRoute.COMPETE_ROUTE)
        assertEquals("progress", AppRoute.PROGRESS_ROUTE)
        assertEquals("profile", AppRoute.PROFILE_ROUTE)

        assertEquals(5, AppRoute.items.size)
        assertEquals(AppRoute.Feed, AppRoute.items[0])
        assertEquals(AppRoute.Practice, AppRoute.items[1])
        assertEquals(AppRoute.Compete, AppRoute.items[2])
        assertEquals(AppRoute.Progress, AppRoute.items[3])
        assertEquals(AppRoute.Profile, AppRoute.items[4])

        assertEquals(AppRoute.Feed, AppRoute.fromRoute("feed"))
        assertEquals(AppRoute.Practice, AppRoute.fromRoute("practice"))
        assertEquals(AppRoute.Compete, AppRoute.fromRoute("compete"))
        assertEquals(AppRoute.Progress, AppRoute.fromRoute("progress"))
        assertEquals(AppRoute.Profile, AppRoute.fromRoute("profile"))
        assertEquals(AppRoute.Feed, AppRoute.fromRoute("invalid"))

        assertEquals(0, AppRoute.getIndex("feed"))
        assertEquals(1, AppRoute.getIndex("practice"))
        assertEquals(2, AppRoute.getIndex("compete"))
        assertEquals(3, AppRoute.getIndex("progress"))
        assertEquals(4, AppRoute.getIndex("profile"))
    }

    @Test
    fun testTabRoutesOrderAndIndices() {
        assertEquals("feed", DashboardTabRoutes.FEED)
        assertEquals("practice", DashboardTabRoutes.PRACTICE)
        assertEquals("compete", DashboardTabRoutes.COMPETE)
        assertEquals("progress", DashboardTabRoutes.PROGRESS)
        assertEquals("profile", DashboardTabRoutes.PROFILE)

        assertEquals(0, DashboardTabRoutes.getIndex(DashboardTabRoutes.FEED))
        assertEquals(1, DashboardTabRoutes.getIndex(DashboardTabRoutes.PRACTICE))
        assertEquals(2, DashboardTabRoutes.getIndex(DashboardTabRoutes.COMPETE))
        assertEquals(3, DashboardTabRoutes.getIndex(DashboardTabRoutes.PROGRESS))
        assertEquals(4, DashboardTabRoutes.getIndex(DashboardTabRoutes.PROFILE))
        assertEquals(0, DashboardTabRoutes.getIndex("unknown_route"))
    }

    @Test
    fun testTabTransitionDirection() {
        val feedIndex = DashboardTabRoutes.getIndex(DashboardTabRoutes.FEED)
        val practiceIndex = DashboardTabRoutes.getIndex(DashboardTabRoutes.PRACTICE)
        val competeIndex = DashboardTabRoutes.getIndex(DashboardTabRoutes.COMPETE)
        val progressIndex = DashboardTabRoutes.getIndex(DashboardTabRoutes.PROGRESS)
        val profileIndex = DashboardTabRoutes.getIndex(DashboardTabRoutes.PROFILE)

        assertTrue(practiceIndex > feedIndex)
        assertTrue(competeIndex > practiceIndex)
        assertTrue(progressIndex > competeIndex)
        assertTrue(profileIndex > progressIndex)
    }
}
