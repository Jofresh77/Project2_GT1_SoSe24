package logic;

import lenz.htw.krub.world.GraphNode;

import java.util.*;

import static logic.Helper.calculateDistance;

public class ClusterAnalyzer {

    private static final int NUM_COLORS = 3;  // Number of colors (excluding neutral)
    private static final float SIMILARITY_THRESHOLD = 0.01f; // Adjust this value to control cluster merging
    private static final float ALPHA = 0.2f; // Adjust this value to tune the balance

    private static class Cluster {
        List<GraphNode> nodes;
        GraphNode centroid;
        int majorityColor;

        Cluster(GraphNode node) {
            this.nodes = new ArrayList<>();
            this.nodes.add(node);
            this.centroid = node;
            this.majorityColor = node.getOwner() + 1; // Adjust for neutral color
        }

        // Method to merge with another cluster
        void merge(Cluster otherCluster) {
            this.nodes.addAll(otherCluster.nodes);
            this.centroid = calculateCentroid(this.nodes);
            this.majorityColor = calculateMajorityColor(this.nodes);
        }
    }

    private static class ClusterPair implements Comparable<ClusterPair> {
        int cluster1Index;
        int cluster2Index;
        float distance;

        ClusterPair(int cluster1Index, int cluster2Index, float distance) {
            this.cluster1Index = cluster1Index;
            this.cluster2Index = cluster2Index;
            this.distance = distance;
        }

        @Override
        public int compareTo(ClusterPair other) {
            return Float.compare(this.distance, other.distance);
        }
    }

    private static float calculateClusterSimilarity(Cluster cluster1, Cluster cluster2, Map<ClusterPair, Float> similarityCache) {
        ClusterPair key = new ClusterPair(cluster1.nodes.hashCode(), cluster2.nodes.hashCode(), 0);
        if (similarityCache.containsKey(key)) {
            return similarityCache.get(key);
        } else {
            float colorSimilarity = (cluster1.majorityColor == cluster2.majorityColor) ? 0 : 1;

            float spatialDistance = calculateDistance(cluster1.centroid.getPosition(), cluster2.centroid.getPosition());

            // Normalize spatialDistance
            float maxSpatialDistance = (float) Math.sqrt(12);
            spatialDistance /= maxSpatialDistance;

            // Combine normalized values using a weighted average
            float similarity = (1 - ALPHA) * colorSimilarity + ALPHA * (1 / (1 + spatialDistance));
            similarityCache.put(key, similarity);
            return similarity;
        }
    }

    private static int calculateMajorityColor(List<GraphNode> cluster) {
        int[] colorCounts = new int[NUM_COLORS + 1]; // +1 to include neutral

        for (GraphNode node : cluster) {
            int owner = node.getOwner();
            colorCounts[owner + 1]++; // Increment count at index (owner + 1) to account for -1 (neutral)
        }

        // Find the index of the color with the highest count
        int majorityColorIndex = 0; // Start with neutral
        int maxCount = colorCounts[0]; // Count for neutral
        for (int i = 1; i < colorCounts.length; i++) { // Start from 1 to skip neutral in this loop
            if (colorCounts[i] > maxCount) {
                maxCount = colorCounts[i];
                majorityColorIndex = i;
            }
        }

        return majorityColorIndex - 1; // Convert back to original color representation (-1 for neutral)
    }

    public static List<GraphNode> performAHC(GraphNode[] graphNodes) {
        List<Cluster> clusters = new ArrayList<>();
        for (GraphNode node : graphNodes) {
            clusters.add(new Cluster(node)); // Create clusters directly
        }

        PriorityQueue<ClusterPair> pq = new PriorityQueue<>();
        Map<ClusterPair, Float> similarityCache = new HashMap<>();

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = i + 1; j < clusters.size(); j++) {
                float distance = calculateClusterSimilarity(clusters.get(i), clusters.get(j), similarityCache);
                pq.offer(new ClusterPair(i, j, distance));
            }
        }

        while (clusters.size() > 1) {
            ClusterPair closestPair = pq.poll();
            int cluster1Index = closestPair.cluster1Index;
            int cluster2Index = closestPair.cluster2Index;
            float distance = closestPair.distance;

            System.out.println(distance);
            if (distance > SIMILARITY_THRESHOLD) {
                break;
            }

            clusters.get(cluster1Index).merge(clusters.get(cluster2Index));
            clusters.remove(cluster2Index);

            for (ClusterPair pair : new ArrayList<>(pq)) {
                if (pair.cluster1Index == cluster2Index || pair.cluster2Index == cluster2Index) {
                    pq.remove(pair);
                } else {
                    if (pair.cluster1Index > cluster2Index) {
                        pair.cluster1Index--;
                    }
                    if (pair.cluster2Index > cluster2Index) {
                        pair.cluster2Index--;
                    }

                    float newDistance = calculateClusterSimilarity(clusters.get(pair.cluster1Index), clusters.get(pair.cluster2Index), similarityCache);
                    pq.remove(pair);
                    pq.offer(new ClusterPair(pair.cluster1Index, pair.cluster2Index, newDistance));
                }
            }
        }

        List<GraphNode> centroids = new ArrayList<>();
        for (Cluster cluster : clusters) {
            centroids.add(cluster.centroid);
        }
        return centroids;
    }


    public static GraphNode calculateCentroid(List<GraphNode> cluster) {
        float xSum = 0, ySum = 0, zSum = 0;
        for (GraphNode node : cluster) {
            xSum += node.getX();
            ySum += node.getY();
            zSum += node.getZ();
        }

        GraphNode closestNode = null;
        float minDistanceSquared = Float.MAX_VALUE;
        for (GraphNode node : cluster) {
            float distanceSquared = (node.getX() - xSum / cluster.size()) * (node.getX() - xSum / cluster.size()) +
                    (node.getY() - ySum / cluster.size()) * (node.getY() - ySum / cluster.size()) +
                    (node.getZ() - zSum / cluster.size()) * (node.getZ() - zSum / cluster.size());
            if (distanceSquared < minDistanceSquared) {
                closestNode = node;
                minDistanceSquared = distanceSquared;
            }
        }

        return closestNode;
    }
}
