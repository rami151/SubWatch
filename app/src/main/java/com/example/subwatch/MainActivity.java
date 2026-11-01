package com.example.subwatch;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.subwatch.databinding.ActivityMainBinding;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    /** IDs of the three top-level (bottom-nav) destinations. */
    private static final Set<Integer> TOP_LEVEL_IDS = Set.of(
            R.id.dashboardFragment,
            R.id.subscriptionsFragment,
            R.id.statsFragment
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Navigation setup
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        navController = navHost.getNavController();

        // Top-level destinations: no back arrow, show gear icon
        AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(TOP_LEVEL_IDS).build();

        // Wire toolbar + collapsing toolbar to NavController
        NavigationUI.setupWithNavController(
                binding.collapsingToolbar, binding.toolbar, navController, appBarConfig);

        // Wire bottom nav to NavController
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        // React to every destination change
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            boolean isTopLevel = TOP_LEVEL_IDS.contains(destId);
            boolean isFormulaire = destId == R.id.formulaireFragment;
            boolean isSettings = destId == R.id.settingsFragment;
            boolean isFiche = destId == R.id.ficheFragment;

            // --- Bottom bar visibility ---
            // Hide on Formulaire and Paramètres
            binding.bottomNav.setVisibility(
                    (isFormulaire || isSettings) ? View.GONE : View.VISIBLE);

            // --- Toolbar navigation icon ---
            binding.toolbar.setNavigationIcon(null);
            binding.toolbar.setNavigationOnClickListener(null);

            if (isTopLevel) {
                // Gear icon on the three main tabs
                binding.toolbar.setNavigationIcon(null);
                binding.toolbar.getMenu().clear();
                binding.toolbar.inflateMenu(R.menu.toolbar_settings);
                binding.toolbar.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_settings) {
                        navController.navigate(R.id.settingsFragment);
                        return true;
                    }
                    return false;
                });
            } else if (isFormulaire) {
                // Close (X) icon
                binding.toolbar.getMenu().clear();
                binding.toolbar.setNavigationIcon(R.drawable.ic_close);
                binding.toolbar.setNavigationContentDescription(R.string.cd_close);
                binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
            } else if (isFiche || isSettings) {
                // Back arrow
                binding.toolbar.getMenu().clear();
                binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                binding.toolbar.setNavigationContentDescription(R.string.cd_back);
                binding.toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
            }

            // Expand the app bar when switching destinations
            binding.appBarLayout.setExpanded(true, true);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}