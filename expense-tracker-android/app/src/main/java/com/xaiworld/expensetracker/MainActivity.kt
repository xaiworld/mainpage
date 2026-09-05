package com.xaiworld.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.xaiworld.expensetracker.sync.NearbyPermissions
import com.xaiworld.expensetracker.ui.ExpenseApp
import com.xaiworld.expensetracker.ui.ExpenseViewModel
import com.xaiworld.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    private var onPermissionsResolved: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            onPermissionsResolved?.invoke()
        }
        onPermissionsResolved = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackerTheme {
                ExpenseApp(
                    viewModel = viewModel,
                    onRequestSync = { requestNearbyPermissionsThen { viewModel.startSync() } }
                )
            }
        }
    }

    private fun requestNearbyPermissionsThen(action: () -> Unit) {
        if (NearbyPermissions.allGranted(this)) {
            action()
        } else {
            onPermissionsResolved = action
            permissionLauncher.launch(NearbyPermissions.required())
        }
    }
}
