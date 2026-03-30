package com.example.armoredage

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.armoredage.ui.theme.ArmoredAgeTheme

data class OpenSourceNotice(
    val title: String,
    val body: String
)

class OpenSourceLicensesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArmoredAgeTheme {
                val notices = remember { loadOpenSourceNotices(this) }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OpenSourceLicensesScreen(
                        notices = notices,
                        onBack = ::finish
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenSourceLicensesScreen(
    notices: List<OpenSourceNotice>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val title = androidx.compose.ui.res.stringResource(R.string.settings_open_source_menu_title)
    val backLabel = androidx.compose.ui.res.stringResource(R.string.action_back)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = backLabel
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.settings_open_source_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            itemsIndexed(notices, key = { index, _ -> index }) { index, notice ->
                OpenSourceNoticeCard(
                    notice = notice,
                    stateKey = "notice-$index",
                    onOpenReference = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }
        }
    }
}

@Composable
private fun OpenSourceNoticeCard(
    notice: OpenSourceNotice,
    stateKey: String,
    onOpenReference: (String) -> Unit
) {
    val isReferenceUrl = notice.body.startsWith("http://") || notice.body.startsWith("https://")
    var expanded by rememberSaveable(stateKey) { mutableStateOf(false) }
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = notice.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            if (isReferenceUrl) {
                TextButton(onClick = { onOpenReference(notice.body) }) {
                    Text(androidx.compose.ui.res.stringResource(R.string.action_open_reference))
                }
            } else {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded) {
                            androidx.compose.ui.res.stringResource(R.string.action_show_less)
                        } else {
                            androidx.compose.ui.res.stringResource(R.string.action_show_license)
                        }
                    )
                }
            }
        }
    }
}

private fun loadOpenSourceNotices(context: Context): List<OpenSourceNotice> {
    val metadata = context.resources.openRawResource(R.raw.third_party_license_metadata)
        .bufferedReader()
        .use { it.readLines() }
    val licenseBytes = context.resources.openRawResource(R.raw.third_party_licenses)
        .use { it.readBytes() }

    return metadata.mapNotNull { line ->
        val firstSpace = line.indexOf(' ')
        if (firstSpace <= 0) return@mapNotNull null

        val range = line.substring(0, firstSpace)
        val title = line.substring(firstSpace + 1).trim()
        val separator = range.indexOf(':')
        if (separator <= 0) return@mapNotNull null

        val offset = range.substring(0, separator).toIntOrNull() ?: return@mapNotNull null
        val length = range.substring(separator + 1).toIntOrNull() ?: return@mapNotNull null
        val body = licenseBytes.copyOfRange(offset, offset + length).toString(Charsets.UTF_8).trim()

        OpenSourceNotice(title = title, body = body)
    }.distinctBy { it.title to it.body }
        .sortedBy { it.title.lowercase() }
}
