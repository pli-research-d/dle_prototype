package com.example.dle_prototype

import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.data.UserSettings
import com.example.dle_prototype.data.UserSettingsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomThemeAndFeaturesTest {

    @Test
    fun testPredefinedThemesContainRequiredColorsAndButtonClickedColor() {
        val themes = UserSettingsManager.PREDEFINED_THEMES
        assertTrue("Predefined themes list should have multiple themes", themes.size >= 8)

        for (theme in themes) {
            assertTrue("Theme ID should not be blank", theme.id.isNotBlank())
            assertTrue("Theme name should not be blank", theme.name.isNotBlank())
            assertTrue("Background color should be valid hex", theme.backgroundColorHex.startsWith("#"))
            assertTrue("Foreground color should be valid hex", theme.foregroundColorHex.startsWith("#"))
            assertTrue("Accent color should be valid hex", theme.accentColorHex.startsWith("#"))
            assertTrue("Button color should be valid hex", theme.buttonColorHex.startsWith("#"))
            assertTrue("Button clicked color should be valid hex", theme.buttonClickedColorHex.startsWith("#"))

            // Verify parseHexColor successfully parses all colors
            val bg = UserSettingsManager.parseHexColor(theme.backgroundColorHex)
            val fg = UserSettingsManager.parseHexColor(theme.foregroundColorHex)
            val accent = UserSettingsManager.parseHexColor(theme.accentColorHex)
            val btn = UserSettingsManager.parseHexColor(theme.buttonColorHex)
            val btnClicked = UserSettingsManager.parseHexColor(theme.buttonClickedColorHex)

            assertNotNull(bg)
            assertNotNull(fg)
            assertNotNull(accent)
            assertNotNull(btn)
            assertNotNull(btnClicked)
        }
    }

    @Test
    fun testWcagContrastTextCalculation() {
        // Pure dark background -> Must return White text
        val darkBg = Color(0xFF0B0F19)
        val darkContrastText = UserSettingsManager.getContrastingTextColor(darkBg)
        assertEquals(Color(0xFFFFFFFF), darkContrastText)

        // Pure white background -> Must return Dark text for high contrast readability
        val lightBg = Color(0xFFFFFFFF)
        val lightContrastText = UserSettingsManager.getContrastingTextColor(lightBg)
        assertEquals(Color(0xFF0F172A), lightContrastText)

        // Nordic Frost light background -> Must return Dark text
        val nordicBg = Color(0xFFF8FAFC)
        val nordicContrastText = UserSettingsManager.getContrastingTextColor(nordicBg)
        assertEquals(Color(0xFF0F172A), nordicContrastText)

        // Midnight black -> Must return White text
        val blackBg = Color(0xFF000000)
        val blackContrastText = UserSettingsManager.getContrastingTextColor(blackBg)
        assertEquals(Color(0xFFFFFFFF), blackContrastText)
    }

    @Test
    fun testUserSettingsHoldsCustomColors() {
        val custom = UserSettings(
            backgroundColorHex = "#180D2B",
            foregroundColorHex = "#27123D",
            accentColorHex = "#F43F5E",
            buttonColorHex = "#9333EA",
            buttonClickedColorHex = "#7E22CE",
            themePresetId = "sunset_vaporwave"
        )

        assertEquals("#180D2B", custom.backgroundColorHex)
        assertEquals("#27123D", custom.foregroundColorHex)
        assertEquals("#F43F5E", custom.accentColorHex)
        assertEquals("#9333EA", custom.buttonColorHex)
        assertEquals("#7E22CE", custom.buttonClickedColorHex)
        assertEquals("sunset_vaporwave", custom.themePresetId)
    }
}
