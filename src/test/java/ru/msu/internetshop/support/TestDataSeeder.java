package ru.msu.internetshop.support;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import ru.msu.internetshop.model.Category;
import ru.msu.internetshop.model.Customer;
import ru.msu.internetshop.model.CustomerOrder;
import ru.msu.internetshop.model.Delivery;
import ru.msu.internetshop.model.Manufacturer;
import ru.msu.internetshop.model.OrderStatus;
import ru.msu.internetshop.model.Product;

public final class TestDataSeeder {

    private TestDataSeeder() {
    }

    public static TestDataSet seed(SessionFactory sessionFactory) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                deleteAll(session);

                Category televisions = new Category("Телевизоры");
                Category refrigerators = new Category("Холодильники");
                Category dvdPlayers = new Category("DVD-проигрыватели");
                session.persist(televisions);
                session.persist(refrigerators);
                session.persist(dvdPlayers);

                Manufacturer samsung = new Manufacturer("Samsung", "South Korea");
                Manufacturer lg = new Manufacturer("LG", "South Korea");
                Manufacturer sony = new Manufacturer("Sony", "Japan");
                session.persist(samsung);
                session.persist(lg);
                session.persist(sony);

                Product tv = new Product();
                tv.setCategory(televisions);
                tv.setManufacturer(samsung);
                tv.setModelName("Samsung QE55Q70");
                tv.setDescription("QLED TV 55 inches");
                tv.setPrice(new BigDecimal("49990.00"));
                tv.setAssemblyPlace("Slovakia");
                tv.setStockQty(5);
                tv.setWarrantyMonths(24);
                tv.addAttribute("Диагональ", "55");
                tv.addAttribute("Разрешение", "3840x2160");
                session.persist(tv);

                Product fridge = new Product();
                fridge.setCategory(refrigerators);
                fridge.setManufacturer(lg);
                fridge.setModelName("LG DoorCooling");
                fridge.setDescription("Two-door refrigerator");
                fridge.setPrice(new BigDecimal("65990.00"));
                fridge.setAssemblyPlace("Poland");
                fridge.setStockQty(1);
                fridge.setWarrantyMonths(36);
                fridge.addAttribute("No Frost", "Да");
                fridge.addAttribute("Цвет", "Белый");
                session.persist(fridge);

                Product dvd = new Product();
                dvd.setCategory(dvdPlayers);
                dvd.setManufacturer(sony);
                dvd.setModelName("Sony DVP-SR760H");
                dvd.setDescription("DVD player with HDMI");
                dvd.setPrice(new BigDecimal("6990.00"));
                dvd.setAssemblyPlace("China");
                dvd.setStockQty(10);
                dvd.setWarrantyMonths(12);
                dvd.addAttribute("Интерфейсы", "HDMI");
                session.persist(dvd);

                Customer firstCustomer = new Customer(
                        "Иванов Иван Иванович",
                        "+7-900-111-22-33",
                        "ivanov@example.com",
                        "Москва, ул. Пушкина, д. 10"
                );
                Customer secondCustomer = new Customer(
                        "Петрова Анна Сергеевна",
                        "+7-900-444-55-66",
                        "petrova@example.com",
                        "Санкт-Петербург, Невский пр., д. 25"
                );
                session.persist(firstCustomer);
                session.persist(secondCustomer);

                OrderStatus processing = new OrderStatus("в обработке");
                OrderStatus assembled = new OrderStatus("собран");
                OrderStatus delivered = new OrderStatus("поставлен");
                session.persist(processing);
                session.persist(assembled);
                session.persist(delivered);

                CustomerOrder existingOrder = new CustomerOrder(
                        secondCustomer,
                        assembled,
                        LocalDateTime.of(2026, 2, 21, 12, 40)
                );
                existingOrder.addItem(dvd, 1);
                existingOrder.setDelivery(new Delivery(
                        "Санкт-Петербург, Невский пр., д. 25",
                        LocalDateTime.of(2026, 2, 22, 10, 0),
                        LocalDateTime.of(2026, 2, 22, 14, 0)
                ));
                session.persist(existingOrder);

                session.flush();
                transaction.commit();

                return new TestDataSet(
                        televisions.getId(),
                        refrigerators.getId(),
                        dvdPlayers.getId(),
                        samsung.getId(),
                        lg.getId(),
                        sony.getId(),
                        tv.getId(),
                        fridge.getId(),
                        dvd.getId(),
                        firstCustomer.getId(),
                        secondCustomer.getId(),
                        existingOrder.getId()
                );
            } catch (RuntimeException exception) {
                transaction.rollback();
                throw exception;
            }
        }
    }

    private static void deleteAll(Session session) {
        session.createQuery("delete from Delivery").executeUpdate();
        session.createQuery("delete from OrderItem").executeUpdate();
        session.createQuery("delete from CustomerOrder").executeUpdate();
        session.createQuery("delete from ProductAttribute").executeUpdate();
        session.createQuery("delete from Product").executeUpdate();
        session.createQuery("delete from Customer").executeUpdate();
        session.createQuery("delete from OrderStatus").executeUpdate();
        session.createQuery("delete from Manufacturer").executeUpdate();
        session.createQuery("delete from Category").executeUpdate();
    }
}
