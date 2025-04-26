package com.example.b_shop.ui.wishlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.b_shop.R;
import com.example.b_shop.databinding.FragmentWishListBinding;
import com.example.b_shop.ui.adapters.ProductAdapter;
import com.google.android.material.snackbar.Snackbar;

public class WishListFragment extends Fragment {

    private FragmentWishListBinding binding;
    private WishListViewModel viewModel;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentWishListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(product -> {
            Bundle args = new Bundle();
            args.putInt("productId", product.getProductId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_wishlist_to_product_details, args);
        });

        binding.wishListRecyclerView.setAdapter(adapter);
        binding.wishListRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(WishListViewModel.class);
        
        viewModel.getWishListProducts().observe(getViewLifecycleOwner(), products -> {
            adapter.submitList(products);
            binding.emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            binding.wishListRecyclerView.setVisibility(products.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}