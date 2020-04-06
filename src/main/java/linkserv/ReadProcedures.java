package linkserv;

import models.OutlinkNode;
import models.RootNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.*;
import constants.Constants;
import java.util.stream.Stream;

public class ReadProcedures {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "linkserv.getRootNode", mode = Mode.READ)
    public Stream<RootNode> getRootNode(@Name("url") String url, @Name("timestamp") String timestamp) {

        String[] queryFragments = new String[]{"MATCH (parent:", Constants.parentNodeLabel, ")-[:", Constants.versionRelationshipType, "]->(v:",
                Constants.versionNodeLabel, "{", Constants.versionProperty, ":\"", timestamp, "\"}) WHERE parent.", Constants.nameProperty,
                " =~ '(?i)", url, "' RETURN parent.", Constants.nameProperty, ", v.", Constants.versionProperty, ", ID(v);"};

        StringBuilder queryBuilder = new StringBuilder("");
        String query;

        for (String fragment : queryFragments) {
            queryBuilder.append(fragment);
        }

        query = queryBuilder.toString();

        return db.execute(query).stream().map(RootNode::new);
    }

    @Procedure(value = "linkserv.getOutlinkNodes", mode = Mode.READ)
    public Stream<OutlinkNode> getOutlinkNodes(@Name("nodeName") String nodeName, @Name("timestamp") String nodeVersion) {

        String[] queryFragments = new String[]{"MATCH (parent1:", Constants.parentNodeLabel, " {", Constants.nameProperty, ": \"", nodeName,
                "\"})-[:", Constants.versionRelationshipType, "]->(version1:", Constants.versionNodeLabel, " {", Constants.versionProperty, ": \"",
                nodeVersion, "\"})-[r:", Constants.linkRelationshipType, "]->(parent2:", Constants.parentNodeLabel,
                ") OPTIONAL MATCH (parent2)-[:", Constants.versionRelationshipType, "]->(version2:", Constants.versionNodeLabel, " {",
                Constants.versionProperty, ":\"", nodeVersion, "\"}) RETURN ID(parent2), parent2.", Constants.nameProperty,
                ", ID(version1), ID(r), ID(version2), version2.", Constants.versionProperty, ";"};

        StringBuilder queryBuilder = new StringBuilder("");
        String query;

        for (String fragment : queryFragments) {
            queryBuilder.append(fragment);
        }
        query = queryBuilder.toString();

        return db.execute(query).stream().map(OutlinkNode::new);
    }
}