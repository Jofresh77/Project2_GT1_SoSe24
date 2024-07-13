import bot.Bot;
import bot.Bot0;
import bot.Bot1;
import bot.Bot2;
import lenz.htw.krub.net.NetworkClient;
import lenz.htw.krub.world.GraphNode;
import logic.Cluster;

import java.util.ArrayList;
import java.util.List;

import static logic.DBSCAN.getClusters;

public class Client {
    public static Client self;

    public final int id;
    final NetworkClient net;

    public int lastUpdate;
    public GraphNode[] blankGraph;
    public List<GraphNode> blockers;
    public List<Cluster> clusters;

    public Bot bot0;
    public Bot bot1;
    public Bot bot2;

    private static final float DISTANCE_THRESHOLD = 0.05f;

    public Client(String nameNb) {
        net = new NetworkClient(null, "NotAnAI" + nameNb, "It was consciously though");
        id = net.getMyPlayerNumber();
        System.out.println(id);

        blankGraph = net.getGraph();
        blockers = new ArrayList<>();

        for (GraphNode graphNode : blankGraph) {
            if (graphNode.blocked) {
                blockers.add(graphNode);
            }
        }

        bot0 = new Bot0(id, 0, net.getBotPosition(id, 0), net.getBotDirection(0), blankGraph);
        bot1 = new Bot1(id, 1, net.getBotPosition(id, 1), net.getBotDirection(1), blankGraph);
        bot2 = new Bot2(id, 2, net.getBotPosition(id, 2), net.getBotDirection(2), blankGraph);
    }

    public static void main(String[] args) {
        self = new Client(args[0]);
        self.play();
    }

    public void play() {
        lastUpdate = net.getMostRecentUpdateId();
        long lastClusterTime = System.currentTimeMillis();
        long lastObstacleCheckTime = System.currentTimeMillis();
        long lastStuckCheckTime = System.currentTimeMillis();

        try {
            while (net.isGameRunning()) {
                if (net.getMostRecentUpdateId() == 0) continue;

                long currentTime = System.currentTimeMillis();

                if (currentTime - lastObstacleCheckTime >= 100) {
                    lastObstacleCheckTime = currentTime;

                    updateData(false);

                    float avoidanceAngle0 = bot0.detectAndAvoidObstacles(blankGraph);
                    if (avoidanceAngle0 != 0)
                        net.changeMoveDirection(0, avoidanceAngle0);

                    float avoidanceAngle1 = bot1.detectAndAvoidObstacles(blankGraph);
                    if (avoidanceAngle1 != 0)
                        net.changeMoveDirection(1, avoidanceAngle1);

                    continue;
                }

                if (currentTime - lastStuckCheckTime >= 1500) {
                    lastStuckCheckTime = currentTime;

                    updateData(false);

                    checkAndTurnIfStuck(bot0);
                    checkAndTurnIfStuck(bot1);

                    continue;
                }

                if (currentTime - lastClusterTime >= 3000) {
                    lastClusterTime = currentTime;

                    updateData(true);

                    if (bot0.goal != null) {
                        net.changeMoveDirection(0, bot0.calculateSteeringAngle());
                    }

                    if (bot1.goal != null) {
                        net.changeMoveDirection(1, bot1.calculateSteeringAngle());
                    }

                    if (bot2.goal != null) {
                        net.changeMoveDirection(2, bot2.calculateSteeringAngle());
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateData(boolean updateClusters) {
        if (updateClusters) {

            clusters = getClusters(net.getGraph());

            int highestScoreOpponent = getHighestScoreOpponent();

            bot0.setGoal(clusters, highestScoreOpponent);
            bot1.setGoal(clusters, highestScoreOpponent);
            bot2.setGoal(clusters, highestScoreOpponent);
        }

        float[] newBot0Position = net.getBotPosition(id, 0);
        bot0.position = newBot0Position;
        bot0.direction = net.getBotDirection(0);

        float[] newBot1Position = net.getBotPosition(id, 1);
        bot1.position = newBot0Position;
        bot1.direction = net.getBotDirection(1);

        float[] newBot2Position = net.getBotPosition(id, 2);
        bot2.position = newBot0Position;
        bot2.direction = net.getBotDirection(2);


        bot0.incrementTraveledDistance(newBot0Position);
        bot1.incrementTraveledDistance(newBot1Position);
        bot2.incrementTraveledDistance(newBot2Position);
    }

    private void checkAndTurnIfStuck(Bot bot) {
        float averageDistance = bot.totalDistanceTraveled / bot.numUpdates;
        if (averageDistance < DISTANCE_THRESHOLD)
            net.changeMoveDirection(bot.botNr, (float) Math.PI);

        bot.totalDistanceTraveled = 0;
        bot.numUpdates = 0;
    }

    private int getHighestScoreOpponent() {
        int highestScore = 0;
        int opponentId = -1;

        for (int i = 0; i < 3; i++) {
            if (i != id) {
                int score = net.getScore(i);
                if (score > highestScore) {
                    highestScore = score;
                    opponentId = i;
                }
            }
        }

        return opponentId;
    }
}
