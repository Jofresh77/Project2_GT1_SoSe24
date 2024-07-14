package bot;

import lenz.htw.krub.world.GraphNode;
import logic.Cluster;

import java.util.*;

import static logic.Helper.*;

public abstract class Bot {
    // <editor-fold defaultstate="collapsed" desc="Properties">
    public final int playerId;
    public final int botNr;
    protected final GraphNode[] blankGraph;

    public float[] position;
    public float[] previousPosition;
    public float[] direction;
    public GraphNode goal;

    public float totalDistanceTraveled = 0f;
    public int numUpdates = 0;
    public boolean isStuck = false;

    private static final float MAX_CHECK_DISTANCE = 0.1f;
    private static final float MAX_CHECK_ANGLE = (float) Math.toRadians(45);
    private static final float DISTANCE_THRESHOLD = 0.01f;
    // </editor-fold>

    public Bot(int playerId, int botNr, float[] position, float[] direction, GraphNode[] blankGraph) {
        this.playerId = playerId;
        this.botNr = botNr;

        this.position = position;
        this.previousPosition = position;
        this.direction = direction;
        this.goal = null;

        this.blankGraph = blankGraph;
    }

    // <editor-fold defaultstate="collapsed" desc="goal-definition">
    protected abstract Cluster getRelevantCluster(List<Cluster> clusters, int highestScoreOpponent);

    public void setGoal(List<Cluster> clusters, int highestScoreOpponent) {
        Cluster relevantCluster = getRelevantCluster(clusters, highestScoreOpponent);
        if (relevantCluster == null) {
            this.goal = null;
            return;
        }

        if (isOnGoal(relevantCluster)) {
            this.goal = getClosestEdgeNode(relevantCluster.nodes()); //try to stay on target cluster's edge
        } else {
            this.goal = getClosestNode(relevantCluster.nodes()); //try to get ASAP to target cluster
        }
    }

    protected boolean isOnGoal(Cluster goal) {
        GraphNode position = getGraphNode(blankGraph, this.position);

        for (GraphNode node : goal.nodes()) {
            if (position.equals(node))
                return true;
        }

        return false;
    }

    protected Cluster getLargestCluster(List<Cluster> clusters) {
        Cluster largest = new Cluster(-1, new ArrayList<>());

        for (Cluster cluster : clusters) {
            if (cluster.nodes().size() > largest.nodes().size()) {
                largest = cluster;
            }
        }

        return largest;
    }

    protected GraphNode getClosestNode(List<GraphNode> nodes) {
        GraphNode closest = null;
        float minDistance = Float.MAX_VALUE;

        for (GraphNode node : nodes) {
            float distance = calculateDistance(node, this.position[0], this.position[1], this.position[2]);
            if (distance < minDistance
                    && (noBlockersAround(node) || !node.isBlocked())) {
                closest = node;
                minDistance = distance;
            }
        }

        return closest;
    }

    protected GraphNode getClosestEdgeNode(List<GraphNode> nodes) {
        GraphNode closest = null;
        float minDistance = Float.MAX_VALUE;

        for (GraphNode node : nodes) {
            float distance = calculateDistance(node, this.position[0], this.position[1], this.position[2]);

            if (isEdgeNode(node)
                    && distance < minDistance
                    && (noBlockersAround(node) || !node.isBlocked())) {
                closest = node;
                minDistance = distance;
            }
        }

        return closest;
    }

    private boolean isEdgeNode(GraphNode node) {
        for (GraphNode neighbor : node.getNeighbors()) {
            if (neighbor.getOwner() != node.getOwner()) {
                return true;
            }
        }

        return false;
    }

