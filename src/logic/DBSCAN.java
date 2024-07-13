package logic;

import lenz.htw.krub.world.GraphNode;

import java.util.*;

import static logic.Helper.calculateDistance;

public class DBSCAN {
    private static final float EPSILON = 0.5f;
    private static final int MIN_PTS = 300;

    public static List<Cluster> getClusters(GraphNode[] nodes) {
        List<Cluster> clusters = new ArrayList<>();
        Set<GraphNode> visited = new HashSet<>();

        for (GraphNode node : nodes) {
            if (!visited.contains(node)) {
                List<GraphNode> clusterNodes = expandCluster(node, visited);
                if (clusterNodes != null) {
                    clusters.add(new Cluster(clusterNodes.get(0).getOwner(), clusterNodes));
                }
            }
        }

        return clusters;
    }

    private static List<GraphNode> expandCluster(GraphNode node, Set<GraphNode> visited) {
        List<GraphNode> cluster = new ArrayList<>();
        Queue<GraphNode> queue = new LinkedList<>();
        queue.offer(node);
        visited.add(node);

        while (!queue.isEmpty()) {
            GraphNode currentNode = queue.poll();
            cluster.add(currentNode);

            for (GraphNode neighbor : currentNode.getNeighbors()) {
                if (!visited.contains(neighbor) && isSimilar(currentNode, neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return cluster.size() >= MIN_PTS ? cluster : null;
    }

    private static boolean isSimilar(GraphNode a, GraphNode b) {
        return a.getOwner() == b.getOwner() && calculateDistance(a, b) <= EPSILON;
    }

    public static GraphNode computeClusterCentroid(Cluster cluster) {
        if (cluster == null || cluster.nodes().isEmpty()) {
            return null;
        }

        float sumX = 0, sumY = 0, sumZ = 0;
        for (GraphNode node : cluster.nodes()) {
            sumX += node.getX();
            sumY += node.getY();
            sumZ += node.getZ();
        }

        int size = cluster.nodes().size();
        float avgX = sumX / size;
        float avgY = sumY / size;
        float avgZ = sumZ / size;

        GraphNode centroid = cluster.nodes().get(0);
        float minDistance = Float.MAX_VALUE;

        for (GraphNode node : cluster.nodes()) {
            float distance = calculateDistance(node, avgX, avgY, avgZ);
            if (distance < minDistance) {
                minDistance = distance;
                centroid = node;
            }
        }

        return centroid;
    }

    public static List<GraphNode> computeAllClusterCentroids(List<Cluster> clusters) {
        List<GraphNode> centroids = new ArrayList<>();
        for (Cluster cluster : clusters) {
            GraphNode centroid = computeClusterCentroid(cluster);
            if (centroid != null) {
                centroids.add(centroid);
            }
        }
        return centroids;
    }
}