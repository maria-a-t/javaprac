package ru.msu.internetshop.support;

import org.hibernate.SessionFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import ru.msu.internetshop.util.HibernateUtil;

public abstract class HibernateDaoTestSupport {

    protected SessionFactory sessionFactory;
    protected TestDataSet dataSet;

    @BeforeClass(alwaysRun = true)
    public void createSessionFactory() {
        sessionFactory = HibernateUtil.buildSessionFactory("hibernate-test.cfg.xml");
    }

    @AfterClass(alwaysRun = true)
    public void closeSessionFactory() {
        HibernateUtil.close(sessionFactory);
    }

    @BeforeMethod(alwaysRun = true)
    public void seedDatabase() {
        dataSet = TestDataSeeder.seed(sessionFactory);
    }
}
