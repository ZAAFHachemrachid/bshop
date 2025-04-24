package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.b_shop.data.local.entities.Review;
import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    long insert(Review review);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);

    @Query("SELECT * FROM reviews WHERE reviewId = :reviewId")
    LiveData<Review> getReviewById(int reviewId);

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    LiveData<List<Review>> getReviewsForProduct(int productId);

    @Transaction
    @Query("SELECT r.*, u.name as userName " +
           "FROM reviews r " +
           "INNER JOIN users u ON r.userId = u.userId " +
           "WHERE r.productId = :productId " +
           "ORDER BY r.createdAt DESC")
    LiveData<List<ReviewWithUser>> getReviewsWithUserForProduct(int productId);

    @Query("SELECT AVG(CAST(rating AS FLOAT)) FROM reviews WHERE productId = :productId")
    LiveData<Float> getAverageRatingForProduct(int productId);

    @Query("SELECT " +
           "COUNT(CASE WHEN rating = 5 THEN 1 END) as fiveStars, " +
           "COUNT(CASE WHEN rating = 4 THEN 1 END) as fourStars, " +
           "COUNT(CASE WHEN rating = 3 THEN 1 END) as threeStars, " +
           "COUNT(CASE WHEN rating = 2 THEN 1 END) as twoStars, " +
           "COUNT(CASE WHEN rating = 1 THEN 1 END) as oneStar " +
           "FROM reviews WHERE productId = :productId")
    LiveData<RatingDistribution> getRatingDistributionForProduct(int productId);

    @Query("SELECT EXISTS(SELECT 1 FROM reviews WHERE userId = :userId AND productId = :productId)")
    boolean hasUserReviewedProduct(int userId, int productId);

    @Query("SELECT COUNT(*) FROM reviews WHERE productId = :productId")
    LiveData<Integer> getReviewCountForProduct(int productId);

    // Static classes for complex queries
    static class ReviewWithUser {
        @Embedded
        public Review review;
        public String userName;
    }

    static class RatingDistribution {
        public int fiveStars;
        public int fourStars;
        public int threeStars;
        public int twoStars;
        public int oneStar;

        public int getTotalReviews() {
            return fiveStars + fourStars + threeStars + twoStars + oneStar;
        }
    }
}