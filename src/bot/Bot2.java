package bot;

import lenz.htw.krub.world.GraphNode;
import logic.Cluster;

import java.util.List;

import static logic.Helper.calculateDistance;

public class Bot2 extends Bot {
    public Bot2(int playerId, int botNr, float[] position, float[] direction, GraphNode[] blankGraph) {
        super(playerId, botNr, position, direction, blankGraph);
    }

    @Override
    protected Cluster getRelevantCluster(List<Cluster> clusters, int highestScoreOpponent) {
        List<Cluster> filteredClusters = clusters.stream()
                .filter(c -> c.ownership() == highestScoreOpponent)
                .toList();

        return getLargestCluster(filteredClusters);
    }
}
