package com.recodex.miuisettings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import com.recodex.miuisettings.domain.model.LaunchResult
import com.recodex.miuisettings.domain.util.SettingCategories
import com.recodex.miuisettings.presentation.LaunchSettingViewModel
import com.recodex.miuisettings.presentation.SettingsAdapter
import com.recodex.miuisettings.presentation.SettingsListViewModel
import com.recodex.miuisettings.presentation.model.SettingSummary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

import com.google.android.material.textfield.TextInputEditText

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val listViewModel: SettingsListViewModel by viewModels()
    private val launchViewModel: LaunchSettingViewModel by viewModels()
    private lateinit var adapter: SettingsAdapter
    
    private lateinit var progressBar: ProgressBar
    private lateinit var tvDeviceModel: TextView
    private lateinit var tvDeviceDetails: TextView
    private lateinit var tvCompatibility: TextView
    private lateinit var etSearch: TextInputEditText
    private lateinit var disclaimerCard: MaterialCardView
    private lateinit var emptyStateLayout: View
    
    private var allSettings: List<SettingSummary> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable Edge-to-Edge
        // Note: For full edge-to-edge, ensure themes.xml has transparent status/nav bars
        // WindowCompat.setDecorFitsSystemWindows(window, false) is deprecated in favor of enableEdgeToEdge() 
        // if using androidx.activity 1.8.0+, but we can just use the ViewCompat approach for compatibility.
        
        setContentView(R.layout.activity_main)

        setupViews()
        setupDisclaimer()
        setupObservers()
        setupWindowInsets()
        
        listViewModel.load()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top)
            insets
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycler_view_settings)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = bars.bottom + 16) // Add extra padding for better UX
            insets
        }
    }

    private fun setupViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_settings)
        adapter = SettingsAdapter { item ->
            launchViewModel.launch(item.id)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        progressBar = findViewById(R.id.progress_bar)
        tvDeviceModel = findViewById(R.id.tv_device_model)
        tvDeviceDetails = findViewById(R.id.tv_device_details)
        tvCompatibility = findViewById(R.id.tv_compatibility_summary)
        etSearch = findViewById(R.id.et_search)
        disclaimerCard = findViewById(R.id.card_disclaimer)
        emptyStateLayout = findViewById(R.id.layout_empty_state)
        
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_categories)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: View.NO_ID
            val category = when (checkedId) {
                R.id.chip_network -> SettingCategories.NETWORK
                R.id.chip_display -> SettingCategories.DISPLAY
                R.id.chip_battery -> SettingCategories.BATTERY
                R.id.chip_security -> SettingCategories.SECURITY
                else -> null
            }
            listViewModel.load(category)
            etSearch.text?.clear()
        }
        
        etSearch.addTextChangedListener { text ->
             filterList(text?.toString() ?: "")
        }
    }

    private fun setupDisclaimer() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val accepted = prefs.getBoolean("disclaimer_accepted", false)
        if (!accepted) {
            disclaimerCard.visibility = View.VISIBLE
            findViewById<Button>(R.id.btn_disclaimer_ok).setOnClickListener {
                disclaimerCard.visibility = View.GONE
                prefs.edit().putBoolean("disclaimer_accepted", true).apply()
            }
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    listViewModel.state.collect { state ->
                        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                        
                        state.deviceProfile?.let {
                            tvDeviceModel.text = it.manufacturer.replaceFirstChar { char -> char.uppercase() }
                            val osName = if (it.isHyperOs) "HyperOS" else "MIUI"
                            val version = it.miuiVersion ?: "Unknown"
                            tvDeviceDetails.text = "Android ${it.sdkInt} | $osName $version"
                        }
                        
                        state.compatibilityReport?.let {
                            tvCompatibility.text = "Disponíveis: ${it.compatibleSettings} de ${it.totalSettings}"
                        }
                        
                        allSettings = state.settings
                        filterList(etSearch.text?.toString() ?: "")
                        
                        state.errorMessage?.let {
                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                launch {
                    launchViewModel.state.collect { state ->
                        if (!state.isLaunching && state.lastResult != null) {
                             when (val result = state.lastResult) {
                                 is LaunchResult.Launched -> {
                                     // Success
                                 }
                                 is LaunchResult.Unsupported -> {
                                     Toast.makeText(this@MainActivity, "Não suportado: ${result.reason}", Toast.LENGTH_SHORT).show()
                                 }
                                 LaunchResult.NotFound -> {
                                     Toast.makeText(this@MainActivity, "Configuração não encontrada", Toast.LENGTH_SHORT).show()
                                 }
                                 is LaunchResult.Failed -> {
                                     Toast.makeText(this@MainActivity, "Erro ao abrir: ${result.throwable.message}", Toast.LENGTH_SHORT).show()
                                 }
                                 else -> {}
                             }
                        }
                    }
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                listViewModel.syncCatalog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterList(query: String) {
        val filtered = if (query.isBlank()) {
            allSettings
        } else {
            allSettings.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        emptyStateLayout.visibility = if (filtered.isEmpty() && !progressBar.isShown) View.VISIBLE else View.GONE
    }
}
