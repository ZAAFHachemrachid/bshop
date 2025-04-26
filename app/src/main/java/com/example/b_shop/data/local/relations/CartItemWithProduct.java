package com.example.b_shop.data.local.relations;

import androidx.room.Embedded;
import androidx.room.Relation;
import androidx.room.TypeConverters;
import com.example.b_shop.data.local.converters.StringListConverter;
import com.example.b_shop.data.local.entities.CartItem;
import com.example.b_shop.data.local.entities.Product;

@TypeConverters(StringListConverter.class)
public class CartItemWithProduct {
    @Embedded
    public CartItem cartItem;
    
    @Relation(
        parentColumn = "productId",
        entityColumn = "productId"
    )
    public Product product;
    
    public float getTotalPrice() {
        return cartItem.getQuantity() * cartItem.getItemPrice();
    }
}