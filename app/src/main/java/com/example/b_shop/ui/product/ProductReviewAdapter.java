package com.example.b_shop.ui.product;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b_shop.data.local.entities.Review;
import com.example.b_shop.databinding.ItemProductReviewBinding;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProductReviewAdapter extends ListAdapter<Review, ProductReviewAdapter.ReviewViewHolder> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ProductReviewAdapter() {
        super(new ReviewDiffCallback());
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewViewHolder(
            ItemProductReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
            )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = getItem(position);
        holder.bind(review);
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductReviewBinding binding;
        private ImageLoadTask currentTask;

        ReviewViewHolder(@NonNull ItemProductReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Review review) {
            // Set user info
            binding.userName.setText(review.getReviewerName());
            binding.reviewDate.setText(dateFormat.format(review.getCreatedAt()));

            // Load user avatar
            if (currentTask != null) {
                currentTask.cancel(true);
            }
            currentTask = new ImageLoadTask(binding.userAvatar);
            currentTask.execute(review.getReviewerAvatarUrl());

            // Set rating and review text
            binding.ratingBar.setRating(review.getRating());
            binding.reviewText.setText(review.getComment());

            // Setup review images if any
            if (review.getImages() != null && !review.getImages().isEmpty()) {
                binding.reviewImages.setVisibility(android.view.View.VISIBLE);
                // TODO: Implement review images adapter
            } else {
                binding.reviewImages.setVisibility(android.view.View.GONE);
            }

            // Setup action buttons
            binding.helpfulButton.setOnClickListener(v -> {
                // TODO: Implement helpful action
            });

            binding.reportButton.setOnClickListener(v -> {
                // TODO: Implement report action
            });
        }
    }

    private static class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        ImageLoadTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
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
            if (imageView != null && result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }

    static class ReviewDiffCallback extends DiffUtil.ItemCallback<Review> {
        @Override
        public boolean areItemsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return oldItem.getReviewId() == newItem.getReviewId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Review oldItem, @NonNull Review newItem) {
            return oldItem.getComment().equals(newItem.getComment()) &&
                   oldItem.getRating() == newItem.getRating() &&
                   oldItem.getCreatedAt().equals(newItem.getCreatedAt());
        }
    }
}