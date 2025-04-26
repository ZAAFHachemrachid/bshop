package com.example.b_shop.ui.category;

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
import com.example.b_shop.databinding.FragmentCategoryProductsBinding;
import com.example.b_shop.ui.adapters.ProductAdapter;

public class CategoryProductsFragment extends Fragment {
    
    private FragmentCategoryProductsBinding binding;
    private CategoryProductsViewModel viewModel;
    private ProductAdapter productAdapter;
    
    private static final String ARG_CATEGORY_ID = "categoryId";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentCategoryProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Bundle args = getArguments();
        if (args == null || !args.containsKey(ARG_CATEGORY_ID)) {
            throw new IllegalArgumentException("Must pass categoryId argument");
        }
        int categoryId = args.getInt(ARG_CATEGORY_ID);
        
        viewModel = new ViewModelProvider(this).get(CategoryProductsViewModel.class);
        viewModel.setCategoryId(categoryId);
        
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(product -> {
            Bundle args = new Bundle();
            args.putInt("productId", product.getProductId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_category_products_to_product_details, args);
        });
        
        binding.productsRecyclerView.setAdapter(productAdapter);
        binding.productsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void observeViewModel() {
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            productAdapter.submitList(products);
            binding.progressBar.setVisibility(View.GONE);
            
            if (products.isEmpty()) {
                binding.emptyView.setVisibility(View.VISIBLE);
            } else {
                binding.emptyView.setVisibility(View.GONE);
            }
        });

        viewModel.getCategoryName().observe(getViewLifecycleOwner(), categoryName -> {
            if (categoryName != null) {
                requireActivity().setTitle(categoryName);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}