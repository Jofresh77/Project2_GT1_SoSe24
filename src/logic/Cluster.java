package logic;

import lenz.htw.krub.world.GraphNode;

import java.util.List;

public record Cluster(int ownership, List<GraphNode> nodes) {}