    // a target-node with blocker neighbors might help the bot to get stuck faster, so we avoid them
    private boolean noBlockersAround(GraphNode node) {
        return Arrays.stream(node.getNeighbors()).noneMatch(GraphNode::isBlocked);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="obstacle-avoidance">
    public float detectAndAvoidObstacles(GraphNode[] blankGraph) {
        List<GraphNode> blockers = findBlockersInSight(blankGraph);
        if (blockers.isEmpty()) {
            return 0;
        }
        return calculateAvoidanceAngle(blockers);
    }

    //Usage of 'Breadth First Search' algorithm to find blockers in front of bot's current direction
    private List<GraphNode> findBlockersInSight(GraphNode[] blankGraph) {
        List<GraphNode> blockers = new ArrayList<>();
        GraphNode currentNode = getGraphNode(blankGraph, position);

        Queue<GraphNode> nodesToCheck = new LinkedList<>();
        Set<GraphNode> visitedNodes = new HashSet<>();

        nodesToCheck.offer(currentNode);
        visitedNodes.add(currentNode);

        while (!nodesToCheck.isEmpty()) {
            GraphNode node = nodesToCheck.poll();

            if (node != currentNode && isNodeInViewCone(node) && isNodeWithinDistance(node)) {
                if (node.isBlocked()) {
                    blockers.add(node);
                }

                for (GraphNode neighbor : node.getNeighbors()) {
                    if (!visitedNodes.contains(neighbor)) {
                        nodesToCheck.offer(neighbor);
                        visitedNodes.add(neighbor);
                    }
                }
            }
        }

        return blockers;
    }

    private boolean isNodeInViewCone(GraphNode node) {
        float[] directionToNode = {
                node.getX() - position[0],
                node.getY() - position[1],
                node.getZ() - position[2]
        };
        float dotProduct = dotProduct(direction, directionToNode);
        float angle = (float) Math.acos(dotProduct / (magnitude(direction) * magnitude(directionToNode)));
        return angle <= MAX_CHECK_ANGLE;
    }

    private boolean isNodeWithinDistance(GraphNode node) {
        return calculateDistance(node, position[0], position[1], position[2]) <= MAX_CHECK_DISTANCE;
    }

    private float calculateAvoidanceAngle(List<GraphNode> blockers) {
        float[] averageBlockerDirection = {0, 0, 0};
        for (GraphNode blocker : blockers) {
            float[] directionToBlocker = {
                    blocker.getX() - position[0],
                    blocker.getY() - position[1],
                    blocker.getZ() - position[2]
            };
            averageBlockerDirection[0] += directionToBlocker[0];
            averageBlockerDirection[1] += directionToBlocker[1];
            averageBlockerDirection[2] += directionToBlocker[2];
        }

        averageBlockerDirection = normalize(averageBlockerDirection);

        float[] localUp = normalize(position);

        float[] avoidanceDirection = crossProduct(averageBlockerDirection, localUp);
        avoidanceDirection = normalize(avoidanceDirection);

        float dotProduct = dotProduct(direction, avoidanceDirection);
        float angle = (float) Math.acos(dotProduct);

        float[] cross = crossProduct(direction, avoidanceDirection);
        float sign = Math.signum(dotProduct(cross, localUp));

        return angle * sign;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="stuck-detection">
    public void incrementTraveledDistance(float[] oldPos, float[] newPos) {
        float distance = calculateDistance(oldPos, newPos);
        totalDistanceTraveled += distance;
        numUpdates++;
    }

    public void updateStuckState() {
        isStuck = totalDistanceTraveled / numUpdates < DISTANCE_THRESHOLD;
    }
    // </editor-fold>

    //method is used if a goal has been set up to return the bot's steering angle
    //based on the angle between bot forward direction vector and to goal-node direction vector
    public float calculateSteeringAngle() {
        float[] directionToTarget = {
                goal.getPosition()[0] - position[0],
                goal.getPosition()[1] - position[1],
                goal.getPosition()[2] - position[2]
        };

        float magnitude = (float) Math.sqrt(
                directionToTarget[0] * directionToTarget[0] +
                        directionToTarget[1] * directionToTarget[1] +
                        directionToTarget[2] * directionToTarget[2]
        );
        directionToTarget[0] /= magnitude;
        directionToTarget[1] /= magnitude;
        directionToTarget[2] /= magnitude;

        float angleRad = (float) Math.acos(dotProduct(direction, directionToTarget));

        float[] crossProduct = crossProduct(direction, directionToTarget);
        float sign = Math.signum(crossProduct[0] + crossProduct[1] + crossProduct[2]);

        return angleRad * sign;
    }
}
