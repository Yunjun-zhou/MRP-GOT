package org.example.yuna.data;

import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Query;
import org.example.yuna.utils.AffectCounter;
import org.example.yuna.utils.MyComparator;

import java.util.*;

import static org.example.yuna.conf.Setting.isRushHour;

public class FlowsNetwork {


    // PriorityQueue to update
    public static Queue<Query> greedyQueryQueue = new PriorityQueue<Query>(MyComparator.cQuery);

    //Record the traffic information of the current road network
    public static HashMap<Edge,PriorityQueue<Flow>> roadFlows = new HashMap<>();


    /**
     * use query generate flows
     * @param users
     * @param map
     * @return
     */
    public static  HashMap<Edge, ArrayList<Flow>> generateQueryInfos(List<Query> users,HashMap<Edge, ArrayList<Flow>> map) {
        HashMap<Edge, ArrayList<Flow>> maps = new HashMap<Edge, ArrayList<Flow>>();
        for (Edge t:map.keySet()){
            maps.put(t, (ArrayList<Flow>) map.get(t).clone());
        }
        if(users != null){
            for (int userCount = 0; userCount < users.size(); userCount++) {
                Query user = users.get(userCount);
                ArrayList<Float> arriveTime = user.crossNodeTimes;
                for (int i = 0; i < user.crossNodeTimes.size() - 1; i++) {
                    float arrTime = arriveTime.get(i);
                    float processTime;
                    processTime = arriveTime.get(i + 1) - arriveTime.get(i);
                    Edge thisEdge = user.crossEdges.get(i);
                    if (!maps.containsKey(thisEdge)) {
                        maps.put(thisEdge, new ArrayList<Flow>());
                    }
                    maps.get(thisEdge).add(new Flow(arrTime, processTime));
                }
            }
        }
        return maps;
    }

    public static  HashMap<Edge,PriorityQueue<Flow>> generatePriorQueryInfos(List<Query> users) {
        HashMap<Edge,PriorityQueue<Flow>> map = new HashMap<Edge,PriorityQueue<Flow>>();
        for (int userCount = 0; userCount < users.size(); userCount++) {
            Query user = users.get(userCount);
            ArrayList<Float> arriveTime = user.crossNodeTimes;
            for (int i = 0; i < user.crossNodeTimes.size() - 1; i++) {
                float arrTime = arriveTime.get(i);
                float processTime;
                processTime = arriveTime.get(i + 1) - arriveTime.get(i);
                Edge thisEdge = user.crossEdges.get(i);
                if (!map.containsKey(thisEdge)) {
                    map.put(thisEdge, new PriorityQueue<>(MyComparator.nodeInfoComparator));
                }
                map.get(thisEdge).add(new Flow(arrTime, processTime));
            }
        }
        return map;
    }

    /**
     * Update current flow situation in road network
     */
    public static void Update() {
        while (!greedyQueryQueue.isEmpty()) {//
            // 1.  select a query
            Query query = greedyQueryQueue.poll();
            int currentIndex = query.getCurrentIndex();
            float arrTime = query.currentTime;


            // 2. expansion
            Edge currentEdge = query.crossEdges.get(currentIndex);
            int affectCount = AffectCounter.calcAffectCount(roadFlows.get(currentEdge), arrTime);//
            float nextCost = currentEdge.getCrossTime(arrTime, affectCount, isRushHour);


            // update the records of edge labels
            if (!roadFlows.containsKey(currentEdge)) {
                roadFlows.put(currentEdge, new PriorityQueue<>(MyComparator.nodeInfoComparator));
            }
            roadFlows.get(currentEdge).add(new Flow(arrTime, nextCost));

            // update the records of the query
            query.crossNodeTimes.set(currentIndex + 1, arrTime + nextCost);
            query.setCurrentIndex(currentIndex + 1);//Set the current time, the point where the arrival

            int nextNodeId = query.currentVertex;
            if (nextNodeId == query.endVertexId) {
                query.setCurrentIndex(0);
            } else {
                greedyQueryQueue.add(query);
            }
        }
        roadFlows = new HashMap<>();
    }

    public  static void clearFlow(){
        roadFlows.clear();
        greedyQueryQueue.clear();
    }

    public static int calcFlowsNum(HashMap<Edge, ArrayList<Flow>> flows){
        int num = 0;
        for (Map.Entry<Edge, ArrayList<Flow>> entry:flows.entrySet()){
            num+=entry.getValue().size();
        }
        return num;
    }


}
