package logic;

import lenz.htw.krub.world.GraphNode;

public class Helper {

    //return the exact GraphNode the bot is currently on, based on his current position
    public static GraphNode getGraphNode(GraphNode[] graphNodes, float[] pos) {
        GraphNode closestNode = null;
        float minDistanceSquared = Float.MAX_VALUE;

        for (GraphNode node : graphNodes) {
            float distanceSquared = squaredDistance(node, pos);
            if (distanceSquared < minDistanceSquared) {
                closestNode = node;
                minDistanceSquared = distanceSquared;
            }
        }

        return closestNode;
    }

    // avoid unnecessary sqrt call
    private static float squaredDistance(GraphNode node, float[] pos) {
        float dx = node.x - pos[0];
        float dy = node.y - pos[1];
        float dz = node.z - pos[2];
        return dx * dx + dy * dy + dz * dz;
    }

    public static float calculateDistance(float[] pos1, float[] pos2) {
        float dx = pos1[0] - pos2[0];
        float dy = pos1[1] - pos2[1];
        float dz = pos1[2] - pos2[2];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float calculateDistance(GraphNode a, GraphNode b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float calculateDistance(GraphNode node, float x, float y, float z) {
        float dx = node.getX() - x;
        float dy = node.getY() - y;
        float dz = node.getZ() - z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float dotProduct(float[] vector1, float[] vector2) {
        return vector1[0] * vector2[0] + vector1[1] * vector2[1] + vector1[2] * vector2[2];
    }

    public static float[] crossProduct(float[] vector1, float[] vector2) {
        float[] result = new float[3];
        result[0] = vector1[1] * vector2[2] - vector1[2] * vector2[1];
        result[1] = vector1[2] * vector2[0] - vector1[0] * vector2[2];
        result[2] = vector1[0] * vector2[1] - vector1[1] * vector2[0];
        return result;
    }

    public static float magnitude(float[] vector) {
        return (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
    }

    public static float[] normalize(float[] vector) {
        float magnitude = magnitude(vector);
        float[] normalized = new float[3];

        for (int i = 0; i < vector.length; i++) {
            normalized[i] /= magnitude;
        }

        return normalized;
    }
}
