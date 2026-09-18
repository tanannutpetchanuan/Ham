package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.HamLogRepository
import com.example.ui.HamLogViewModel
import com.example.ui.HamLogViewModelFactory
import com.example.ui.components.UtcClockBar
import com.example.ui.screens.CallbookScreen
import com.example.ui.screens.LogQsoScreen
import com.example.ui.screens.LogbookScreen
import com.example.ui.screens.StatsExportScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RadioAmberPrimary
import com.example.ui.theme.RadioCyanSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ThaiHamLogApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThaiHamLogApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val database = remember { AppDatabase.getDatabase(context, coroutineScope) }
    val repository = remember { HamLogRepository(database.qsoDao(), database.callsignDao()) }
    val viewModel: HamLogViewModel = viewModel(
        factory = HamLogViewModelFactory(repository)
    )

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val allQsos by viewModel.allQsos.collectAsStateWithLifecycle()
    val filteredQsos by viewModel.filteredQsos.collectAsStateWithLifecycle()
    val logSearchQuery by viewModel.logSearchQuery.collectAsStateWithLifecycle()
    val selectedBandFilter by viewModel.selectedBandFilter.collectAsStateWithLifecycle()
    val selectedModeFilter by viewModel.selectedModeFilter.collectAsStateWithLifecycle()

    val filteredCallsigns by viewModel.filteredCallsigns.collectAsStateWithLifecycle()
    val dirSearchQuery by viewModel.directorySearchQuery.collectAsStateWithLifecycle()
    val selectedAreaFilter by viewModel.selectedAreaFilter.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = RadioAmberPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = "THAI HAM LOG",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "HS / E2 Radio Operator Logbook",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Log QSO") },
                    label = { Text("Log QSO") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = RadioAmberPrimary
                    ),
                    modifier = Modifier.testTag("nav_log_qso")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Logbook") },
                    label = { Text("Logbook (${allQsos.size})") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = RadioAmberPrimary
                    ),
                    modifier = Modifier.testTag("nav_logbook")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Callbook") },
                    label = { Text("Callbook") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = RadioAmberPrimary
                    ),
                    modifier = Modifier.testTag("nav_callbook")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Metrics") },
                    label = { Text("Stats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = RadioAmberPrimary
                    ),
                    modifier = Modifier.testTag("nav_stats")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live UTC / ICT Clock and operating status header
            UtcClockBar(
                activeBand = formState.band,
                activeMode = formState.mode,
                totalQsos = allQsos.size,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Content according to selected tab
            when (selectedTabIndex) {
                0 -> LogQsoScreen(
                    formState = formState,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> LogbookScreen(
                    qsos = filteredQsos,
                    searchQuery = logSearchQuery,
                    selectedBand = selectedBandFilter,
                    selectedMode = selectedModeFilter,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> CallbookScreen(
                    callsigns = filteredCallsigns,
                    searchQuery = dirSearchQuery,
                    selectedArea = selectedAreaFilter,
                    viewModel = viewModel,
                    onSelectForLog = { entry ->
                        viewModel.onCallsignChanged(entry.callsign)
                        if (entry.radioId.isNotEmpty()) {
                            viewModel.onRadioIdChanged(entry.radioId)
                        }
                        selectedTabIndex = 0
                    },
                    modifier = Modifier.fillMaxSize()
                )
                3 -> StatsExportScreen(
                    qsos = allQsos,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Retain Greeting for Robolectric and Screenshot test compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
