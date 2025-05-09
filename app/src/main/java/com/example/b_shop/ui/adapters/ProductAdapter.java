package com.example.b_shop.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.example.b_shop.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.databinding.ItemProductBinding;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    private final OnProductClickListener listener;
    private final NumberFormat currencyFormat;

    public ProductAdapter(OnProductClickListener listener) {
        super(new ProductDiffCallback());
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            // Set product name
            binding.productName.setText(product.getName());
            
            // Set product price
            binding.productPrice.setText(currencyFormat.format(product.getPrice()));

            // Load product image using Picasso
            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get()
                    .load(imagePath)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(binding.productImage);
            } else {
                binding.productImage.setImageResource(R.drawable.ic_product_placeholder);
            }

            // Set click listener
            binding.getRoot().setOnClickListener(v -> listener.onProductClick(product));
        }
    }

    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getProductId() == newItem.getProductId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   (oldItem.getImagePath() == null ? newItem.getImagePath() == null :
                    oldItem.getImagePath().equals(newItem.getImagePath()));
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}