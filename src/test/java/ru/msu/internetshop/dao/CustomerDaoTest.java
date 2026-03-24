package ru.msu.internetshop.dao;

import java.util.Optional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.msu.internetshop.model.Customer;
import ru.msu.internetshop.support.HibernateDaoTestSupport;

public class CustomerDaoTest extends HibernateDaoTestSupport {

    private CustomerDao customerDao;

    @BeforeMethod(alwaysRun = true)
    public void createDao() {
        customerDao = new CustomerDao(sessionFactory);
    }

    @Test
    public void findByIdShouldReturnCustomer() {
        Optional<Customer> customerOptional = customerDao.findById(dataSet.getFirstCustomerId());

        Assert.assertTrue(customerOptional.isPresent());
        Assert.assertEquals(customerOptional.get().getFullName(), "Иванов Иван Иванович");
        Assert.assertEquals(customerOptional.get().getEmail(), "ivanov@example.com");
    }

    @Test
    public void findByIdShouldReturnEmptyForMissingCustomer() {
        Optional<Customer> customerOptional = customerDao.findById(99999);

        Assert.assertFalse(customerOptional.isPresent());
    }

    @Test
    public void saveShouldPersistCustomer() {
        Customer customer = new Customer(
                "Смирнова Мария Олеговна",
                "+7-901-000-11-22",
                "smirnova@example.com",
                "Екатеринбург, ул. Ленина, д. 15"
        );

        Customer saved = customerDao.save(customer);
        Optional<Customer> reloaded = customerDao.findById(saved.getId());

        Assert.assertNotNull(saved.getId());
        Assert.assertTrue(reloaded.isPresent());
        Assert.assertEquals(reloaded.get().getFullName(), "Смирнова Мария Олеговна");
        Assert.assertEquals(reloaded.get().getPhone(), "+7-901-000-11-22");
    }

    @Test
    public void updateShouldModifyCustomer() {
        Customer customer = new Customer();
        customer.setId(dataSet.getSecondCustomerId());
        customer.setFullName("Петрова Анна Игоревна");
        customer.setPhone("+7-900-555-66-77");
        customer.setEmail("petrova.new@example.com");
        customer.setAddress("Санкт-Петербург, ул. Рубинштейна, д. 5");

        Customer updated = customerDao.update(customer);
        Optional<Customer> reloaded = customerDao.findById(dataSet.getSecondCustomerId());

        Assert.assertEquals(updated.getEmail(), "petrova.new@example.com");
        Assert.assertTrue(reloaded.isPresent());
        Assert.assertEquals(reloaded.get().getFullName(), "Петрова Анна Игоревна");
        Assert.assertEquals(reloaded.get().getAddress(), "Санкт-Петербург, ул. Рубинштейна, д. 5");
    }

    @Test
    public void deleteShouldRemoveCustomer() {
        Customer customer = new Customer(
                "Временный клиент",
                "+7-999-000-00-00",
                "temp@example.com",
                "Казань, ул. Баумана, д. 3"
        );
        Customer saved = customerDao.save(customer);

        boolean deleted = customerDao.delete(saved.getId());
        Optional<Customer> reloaded = customerDao.findById(saved.getId());

        Assert.assertTrue(deleted);
        Assert.assertFalse(reloaded.isPresent());
    }

    @Test
    public void deleteShouldReturnFalseForMissingCustomer() {
        boolean deleted = customerDao.delete(99999);

        Assert.assertFalse(deleted);
    }
}
