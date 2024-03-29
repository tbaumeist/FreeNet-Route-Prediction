package frp.routing;

import java.util.*;

import frp.utils.CircleList;

public class Topology {

    private List<Node> nodes = new CircleList<Node>();

    private final double NOT_INITED_LOC = -1.0;

    public Node findNode(double location, String id) {
        Node tmp = new Node(location, id);
        if (!this.nodes.contains(tmp))
            return null;

        return this.nodes.get(this.nodes.indexOf(tmp));
    }

    public List<Node> getAllNodes() {
        return this.nodes;
    }

    @Override
    public String toString() {
        String out = "";
        for (Node n : getAllNodes()) {
            out += n + " -> (";
            for (Node n2 : n.getDirectNeighbors()) {
                out += " " + n2 + ",";
            }
            out += ")\n";
        }
        return out;
    }

    public void addNode(Node node) {
        if (this.nodes.contains(node))
            return;
        if (node.getLocation() == NOT_INITED_LOC)
            return;

        this.nodes.add(node);
        Collections.sort(this.nodes);
    }

    public void clearOneWayConnections() {
        List<Node> removeNodes = new ArrayList<Node>();

        for (Node n : getAllNodes()) {
            List<Node> removeNeighbors = new ArrayList<Node>();

            for (Node n2 : n.getDirectNeighbors()) {
                if (!n2.getDirectNeighbors().contains(n))
                    removeNeighbors.add(n2);
            }
            n.removeNeighbors(removeNeighbors);
            if (n.getDirectNeighbors().isEmpty())
                removeNodes.add(n);
        }
        getAllNodes().removeAll(removeNodes);
    }
}
