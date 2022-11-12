package org.example.yuna.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static org.example.yuna.conf.Setting.alpha;
import static org.example.yuna.conf.Setting.beta;

@Data
@AllArgsConstructor
public class Edge {
    Node startNode;
    Node endNode;
    float minCrossTime;
    int capacity;



    public void showInfo()
    {
        System.out.println(startNode.nodeId+"->"+endNode.nodeId+" minCrossTime: "+minCrossTime+" capacity: "+capacity);
    }

    /**
     * Get the non-query traffic flow of current time
     * @param currentTime current time
     * @capacity balance the averaged flow
     * @return the non-query traffic flow of current time
     */
    public  int calcNonQueryFlow(float currentTime, boolean isRushHour)
    {
        int capacity_balance;
        if(isRushHour)
        {
            capacity_balance = (int)(capacity*0.5);
        }else {
            capacity_balance = (int)(capacity*0.3);
        }
        return (int)(0.2*capacity_balance*Math.sin(Math.PI/60*currentTime)+capacity_balance);
    }

    /**
     *  calculate the extra travel time caused by planned routes
     * @param currentFlow current flow
     * @return the extra travel time caused by planned routes
     */
    public float calcExtraCost(float currentFlow)
    {

        float cost = (float)Math.pow(currentFlow/capacity, beta)*minCrossTime*alpha;
        return cost;
    }

    // Calculate the travel time to pass through the edge based on the current time
    public float getCrossTime(float currentTime,  HashMap<Edge, ArrayList<Flow>> map1, boolean isRushHour)
    {
        int affectCount = 0;
        ArrayList<Flow> userNodeInfos = map1.get(this);
        if(userNodeInfos != null)
        {
            for (Flow flow:userNodeInfos){
                if(flow.getArriveTime()<=currentTime && flow.getProcessTime()+flow.getArriveTime()>=currentTime)
                {
                    affectCount ++;
                }
            }

        }
        int currentFlow = calcNonQueryFlow(currentTime, isRushHour) + affectCount;
        float delayTime = calcExtraCost(currentFlow);
        return minCrossTime + delayTime;
    }



    // Calculate the travel time to pass through the edge based on the current time
    public float getCrossTime(float currentTime, int affectCount, boolean isRushHour)
    {
        int currentFlow = calcNonQueryFlow(currentTime, isRushHour) + affectCount;
        float delayTime = calcExtraCost(currentFlow);
        return minCrossTime + delayTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return startNode.equals(edge.startNode) && endNode.equals(edge.endNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNode, endNode);
    }
}
