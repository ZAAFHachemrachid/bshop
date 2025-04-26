package com.example.b_shop.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import com.example.b_shop.R;
import com.example.b_shop.databinding.FragmentHomeBinding;
import com.example.b_shop.ui.adapters.CategoryAdapter;
import com.example.b_shop.ui.adapters.FeaturedSliderAdapter;
import com.example.b_shop.ui.adapters.ProductAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {
    
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    private FeaturedSliderAdapter featuredSliderAdapter;
    private ProductAdapter topRatedProductsAdapter;
    private Timer autoScrollTimer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long AUTO_SCROLL_DELAY = 3000L; // 3 seconds

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
        setupSwipeRefresh();
        setupRecyclerViews();
        setupFeaturedSlider();
        setupObservers();
        setupListeners();
        startShimmer();
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshData();
            stopAutoScroll(); // Stop auto-scroll during refresh
            startAutoScroll(); // Restart auto-scroll after refresh
        });
    }

    private void setupRecyclerViews() {
        // Setup Categories RecyclerView
        categoryAdapter = new CategoryAdapter(
            // Category click listener
            category -> {
                Bundle args = new Bundle();
                args.putInt("categoryId", category.getCategoryId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_category_list, args);
            },
            // Product click listener
            product -> {
                Bundle args = new Bundle();
                args.putInt("productId", product.getProductId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_product_details, args);
            }
        );
        
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
        binding.categoriesRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        // Setup Top Rated Products RecyclerView
        topRatedProductsAdapter = new ProductAdapter(product -> {
            Bundle args = new Bundle();
            args.putInt("productId", product.getProductId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_product_details, args);
        });
        binding.topRatedProductsRecyclerView.setAdapter(topRatedProductsAdapter);
        binding.topRatedProductsRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupFeaturedSlider() {
        featuredSliderAdapter = new FeaturedSliderAdapter(product -> {
            Bundle args = new Bundle();
            args.putInt("productId", product.getProductId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_product_details, args);
        });

        binding.featuredProductsPager.setAdapter(featuredSliderAdapter);
        binding.featuredProductsPager.setOffscreenPageLimit(1);

        // Setup ViewPager2 indicator
        new TabLayoutMediator(binding.featuredProductsIndicator, binding.featuredProductsPager,
            (tab, position) -> {
                // No title needed for dots
            }).attach();

        // Start auto-scrolling
        startAutoScroll();
    }

    private void startAutoScroll() {
        if (autoScrollTimer != null) {
            autoScrollTimer.cancel();
        }

        autoScrollTimer = new Timer();
        autoScrollTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    if (binding != null && featuredSliderAdapter != null) {
                        int currentItem = binding.featuredProductsPager.getCurrentItem();
                        int totalItems = featuredSliderAdapter.getItemCount();
                        if (totalItems > 0) {
                            binding.featuredProductsPager.setCurrentItem(
                                (currentItem + 1) % totalItems, true);
                        }
                    }
                });
            }
        }, AUTO_SCROLL_DELAY, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {
        if (autoScrollTimer != null) {
            autoScrollTimer.cancel();
            autoScrollTimer = null;
        }
    }

    private void startShimmer() {
        binding.categoriesShimmer.startShimmer();
        binding.categoriesRecyclerView.setVisibility(View.GONE);
    }

    private void stopShimmer() {
        binding.categoriesShimmer.stopShimmer();
        binding.categoriesShimmer.setVisibility(View.GONE);
        binding.categoriesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void setupObservers() {
        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.submitList(categories);
            stopShimmer();
            binding.noProductsText.setVisibility(
                categories.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Observe featured products
        viewModel.getFeaturedProducts().observe(getViewLifecycleOwner(), products -> {
            featuredSliderAdapter.submitList(products);
            if (products.isEmpty()) {
                stopAutoScroll();
            } else {
                startAutoScroll();
            }
        });

        // Observe top rated products
        viewModel.getTopRatedProducts().observe(getViewLifecycleOwner(), products -> {
            topRatedProductsAdapter.submitList(products);
            binding.topRatedProductsRecyclerView.setVisibility(
                products.isEmpty() ? View.GONE : View.VISIBLE);
        });

        // Observe refresh state
        viewModel.getIsRefreshing().observe(getViewLifecycleOwner(), isRefreshing -> 
            binding.swipeRefresh.setRefreshing(isRefreshing));
    }

    private void setupListeners() {
        binding.seeAllCategories.setOnClickListener(v ->
            Navigation.findNavController(v)
                    .navigate(R.id.navigation_categories));

        binding.searchBar.setOnClickListener(v ->
            Navigation.findNavController(v)
                    .navigate(R.id.navigation_wishlist));
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
        binding = null;
    }
}