package ru.msu.internetshop.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.msu.internetshop.dto.OrderRequest;
import ru.msu.internetshop.model.Customer;
import ru.msu.internetshop.model.CustomerOrder;
import ru.msu.internetshop.model.Delivery;
import ru.msu.internetshop.model.OrderStatus;
import ru.msu.internetshop.model.Product;

public class OrderDao extends AbstractDao {

    private static final String INITIAL_STATUS_NAME = "в обработке";

    public OrderDao() {
    }

    public OrderDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public int placeOrder(OrderRequest request) {
        validateRequest(request);
        return executeInTransaction(session -> {
            Customer customer = session.get(Customer.class, request.getCustomerId());
            if (customer == null) {
                throw new IllegalArgumentException("Customer not found: " + request.getCustomerId());
            }

            OrderStatus initialStatus = findStatusByName(session, INITIAL_STATUS_NAME)
                    .orElseThrow(() -> new IllegalStateException("Order status not found: " + INITIAL_STATUS_NAME));

            CustomerOrder order = new CustomerOrder(customer, initialStatus, resolveCreatedAt(request));
            for (Map.Entry<Integer, Integer> entry : request.getProductQuantities().entrySet()) {
                Product product = session.get(Product.class, entry.getKey());
                if (product == null) {
                    throw new IllegalArgumentException("Product not found: " + entry.getKey());
                }
                int quantity = entry.getValue();
                product.decreaseStock(quantity);
                order.addItem(product, quantity);
            }

            Delivery delivery = new Delivery(
                    request.getDeliveryAddress().trim(),
                    request.getDeliveryFrom(),
                    request.getDeliveryTo()
            );
            order.setDelivery(delivery);
            order.recalculateTotal();

            session.persist(order);
            session.flush();
            return order.getId();
        });
    }

    public Optional<String> findStatusByOrderId(int orderId) {
        return executeRead(session -> {
            List<String> statuses = session.createQuery(
                            "select s.statusName from CustomerOrder o join o.status s where o.id = :orderId",
                            String.class
                    )
                    .setParameter("orderId", orderId)
                    .getResultList();
            if (statuses.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(statuses.get(0));
        });
    }

    public Optional<CustomerOrder> findById(int orderId) {
        return executeRead(session -> fetchOrder(session, orderId));
    }

    private Optional<CustomerOrder> fetchOrder(Session session, int orderId) {
        List<CustomerOrder> orders = session.createQuery(
                        "select distinct o from CustomerOrder o "
                                + "join fetch o.customer "
                                + "join fetch o.status "
                                + "left join fetch o.items i "
                                + "left join fetch i.product "
                                + "left join fetch o.delivery "
                                + "where o.id = :orderId",
                        CustomerOrder.class
                )
                .setParameter("orderId", orderId)
                .getResultList();
        if (orders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(orders.get(0));
    }

    private Optional<OrderStatus> findStatusByName(Session session, String statusName) {
        List<OrderStatus> statuses = session.createQuery(
                        "from OrderStatus s where s.statusName = :statusName",
                        OrderStatus.class
                )
                .setParameter("statusName", statusName)
                .getResultList();
        if (statuses.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(statuses.get(0));
    }

    private LocalDateTime resolveCreatedAt(OrderRequest request) {
        return request.getCreatedAt() == null ? LocalDateTime.now() : request.getCreatedAt();
    }

    private void validateRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request must not be null");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer id must not be null");
        }
        if (request.getProductQuantities() == null || request.getProductQuantities().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address must not be blank");
        }
        if (request.getDeliveryFrom() == null || request.getDeliveryTo() == null) {
            throw new IllegalArgumentException("Delivery interval must be provided");
        }
        if (request.getDeliveryTo().isBefore(request.getDeliveryFrom())) {
            throw new IllegalArgumentException("Delivery end time must not be earlier than start time");
        }
        for (Map.Entry<Integer, Integer> entry : request.getProductQuantities().entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Product id must not be null");
            }
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Product quantity must be positive");
            }
        }
    }
}
