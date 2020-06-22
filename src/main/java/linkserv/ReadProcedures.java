package linkserv;

import models.OutlinkNode;
import models.RootNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.procedure.*;
import constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReadProcedures {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "linkserv.getRootNode", mode = Mode.READ)
    public Stream<RootNode> getRootNode(@Name("url") String url,
                                        @Name("timestamp") String timestamp) {

        String[] queryFragments = new String[]{"{", Constants.versionProperty, ":\"", timestamp, "\"})"};
        return buildQueryandExecuteGetRootNodes(url, queryFragments).stream();
    }

    @Procedure(value = "linkserv.getRootNodes", mode = Mode.READ)
    public Stream<RootNode> getRootNodes(@Name("url") String url,
                                         @Name("startTimestamp") String startTimestamp,
                                         @Name("endTimestamp") String endTimestamp) {

        String[] queryFragments = new String[]{") \n WHERE apoc.date.fromISO8601(v.", Constants.versionProperty,
                ") >= apoc.date.fromISO8601(\"", startTimestamp, "\") AND apoc.date.fromISO8601(v.",
                Constants.versionProperty, ") <= apoc.date.fromISO8601(\"", endTimestamp,
                "\")"};

        return buildQueryandExecuteGetRootNodes(url, queryFragments).stream();
    }

    private ArrayList<RootNode> buildQueryandExecuteGetRootNodes(String url, String[] queryFragments) {
        ArrayList<RootNode> rootNodes = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("");
        String[] matchFragments = {"MATCH (parent:", Constants.parentNodeLabel, "{", Constants.nameProperty,
                ":\"", url, "\"})-[:", Constants.versionRelationshipType, "]->(v:",
                Constants.versionNodeLabel,};
        String[] returnFragments = {"\n RETURN parent.", Constants.nameProperty, ", v.",
                Constants.versionProperty, ", ID(v);"};
        String query;

        List<String> queryFragmentsList;
        queryFragmentsList = new ArrayList<>(Arrays.asList(matchFragments));
        queryFragmentsList.addAll(Arrays.asList(queryFragments));
        queryFragmentsList.addAll(Arrays.asList(returnFragments));

        for (String fragment : queryFragmentsList) {
            queryBuilder.append(fragment);
        }
        query = queryBuilder.toString();

        Result result = db.beginTx().execute(query);
        while (result.hasNext()) {
            rootNodes.add(new RootNode(result.next()));
        }
        return rootNodes;
    }

    @Procedure(value = "linkserv.getOutlinkNodes", mode = Mode.READ)
    public Stream<OutlinkNode> getOutlinkNodes(@Name("nodeName") String nodeName, @Name("timestamp") String nodeVersion) {
        ArrayList<OutlinkNode> outlinkNodes = new ArrayList<>();
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

        Result result = db.beginTx().execute(query);
        while (result.hasNext()) {
            outlinkNodes.add(new OutlinkNode(result.next()));
        }
        return outlinkNodes.stream();
    }
}