package ru.msu.internetshop.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.msu.internetshop.dto.ProductSearchCriteria;
import ru.msu.internetshop.model.Category;
import ru.msu.internetshop.model.Manufacturer;
import ru.msu.internetshop.model.Product;
import ru.msu.internetshop.support.HibernateDaoTestSupport;

public class ProductDaoTest extends HibernateDaoTestSupport {

    private ProductDao productDao;

    @BeforeMethod(alwaysRun = true)
    public void createDao() {
        productDao = new ProductDao(sessionFactory);
    }

    @Test
    public void findByCriteriaShouldFilterByCategoryManufacturerAndAttribute() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategoryName("Телевизоры");
        criteria.setManufacturerName("Samsung");
        criteria.addAttribute("Диагональ", "55");

        List<Product> products = productDao.findByCriteria(criteria);

        Assert.assertEquals(products.size(), 1);
        Product product = products.get(0);
        Assert.assertEquals(product.getId().intValue(), dataSet.getTvProductId());
        Assert.assertEquals(product.getCategory().getName(), "Телевизоры");
        Assert.assertEquals(product.getManufacturer().getName(), "Samsung");
        Assert.assertEquals(product.getAttributes().size(), 2);
    }

    @Test
    public void findByCriteriaShouldReturnEmptyWhenNothingMatches() {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCategoryName("Телевизоры");
        criteria.addAttribute("Диагональ", "70");

        List<Product> products = productDao.findByCriteria(criteria);

        Assert.assertTrue(products.isEmpty());
    }

    @Test
    public void findByCriteriaShouldReturnAllWhenCriteriaIsNull() {
        List<Product> products = productDao.findByCriteria(null);

        Assert.assertEquals(products.size(), 3);
        Assert.assertEquals(products.get(0).getId().intValue(), dataSet.getTvProductId());
        Assert.assertEquals(products.get(1).getId().intValue(), dataSet.getFridgeProductId());
        Assert.assertEquals(products.get(2).getId().intValue(), dataSet.getDvdProductId());
    }

    @Test
    public void findByIdShouldReturnProductWithLoadedAttributes() {
        Optional<Product> productOptional = productDao.findById(dataSet.getTvProductId());

        Assert.assertTrue(productOptional.isPresent());
        Product product = productOptional.get();
        Assert.assertEquals(product.getModelName(), "Samsung QE55Q70");
        Assert.assertEquals(product.getAttributes().size(), 2);
        Assert.assertEquals(product.getPrice(), new BigDecimal("49990.00"));
    }

    @Test
    public void saveShouldPersistProductWithAttributes() {
        Product product = new Product();
        product.setCategory(new Category(dataSet.getDvdCategoryId()));
        product.setManufacturer(new Manufacturer(dataSet.getSonyManufacturerId()));
        product.setModelName("Sony DVP-SR370");
        product.setDescription("Compact DVD player");
        product.setPrice(new BigDecimal("5990.00"));
        product.setAssemblyPlace("China");
        product.setStockQty(4);
        product.setWarrantyMonths(12);
        product.addAttribute("Интерфейсы", "USB");
        product.addAttribute("Поддержка форматов", "DVD, CD, MP3");

        Product saved = productDao.save(product);
        Optional<Product> reloaded = productDao.findById(saved.getId());

        Assert.assertNotNull(saved.getId());
        Assert.assertTrue(reloaded.isPresent());
        Assert.assertEquals(reloaded.get().getModelName(), "Sony DVP-SR370");
        Assert.assertEquals(reloaded.get().getAttributes().size(), 2);
        Assert.assertEquals(reloaded.get().getCategory().getId().intValue(), dataSet.getDvdCategoryId());
    }

    @Test
    public void saveShouldNormalizeBlankOptionalFieldsAndIgnoreNullAttributes() {
        Product product = createValidProduct();
        product.setDescription("   ");
        product.setAssemblyPlace("\t");
        List<ru.msu.internetshop.model.ProductAttribute> attributes = new ArrayList<>();
        attributes.add(new ru.msu.internetshop.model.ProductAttribute(null, "Интерфейсы", "USB"));
        product.setAttributes(attributes);
        product.getAttributes().add(null);

        Product saved = productDao.save(product);
        Optional<Product> reloaded = productDao.findById(saved.getId());

        Assert.assertTrue(reloaded.isPresent());
        Assert.assertNull(reloaded.get().getDescription());
        Assert.assertNull(reloaded.get().getAssemblyPlace());
        Assert.assertEquals(reloaded.get().getAttributes().size(), 1);
        Assert.assertEquals(reloaded.get().getAttributes().get(0).getAttrName(), "Интерфейсы");
    }

    @Test
    public void saveShouldKeepNullOptionalFieldsAsNull() {
        Product product = createValidProduct();
        product.setDescription(null);
        product.setAssemblyPlace(null);

        Product saved = productDao.save(product);
        Optional<Product> reloaded = productDao.findById(saved.getId());

        Assert.assertTrue(reloaded.isPresent());
        Assert.assertNull(reloaded.get().getDescription());
        Assert.assertNull(reloaded.get().getAssemblyPlace());
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product must not be null")
    public void saveShouldRejectNullProduct() {
        productDao.save(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Category id must not be null")
    public void saveShouldRejectNullCategory() {
        Product product = createValidProduct();
        product.setCategory(null);

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Category id must not be null")
    public void saveShouldRejectMissingCategoryId() {
        Product product = createValidProduct();
        product.setCategory(new Category());

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Category not found: 99999")
    public void saveShouldRejectUnknownCategory() {
        Product product = createValidProduct();
        product.setCategory(new Category(99999));

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Manufacturer id must not be null")
    public void saveShouldRejectMissingManufacturerId() {
        Product product = createValidProduct();
        product.setManufacturer(new Manufacturer());

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Manufacturer id must not be null")
    public void saveShouldRejectNullManufacturer() {
        Product product = createValidProduct();
        product.setManufacturer(null);

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Manufacturer not found: 99999")
    public void saveShouldRejectUnknownManufacturer() {
        Product product = createValidProduct();
        product.setManufacturer(new Manufacturer(99999));

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product model name must not be blank")
    public void saveShouldRejectBlankModelName() {
        Product product = createValidProduct();
        product.setModelName("   ");

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product price must not be null")
    public void saveShouldRejectNullPrice() {
        Product product = createValidProduct();
        product.setPrice(null);

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product price must not be negative")
    public void saveShouldRejectNegativePrice() {
        Product product = createValidProduct();
        product.setPrice(new BigDecimal("-1.00"));

        productDao.save(product);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product stock must not be negative")
    public void saveShouldRejectNegativeStock() {
        Product product = createValidProduct();
        product.setStockQty(-1);

        productDao.save(product);
    }

    @Test
    public void updateShouldChangeFieldsAndReplaceAttributes() {
        Product product = new Product();
        product.setId(dataSet.getTvProductId());
        product.setCategory(new Category(dataSet.getRefrigeratorsCategoryId()));
        product.setManufacturer(new Manufacturer(dataSet.getLgManufacturerId()));
        product.setModelName("LG InstaView");
        product.setDescription("Smart refrigerator");
        product.setPrice(new BigDecimal("89990.00"));
        product.setAssemblyPlace("Poland");
        product.setStockQty(3);
        product.setWarrantyMonths(48);
        product.addAttribute("No Frost", "Да");
        product.addAttribute("Объем", "420 л");

        Product updated = productDao.update(product);
        Optional<Product> reloaded = productDao.findById(dataSet.getTvProductId());

        Assert.assertEquals(updated.getManufacturer().getId().intValue(), dataSet.getLgManufacturerId());
        Assert.assertTrue(reloaded.isPresent());
        Assert.assertEquals(reloaded.get().getCategory().getId().intValue(), dataSet.getRefrigeratorsCategoryId());
        Assert.assertEquals(reloaded.get().getPrice(), new BigDecimal("89990.00"));
        Assert.assertEquals(reloaded.get().getAttributes().size(), 2);
        Assert.assertEquals(reloaded.get().getAttributes().get(0).getAttrName(), "No Frost");
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product id is required for update")
    public void updateShouldRejectNullProduct() {
        productDao.update(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product id is required for update")
    public void updateShouldRejectProductWithoutId() {
        productDao.update(createValidProduct());
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product not found: 99999")
    public void updateShouldRejectMissingProduct() {
        Product product = createValidProduct();
        product.setId(99999);

        productDao.update(product);
    }

    @Test
    public void deleteShouldRemoveProduct() {
        boolean deleted = productDao.delete(dataSet.getFridgeProductId());
        Optional<Product> reloaded = productDao.findById(dataSet.getFridgeProductId());

        Assert.assertTrue(deleted);
        Assert.assertFalse(reloaded.isPresent());
    }

    @Test
    public void deleteShouldReturnFalseForMissingProduct() {
        boolean deleted = productDao.delete(99999);

        Assert.assertFalse(deleted);
    }

    private Product createValidProduct() {
        Product product = new Product();
        product.setCategory(new Category(dataSet.getDvdCategoryId()));
        product.setManufacturer(new Manufacturer(dataSet.getSonyManufacturerId()));
        product.setModelName("Sony DVP-SR370");
        product.setDescription("Compact DVD player");
        product.setPrice(new BigDecimal("5990.00"));
        product.setAssemblyPlace("China");
        product.setStockQty(4);
        product.setWarrantyMonths(12);
        product.addAttribute("Интерфейсы", "USB");
        return product;
    }
}
