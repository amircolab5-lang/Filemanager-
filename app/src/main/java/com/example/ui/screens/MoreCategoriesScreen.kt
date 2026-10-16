package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryType
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.MoreCategoryGrid

/**
 * More Categories Screen (Screenshot 3).
 * Shows all categories:
 * Audio, Videos, Images, APKs,
 * Documents, WhatsApp, Download, Bluetooth,
 * Messenger, Zips, T-Hide
 * Followed by "Collection of documents" with "+" button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreCategoriesScreen(
    onBack: () -> Unit,
    onCategoryClick: (CategoryType) -> Unit,
    onSearchClick: () -> Unit,
    onCollectionAddClick: () -> Unit,
    onOpenDirectory: (java.io.File) -> Unit = {},
    counts: Map<CategoryType, Int> = emptyMap(),
    whatsAppCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "More",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("more_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("more_screen_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            MoreCategoryGrid(
                onCategoryClick = onCategoryClick,
                counts = counts,
                whatsAppCount = whatsAppCount,
                onCollectionAddClick = onCollectionAddClick,
                onOpenDirectory = onOpenDirectory
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Non-intrusive AdMob Native Ad
            AdMobNativeAdCard(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
