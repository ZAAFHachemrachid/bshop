package com.example.b_shop.data.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.dao.ReviewDao;
import com.example.b_shop.data.local.dao.UserDao;
import com.example.b_shop.data.local.entities.Review;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReviewRepository {
    private final ReviewDao reviewDao;
    private final UserDao userDao;
    private final ProductRepository productRepository;
    private final ExecutorService executorService;

    public ReviewRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        reviewDao = database.reviewDao();
        userDao = database.userDao();
        productRepository = new ProductRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    // Basic CRUD operations
    public Future<Long> addReview(Review review) {
        return executorService.submit(() -> {
            long reviewId = reviewDao.insert(review);
            // Update product rating after adding review
            productRepository.updateProductRating(review.getProductId());
            return reviewId;
        });
    }

    public void updateReview(Review review) {
        executorService.execute(() -> {
            reviewDao.update(review);
            // Update product rating after modifying review
            productRepository.updateProductRating(review.getProductId());
        });
    }

    public void deleteReview(Review review) {
        executorService.execute(() -> {
            reviewDao.delete(review);
            // Update product rating after deleting review
            productRepository.updateProductRating(review.getProductId());
        });
    }

    // Query methods
    public LiveData<Review> getReviewById(int reviewId) {
        return reviewDao.getReviewById(reviewId);
    }

    public LiveData<List<Review>> getReviewsForProduct(int productId) {
        return reviewDao.getReviewsForProduct(productId);
    }

    // New method: Get reviews synchronously
    public List<Review> getProductReviews(int productId) throws Exception {
        return reviewDao.getProductReviewsSync(productId);
    }

    public LiveData<List<ReviewDao.ReviewWithUser>> getReviewsWithUserForProduct(int productId) {
        return reviewDao.getReviewsWithUserForProduct(productId);
    }

    public LiveData<Float> getAverageRatingForProduct(int productId) {
        return reviewDao.getAverageRatingForProduct(productId);
    }

    public LiveData<ReviewDao.RatingDistribution> getRatingDistributionForProduct(int productId) {
        return reviewDao.getRatingDistributionForProduct(productId);
    }

    public Future<Boolean> hasUserReviewedProduct(int userId, int productId) {
        return executorService.submit(() -> 
            reviewDao.hasUserReviewedProduct(userId, productId)
        );
    }

    public LiveData<Integer> getReviewCountForProduct(int productId) {
        return reviewDao.getReviewCountForProduct(productId);
    }

    // Helper methods
    public void refreshProductRating(int productId) {
        executorService.execute(() -> {
            productRepository.updateProductRating(productId);
        });
    }

    // Review validation
    public boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    public boolean isValidComment(String comment) {
        return comment != null && 
               comment.length() >= 10 && 
               comment.length() <= 500;
    }

    // User info methods for reviews
    public String getUserName(int userId) throws Exception {
        return userDao.getUserNameSync(userId);
    }

    public String getUserAvatarUrl(int userId) throws Exception {
        return userDao.getUserAvatarUrlSync(userId);
    }

    // Cleanup
    public void cleanup() {
        executorService.shutdown();
        productRepository.cleanup();
    }
}