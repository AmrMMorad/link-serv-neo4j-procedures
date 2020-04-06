package linkserv;

import models.Output;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class WriteProcedures {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "linkserv.addNodesAndRelationships", mode = Mode.WRITE)
    public Stream<Output> addNodesAndRelationships(@Name("url") String url, @Name("timestamp") String timestamp, @Name("outlinks") List outlinks){

        StringBuilder queryBuilder = new StringBuilder("");
        ArrayList<String> queryFragments = new ArrayList<>();
        String query;

        // Create the ParentNode with its version
        List<String> queryFragmentsList = Arrays.asList("MERGE (parent:", Constants.parentNodeLabel, " {", Constants.nameProperty, ":\"",
                url, "\"}) MERGE (parent)-[:", Constants.versionRelationshipType, "]->(version:", Constants.versionNodeLabel,
                " {", Constants.versionProperty, ":\"", timestamp, "\"}) ");
        queryFragments.addAll(queryFragmentsList);

        // Create relationships with outlinks
        for (int i = 0; i < outlinks.size(); i++) {
            queryFragmentsList = Arrays.asList("MERGE (n", String.valueOf(i), ":", Constants.parentNodeLabel, " {", Constants.nameProperty,
                    ":\"", String.valueOf(outlinks.get(i)), "\"}) ", "MERGE (version)-[:", Constants.linkRelationshipType,
                    "]->(n", String.valueOf(i), ") ");
            queryFragments.addAll(queryFragmentsList);
        }

        for (String fragment : queryFragments) {
            queryBuilder.append(fragment);
        }
        queryBuilder.append("RETURN parent." + Constants.nameProperty + ";");

        query = queryBuilder.toString();
        return db.execute(query).stream().map(Output::new);
    }
}
