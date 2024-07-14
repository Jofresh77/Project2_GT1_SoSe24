package bot;

import lenz.htw.krub.world.GraphNode;
import logic.Cluster;

import java.util.List;

public class Bot1 extends Bot {
    public Bot1(int playerId, int botNr, float[] position, float[] direction, GraphNode[] blankGraph) {
        super(playerId, botNr, position, direction, blankGraph);
    }

    @Override
    protected Cluster getRelevantCluster(List<Cluster> clusters, int ignore) {
        List<Cluster> filteredClusters = clusters.stream()
                .filter(c -> c.ownership() == -1)
                .toList();

        return getLargestCluster(filteredClusters);
    }
}
