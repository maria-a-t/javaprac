package ru.msu.internetshop.dao;

import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import ru.msu.internetshop.model.Customer;

public class CustomerDao extends AbstractDao {

    public CustomerDao() {
    }

    public CustomerDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<Customer> findById(int customerId) {
        return executeRead(session -> {
            List<Customer> customers = session.createQuery(
                            "from Customer c where c.id = :customerId",
                            Customer.class
                    )
                    .setParameter("customerId", customerId)
                    .getResultList();
            if (customers.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(customers.get(0));
        });
    }

    public Customer save(Customer customer) {
        return executeInTransaction(session -> {
            Customer persistentCustomer = new Customer();
            copyState(customer, persistentCustomer);
            session.persist(persistentCustomer);
            session.flush();
            return persistentCustomer;
        });
    }

    public Customer update(Customer customer) {
        return executeInTransaction(session -> {
            if (customer == null || customer.getId() == null) {
                throw new IllegalArgumentException("Customer id is required for update");
            }
            Customer persistentCustomer = session.get(Customer.class, customer.getId());
            if (persistentCustomer == null) {
                throw new IllegalArgumentException("Customer not found: " + customer.getId());
            }
            copyState(customer, persistentCustomer);
            session.flush();
            return persistentCustomer;
        });
    }

    public boolean delete(int customerId) {
        return executeInTransaction(session -> {
            Customer customer = session.get(Customer.class, customerId);
            if (customer == null) {
                return false;
            }
            session.remove(customer);
            return true;
        });
    }

    private void copyState(Customer source, Customer target) {
        requireCustomer(source);
        target.setFullName(requireText(source.getFullName(), "Customer full name"));
        target.setPhone(trimToNull(source.getPhone()));
        target.setEmail(trimToNull(source.getEmail()));
        target.setAddress(trimToNull(source.getAddress()));
    }

    private void requireCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer must not be null");
        }
    }

    private String requireText(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
