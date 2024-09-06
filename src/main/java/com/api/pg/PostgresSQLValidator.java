package com.api.pg;

import com.api.query.PgRestLexer;
import com.api.query.PgRestParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class PostgresSQLValidator {

    private Map<String, TableConfig> tableConfigMap;

    @Autowired
    private QueryConfig queryConfig;  // Inject QueryConfig to access allowed tables and columns

    public PostgresSQLValidator(Map<String, TableConfig> tableConfigMap) {
        this.tableConfigMap = tableConfigMap;
    }

    public boolean isTableAllowed(String tableName) {
        // Check if the table is allowed
        if (!queryConfig.isTableAllowed(tableName)) {
            throw new IllegalArgumentException("Table " + tableName + " is not allowed.");
        }
        return true;
    }

    public void validateQuery(String query) throws Exception {
        CharStream input = CharStreams.fromString(query);
        PgRestLexer lexer = new PgRestLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PgRestParser parser = new PgRestParser(tokens);

        ParseTree tree = parser.query();
        ParseTreeWalker walker = new ParseTreeWalker();

        PostgresSQLCustomListener listener = new PostgresSQLCustomListener(queryConfig);
        walker.walk(listener, tree);
    }
}
