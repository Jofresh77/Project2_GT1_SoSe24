package bot;

import lenz.htw.krub.world.GraphNode;
import logic.Cluster;
import logic.DBSCAN;

import java.util.List;

import static logic.Helper.calculateDistance;

public class Bot0 extends Bot{
    public Bot0(int playerId, int botNr, float[] position, float[] direction, GraphNode[] blankGraph) {
        super(playerId, botNr, position, direction, blankGraph);
    }

    @Override
    protected Cluster getRelevantCluster(List<Cluster> clusters, int ignore) {
        return clusters.stream()
                .filter(c -> c.ownership() == -1)
                .findFirst().orElse(null);
    }
}
