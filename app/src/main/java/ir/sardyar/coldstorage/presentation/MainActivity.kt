package ir.sardyar.coldstorage.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ir.sardyar.coldstorage.core.theme.SardYarTheme
import ir.sardyar.coldstorage.data.repository.InventoryRepositoryImpl
import ir.sardyar.coldstorage.presentation.screens.MainScreen

class MainActivity : ComponentActivity() {

    private val repository = InventoryRepositoryImpl()

    private val viewModel: InventoryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InventoryViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SardYarTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
