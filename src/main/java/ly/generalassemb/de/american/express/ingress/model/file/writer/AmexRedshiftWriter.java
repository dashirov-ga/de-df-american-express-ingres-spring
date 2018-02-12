package ly.generalassemb.de.american.express.ingress.model.file.writer;


import com.amazonaws.services.s3.AmazonS3URI;
import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer.SerializedComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AmexRedshiftWriter implements ItemWriter<List<SerializedComponent<AmazonS3URI>>>{
    private static final Logger LOGGER = LoggerFactory.getLogger(AmexRedshiftWriter.class);

    // place anything you may need subsequent seps to see onto stepExecution
    @NotNull
    private StepExecution stepExecution;

    private List<SerializedComponent<AmazonS3URI>> s3SerializedComponents;

    @NotNull
    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @NotNull
    private String schemaName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @NotNull
    private String redshiftS3AccessCredentials;

    public String getRedshiftS3AccessCredentials() {
        return redshiftS3AccessCredentials;
    }

    public void setRedshiftS3AccessCredentials(String redshiftS3AccessCredentials) {
        this.redshiftS3AccessCredentials = redshiftS3AccessCredentials;
    }

    @NotNull
    private Set<FixedWidthDataFileComponent> includeFilter;
    public Set<FixedWidthDataFileComponent> getIncludeFilter() {
        return includeFilter;
    }
    public void setIncludeFilter(Set<FixedWidthDataFileComponent> includeFilter) {
        this.includeFilter = includeFilter;
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
                            ((SQLException)e).getSQLState());

                    LOGGER.error("Error Code: " +
                            ((SQLException)e).getErrorCode());

                    LOGGER.error("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while(t != null) {
                        LOGGER.error("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public void write(List<? extends List<SerializedComponent<AmazonS3URI>>> items) throws Exception {
        List<SerializedComponent<AmazonS3URI>> wantedComponents
                = items
                .stream().flatMap(Collection::stream)
                .filter(item -> includeFilter.contains(item.getType()))
                .collect(Collectors.toList());

        Connection connection = dataSource.getConnection();
        connection.setSchema(schemaName);
        Statement setPath = connection.createStatement();
        setPath.execute("SET SEARCH_PATH TO "+ schemaName +",public; ");
        setPath.close();

        List<String> statements = new ArrayList<>();
        for (SerializedComponent<AmazonS3URI> entry : wantedComponents) {
            FixedWidthDataFileComponent component = entry.getType();
            AmazonS3URI uri = entry.getPayload();
            String targetTable = component.getDefaultTableName();

            statements.add(
                    "CREATE TEMPORARY TABLE temp_" + targetTable +
                            "( LIKE " + targetTable + "  );"
            );

            statements.add(
                    "COPY  temp_" + targetTable +
                            " FROM '" + java.net.URLDecoder.decode( uri.getURI().toString(), "UTF-8") + "'\n " + // TODO: This is beyond stupid <---
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
            LOGGER.warn(statements.stream().collect(Collectors.joining("\n")));
        }
        List<Statement> statement = new ArrayList<>(); // need these to close them after exec
        try {
            connection.setAutoCommit(false);
            for (String query: statements) {
                Statement st = connection.createStatement();
                statement.add(st);
                boolean is_ok = st.execute(query);
                LOGGER.debug("QUERY: {}\nOK: {}\n UPDATED: {}", query, is_ok , st.getUpdateCount());
            }
            connection.commit();
        } catch (SQLException sqlException){
            printSQLException(sqlException);
            try {
                connection.rollback();
            } catch ( SQLException rollbackException){
                printSQLException(rollbackException);
            }
        } finally {
            for (Statement st: statement)
                if (st!=null)
                    st.close();
            connection.setAutoCommit(true);
        }
    }
}
