package com.example.b_shop.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.b_shop.R;
import com.example.b_shop.databinding.ActivityMainBinding;
import com.example.b_shop.utils.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private UserManager userManager;
    private Menu optionsMenu;
    private CountDownTimer adminSessionTimer;
    private TextView tvAdminSessionTimer;
    private static final long ADMIN_SESSION_CHECK_INTERVAL = 1000; // 1 second

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userManager = UserManager.getInstance(this);
        tvAdminSessionTimer = binding.tvAdminSessionTimer;
        
        setupNavigation();
        setupAdminSessionTimer();
    }

    private void setupNavigation() {
        BottomNavigationView navView = binding.navView;
        
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
            R.id.navigation_home,
            R.id.navigation_categories,
            R.id.navigation_wishlist,
            R.id.navigation_cart,
            R.id.navigation_profile
        ).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.adminDashboardFragment) {
                validateAdminAccess();
                showAdminSessionTimer();
            } else {
                hideAdminSessionTimer();
            }
        });
    }

    private void setupAdminSessionTimer() {
        adminSessionTimer = new CountDownTimer(
            TimeUnit.MINUTES.toMillis(UserManager.getAdminSessionTimeoutMinutes()),
            ADMIN_SESSION_CHECK_INTERVAL
        ) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!userManager.isCurrentUserAdmin()) {
                    hideAdminSessionTimer();
                    return;
                }
                updateSessionTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                handleAdminSessionTimeout();
            }
        };
    }

    private void updateSessionTimerDisplay(long millisUntilFinished) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                TimeUnit.MINUTES.toSeconds(minutes);
        
        String timeLeft = String.format(Locale.getDefault(),
            "Admin Session: %02d:%02d", minutes, seconds);
        tvAdminSessionTimer.setText(timeLeft);

        // Change color to red when less than 5 minutes remaining
        if (minutes < 5) {
            tvAdminSessionTimer.setTextColor(getColor(android.R.color.holo_red_light));
        }
    }

    private void showAdminSessionTimer() {
        if (userManager.isCurrentUserAdmin()) {
            tvAdminSessionTimer.setVisibility(View.VISIBLE);
            adminSessionTimer.start();
        }
    }

    private void hideAdminSessionTimer() {
        tvAdminSessionTimer.setVisibility(View.GONE);
        if (adminSessionTimer != null) {
            adminSessionTimer.cancel();
        }
    }

    private void handleAdminSessionTimeout() {
        hideAdminSessionTimer();
        Toast.makeText(this, R.string.admin_session_expired, Toast.LENGTH_SHORT).show();
        navController.navigate(R.id.action_adminDashboard_to_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        updateMenuVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.navigation_admin_dashboard) {
            if (validateAdminAccess()) {
                navController.navigate(R.id.action_profile_to_adminDashboard);
                return true;
            }
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private boolean validateAdminAccess() {
        if (!userManager.isCurrentUserAdmin()) {
            Toast.makeText(this, R.string.admin_access_required, Toast.LENGTH_SHORT).show();
            navController.navigateUp();
            return false;
        }

        if (!userManager.validateAdminSession()) {
            Toast.makeText(this, R.string.admin_session_expired, Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.auth_activity);
            return false;
        }

        return true;
    }

    private void updateMenuVisibility() {
        if (optionsMenu != null) {
            MenuItem adminItem = optionsMenu.findItem(R.id.navigation_admin_dashboard);
            if (adminItem != null) {
                adminItem.setVisible(userManager.isCurrentUserAdmin());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMenuVisibility();
        if (userManager.isCurrentUserAdmin() && 
            navController.getCurrentDestination().getId() == R.id.adminDashboardFragment) {
            showAdminSessionTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideAdminSessionTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adminSessionTimer != null) {
            adminSessionTimer.cancel();
        }
        binding = null;
    }
}