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
import com.example.b_shop.databinding.FragmentCategoryListBinding;
import com.example.b_shop.ui.adapters.CategoryAdapter;

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
        
        viewModel = new ViewModelProvider(this).get(CategoryListViewModel.class);
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(category -> {
            Bundle args = new Bundle();
            args.putInt("categoryId", category.getCategoryId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_categories_to_products, args);
        });
        
        binding.categoriesGrid.setAdapter(categoryAdapter);
        binding.categoriesGrid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.submitList(categories);
            binding.progressBar.setVisibility(View.GONE);
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