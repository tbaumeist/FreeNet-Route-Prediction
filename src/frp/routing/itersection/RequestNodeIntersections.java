package frp.routing.itersection;

import java.util.*;
import frp.routing.*;

public class RequestNodeIntersections implements Comparable<Object> {
    private Path requestPath;
    private Node intersectNode;
    private double confidence;
    private SubRange intersectionRange;
    private List<Node> possibleTargetNodes = new ArrayList<Node>();

    public RequestNodeIntersections(Path insertPath, Path requestPath,
            Node intersectNode) {
        this.requestPath = requestPath;
        this.confidence = insertPath.getPathConfidence()
                * requestPath.getPathConfidence();
        this.intersectionRange = requestPath.getRange().getIntersection(
                insertPath.getRange());
        this.intersectNode = intersectNode;
        for (Node n : requestPath.getNodes()) {
            if (n.equals(intersectNode)) {
                // The intersect node can be used in the attack
                this.possibleTargetNodes.add(n);
                break;
            }
            if (n.equals(getStartNode()))
                continue;
            this.possibleTargetNodes.add(n);
        }
    }

    public Path getRequestPath() {
        return this.requestPath;
    }

    public Node getIntersectNode() {
        return this.intersectNode;
    }

    public Node getStartNode() {
        return this.requestPath.getStartNode();
    }

    public List<Node> getPossibleTargetNodes() {
        return this.possibleTargetNodes;
    }

    public SubRange getIntersectSubRange() {
        return this.intersectionRange;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public boolean targetNodeNotConnectedToRequestNodeExists() {
        Node start = this.getStartNode();
        for (Node n : this.getPossibleTargetNodes()) {
            if (!start.hasDirectNeighbor(n))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String s = "Intersect Range: " + this.intersectionRange
                + ", Request Node: " + getStartNode();
        s += ", Request Range: " + this.requestPath.getRange();
        s += ", Path: " + this.requestPath.toStringSimple();
        s += ", Target Nodes: {";
        for (Node n : this.possibleTargetNodes) {
            s += n + ", ";
        }

        s += "}, Confidence: " + this.confidence;

        return s;
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == null)
            return 1;
        if (!(obj instanceof RequestNodeIntersections))
            return 1;

        RequestNodeIntersections node = (RequestNodeIntersections) obj;
        return new Double(node.confidence)
                .compareTo(new Double(this.confidence));
    }
}
