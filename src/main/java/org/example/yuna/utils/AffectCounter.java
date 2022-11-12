package org.example.yuna.utils;

import org.example.yuna.structure.Flow;
import java.util.PriorityQueue;

public class AffectCounter {
    /**
     * Calculate the number of flows on each edge
     * @param inNodesInfos the edge labels of planned routes
     * @param currentUserArrTime the current time
     * @return the traffic flow caused by planned routes at time "currentUserArrTime"
     */
    public static int calcAffectCount(PriorityQueue<Flow> inNodesInfos, float currentUserArrTime)
    {
        int affectCount = 0;
        if(inNodesInfos != null)
        {
            while(!inNodesInfos.isEmpty())
            {
                Flow prior = inNodesInfos.poll();
                if(currentUserArrTime <= prior.getArriveTime()+ prior.getProcessTime())
                {
                    inNodesInfos.add(prior);
                    break;
                }
            }
            affectCount = inNodesInfos.size();

        }
        return affectCount;
    }

}
