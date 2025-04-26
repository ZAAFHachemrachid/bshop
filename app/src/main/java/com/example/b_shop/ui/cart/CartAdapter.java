package com.example.b_shop.ui.cart;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.databinding.ItemCartProductBinding;

import java.text.NumberFormat;
import java.util.Locale;

public class CartAdapter extends ListAdapter<CartItemWithProduct, CartAdapter.CartViewHolder> {
    
    private final CartItemListener listener;
    private final NumberFormat currencyFormatter;

    public CartAdapter(CartItemListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartProductBinding binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new CartViewHolder(binding, listener, currencyFormatter);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartProductBinding binding;
        private final CartItemListener listener;
        private final NumberFormat currencyFormatter;
        private CartItemWithProduct currentItem;

        CartViewHolder(
            ItemCartProductBinding binding,
            CartItemListener listener,
            NumberFormat currencyFormatter
        ) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.currencyFormatter = currencyFormatter;
            setupListeners();
        }

        private void setupListeners() {
            binding.buttonRemove.setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onRemoveItem(currentItem.cartItem.getCartItemId());
                }
            });

            binding.buttonDecrement.setOnClickListener(v -> {
                if (currentItem != null) {
                    int newQuantity = currentItem.cartItem.getQuantity() - 1;
                    if (newQuantity >= 0) {
                        listener.onUpdateQuantity(
                            currentItem.cartItem.getCartItemId(),
                            newQuantity
                        );
                    }
                }
            });

            binding.buttonIncrement.setOnClickListener(v -> {
                if (currentItem != null) {
                    listener.onUpdateQuantity(
                        currentItem.cartItem.getCartItemId(),
                        currentItem.cartItem.getQuantity() + 1
                    );
                }
            });
        }

        void bind(CartItemWithProduct item) {
            currentItem = item;

            binding.textProductName.setText(item.product.getName());
            binding.textQuantity.setText(String.valueOf(item.cartItem.getQuantity()));
            binding.textPrice.setText(currencyFormatter.format(item.cartItem.getItemPrice()));
            binding.textTotalPrice.setText(currencyFormatter.format(item.getTotalPrice()));

            // Load product image
            try {
                if (item.product.getImagePath() != null && !item.product.getImagePath().isEmpty()) {
                    binding.imageProduct.setImageBitmap(
                        BitmapFactory.decodeFile(item.product.getImagePath())
                    );
                } else {
                    binding.imageProduct.setImageResource(
                        com.example.b_shop.R.drawable.ic_product_placeholder
                    );
                }
            } catch (Exception e) {
                binding.imageProduct.setImageResource(
                    com.example.b_shop.R.drawable.ic_product_placeholder
                );
            }

            // Update increment button state based on stock availability
            // TODO: Add stock validation
        }
    }

    public interface CartItemListener {
        void onUpdateQuantity(int cartItemId, int quantity);
        void onRemoveItem(int cartItemId);
    }

    private static final DiffUtil.ItemCallback<CartItemWithProduct> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<CartItemWithProduct>() {
            @Override
            public boolean areItemsTheSame(
                @NonNull CartItemWithProduct oldItem,
                @NonNull CartItemWithProduct newItem
            ) {
                return oldItem.cartItem.getCartItemId() == newItem.cartItem.getCartItemId();
            }

            @Override
            public boolean areContentsTheSame(
                @NonNull CartItemWithProduct oldItem,
                @NonNull CartItemWithProduct newItem
            ) {
                return oldItem.cartItem.getQuantity() == newItem.cartItem.getQuantity()
                    && oldItem.cartItem.getItemPrice() == newItem.cartItem.getItemPrice()
                    && oldItem.product.getName().equals(newItem.product.getName())
                    && oldItem.product.getImagePath().equals(newItem.product.getImagePath());
            }
        };
}