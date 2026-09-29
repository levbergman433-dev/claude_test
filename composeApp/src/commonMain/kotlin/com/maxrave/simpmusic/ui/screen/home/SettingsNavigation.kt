package com.maxrave.simpmusic.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.settings_search_empty
import simpmusic.composeapp.generated.resources.settings_search_hint

/**
 * `item(key)` that also records the key in [order], so a section's position in the list — what
 * scrolling to it needs — is its index there. Every item of the Settings list goes through this, or
 * the positions stop lining up.
 */
internal fun LazyListScope.sectionItem(
    order: MutableList<String>,
    key: String,
    content: @Composable LazyItemScope.() -> Unit,
) {
    order += key
    item(key = key, content = content)
}

/** The name of a group of rows inside a Settings page. */
@Composable
internal fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = typo().labelMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
internal fun SettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceContainerHigh)
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(SimpIcons.Search, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Box(modifier = Modifier.weight(1f).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(Res.string.settings_search_hint),
                    style = typo().bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = typo().bodyMedium.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                SimpIcons.Close,
                contentDescription = "Clear",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp).clip(RoundedCornerShape(50)).clickable { onQueryChange("") },
            )
        }
    }
}

/**
 * Every setting and page whose name or description holds all the words typed, in any order and any
 * case. A setting opens its page scrolled to its section.
 */
@Composable
internal fun SettingsSearchResults(
    query: String,
    onOpenCategory: (SettingsCategory) -> Unit,
    onOpenSetting: (SettingsSearchEntry) -> Unit,
) {
    val words = query.lowercase().split(' ').filter { it.isNotBlank() }
    fun matches(vararg texts: String?): Boolean {
        val haystack = texts.filterNotNull().joinToString(" ").lowercase()
        return words.all { it in haystack }
    }
    val sectionTitles = settingsSections.associate { it.key to stringResource(it.title) }
    val categories = SettingsCategory.entries.map { it to stringResource(it.title) }
    val matchingCategories = categories.filter { (_, title) -> matches(title) }
    val matchingSettings =
        settingsSearchIndex
            .map { entry ->
                Triple(entry, stringResource(entry.title), entry.subtitle?.let { stringResource(it) })
            }.filter { (entry, title, subtitle) ->
                matches(title, subtitle, sectionTitles[entry.section], categories.first { it.first == entry.category }.second)
            }

    if (matchingCategories.isEmpty() && matchingSettings.isEmpty()) {
        Text(
            text = stringResource(Res.string.settings_search_empty),
            style = typo().bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 4.dp),
        )
        return
    }
    Column(modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainer)) {
        val rows = matchingCategories.size + matchingSettings.size
        var shown = 0
        matchingCategories.forEach { (entry, title) ->
            SearchResultRow(title = title, path = null, showDivider = ++shown < rows) { onOpenCategory(entry) }
        }
        matchingSettings.forEach { (entry, title, _) ->
            val categoryTitle = categories.first { it.first == entry.category }.second
            val path = listOfNotNull(categoryTitle, sectionTitles[entry.section]).joinToString("  ›  ")
            SearchResultRow(title = title, path = path, showDivider = ++shown < rows) { onOpenSetting(entry) }
        }
    }
}

@Composable
private fun SearchResultRow(
    title: String,
    path: String?,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = typo().bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (path != null) {
                    Text(
                        text = path,
                        style = typo().bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                SimpIcons.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}

/** One pill per named section of the open page; tapping one scrolls to it. */
@Composable
internal fun SettingsSectionChips(
    sections: List<SettingsSectionInfo>,
    onSelect: (SettingsSectionInfo) -> Unit,
) {
    if (sections.size < 2) return
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sections.forEach { section ->
            Text(
                text = stringResource(section.title),
                style = typo().labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { onSelect(section) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}
