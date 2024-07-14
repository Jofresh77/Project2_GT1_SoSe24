import bot.Bot0;
import bot.Bot1;
import bot.Bot2;
import lenz.htw.krub.net.NetworkClient;
import lenz.htw.krub.world.GraphNode;
import logic.Cluster;

import java.util.List;

import static logic.DBSCAN.getClusters;

public class Client {
    public static Client self;

    public final int id;
    final NetworkClient net;

    public GraphNode[] blankGraph;
    public List<Cluster> clusters;

    public Bot0 bot0;
    public Bot1 bot1;
    public Bot2 bot2;

    public Client(String nameNb) {
        net = new NetworkClient(null, "NotAnAI" + nameNb, "It was consciously though");
        id = net.getMyPlayerNumber();
        System.out.println(id);

        blankGraph = net.getGraph(); //usefull when needing to access persistent data about the map

        bot0 = new Bot0(id, 0, net.getBotPosition(id, 0).clone(), net.getBotDirection(0), blankGraph);
        bot1 = new Bot1(id, 1, net.getBotPosition(id, 1).clone(), net.getBotDirection(1), blankGraph);
        bot2 = new Bot2(id, 2, net.getBotPosition(id, 2).clone(), net.getBotDirection(2), blankGraph);
    }

    public static void main(String[] args) {
        self = new Client(args[0]);
        self.play();
    }

    public void play() {
        long lastClusterTime = System.currentTimeMillis(); //goal update timer
        long lastObstacleCheckTime = System.currentTimeMillis(); //obstacles detection timer
        long lastStuckCheckTime = System.currentTimeMillis(); //stuck state detection timer

        try {
            while (net.isGameRunning()) {
                if (net.getMostRecentUpdateId() == 0) continue; //avoid unnecessary update if world didn't change

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
                }

                //emergency strategy when a Bot get stuck is a 135° turn
                if (currentTime - lastStuckCheckTime >= 1500) {
                    lastStuckCheckTime = currentTime;

                    bot0.updateStuckState();
                    if (bot0.isStuck) {
                        net.changeMoveDirection(0, (float) (3 * Math.PI) / 4);
                    } else {
                        bot0.totalDistanceTraveled = 0;
                        bot0.numUpdates = 0;
                    }

                    bot1.updateStuckState();
                    if (bot1.isStuck) {
                        System.out.println("here");
                        net.changeMoveDirection(1, (float) (3 * Math.PI) / 4);
                    } else {
                        bot1.totalDistanceTraveled = 0;
                        bot1.numUpdates = 0;
                    }

                    updateData(false);

                    continue;
                }

                if (currentTime - lastClusterTime >= 500) {
                    lastClusterTime = currentTime;

                    updateData(true);

                    if (bot0.goal != null
                            && !bot0.isStuck) {
                        net.changeMoveDirection(0, bot0.calculateSteeringAngle());
                    }

                    if (bot1.goal != null
                            && !bot1.isStuck) {
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

        float[] newBot1Position = net.getBotPosition(id, 1).clone();
        bot1.incrementTraveledDistance(bot1.position, newBot1Position);
        bot1.position = newBot1Position;
        bot1.direction = net.getBotDirection(1);

        float[] newBot0Position = net.getBotPosition(id, 0).clone();
        bot0.incrementTraveledDistance(bot0.position, newBot0Position);
        bot0.position = newBot0Position;
        bot0.direction = net.getBotDirection(0);
        bot0.bot1pos = newBot1Position; //bot0 wants to be in bot1's color-region for the strategy

        bot2.position = net.getBotPosition(id, 2);
        bot2.direction = net.getBotDirection(2);
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
