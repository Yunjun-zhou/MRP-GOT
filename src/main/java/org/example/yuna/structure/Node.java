package org.example.yuna.structure;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.Objects;

@Data
@AllArgsConstructor
public class Node {
    public int nodeId;

    public Location location;
    public float sDist = 1000000f;
    public float dDist = 1000000f;
    public int pre = -1;
    public float currentTime = -1;

    public Node(int nodeId, float dDist) {
        this.nodeId = nodeId;
        this.dDist = dDist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return nodeId == node.nodeId;
    }


    @Override
    public String toString() {
        return "Node{" +
                "nodeId=" + nodeId +
                ", sDist=" + sDist +
                ", dDist=" + dDist +
                ", pre=" + pre +
                ", currentTime=" + currentTime +
                '}';
    }
}
