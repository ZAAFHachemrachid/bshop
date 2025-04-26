package com.example.b_shop.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.R;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.repositories.CategoryRepository;
import com.example.b_shop.databinding.FragmentCategoryListBinding;

public class CategoryListFragment extends Fragment {
    private FragmentCategoryListBinding binding;
    private CategoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentCategoryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get application instance
        BShopApplication application = (BShopApplication) requireActivity().getApplication();
        AppDatabase database = application.getDatabase();

        // Setup repositories
        CategoryRepository categoryRepository = application.getCategoryRepository();

        // Setup adapter with click handler
        adapter = new CategoryAdapter(category -> {
            Bundle args = new Bundle();
            args.putInt("categoryId", category.getCategoryId());
            args.putString("categoryName", category.getName());
            Navigation.findNavController(view)
                    .navigate(R.id.action_category_list_to_category_products, args);
        });

        // Setup RecyclerView
        binding.recyclerCategories.setAdapter(adapter);
        binding.recyclerCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Observe categories
        categoryRepository.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            adapter.submitList(categories);
            binding.progressBar.setVisibility(View.GONE);
            
            if (categories.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}