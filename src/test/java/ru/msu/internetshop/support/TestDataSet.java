package ru.msu.internetshop.support;

public class TestDataSet {

    private final int televisionsCategoryId;
    private final int refrigeratorsCategoryId;
    private final int dvdCategoryId;
    private final int samsungManufacturerId;
    private final int lgManufacturerId;
    private final int sonyManufacturerId;
    private final int tvProductId;
    private final int fridgeProductId;
    private final int dvdProductId;
    private final int firstCustomerId;
    private final int secondCustomerId;
    private final int existingOrderId;

    public TestDataSet(
            int televisionsCategoryId,
            int refrigeratorsCategoryId,
            int dvdCategoryId,
            int samsungManufacturerId,
            int lgManufacturerId,
            int sonyManufacturerId,
            int tvProductId,
            int fridgeProductId,
            int dvdProductId,
            int firstCustomerId,
            int secondCustomerId,
            int existingOrderId
    ) {
        this.televisionsCategoryId = televisionsCategoryId;
        this.refrigeratorsCategoryId = refrigeratorsCategoryId;
        this.dvdCategoryId = dvdCategoryId;
        this.samsungManufacturerId = samsungManufacturerId;
        this.lgManufacturerId = lgManufacturerId;
        this.sonyManufacturerId = sonyManufacturerId;
        this.tvProductId = tvProductId;
        this.fridgeProductId = fridgeProductId;
        this.dvdProductId = dvdProductId;
        this.firstCustomerId = firstCustomerId;
        this.secondCustomerId = secondCustomerId;
        this.existingOrderId = existingOrderId;
    }

    public int getTelevisionsCategoryId() {
        return televisionsCategoryId;
    }

    public int getRefrigeratorsCategoryId() {
        return refrigeratorsCategoryId;
    }

    public int getDvdCategoryId() {
        return dvdCategoryId;
    }

    public int getSamsungManufacturerId() {
        return samsungManufacturerId;
    }

    public int getLgManufacturerId() {
        return lgManufacturerId;
    }

    public int getSonyManufacturerId() {
        return sonyManufacturerId;
    }

    public int getTvProductId() {
        return tvProductId;
    }

    public int getFridgeProductId() {
        return fridgeProductId;
    }

    public int getDvdProductId() {
        return dvdProductId;
    }

    public int getFirstCustomerId() {
        return firstCustomerId;
    }

    public int getSecondCustomerId() {
        return secondCustomerId;
    }

    public int getExistingOrderId() {
        return existingOrderId;
    }
}
