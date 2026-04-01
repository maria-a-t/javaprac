package ru.msu.internetshop.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.testng.annotations.Test;
import ru.msu.internetshop.support.HibernateDaoTestSupport;

public class AbstractDaoTest extends HibernateDaoTestSupport {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "SessionFactory must not be null")
    public void constructorShouldRejectNullSessionFactory() {
        new CustomerDao(null);
    }

    @Test
    public void executeReadShouldWorkForSubclass() {
        TestDao dao = new TestDao(sessionFactory);

        Integer customerCount = dao.read(session ->
                ((Long) session.createQuery("select count(c) from Customer c").uniqueResult()).intValue()
        );

        org.testng.Assert.assertEquals(customerCount.intValue(), 2);
    }

    private static final class TestDao extends AbstractDao {

        private TestDao(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        private <T> T read(java.util.function.Function<Session, T> action) {
            return super.executeRead(action);
        }
    }
}
