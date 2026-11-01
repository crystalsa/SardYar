package ir.sardyar.coldstorage

import android.app.Application
import ir.sardyar.coldstorage.data.repository.InventoryRepositoryImpl
import ir.sardyar.coldstorage.domain.repository.InventoryRepository

class SardYarApp : Application() {

    lateinit var repository: InventoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = InventoryRepositoryImpl(applicationContext)
    }
}
