package com.example.dle_prototype.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dle_prototype.data.PredefinedTheme
import com.example.dle_prototype.data.UserSettings
import com.example.dle_prototype.data.UserSettingsManager
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeCustomizerDialog(
    initialSettings: UserSettings,
    onDismiss: () -> Unit,
    onSaveTheme: (UserSettings) -> Unit
) {
    var selectedThemeId by remember { mutableStateOf(initialSettings.themePresetId) }
    var bgHex by remember { mutableStateOf(initialSettings.backgroundColorHex) }
    var fgHex by remember { mutableStateOf(initialSettings.foregroundColorHex) }
    var accentHex by remember { mutableStateOf(initialSettings.accentColorHex) }
    var btnHex by remember { mutableStateOf(initialSettings.buttonColorHex) }
    var btnClickedHex by remember { mutableStateOf(initialSettings.buttonClickedColorHex) }

    // Active color customization category: 0 = Background, 1 = Foreground, 2 = Accent, 3 = Button, 4 = Button Over/Clicked
    var activeColorCategory by remember { mutableIntStateOf(0) }

    val currentBg = UserSettingsManager.parseHexColor(bgHex, Color(0xFF0B0F19))
    val currentFg = UserSettingsManager.parseHexColor(fgHex, Color(0xFF151D2F))
    val currentAccent = UserSettingsManager.parseHexColor(accentHex, CyanAccent)
    val currentBtn = UserSettingsManager.parseHexColor(btnHex, Color(0xFF6366F1))
    val currentBtnClicked = UserSettingsManager.parseHexColor(btnClickedHex, Color(0xFF4338CA))

    // WCAG High-contrast text colors
    val onBgText = UserSettingsManager.getContrastingTextColor(currentBg)
    val onFgText = UserSettingsManager.getContrastingTextColor(currentFg)
    val onFgSecondary = UserSettingsManager.getSecondaryContrastingTextColor(currentFg)
    val onBtnText = UserSettingsManager.getContrastingTextColor(currentBtn)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_customizer_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyanAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Themes & Colors",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Themes & Colors",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Pick predefined presets or customize colors",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. PREDEFINED THEMES SECTION
                Text(
                    text = "PREDEFINED THEMES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyanAccent
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(UserSettingsManager.PREDEFINED_THEMES) { theme ->
                        val isSelected = selectedThemeId == theme.id
                        val themeBg = UserSettingsManager.parseHexColor(theme.backgroundColorHex)
                        val themeFg = UserSettingsManager.parseHexColor(theme.foregroundColorHex)
                        val themeAccent = UserSettingsManager.parseHexColor(theme.accentColorHex)
                        val themeBtn = UserSettingsManager.parseHexColor(theme.buttonColorHex)
                        val themeBtnClicked = UserSettingsManager.parseHexColor(theme.buttonClickedColorHex)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF090E1A),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) CyanAccent else Color(0xFF1E293B)
                            ),
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    selectedThemeId = theme.id
                                    bgHex = theme.backgroundColorHex
                                    fgHex = theme.foregroundColorHex
                                    accentHex = theme.accentColorHex
                                    btnHex = theme.buttonColorHex
                                    btnClickedHex = theme.buttonClickedColorHex
                                }
                                .testTag("theme_preset_${theme.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = theme.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) CyanAccent else Color(0xFFF1F5F9),
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = theme.description,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 13.sp,
                                    maxLines = 2
                                )

                                // Palette swatch preview dots
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ColorDot(themeBg, "Background")
                                    ColorDot(themeFg, "Foreground")
                                    ColorDot(themeAccent, "Accent")
                                    ColorDot(themeBtn, "Button")
                                    ColorDot(themeBtnClicked, "Button Clicked")
                                }
                            }
                        }
                    }
                }

                // 2. LIVE INTERACTIVE PREVIEW CARD
                Text(
                    text = "LIVE INTERACTIVE PREVIEW",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyanAccent
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = currentBg,
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Canvas Background Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onBgText
                        )

                        // Sample Card on Canvas
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = currentFg,
                            border = BorderStroke(1.dp, currentAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "High Contrast Card",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = onFgText
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(currentAccent)
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Accent Chip",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = UserSettingsManager.getContrastingTextColor(currentAccent)
                                        )
                                    }
                                }

                                Text(
                                    text = "Text dynamically adapts with WCAG contrast against this background.",
                                    fontSize = 11.sp,
                                    color = onFgSecondary
                                )

                                // Interactive Button with Pressed / Hover test state
                                val buttonInteractionSource = remember { MutableInteractionSource() }
                                val isPressed by buttonInteractionSource.collectIsPressedAsState()
                                var isHoverSimulated by remember { mutableStateOf(false) }

                                val isButtonClickedState = isPressed || isHoverSimulated
                                val effectiveBtnBg = if (isButtonClickedState) currentBtnClicked else currentBtn
                                val effectiveBtnText = UserSettingsManager.getContrastingTextColor(effectiveBtnBg)

                                Button(
                                    onClick = { isHoverSimulated = !isHoverSimulated },
                                    interactionSource = buttonInteractionSource,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = effectiveBtnBg,
                                        contentColor = effectiveBtnText
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("preview_interactive_button")
                                ) {
                                    Icon(
                                        imageVector = if (isButtonClickedState) Icons.Default.TouchApp else Icons.Default.ColorLens,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = effectiveBtnText
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isButtonClickedState) "Button State: OVER / CLICKED" else "Tap/Hold to Test Button Clicked Color",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. COLOR CUSTOMIZER TABS
                Text(
                    text = "CUSTOMIZE COLOR PALETTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyanAccent
                )

                val categories = listOf(
                    "Background" to bgHex,
                    "Foreground" to fgHex,
                    "Accent" to accentHex,
                    "Button" to btnHex,
                    "Button Clicked" to btnClickedHex
                )

                ScrollableTabRow(
                    selectedTabIndex = activeColorCategory,
                    containerColor = Color(0xFF090E1A),
                    contentColor = CyanAccent,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeColorCategory]),
                            color = CyanAccent
                        )
                    }
                ) {
                    categories.forEachIndexed { index, (label, hex) ->
                        Tab(
                            selected = activeColorCategory == index,
                            onClick = { activeColorCategory = index },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(UserSettingsManager.parseHexColor(hex))
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (activeColorCategory == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                    }
                }

                // Swatches and Hex Input for Active Category
                val (activeHex, swatches, onHexChange) = when (activeColorCategory) {
                    0 -> Triple(bgHex, UserSettingsManager.SWATCHES_BACKGROUND) { h: String -> bgHex = h; selectedThemeId = "custom" }
                    1 -> Triple(fgHex, UserSettingsManager.SWATCHES_FOREGROUND) { h: String -> fgHex = h; selectedThemeId = "custom" }
                    2 -> Triple(accentHex, UserSettingsManager.SWATCHES_ACCENT) { h: String -> accentHex = h; selectedThemeId = "custom" }
                    3 -> Triple(btnHex, UserSettingsManager.SWATCHES_BUTTON) { h: String -> btnHex = h; selectedThemeId = "custom" }
                    else -> Triple(btnClickedHex, UserSettingsManager.SWATCHES_BUTTON_CLICKED) { h: String -> btnClickedHex = h; selectedThemeId = "custom" }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF090E1A))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Curated Swatches:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        swatches.forEach { hex ->
                            val color = UserSettingsManager.parseHexColor(hex)
                            val isSelected = activeHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onHexChange(hex) }
                                    .testTag("swatch_${hex.removePrefix("#")}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = UserSettingsManager.getContrastingTextColor(color),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Custom Hex Input Field
                    OutlinedTextField(
                        value = activeHex,
                        onValueChange = { newVal ->
                            val sanitized = if (!newVal.startsWith("#")) "#$newVal" else newVal
                            if (sanitized.length <= 9) {
                                onHexChange(sanitized)
                            }
                        },
                        label = { Text("Hex Code (#RRGGBB)", fontSize = 11.sp) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(UserSettingsManager.parseHexColor(activeHex))
                                    .border(1.dp, Color(0xFF64748B), CircleShape)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hex_color_input")
                    )

                    // Text Contrast status badge
                    val activeColor = UserSettingsManager.parseHexColor(activeHex)
                    val contrastText = UserSettingsManager.getContrastingTextColor(activeColor)
                    val isLight = contrastText == Color(0xFF0F172A)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WCAG Contrast Safe:",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E293B))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isLight) "Light Surface → Dark Text Active" else "Dark Surface → White Text Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialSettings.copy(
                        themePresetId = selectedThemeId,
                        backgroundColorHex = bgHex,
                        foregroundColorHex = fgHex,
                        accentColorHex = accentHex,
                        buttonColorHex = btnHex,
                        buttonClickedColorHex = btnClickedHex
                    )
                    onSaveTheme(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                modifier = Modifier.testTag("save_theme_button")
            ) {
                Text("Save & Apply Theme", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val def = UserSettingsManager.PREDEFINED_THEMES[0]
                        selectedThemeId = def.id
                        bgHex = def.backgroundColorHex
                        fgHex = def.foregroundColorHex
                        accentHex = def.accentColorHex
                        btnHex = def.buttonColorHex
                        btnClickedHex = def.buttonClickedColorHex
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun ColorDot(color: Color, description: String) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color(0xFF334155), CircleShape)
    )
}
