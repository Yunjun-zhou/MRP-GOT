package org.example.yuna.utils;

import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Node;
import org.example.yuna.structure.Query;

import java.util.*;

/**
 * A set of methods that shows the experimental results
 * Including calculating a total time to query
 */
public class Shower {
    public static void showInfo(ArrayList<Node> nodes)
    {
        for(int i=0;i<nodes.size();i++)
        {
            Node cuNode = nodes.get(i);
            System.out.println(cuNode.nodeId +" "+cuNode.sDist+" "+cuNode.dDist+ " Pre:"+cuNode.pre+" curTime: "+cuNode.currentTime);
        }
        System.out.println();
    }

    public static void show(ArrayList<Query> greedyUsers) {
        System.out.println();
        for (Query u : greedyUsers) {
            u.showInfo();
            System.out.println("-----------------");
        }
    }

    public static void show(HashMap<String, PriorityQueue<Flow>> hashmap)
    {
        System.out.println();
        for(Map.Entry<String, PriorityQueue<Flow>> entry:hashmap.entrySet())
        {
            for(Flow info:entry.getValue())
            {
                info.showInfo();
            }
        }
    }

    public static void show(Queue<Query> greedyUsers) {
        System.out.println();
        for (Query u : greedyUsers) {
            u.showInfo();
            System.out.println(u.currentTime);
            System.out.println("-----------------");
        }
    }

    public static void showMap(HashMap<String, ArrayList<Flow>> hashmap)
    {
        System.out.println();
        for(Map.Entry<String, ArrayList<Flow>> entry:hashmap.entrySet())
        {
            for(Flow info:entry.getValue())
            {
                info.showInfo();
            }
        }
    }

    /**
     * Calculate Total Travel Time of all queries
     * @param users
     * @return
     */
    public static float time(List<Query> users) {
        float totalTime = 0;
        for (int i = 0; i < users.size(); i++) {
            totalTime += users.get(i).totalTimeCost();
        }
        return totalTime/60f;
    }

    public static int flowsInReal(ArrayList<Query> users){
        int sum = 0;
        for (Query q:users
             ) {
            sum+=q.crossEdges.size();
        }
        return sum;
    }
    public static boolean isHaveLoop(ArrayList<Integer> pathIds){
        Set<Integer> set = new HashSet<>();
        for (int i : pathIds) {
            if (set.contains(i)) {
                return true;
            }
            set.add(i);
        }
        return false;
    }

    /**
     * Copy users to operate
     * @param users
     * @return
     */
    public  static ArrayList<Query> copyUser(List<Query> users) {
        ArrayList<Query> copyUsers = new ArrayList<Query>();
        for (int i = 0; i < users.size(); i++) {
            copyUsers.add(users.get(i).copy());
        }
        return copyUsers;
    }
}
