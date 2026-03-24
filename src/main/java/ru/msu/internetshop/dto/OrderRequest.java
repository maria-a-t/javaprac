package ru.msu.internetshop.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderRequest {

    private Integer customerId;
    private Map<Integer, Integer> productQuantities = new LinkedHashMap<>();
    private String deliveryAddress;
    private LocalDateTime deliveryFrom;
    private LocalDateTime deliveryTo;
    private LocalDateTime createdAt;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Map<Integer, Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Map<Integer, Integer> productQuantities) {
        this.productQuantities = new LinkedHashMap<>();
        if (productQuantities == null) {
            return;
        }
        this.productQuantities.putAll(productQuantities);
    }

    public void addProduct(Integer productId, Integer quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product id must not be null");
        }
        productQuantities.put(productId, quantity);
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDateTime getDeliveryFrom() {
        return deliveryFrom;
    }

    public void setDeliveryFrom(LocalDateTime deliveryFrom) {
        this.deliveryFrom = deliveryFrom;
    }

    public LocalDateTime getDeliveryTo() {
        return deliveryTo;
    }

    public void setDeliveryTo(LocalDateTime deliveryTo) {
        this.deliveryTo = deliveryTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
