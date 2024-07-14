package bot;

import lenz.htw.krub.world.GraphNode;
import logic.Cluster;
import logic.Helper;

import java.util.Comparator;
import java.util.List;

import static logic.Helper.calculateDistance;
import static logic.Helper.getGraphNode;

public class Bot0 extends Bot {
    public float[] bot1pos;

    public Bot0(int playerId, int botNr, float[] position, float[] direction, GraphNode[] blankGraph) {
        super(playerId, botNr, position, direction, blankGraph);
    }

    @Override
    protected Cluster getRelevantCluster(List<Cluster> clusters, int paramIgnore) {
        List<Cluster> filteredClusters = clusters.stream()
                .filter(c -> c.ownership() == playerId)
                .toList();
        GraphNode bot1Node = getGraphNode(blankGraph, bot1pos);

        if (filteredClusters.size() == 1)
            return filteredClusters.get(0);

        return filteredClusters.stream()
                .min(Comparator.comparingDouble(cluster ->
                        cluster.nodes().stream()
                                .mapToDouble(node -> Helper.calculateDistance(node, bot1Node))
                                .min()
                                .orElse(Double.MAX_VALUE)
                ))
                .orElse(null);
    }
}
