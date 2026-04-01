package ru.msu.internetshop.dao;

import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public abstract class AbstractDao {

    private final SessionFactory sessionFactory;

    protected AbstractDao(SessionFactory sessionFactory) {
        if (sessionFactory == null) {
            throw new IllegalArgumentException("SessionFactory must not be null");
        }
        this.sessionFactory = sessionFactory;
    }

    protected <T> T executeRead(Function<Session, T> action) {
        try (Session session = sessionFactory.openSession()) {
            return action.apply(session);
        }
    }

    protected <T> T executeInTransaction(Function<Session, T> action) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                T result = action.apply(session);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) {
                rollbackQuietly(transaction);
                throw exception;
            }
        }
    }

    private void rollbackQuietly(Transaction transaction) {
        try {
            transaction.rollback();
        } catch (RuntimeException ignored) {
            // The original exception is more useful for the caller.
        }
    }
}
