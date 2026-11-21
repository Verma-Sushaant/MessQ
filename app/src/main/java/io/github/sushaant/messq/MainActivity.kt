package io.github.sushaant.messq

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.sushaant.messq.data.local.datastore.UserPreferencesManager
import io.github.sushaant.messq.data.local.room.AppDatabase
import io.github.sushaant.messq.data.repository.AuthRepositoryImpl
import io.github.sushaant.messq.data.repository.CrowdRepositoryImpl
import io.github.sushaant.messq.data.repository.MenuRepositoryImpl
import io.github.sushaant.messq.data.repository.SlotRepositoryImpl
import io.github.sushaant.messq.ui.navigation.MessQNavHost
import io.github.sushaant.messq.ui.theme.MessQTheme
import io.github.sushaant.messq.viewmodel.AuthViewModel
import io.github.sushaant.messq.viewmodel.customer.StudentViewModel
import io.github.sushaant.messq.viewmodel.staff.StaffViewModel

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = UserPreferencesManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val authRepository = AuthRepositoryImpl(preferencesManager)
        val slotRepository = SlotRepositoryImpl()
        val menuRepository = MenuRepositoryImpl(database.menuDao())
        val crowdRepository = CrowdRepositoryImpl()

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                        AuthViewModel(
                            authRepository,
                            preferencesManager
                        ) as T
                    modelClass.isAssignableFrom(StudentViewModel::class.java) ->
                        StudentViewModel(authRepository, slotRepository, menuRepository, crowdRepository) as T
                    modelClass.isAssignableFrom(StaffViewModel::class.java) ->
                        StaffViewModel(slotRepository, crowdRepository, menuRepository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
        }

        setContent {
            MessQTheme {
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val studentViewModel: StudentViewModel = viewModel(factory = factory)
                val staffViewModel: StaffViewModel = viewModel(factory = factory)

                MessQNavHost(
                    authViewModel = authViewModel,
                    studentViewModel = studentViewModel,
                    staffViewModel = staffViewModel,
                    preferencesManager = preferencesManager
                )
            }
        }
    }
}