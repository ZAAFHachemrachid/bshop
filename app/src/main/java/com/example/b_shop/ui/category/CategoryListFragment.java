package com.example.b_shop.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.b_shop.R;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.databinding.FragmentCategoryListBinding;
import com.example.b_shop.ui.adapters.CategoryAdapter;
import com.example.b_shop.ui.adapters.CategoryProductsAdapter;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;

public class CategoryListFragment extends Fragment {
    
    private FragmentCategoryListBinding binding;
    private CategoryListViewModel viewModel;
    private CategoryAdapter categoryAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        observeViewModel();
    }

    private void setupViewModel() {
        AppDatabase database = AppDatabase.getInstance(requireContext());
        CategoryRepository categoryRepository = new CategoryRepository(database.categoryDao());
        ProductRepository productRepository = new ProductRepository(database.productDao(), database.userDao());
        
        CategoryListViewModel.Factory factory = new CategoryListViewModel.Factory(
            categoryRepository, productRepository);
        
        viewModel = new ViewModelProvider(this, factory).get(CategoryListViewModel.class);
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(
            // Category click listener
            category -> {
                Bundle args = new Bundle();
                args.putInt("categoryId", category.getCategoryId());
                args.putString("categoryName", category.getName());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_category_list_to_category_products, args);
            },
            // Product click listener
            product -> {
                Bundle args = new Bundle();
                args.putInt("categoryId", product.getCategoryId());
                args.putString("categoryName", "Products"); // Default category name
                args.putInt("selectedProductId", product.getProductId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_category_list_to_category_products, args);
            }
        );
        
        binding.categoriesList.setAdapter(categoryAdapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.submitList(categories);
            loadProductsForCategories(categories);
            updateEmptyState(categories);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void loadProductsForCategories(java.util.List<Category> categories) {
        for (Category category : categories) {
            viewModel.getProductsForCategory(category.getCategoryId())
                    .observe(getViewLifecycleOwner(), products -> 
                        categoryAdapter.setProductsForCategory(category.getCategoryId(), products));
        }
    }

    private void updateEmptyState(java.util.List<Category> categories) {
        binding.emptyView.setVisibility(
            categories != null && categories.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}