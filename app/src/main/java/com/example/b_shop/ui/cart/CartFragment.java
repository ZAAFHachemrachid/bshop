package com.example.b_shop.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.b_shop.R;
import com.example.b_shop.databinding.FragmentCartBinding;
import com.example.b_shop.domain.usecases.CheckoutUseCase.CheckoutState;
import com.example.b_shop.ui.cart.CartAdapter.CartItemListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Locale;

public class CartFragment extends Fragment implements CartItemListener {

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        setupViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
            Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        binding.recyclerCart.setAdapter(adapter);
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupButtons() {
        binding.buttonBrowse.setOnClickListener(v ->
            Navigation.findNavController(v).navigateUp());

        binding.buttonCheckout.setOnClickListener(v ->
            viewModel.checkout());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CartViewModel.class);
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(CartViewModel.CartUIState state) {
        // Update loading state
        binding.loading.setVisibility(state.isLoading() ? View.VISIBLE : View.GONE);

        // Update empty state
        binding.emptyState.setVisibility(state.isEmpty() ? View.VISIBLE : View.GONE);
        binding.recyclerCart.setVisibility(state.isEmpty() ? View.GONE : View.VISIBLE);
        binding.checkoutContainer.setVisibility(state.isEmpty() ? View.GONE : View.VISIBLE);

        // Update cart items
        if (state.getCartState() != null) {
            adapter.submitList(state.getCartState().getItems());
            binding.textTotal.setText(currencyFormatter.format(state.getTotal()));
        }

        // Handle errors
        if (state.getError() != null) {
            showError(state.getError());
        }

        // Handle checkout state
        handleCheckoutState(state.getCheckoutState(), state.getCheckoutError());
    }

    private void handleCheckoutState(CheckoutState checkoutState, String error) {
        if (checkoutState == null) return;

        binding.buttonCheckout.setEnabled(checkoutState != CheckoutState.PROCESSING);

        switch (checkoutState) {
            case PROCESSING:
                // Show loading indicator
                break;
            case COMPLETED:
                showCheckoutSuccess();
                break;
            case ERROR:
                if (error != null) {
                    showError(error);
                }
                break;
        }
    }

    private void showCheckoutSuccess() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.checkout_success_title)
            .setMessage(R.string.checkout_success_message)
            .setPositiveButton(R.string.view_orders, (dialog, which) -> {
                // TODO: Navigate to orders screen
            })
            .setNegativeButton(R.string.continue_shopping, (dialog, which) ->
                Navigation.findNavController(requireView()).navigateUp())
            .setCancelable(false)
            .show();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            showClearCartDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearCartDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_cart_title)
            .setMessage(R.string.clear_cart_message)
            .setPositiveButton(R.string.clear, (dialog, which) ->
                viewModel.clearCart())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onUpdateQuantity(int cartItemId, int quantity) {
        viewModel.updateQuantity(cartItemId, quantity);
    }

    @Override
    public void onRemoveItem(int cartItemId) {
        viewModel.removeItem(cartItemId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}