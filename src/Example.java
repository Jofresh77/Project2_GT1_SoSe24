import lenz.htw.krub.net.NetworkClient;
import lenz.htw.krub.world.GraphNode;

public class Example {
    public static void main(String[] args) {
        NetworkClient client = new NetworkClient(null, "TL", "YAY!");

        client.getMyPlayerNumber();
        client.getScore(0);

        client.getBotSpeed(0); // konstante pro bot.Bot
        client.getMostRecentUpdateId(); // wenn sich diese Nummer ändert, gab es neue Daten

        float[] botPosition = client.getBotPosition(0, 0); // x,y,z Punkt auf Kugeloberfläche
        float[] botDirection = client.getBotDirection(0); // x,y,z Tangentenvektor

        client.changeMoveDirection(2, 0, 0, 0); // Wunschrichtungsvektor
        client.changeMoveDirection(0, 0);

        GraphNode[] graph = client.getGraph();
        graph[0].isBlocked();    //Hindernis?
        graph[0].getOwner();    //wer hat die Mehrheit an dieser Stelle?
        graph[0].getPosition();    //x,y,z 3D-Koordinate des Knotens
        GraphNode[] neighbors = graph[0].getNeighbors();
    }
}
