package ru.msu.internetshop.dao;

import java.math.BigDecimal;
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
}
