package com.example.b_shop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.b_shop.R;
import com.example.b_shop.databinding.FragmentHomeBinding;
import com.example.b_shop.ui.adapters.CategoryAdapter;
import com.example.b_shop.ui.adapters.ProductAdapter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter featuredProductsAdapter;
    private ProductAdapter topRatedProductsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerViews();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerViews() {
        // Setup Categories RecyclerView
        categoryAdapter = new CategoryAdapter(category -> 
            Navigation.findNavController(requireView())
                    .navigate(HomeFragmentDirections
                            .actionHomeToCategoryProducts(category.getCategoryId())));
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
        binding.categoriesRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Setup Featured Products RecyclerView
        featuredProductsAdapter = new ProductAdapter(product ->
            Navigation.findNavController(requireView())
                    .navigate(HomeFragmentDirections
                            .actionHomeToProductDetails(product.getProductId())));
        binding.featuredProductsRecyclerView.setAdapter(featuredProductsAdapter);
        binding.featuredProductsRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Setup Top Rated Products RecyclerView
        topRatedProductsAdapter = new ProductAdapter(product ->
            Navigation.findNavController(requireView())
                    .navigate(HomeFragmentDirections
                            .actionHomeToProductDetails(product.getProductId())));
        binding.topRatedProductsRecyclerView.setAdapter(topRatedProductsAdapter);
        binding.topRatedProductsRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupObservers() {
        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.submitList(categories);
            binding.categoriesProgress.setVisibility(View.GONE);
            binding.categoriesRecyclerView.setVisibility(
                categories.isEmpty() ? View.GONE : View.VISIBLE);
            binding.noCategoriesText.setVisibility(
                categories.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe featured products
        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), products -> {
            featuredProductsAdapter.submitList(products);
            binding.featuredProgress.setVisibility(View.GONE);
            binding.featuredProductsRecyclerView.setVisibility(
                products.isEmpty() ? View.GONE : View.VISIBLE);
            binding.noFeaturedText.setVisibility(
                products.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe top rated products
        viewModel.getTopRatedProducts().observe(getViewLifecycleOwner(), products -> {
            topRatedProductsAdapter.submitList(products);
            binding.topRatedProgress.setVisibility(View.GONE);
            binding.topRatedProductsRecyclerView.setVisibility(
                products.isEmpty() ? View.GONE : View.VISIBLE);
            binding.noTopRatedText.setVisibility(
                products.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        binding.seeAllCategories.setOnClickListener(v ->
            Navigation.findNavController(v)
                    .navigate(R.id.navigation_categories));

        binding.searchBar.setOnClickListener(v ->
            Navigation.findNavController(v)
                    .navigate(R.id.navigation_search));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}