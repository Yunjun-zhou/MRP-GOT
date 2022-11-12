package org.example.yuna.utils;

import org.example.yuna.data.GraphData;
import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Node;
import org.example.yuna.structure.Query;

import java.util.Comparator;

import static org.example.yuna.conf.Setting.*;

public class MyComparator {
    // User-User comparator
    public static Comparator<Query> cQuery= (o1, o2) -> {
        if(o1.currentTime>o2.currentTime)
        {
            return 1;
        }else if(o1.currentTime<o2.currentTime)
        {
            return -1;
        }
        else {
            return 0;
        }
    };

    // UserNodeInfo-UserNodeInfo Comparator
    public static Comparator<Flow> nodeInfoComparator = (u1, u2) -> {
        if(u1.getArriveTime()+u1.getProcessTime()>u2.getArriveTime()+u2.getProcessTime())
        {
            return 1;
        }else if(u1.getArriveTime()+u1.getProcessTime()<u2.getArriveTime()+u2.getProcessTime())
        {
            return -1;
        }else {
            return 0;
        }
    };


    //Node-Node comparator
    public static Comparator<Node> openComparator = (o1, o2) -> {
        if(o1.sDist+o1.dDist >o2.sDist+o2.dDist)//+o2.dDist
        {
            return 1;
        }else if(o1.sDist+o1.dDist<o2.sDist+o2.dDist){//+o2.dDist
            return -1;
        }else {
            return 0;
        }
    };

    public static Comparator<Query> disComparator = ((o1, o2) -> {
        GraphData graphData = GraphData.getInstance(edgeFile,nodeFile,miniFile,false);
        Node s1 = graphData.nodes.get(o1.getStartVertexId());
        Node d1 = graphData.nodes.get(o1.getEndVertexId());

        Node s2 = graphData.nodes.get(o2.getStartVertexId());
        Node d2 = graphData.nodes.get(o2.getEndVertexId());

        if(DistanceUtil.getDistance(s1,d1)>DistanceUtil.getDistance(s2,d2)){
            return -1;
        }else if(DistanceUtil.getDistance(s1,d1)==DistanceUtil.getDistance(s2,d2)){
            return 0;
        }else {
            return 1;
        }
    });


}
