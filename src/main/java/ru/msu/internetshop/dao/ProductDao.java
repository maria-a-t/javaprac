package ru.msu.internetshop.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.msu.internetshop.dto.ProductSearchCriteria;
import ru.msu.internetshop.model.Category;
import ru.msu.internetshop.model.Manufacturer;
import ru.msu.internetshop.model.Product;
import ru.msu.internetshop.model.ProductAttribute;

public class ProductDao extends AbstractDao {

    public ProductDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Product> findByCriteria(ProductSearchCriteria criteria) {
        final ProductSearchCriteria effectiveCriteria = criteria == null ? new ProductSearchCriteria() : criteria;
        return executeRead(session -> {
            StringBuilder hql = new StringBuilder(
                    "select distinct p from Product p "
                            + "join fetch p.category c "
                            + "join fetch p.manufacturer m "
                            + "left join fetch p.attributes attrs "
                            + "where 1 = 1"
            );

            if (effectiveCriteria.getCategoryName() != null) {
                hql.append(" and c.name = :categoryName");
            }
            if (effectiveCriteria.getManufacturerName() != null) {
                hql.append(" and m.name = :manufacturerName");
            }

            int index = 0;
            for (Map.Entry<String, String> entry : effectiveCriteria.getAttributes().entrySet()) {
                hql.append(" and exists (")
                        .append("select 1 from ProductAttribute a").append(index)
                        .append(" where a").append(index).append(".product = p")
                        .append(" and a").append(index).append(".attrName = :attrName").append(index)
                        .append(" and a").append(index).append(".attrValue = :attrValue").append(index)
                        .append(")");
                index++;
            }
            hql.append(" order by p.id");

            Query<Product> query = session.createQuery(hql.toString(), Product.class);
            if (effectiveCriteria.getCategoryName() != null) {
                query.setParameter("categoryName", effectiveCriteria.getCategoryName());
            }
            if (effectiveCriteria.getManufacturerName() != null) {
                query.setParameter("manufacturerName", effectiveCriteria.getManufacturerName());
            }

            index = 0;
            for (Map.Entry<String, String> entry : effectiveCriteria.getAttributes().entrySet()) {
                query.setParameter("attrName" + index, entry.getKey());
                query.setParameter("attrValue" + index, entry.getValue());
                index++;
            }

            return query.getResultList();
        });
    }

    public Optional<Product> findById(int productId) {
        return executeRead(session -> fetchProduct(session, productId));
    }

    public Product save(Product product) {
        return executeInTransaction(session -> {
            Product persistentProduct = new Product();
            copyState(session, product, persistentProduct);
            session.persist(persistentProduct);
            session.flush();
            return persistentProduct;
        });
    }

    public Product update(Product product) {
        return executeInTransaction(session -> {
            if (product == null || product.getId() == null) {
                throw new IllegalArgumentException("Product id is required for update");
            }
            Product persistentProduct = fetchProduct(session, product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + product.getId()));
            copyState(session, product, persistentProduct);
            session.flush();
            return persistentProduct;
        });
    }

    public boolean delete(int productId) {
        return executeInTransaction(session -> {
            Product product = session.get(Product.class, productId);
            if (product == null) {
                return false;
            }
            session.remove(product);
            return true;
        });
    }

    private Optional<Product> fetchProduct(Session session, int productId) {
        List<Product> products = session.createQuery(
                        "select distinct p from Product p "
                                + "join fetch p.category "
                                + "join fetch p.manufacturer "
                                + "left join fetch p.attributes "
                                + "where p.id = :productId",
                        Product.class
                )
                .setParameter("productId", productId)
                .getResultList();
        if (products.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(products.get(0));
    }

    private void copyState(Session session, Product source, Product target) {
        requireProduct(source);
        target.setCategory(resolveCategory(session, source.getCategory()));
        target.setManufacturer(resolveManufacturer(session, source.getManufacturer()));
        target.setModelName(requireText(source.getModelName(), "Product model name"));
        target.setDescription(trimToNull(source.getDescription()));
        target.setPrice(requireNonNegativePrice(source.getPrice()));
        target.setAssemblyPlace(trimToNull(source.getAssemblyPlace()));
        target.setStockQty(requireNonNegativeNumber(source.getStockQty(), "Product stock"));
        target.setWarrantyMonths(requireNonNegativeNumber(source.getWarrantyMonths(), "Warranty months"));
        target.replaceAttributes(toAttributeMap(source.getAttributes()));
    }

    private Category resolveCategory(Session session, Category category) {
        Integer categoryId = category == null ? null : category.getId();
        if (categoryId == null) {
            throw new IllegalArgumentException("Category id must not be null");
        }
        Category persistentCategory = session.get(Category.class, categoryId);
        if (persistentCategory == null) {
            throw new IllegalArgumentException("Category not found: " + categoryId);
        }
        return persistentCategory;
    }

    private Manufacturer resolveManufacturer(Session session, Manufacturer manufacturer) {
        Integer manufacturerId = manufacturer == null ? null : manufacturer.getId();
        if (manufacturerId == null) {
            throw new IllegalArgumentException("Manufacturer id must not be null");
        }
        Manufacturer persistentManufacturer = session.get(Manufacturer.class, manufacturerId);
        if (persistentManufacturer == null) {
            throw new IllegalArgumentException("Manufacturer not found: " + manufacturerId);
        }
        return persistentManufacturer;
    }

    private Map<String, String> toAttributeMap(List<ProductAttribute> attributes) {
        Map<String, String> attributeMap = new LinkedHashMap<>();
        for (ProductAttribute attribute : attributes) {
            if (attribute == null) {
                continue;
            }
            attributeMap.put(attribute.getAttrName(), attribute.getAttrValue());
        }
        return attributeMap;
    }

    private void requireProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
    }

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private java.math.BigDecimal requireNonNegativePrice(java.math.BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Product price must not be null");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Product price must not be negative");
        }
        return price;
    }

    private int requireNonNegativeNumber(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
