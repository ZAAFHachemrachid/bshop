package com.example.b_shop.ui.product;

import com.example.b_shop.data.repositories.UserRepository;

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
import androidx.viewpager2.widget.ViewPager2;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.R;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.data.repositories.ReviewRepository;
import com.example.b_shop.databinding.FragmentProductDetailsBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private ProductDetailsViewModel viewModel;
    private ProductImageAdapter imageAdapter;
    private ProductReviewAdapter reviewAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        
        // Load product details after ViewModel is initialized
        if (getArguments() != null) {
            int productId = getArguments().getInt("productId", -1);
            if (productId != -1) {
                viewModel.loadProductDetails(productId);
            }
        }
        
        setupToolbar();
        setupImageGallery();
        setupReviewsList();
        setupClickListeners();
        observeViewModel();
    }

    private void setupViewModel() {
        // Get application instance
        BShopApplication application = (BShopApplication) requireActivity().getApplication();
        
        // Get repositories
        ProductRepository productRepository = application.getProductRepository();
        ReviewRepository reviewRepository = application.getReviewRepository();
        UserRepository userRepository = application.getUserRepository();
        
        // Create factory
        ProductDetailsViewModel.Factory factory = new ProductDetailsViewModel.Factory(
            productRepository,
            reviewRepository,
            userRepository
        );
        
        // Get ViewModel instance using factory
        viewModel = new ViewModelProvider(this, factory).get(ProductDetailsViewModel.class);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> 
            Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupImageGallery() {
        imageAdapter = new ProductImageAdapter();
        binding.imageGalleryPager.setAdapter(imageAdapter);
        binding.imageGalleryPager.setOffscreenPageLimit(1);

        // Setup image indicator
        new TabLayoutMediator(binding.imageIndicator, binding.imageGalleryPager,
            (tab, position) -> {
                // No title needed for dots
            }).attach();

        // Page change callback for parallax effect
        binding.imageGalleryPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update current image position
                viewModel.setCurrentImagePosition(position);
            }
        });
    }

    private void setupReviewsList() {
        reviewAdapter = new ProductReviewAdapter();
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);
        binding.reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupClickListeners() {
        binding.buttonAddToCart.setOnClickListener(v -> {
            viewModel.addToCart(1);
        });

        binding.buttonFavorite.setOnClickListener(v -> {
            viewModel.toggleFavorite();
        });
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
            }
        });

        // Observe product details
        viewModel.getProduct().observe(getViewLifecycleOwner(), product -> {
            if (product != null) {
                binding.productTitle.setText(product.getName());
                binding.productPrice.setText(String.format("$%.2f", product.getPrice()));
                binding.productDescription.setText(product.getDescription());
                binding.ratingBar.setRating(product.getAverageRating());
                binding.ratingCount.setText(String.format("(%d)", product.getReviewCount()));
                
                // Update images
                imageAdapter.submitList(product.getImages());
            }
        });

        // Observe reviews
        viewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            reviewAdapter.submitList(reviews);
        });

        // Observe favorite state
        viewModel.isFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
            binding.buttonFavorite.setIcon(getResources().getDrawable(
                isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border,
                requireContext().getTheme()
            ));
        });

        // Observe cart operation status
        viewModel.getAddToCartStatus().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Snackbar.make(binding.getRoot(), 
                    R.string.added_to_cart,
                    Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}