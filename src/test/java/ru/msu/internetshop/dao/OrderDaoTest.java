package ru.msu.internetshop.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.Transaction;
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

    @Test
    public void findByIdShouldReturnExistingOrderWithDeliveryAndItems() {
        Optional<CustomerOrder> orderOptional = orderDao.findById(dataSet.getExistingOrderId());

        Assert.assertTrue(orderOptional.isPresent());
        Assert.assertEquals(orderOptional.get().getStatus().getStatusName(), "собран");
        Assert.assertEquals(orderOptional.get().getItems().size(), 1);
        Assert.assertNotNull(orderOptional.get().getDelivery());
        Assert.assertEquals(orderOptional.get().getDelivery().getAddress(), "Санкт-Петербург, Невский пр., д. 25");
    }

    @Test
    public void findByIdShouldReturnEmptyForMissingOrder() {
        Assert.assertFalse(orderDao.findById(99999).isPresent());
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Order request must not be null")
    public void placeOrderShouldRejectNullRequest() {
        orderDao.placeOrder(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Customer id must not be null")
    public void placeOrderShouldRejectNullCustomerId() {
        OrderRequest request = createValidRequest();
        request.setCustomerId(null);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Delivery address must not be blank")
    public void placeOrderShouldRejectBlankDeliveryAddress() {
        OrderRequest request = createValidRequest();
        request.setDeliveryAddress("   ");

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Delivery address must not be blank")
    public void placeOrderShouldRejectNullDeliveryAddress() {
        OrderRequest request = createValidRequest();
        request.setDeliveryAddress(null);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Delivery interval must be provided")
    public void placeOrderShouldRejectMissingDeliveryInterval() {
        OrderRequest request = createValidRequest();
        request.setDeliveryFrom(null);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Delivery interval must be provided")
    public void placeOrderShouldRejectMissingDeliveryEnd() {
        OrderRequest request = createValidRequest();
        request.setDeliveryTo(null);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Delivery end time must not be earlier than start time")
    public void placeOrderShouldRejectReversedDeliveryInterval() {
        OrderRequest request = createValidRequest();
        request.setDeliveryFrom(LocalDateTime.of(2026, 3, 2, 18, 0));
        request.setDeliveryTo(LocalDateTime.of(2026, 3, 2, 10, 0));

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product id must not be null")
    public void placeOrderShouldRejectNullProductId() {
        OrderRequest request = createValidRequest();
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        quantities.put(null, 1);
        request.setProductQuantities(quantities);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product quantity must be positive")
    public void placeOrderShouldRejectNullProductQuantity() {
        OrderRequest request = createValidRequest();
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        quantities.put(dataSet.getTvProductId(), null);
        request.setProductQuantities(quantities);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product quantity must be positive")
    public void placeOrderShouldRejectZeroProductQuantity() {
        OrderRequest request = createValidRequest();
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        quantities.put(dataSet.getTvProductId(), 0);
        request.setProductQuantities(quantities);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Customer not found: 99999")
    public void placeOrderShouldRejectMissingCustomer() {
        OrderRequest request = createValidRequest();
        request.setCustomerId(99999);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Product not found: 99999")
    public void placeOrderShouldRejectMissingProduct() {
        OrderRequest request = createValidRequest();
        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        quantities.put(99999, 1);
        request.setProductQuantities(quantities);

        orderDao.placeOrder(request);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Order status not found: в обработке")
    public void placeOrderShouldRejectWhenInitialStatusIsMissing() {
        deleteOrdersAndStatuses();

        orderDao.placeOrder(createValidRequest());
    }

    private OrderRequest createValidRequest() {
        OrderRequest request = new OrderRequest();
        request.setCustomerId(dataSet.getFirstCustomerId());
        request.addProduct(dataSet.getTvProductId(), 1);
        request.setDeliveryAddress("Москва, ул. Тверская, д. 1");
        request.setDeliveryFrom(LocalDateTime.of(2026, 3, 1, 10, 0));
        request.setDeliveryTo(LocalDateTime.of(2026, 3, 1, 14, 0));
        return request;
    }

    private void deleteOrdersAndStatuses() {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createQuery("delete from Delivery").executeUpdate();
            session.createQuery("delete from OrderItem").executeUpdate();
            session.createQuery("delete from CustomerOrder").executeUpdate();
            session.createQuery("delete from OrderStatus").executeUpdate();
            transaction.commit();
        }
    }
}
