package ru.msu.internetshop.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {

    private static SessionFactory sessionFactory;

    private HibernateUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null || sessionFactory.isClosed()) {
            sessionFactory = buildSessionFactory("hibernate.cfg.xml");
        }
        return sessionFactory;
    }

    public static SessionFactory buildSessionFactory(String configurationResource) {
        try {
            return new Configuration().configure(configurationResource).buildSessionFactory();
        } catch (Throwable exception) {
            throw new ExceptionInInitializerError(
                    "Failed to build SessionFactory from " + configurationResource + ": " + exception.getMessage()
            );
        }
    }

    public static void shutdown() {
        close(sessionFactory);
        sessionFactory = null;
    }

    public static void close(SessionFactory sessionFactory) {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}
