package org.example.yuna.alg;

import org.example.yuna.data.GraphData;
import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Node;
import org.example.yuna.utils.MyComparator;

import java.util.*;

import static org.example.yuna.conf.Setting.isRushHour;


/**
 * Step1-Initial Route Search
 */
public class RouteSearch {
    public ArrayList<Integer> paths = new ArrayList<Integer>();
    public  ArrayList<Float> times = new ArrayList<Float>();
    public 	ArrayList<Edge> crossEdges = new ArrayList<Edge>();

    public boolean loop = false ;

    /**
     * initialize the start and the end
     * @param start the source
     * @param end the destination
     * @param t the departure time
     * @param g the Graph
     */
    public static void setRoot(int start, int end, float t, GraphData g)
    {

        ArrayList<Node> nodes = g.nodes;
        nodes.get(start).pre = -1;
        nodes.get(start).sDist = 0;
        nodes.get(start).currentTime = t;
        nodes.get(end).dDist = 0;

    }


    //SBP path search
    public static int go(int start, int end, float t, GraphData g,
                         Boolean isSelfAware, HashMap<Edge, ArrayList<Flow>> nodeMap)
    {
        ArrayList<Node> nodes = g.nodes;
        Queue<Node> open = new PriorityQueue<Node>(MyComparator.openComparator);//Sort by sDist+dDist, and calculate it through traffic
        setRoot(start, end, t, g);

        open.add(nodes.get(start));

        while(!open.isEmpty())
        {
            Node currentNode = open.poll();
            ArrayList<Edge> edges = g.edges.get(currentNode);
            if(currentNode.nodeId == end)
            {
                return 1;
            }
            float currentTime = currentNode.currentTime;
            for(int i=0;i<edges.size();i++)
            {
                Edge curEdge = edges.get(i);
                Node outNode = curEdge.getEndNode();
                float curEdgeCrossTime = isSelfAware?curEdge.getCrossTime(currentTime, nodeMap, isRushHour):curEdge.getCrossTime(currentTime, 0, isRushHour);
                if(currentNode.sDist+curEdgeCrossTime<outNode.sDist)
                {
                    outNode.sDist = currentNode.sDist+curEdgeCrossTime;
                    outNode.pre = currentNode.nodeId;
                    outNode.currentTime = currentTime + curEdgeCrossTime;
                    if(!open.contains(outNode))
                    {
                        open.add(outNode);
                    }
                }
            }
        }
        return 0;
    }


    /**
     * Back to find path
     * @param start  start node
     * @param end end node
     * @param t start time
     * @param g graph
     * @param isSelfAware if aware
     * @param nodeMap flow map
     * @return
     */
    public boolean travel(int start, int end, float t, GraphData g,
                          Boolean isSelfAware, HashMap<Edge, ArrayList<Flow>> nodeMap)
    {
        ArrayList<Node> nodes = g.nodes;
        int res = go(start, end,t, g, isSelfAware,nodeMap);

        if(res == 0)
        {
            System.out.println(start+"->"+end+"is not connected");
        }else {
            Node curNode = nodes.get(end);
            while(curNode.nodeId != start)
            {
                if(paths.contains(curNode.nodeId))
                {
                    return true;
                }
                paths.add(0,curNode.nodeId);
                times.add(0,curNode.currentTime);

                Node preNode = nodes.get(curNode.pre);
                for (int i = 0; i < g.edges.get(preNode).size(); i++) {
                    if(g.edges.get(preNode).get(i).getEndNode() == nodes.get(curNode.nodeId)){
                        crossEdges.add(0,g.edges.get(preNode).get(i));
                        break;
                    }
                }

                curNode = nodes.get(curNode.pre);
            }
            paths.add(0,start);
            times.add(0,t);
        }
        return false;
    }


    //our path search method
    public static int searchPath(int start, int end, float t, GraphData g, Boolean isSelfAware,HashMap<Edge, ArrayList<Flow>> nodeMap)
    {
        ArrayList<Node> nodes = g.nodes;
        HashMap<Node, ArrayList<Edge>> map = g.edges;
        Queue<Node> open = new PriorityQueue<Node>(MyComparator.openComparator);
        setRoot(start, end, t, g);
        open.add(nodes.get(start));

        Set<Node> close = new HashSet<>();

        while(!open.isEmpty())
        {
            Node currentNode = open.poll();
            close.add(currentNode);
            if(currentNode.nodeId == end)
            {
                return 1;
            }
            float currentTime = currentNode.currentTime;
            ArrayList<Edge> adjEdges = map.get(currentNode);
            for(int i=0;i<adjEdges.size();i++)
            {
                Edge curEdge = adjEdges.get(i);
                Node outNode = curEdge.getEndNode();
                if(close.contains(outNode)){
                    continue;
                }

                float curEdgeCrossTime = isSelfAware?curEdge.getCrossTime(currentTime, nodeMap, isRushHour):curEdge.getCrossTime(currentTime, 0, isRushHour);
                if(!open.contains(outNode)||(currentNode.sDist+curEdgeCrossTime<outNode.sDist))
                {
                    outNode.sDist = currentNode.sDist+curEdgeCrossTime;
                    outNode.pre = currentNode.nodeId;
                    outNode.currentTime = currentTime + curEdgeCrossTime;
                    open.add(outNode);
                }
            }
        }
        return 0;
    }

    /**
     * Back to find path IA*
     * @param start  start node
     * @param end end node
     * @param t start time
     * @param g graph
     * @param isSelfAware if aware
     * @param nodeMap flow map
     * @return
     */
    public boolean getPath(int start, int end, float t, GraphData g,Boolean isSelfAware ,HashMap<Edge, ArrayList<Flow>> nodeMap)
    {
        ArrayList<Node> nodes = g.nodes;
        int res = searchPath(start, end,t, g,isSelfAware,nodeMap);

        if(res == 0)
        {
            System.out.println(start+"->"+end+"is not connected");
        }else {
            Node curNode = nodes.get(end);
            while(curNode.nodeId != start)
            {

                if(paths.contains(curNode.nodeId))
                {
                    return true;
                }
                paths.add(0,curNode.nodeId);
                times.add(0,curNode.currentTime);

                Node preNode = g.nodes.get(curNode.pre);
                for (int i = 0; i < g.edges.get(preNode).size(); i++) {
                    if(g.edges.get(preNode).get(i).getEndNode() == g.nodes.get(curNode.nodeId)){
                        crossEdges.add(0,g.edges.get(preNode).get(i));
                        break;
                    }
                }
                curNode = nodes.get(curNode.pre);
            }
            paths.add(0,start);
            times.add(0,t);
        }
        return false;
    }


}
