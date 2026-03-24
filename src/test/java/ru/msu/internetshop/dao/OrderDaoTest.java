package ru.msu.internetshop.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.msu.internetshop.dto.OrderRequest;
import ru.msu.internetshop.model.CustomerOrder;
import ru.msu.internetshop.model.Product;
import ru.msu.internetshop.support.HibernateDaoTestSupport;

public class OrderDaoTest extends HibernateDaoTestSupport {

    private OrderDao orderDao;
    private ProductDao productDao;

    @BeforeMethod(alwaysRun = true)
    public void createDaos() {
        orderDao = new OrderDao(sessionFactory);
        productDao = new ProductDao(sessionFactory);
    }

    @Test
    public void placeOrderShouldCreateOrderAndDecreaseStock() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(dataSet.getFirstCustomerId());
        request.addProduct(dataSet.getTvProductId(), 1);
        request.addProduct(dataSet.getDvdProductId(), 2);
        request.setDeliveryAddress("Москва, ул. Тверская, д. 1");
        request.setDeliveryFrom(LocalDateTime.of(2026, 3, 1, 10, 0));
        request.setDeliveryTo(LocalDateTime.of(2026, 3, 1, 14, 0));
        request.setCreatedAt(LocalDateTime.of(2026, 2, 28, 18, 30));

        int orderId = orderDao.placeOrder(request);
        Optional<CustomerOrder> orderOptional = orderDao.findById(orderId);
        Optional<Product> tvOptional = productDao.findById(dataSet.getTvProductId());
        Optional<Product> dvdOptional = productDao.findById(dataSet.getDvdProductId());

        Assert.assertTrue(orderOptional.isPresent());
        CustomerOrder order = orderOptional.get();
        Assert.assertEquals(order.getCustomer().getId().intValue(), dataSet.getFirstCustomerId());
        Assert.assertEquals(order.getStatus().getStatusName(), "в обработке");
        Assert.assertEquals(order.getItems().size(), 2);
        Assert.assertEquals(order.getTotalCost(), new BigDecimal("63970.00"));
        Assert.assertNotNull(order.getDelivery());
        Assert.assertEquals(order.getDelivery().getAddress(), "Москва, ул. Тверская, д. 1");

        Assert.assertTrue(tvOptional.isPresent());
        Assert.assertTrue(dvdOptional.isPresent());
        Assert.assertEquals(tvOptional.get().getStockQty(), 4);
        Assert.assertEquals(dvdOptional.get().getStockQty(), 8);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void placeOrderShouldRejectWhenStockIsInsufficient() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(dataSet.getFirstCustomerId());
        request.addProduct(dataSet.getFridgeProductId(), 2);
        request.setDeliveryAddress("Москва, ул. Арбат, д. 15");
        request.setDeliveryFrom(LocalDateTime.of(2026, 3, 2, 10, 0));
        request.setDeliveryTo(LocalDateTime.of(2026, 3, 2, 18, 0));

        try {
            orderDao.placeOrder(request);
        } finally {
            Optional<Product> fridgeOptional = productDao.findById(dataSet.getFridgeProductId());
            Assert.assertTrue(fridgeOptional.isPresent());
            Assert.assertEquals(fridgeOptional.get().getStockQty(), 1);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void placeOrderShouldRejectEmptyBasket() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(dataSet.getFirstCustomerId());
        request.setDeliveryAddress("Москва, ул. Арбат, д. 15");
        request.setDeliveryFrom(LocalDateTime.of(2026, 3, 2, 10, 0));
        request.setDeliveryTo(LocalDateTime.of(2026, 3, 2, 18, 0));

        orderDao.placeOrder(request);
    }

    @Test
    public void findStatusByOrderIdShouldReturnStatusForExistingOrder() {
        Optional<String> statusOptional = orderDao.findStatusByOrderId(dataSet.getExistingOrderId());

        Assert.assertTrue(statusOptional.isPresent());
        Assert.assertEquals(statusOptional.get(), "собран");
    }

    @Test
    public void findStatusByOrderIdShouldReturnEmptyForMissingOrder() {
        Optional<String> statusOptional = orderDao.findStatusByOrderId(99999);

        Assert.assertFalse(statusOptional.isPresent());
    }
}
