package com.oheers.fish.database.execute;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.connection.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class ExecuteUpdate extends ExecuteBase {

    protected ExecuteUpdate(final ConnectionFactory connectionFactory, final Settings settings) {
        super(connectionFactory, settings);
    }


    /**
     * Executes an update operation and returns the number of rows affected.
     */
    public int executeUpdate() {
        try (Connection connection = getConnection()) {
            DSLContext dslContext = getContext(connection);
            return onRunUpdate(dslContext);
        } catch (SQLException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE,"Update execution failed", e);
            return 0;
        }
    }

    /**
     * Executes an update operation within a transaction.
     */
    public void executeInTransaction() {
        try (Connection connection = getConnection()) {
            DSLContext dslContext = getContext(connection);
            dslContext.transaction(configuration -> {
                DSLContext transactionalDsl = DSL.using(configuration);
                onRunUpdate(transactionalDsl);
            });
        } catch (SQLException e) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE,"Transactional update execution failed", e);
        }
    }

    /**
     * Abstract method for performing the update operation.
     * Implementations must define the logic to execute the update.
     *
     * @param dslContext the DSLContext used for executing the update
     * @return the number of rows affected by the update
     */
    protected abstract int onRunUpdate(DSLContext dslContext);
}
