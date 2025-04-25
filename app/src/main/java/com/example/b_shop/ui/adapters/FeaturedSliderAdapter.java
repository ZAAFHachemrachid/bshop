package com.example.b_shop.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.Product;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

public class FeaturedSliderAdapter extends ListAdapter<Product, FeaturedSliderAdapter.FeaturedSliderViewHolder> {

    private final OnProductClickListener listener;

    public FeaturedSliderAdapter(OnProductClickListener listener) {
        super(new ProductDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public FeaturedSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_featured_product, parent, false);
        return new FeaturedSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedSliderViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product, listener);
    }

    static class FeaturedSliderViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView imageView;
        private final TextView titleView;
        private final TextView priceView;
        private final TextView ratingView;

        public FeaturedSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imageView = itemView.findViewById(R.id.product_image);
            titleView = itemView.findViewById(R.id.product_title);
            priceView = itemView.findViewById(R.id.product_price);
            ratingView = itemView.findViewById(R.id.product_rating);
        }

        public void bind(Product product, OnProductClickListener listener) {
            titleView.setText(product.getName());
            priceView.setText(String.format("$%.2f", product.getPrice()));
            ratingView.setText(String.format("%.1f ★", product.getRating()));

            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                Picasso.get()
                        .load(imagePath)
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_product_placeholder);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }

    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getProductId() == newItem.getProductId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.equals(newItem);
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}