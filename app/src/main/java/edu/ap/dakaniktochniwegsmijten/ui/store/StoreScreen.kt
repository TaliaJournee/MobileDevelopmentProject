package edu.ap.dakaniktochniwegsmijten.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.dakaniktochniwegsmijten.R
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import edu.ap.dakaniktochniwegsmijten.ui.map.MapScreen
import edu.ap.dakaniktochniwegsmijten.ui.map.MapViewModel

@Composable
fun StoreScreen(
    modifier: Modifier = Modifier,
    storeViewModel: StoreViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by storeViewModel.uiState.collectAsStateWithLifecycle()
    var isChoosingLocation by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = isChoosingLocation) {
        isChoosingLocation = false
    }

    if (isChoosingLocation) {
        Column(modifier = modifier.fillMaxSize()) {
            TextButton(onClick = { isChoosingLocation = false }) {
                Text(stringResource(R.string.store_back_to_form))
            }

            MapScreen(
                modifier = Modifier.weight(1f),
                mapViewModel = mapViewModel
            )
        }
        return
    }

    val mapUiState by mapViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.merchant_home_title),
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = storeViewModel::updateName,
            label = { Text(stringResource(R.string.store_name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.category,
            onValueChange = storeViewModel::updateCategory,
            label = { Text(stringResource(R.string.store_category_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        val latitude = mapUiState.selectedLatitude
        val longitude = mapUiState.selectedLongitude

        if (latitude != null && longitude != null) {
            Text(
                text = stringResource(
                    R.string.store_selected_coordinates,
                    latitude,
                    longitude
                )
            )
        }

        Button(
            onClick = { isChoosingLocation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.store_choose_location))
        }
    }
}