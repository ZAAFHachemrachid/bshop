package com.example.b_shop.ui.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b_shop.databinding.ItemProductImageBinding;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.ref.WeakReference;

public class ProductImageAdapter extends ListAdapter<String, ProductImageAdapter.ImageViewHolder> {

    public ProductImageAdapter() {
        super(new ImageDiffCallback());
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(
            ItemProductImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
            )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = getItem(position);
        holder.bind(imageUrl);
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductImageBinding binding;
        private ImageLoadTask currentTask;

        ImageViewHolder(@NonNull ItemProductImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String imageUrl) {
            // Cancel any existing task
            if (currentTask != null) {
                currentTask.cancel(true);
            }

            // Start shimmer and show loading state
            binding.shimmerLayout.startShimmer();
            binding.shimmerLayout.setVisibility(View.VISIBLE);

            // Load image
            currentTask = new ImageLoadTask(binding.productImage, binding.shimmerLayout);
            currentTask.execute(imageUrl);
        }
    }

    private static class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<View> shimmerReference;

        ImageLoadTask(ImageView imageView, View shimmer) {
            imageViewReference = new WeakReference<>(imageView);
            shimmerReference = new WeakReference<>(shimmer);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = imageViewReference.get();
            View shimmer = shimmerReference.get();
            
            if (imageView != null && shimmer != null && result != null) {
                imageView.setImageBitmap(result);
                shimmer.setVisibility(View.GONE);
            }
        }
    }

    static class ImageDiffCallback extends DiffUtil.ItemCallback<String> {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    }
}