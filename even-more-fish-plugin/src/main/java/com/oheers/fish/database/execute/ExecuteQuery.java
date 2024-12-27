package com.oheers.fish.database.execute;


import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.connection.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;

import java.sql.Connection;
import java.util.logging.Level;

public abstract class ExecuteQuery<T> extends ExecuteBase {

    protected ExecuteQuery(final ConnectionFactory connectionFactory, final Settings settings) {
        super(connectionFactory, settings);
    }

    protected ExecuteQuery(final ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }
    /**
     * Prepares and executes a query, returning the result.
     * In case of failure, returns the fallback result from `empty()`.
     */
    public T prepareAndRunQuery() {
        try (Connection connection = getConnection()) {
            DSLContext dslContext = getContext(connection);
            return onRunQuery(dslContext);
        } catch (Exception e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE,"Query execution failed", e);
            return empty();
        }
    }

    /**
     * Abstract method for running the query logic.
     *
     * @param dslContext the DSLContext used for executing the query
     * @return the query result
     * @throws Exception if an error occurs during query execution
     */
    protected abstract T onRunQuery(DSLContext dslContext) throws Exception;

    /**
     * Provides a fallback result to return in case of errors or no results.
     *
     * @return the fallback result
     */
    protected abstract T empty();
}
