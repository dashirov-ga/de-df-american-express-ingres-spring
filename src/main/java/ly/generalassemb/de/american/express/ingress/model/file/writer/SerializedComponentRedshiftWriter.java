package ly.generalassemb.de.american.express.ingress.model.file.writer;

import com.amazonaws.services.s3.AmazonS3URI;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SerializedComponentRedshiftWriter implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializedComponentRedshiftWriter.class);

    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private String schemaName;
    private String redshiftS3AccessCredentials;


    /**
     * Given a single componsnt and corresponding content, without asking too many questions,
     * serialize to S3 and return the component and corresponding S3 URL back
     *
     * @param entry - component, and corresponding conent
     * @return - component, and corresponding S3 url it was stored to
     */
    public void serializeToRedshift(SerializedComponent<AmazonS3URI> entry) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement setPath = connection.createStatement();
        setPath.execute("SET SEARCH_PATH TO " + schemaName + ",public; ");
        setPath.close();

        FixedWidthDataFileComponent component = entry.getType();
        AmazonS3URI uri = entry.getPayload();
        String targetTable = component.getDefaultTableName();

        connection.setAutoCommit(false);
        List<String> statements = new ArrayList<>();
        statements.add(
                "CREATE TEMPORARY TABLE temp_" + targetTable +
                        "( LIKE " + targetTable + "  );"
        );
        String s3Location = uri.getURI().toString();
        try {
            s3Location = java.net.URLDecoder.decode(uri.getURI().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e){
            LOGGER.error(e.getMessage());
        }

        statements.add(
                "COPY  temp_" + targetTable +
                        " FROM '" + s3Location + "'\n " + // TODO: This is beyond stupid <---
                        " CREDENTIALS '" + getRedshiftS3AccessCredentials() + "' " +
                        " CSV IGNOREHEADER 1 DATEFORMAT 'YYYY-MM-DD' TIMEFORMAT 'YYYY-MM-DD HH24:MI:SS';"
        );
        statements.add(
                "DELETE FROM " + targetTable +
                        " WHERE EXISTS ( SELECT 1 FROM temp_" + targetTable +
                        " WHERE " +
                        component.getPrimaryKey()
                                .stream()
                                .map(primaryKey -> String.format("temp_%s.%s=%s.%s", targetTable, primaryKey, targetTable, primaryKey))
                                .collect(Collectors.joining(" AND "))
                        + ");"
        );
        statements.add(
                "INSERT INTO " + targetTable +
                        " select t.* " +
                        " from temp_" + targetTable + " t;"
        );
        statements.add(
                "DROP TABLE IF EXISTS  temp_" + targetTable + ";"
        );

        List<Statement> statement = new ArrayList<>(); // need these to close them after exec
        try {
            connection.setAutoCommit(false);
            for (String query : statements) {
                Statement st = connection.createStatement();
                statement.add(st);
                boolean is_ok = st.execute(query);
                LOGGER.info("QUERY: {}\nOK: {}\n UPDATED: {}", query, is_ok, st.getUpdateCount());
            }
            connection.commit();
        } catch (SQLException sqlException) {
            printSQLException(sqlException);
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                printSQLException(rollbackException);
            }
        } finally {
            for (Statement st : statement)
                if (st != null)
                    st.close();
            connection.setAutoCommit(true);
        }
    }

    private static boolean ignoreSQLException(String sqlState) {
        /*
        Redshift SQL State codes are closely mimicked by
        https://www.postgresql.org/docs/9.6/static/errcodes-appendix.html
         */
        if (sqlState == null) {
            LOGGER.warn("The SQL state is not defined!");
            return false;
        }

        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42P07"))
            return true;

        return false;
    }

    private static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(
                        ((SQLException) e).
                                getSQLState())) {

                    e.printStackTrace(System.err);
                    LOGGER.error("SQLState: " +
                            ((SQLException) e).getSQLState());

                    LOGGER.error("Error Code: " +
                            ((SQLException) e).getErrorCode());

                    LOGGER.error("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while (t != null) {
                        LOGGER.error("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getRedshiftS3AccessCredentials() {
        return redshiftS3AccessCredentials;
    }

    public void setRedshiftS3AccessCredentials(String redshiftS3AccessCredentials) {
        this.redshiftS3AccessCredentials = redshiftS3AccessCredentials;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(dataSource, "Input resource must be set");
        Assert.notNull(schemaName, "schema name must be set");
        Assert.notNull(redshiftS3AccessCredentials, "redshift copy/load credentials must be set");
    }
}


