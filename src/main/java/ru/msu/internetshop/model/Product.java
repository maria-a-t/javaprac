package ru.msu.internetshop.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "assembly_place", length = 100)
    private String assemblyPlace;

    @Column(name = "stock_qty", nullable = false)
    private int stockQty;

    @Column(name = "warranty_months", nullable = false)
    private int warrantyMonths;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ProductAttribute> attributes = new ArrayList<>();

    public Product() {
    }

    public Product(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getAssemblyPlace() {
        return assemblyPlace;
    }

    public void setAssemblyPlace(String assemblyPlace) {
        this.assemblyPlace = assemblyPlace;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public List<ProductAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ProductAttribute> attributes) {
        this.attributes.clear();
        if (attributes == null) {
            return;
        }
        for (ProductAttribute attribute : attributes) {
            if (attribute == null) {
                continue;
            }
            attribute.setProduct(this);
            this.attributes.add(attribute);
        }
    }

    public void addAttribute(String attrName, String attrValue) {
        if (isBlank(attrName)) {
            throw new IllegalArgumentException("Attribute name must not be blank");
        }
        if (isBlank(attrValue)) {
            throw new IllegalArgumentException("Attribute value must not be blank");
        }
        attributes.add(new ProductAttribute(this, attrName.trim(), attrValue.trim()));
    }

    public void replaceAttributes(Map<String, String> newAttributes) {
        attributes.clear();
        if (newAttributes == null) {
            return;
        }
        for (Map.Entry<String, String> entry : newAttributes.entrySet()) {
            addAttribute(entry.getKey(), entry.getValue());
        }
    }

    public void decreaseStock(int quantity) {
        validatePositiveQuantity(quantity, "Quantity must be positive");
        if (quantity > stockQty) {
            throw new IllegalStateException("Not enough product in stock");
        }
        stockQty -= quantity;
    }

    public void increaseStock(int quantity) {
        validatePositiveQuantity(quantity, "Quantity must be positive");
        stockQty += quantity;
    }

    private void validatePositiveQuantity(int quantity, String message) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
