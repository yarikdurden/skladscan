package com.skladscan.warehouse

import android.content.Context
import com.skladscan.warehouse.data.local.AppDatabase
import com.skladscan.warehouse.data.preferences.SettingsRepository
import com.skladscan.warehouse.data.remote.RemoteDataSource
import com.skladscan.warehouse.data.repository.InventoryRepository
import com.skladscan.warehouse.sync.SyncManager
import com.skladscan.warehouse.sync.SyncScheduler
import com.skladscan.warehouse.util.NetworkMonitor

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.build(appContext)
    val settings = SettingsRepository(appContext)
    val remote = RemoteDataSource()
    val networkMonitor = NetworkMonitor(appContext)
    val syncScheduler = SyncScheduler(appContext)

    val repository = InventoryRepository(
        db = database,
        productDao = database.productDao(),
        operationDao = database.operationDao(),
        settings = settings,
    )

    val syncManager = SyncManager(
        repository = repository,
        settings = settings,
        remote = remote,
    )
}
