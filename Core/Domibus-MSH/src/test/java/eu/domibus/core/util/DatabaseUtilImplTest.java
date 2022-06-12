package eu.domibus.core.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseUtilImplTest {

    @InjectMocks
    private DatabaseUtilImpl databaseUtil = new DatabaseUtilImpl();

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @Test
    public void getDatabaseUserName() throws Exception {
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);
        Mockito.when(databaseMetaData.getUserName()).thenReturn("current_db_user");

        databaseUtil.init();

        Mockito.verify(connection).close();
        Assert.assertEquals("Should have returned the correct user name", "current_db_user", databaseUtil.getDatabaseUserName());
    }

    @Test(expected = IllegalStateException.class)
    public void getDatabaseUserName_throwsExceptionWhenFailingToAquireConnection() throws Exception {
        Mockito.when(dataSource.getConnection()).thenThrow(new SQLException());

        databaseUtil.init();
    }
}