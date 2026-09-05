package com.xaiworld.expensetracker.sync

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.xaiworld.expensetracker.data.Expense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Peer-to-peer sync between the couple's two phones using Google's Nearby Connections API.
 *
 * Why Nearby Connections: it is built for exactly this "two devices close to each other"
 * scenario. It transparently picks whichever of Bluetooth, BLE, or a local Wi-Fi hotspot works,
 * needs no internet connection, no server, and no account, and both phones can advertise and
 * discover at the same time so either person can hit "Sync" first.
 *
 * The sync protocol itself, layered on top of that transport, is deliberately simple: on
 * connecting, each phone sends its *entire* expense list (including delete tombstones) as one
 * JSON payload; the receiver merges it in with last-write-wins per expense id (see
 * [com.xaiworld.expensetracker.data.ExpenseRepository.mergeIncoming]). Doing a full-state
 * exchange both ways every time means the two phones always converge to the same list
 * regardless of which one made which change, without needing a central source of truth.
 */
class NearbySyncManager(
    private val context: Context,
    private val getLocalExpenses: suspend () -> List<Expense>,
    private val onExpensesReceived: suspend (List<Expense>) -> Int
) {
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val strategy = Strategy.P2P_CLUSTER
    private val connectedEndpoints = mutableSetOf<String>()

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type != Payload.Type.BYTES) return
            val bytes = payload.asBytes() ?: return
            scope.launch {
                try {
                    val received = ExpenseJson.decodeList(String(bytes, Charsets.UTF_8))
                    val applied = onExpensesReceived(received)
                    _state.value = SyncState.Success(applied)
                } catch (e: Exception) {
                    _state.value = SyncState.Error("Couldn't read data from the other phone: ${e.message}")
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // No progress UI for these small payloads; nothing to do here.
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Every device running this app belongs to the same couple, so auto-accept.
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                connectedEndpoints += endpointId
                _state.value = SyncState.Connected(endpointId)
                stopAdvertisingAndDiscovery()
                sendLocalExpenses(endpointId)
            } else {
                _state.value = SyncState.Error("Connection didn't go through, try again.")
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints -= endpointId
        }
    }

    private var localDisplayName: String = "Phone"

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            _state.value = SyncState.FoundDevice(info.endpointName)
            connectionsClient.requestConnection(localDisplayName, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            // Nothing to clean up: we only ever act once an endpoint is found.
        }
    }

    /** Starts advertising *and* discovering at the same time, so either phone can find the other. */
    fun startSync(localDisplayName: String) {
        this.localDisplayName = localDisplayName
        _state.value = SyncState.Searching

        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(
            localDisplayName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnFailureListener {
            _state.value = SyncState.Error("Couldn't start advertising: ${it.message}")
        }

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnFailureListener {
            _state.value = SyncState.Error("Couldn't start looking for the other phone: ${it.message}")
        }
    }

    fun stopSync() {
        connectionsClient.stopAllEndpoints()
        stopAdvertisingAndDiscovery()
        connectedEndpoints.clear()
        _state.value = SyncState.Idle
    }

    private fun sendLocalExpenses(endpointId: String) {
        scope.launch {
            val json = ExpenseJson.encodeList(getLocalExpenses())
            connectionsClient.sendPayload(endpointId, Payload.fromBytes(json.toByteArray(Charsets.UTF_8)))
        }
    }

    private fun stopAdvertisingAndDiscovery() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
    }

    companion object {
        private const val SERVICE_ID = "com.xaiworld.expensetracker.SYNC_SERVICE"
    }
}
