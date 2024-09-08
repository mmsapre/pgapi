
            package com.example.dsl;

            import java.sql.Connection;
            import java.sql.PreparedStatement;
            import java.sql.ResultSet;
            import java.sql.SQLException;
            import java.util.List;

            public class DslQueryExecutor {

                private Connection connection;

                public DslQueryExecutor(Connection connection) {
                    this.connection = connection;
                }

                public ResultSet executeQuery(String sqlQuery, List<Object> parameters) throws SQLException {
                    PreparedStatement statement = connection.prepareStatement(sqlQuery);

                    // Bind parameters
                    for (int i = 0; i < parameters.size(); i++) {
                        statement.setObject(i + 1, parameters.get(i));
                    }

                    return statement.executeQuery();
                }
            }
            