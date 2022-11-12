package org.example.yuna.alg;

import org.example.yuna.data.FlowsNetwork;
import org.example.yuna.data.MinTravelTimeData;
import org.example.yuna.data.QueriesSet;
import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Query;
import org.example.yuna.utils.Shower;

import java.util.*;

import static org.example.yuna.alg.Test.*;
import static org.example.yuna.conf.Setting.*;

public class SBP {


    /**
     * Calculate affected users to update
     * @param refiningQuery
     * @param queries
     * @return
     */
    public static ArrayList<Query> getAffectUsers( Query refiningQuery, ArrayList<Query> queries)
    {
        ArrayList<Query> affectedQueries = new ArrayList<>();
        for(int i=0;i<refiningQuery.crossEdges.size();i++)
        {
            Edge affectedEdge = refiningQuery.crossEdges.get(i);
            for(int j=0;j<queries.size();j++)
            {
                Query calUser  = queries.get(j);
                if(calUser.crossEdges.contains(affectedEdge) && !affectedQueries.contains(calUser))
                {
                    for(int index = 0;index<calUser.crossEdges.size();index++)
                    {
                        if(calUser.crossEdges.get(index) == affectedEdge && calUser.crossNodeTimes.get(index)>refiningQuery.crossNodeTimes.get(i))
                        {//Requires passing that edge to be later than refine user
                            affectedQueries.add(calUser);
                            break;
                        }
                    }
                }
            }
        }

        return affectedQueries;
    }

    /**
     * Calculate maxDec:preCheck technique
     * @param copyQuery
     * @param queries
     * @param affectedUsers
     * @return
     */
    public static float calMaxDecrement(Query copyQuery, ArrayList<Query> queries, ArrayList<Query> affectedUsers)
    {
        int minCapacity = Integer.MAX_VALUE;
        int edgeSize = copyQuery.crossEdges.size();
        for(int i=0;i<edgeSize;i++)
        {
            Edge affectedEdge = copyQuery.crossEdges.get(i);
            if(affectedEdge.getCapacity() < minCapacity)
            {
                minCapacity = affectedEdge.getCapacity();
            }
            for(int j=0;j<queries.size();j++)
            {
                Query calQuery  = queries.get(j);
                if(calQuery.crossEdges.contains(affectedEdge) && !affectedUsers.contains(calQuery))
                {
                    for(int index = 0;index<calQuery.crossEdges.size();index++)
                    {
                        if(calQuery.crossEdges.get(index) == affectedEdge && calQuery.crossNodeTimes.get(index)>copyQuery.crossNodeTimes.get(i))
                        {
                            affectedUsers.add(calQuery);
                            break;
                        }
                    }
                }
            }
        }
        float userCount = affectedUsers.size();
        float routeSelfTime = copyQuery.totalTimeCost();
        return routeSelfTime+edgeSize*userCount*(minCapacity*minCapacity-(minCapacity-1)*(minCapacity-1))/(minCapacity*minCapacity)*2;
    }

    /**
     * Initial Algorithm
     * @param queryGetter
     * @param batchSize
     * @param batchCount
     * @param refineSize
     * @param map
     * @return
     */
    public static ArrayList<Query> init(QueriesSet queryGetter, int batchSize, int batchCount, int refineSize, HashMap<Edge, ArrayList<Flow>> map) {
        int userSize = refineSize;
        ArrayList<Query> users = new ArrayList<Query>();
        for (int i = 0; i < userSize; i++) {

            int startId = queryGetter.getQueries().get(i+batchCount*batchSize).getStartVertexId();//Get the query information under the current batch, the following is also
            int endId = queryGetter.getQueries().get(i+batchCount*batchSize).getEndVertexId();
            float stime = queryGetter.getQueries().get(i+batchCount*batchSize).getDepartureTime();
            g.reNode(endId, MinTravelTimeData.dDist);//Update the distance from the Node to the end point in graph's dDist
            RouteSearch routeSearch = new RouteSearch();
            routeSearch.travel(startId, endId, stime, g ,  isSelfAware,map);
            users.add(new Query(i+batchCount*batchSize, stime, startId, endId, routeSearch.paths, routeSearch.times, routeSearch.crossEdges));
        }
        return users;
    }


    /**
     * refine Algorithm
     * @param users user information
     * @param eplison threshold
     * @return
     */
    public static ArrayList<Query> refine(ArrayList<Query> users, float eplison)
    {
        ArrayList<Query> copyQueries = Shower.copyUser(users);
        while(true)
        {
            boolean flag = false;
            int size = users.size();
            for(int userCount=0;userCount<size;userCount++)
            {
                Float cost1 = Shower.time(users);
                Query refiningUser = users.get(userCount);
                //Update the total generation of the corresponding traffic
                users.remove(refiningUser);
                ArrayList<Query> temp = new ArrayList<>();
                temp.addAll(users);


                HashMap<Edge, ArrayList<Flow>> otherMap = FlowsNetwork.generateQueryInfos(temp,new HashMap<>());

                int startId = refiningUser.startVertexId;
                int endId = refiningUser.endVertexId;
                float stime = refiningUser.departureTime;
                g.reNode(endId, MinTravelTimeData.dDist);
                RouteSearch routeSearch = new RouteSearch();
                routeSearch.travel(startId, endId, stime, g,isSelfAware, otherMap);

                int size1 = refiningUser.crossNodeIds.size();
                int size2 = routeSearch.paths.size();


                ArrayList<Query> affectedUsers = new ArrayList<>();
                float maxDecrement = isPreCheck?calMaxDecrement(refiningUser, users,affectedUsers):Float.MAX_VALUE;
                if ( (size1== size2 && refiningUser.crossNodeIds.get(size1-2).equals(routeSearch.paths.get(size1-2))) ||maxDecrement<eplison*cost1) {
                    users.add(userCount, refiningUser);
                }else {
                    refiningUser.crossNodeIds = routeSearch.paths;
                    refiningUser.crossNodeTimes = routeSearch.times;
                    refiningUser.crossEdges = routeSearch.crossEdges;
                    users.add(userCount, refiningUser);

                    temp.add(refiningUser);
                    affectedUsers = temp;
                    FlowsNetwork.greedyQueryQueue.addAll(affectedUsers);
                    FlowsNetwork.Update();

                    swapCheckCount++;
                    Float cost2 = Shower.time(users);
                    if((cost1-cost2) >= eplison*cost1)
                    {
                        swapCheckCount_valid++;
                        flag = true;
                        copyQueries = Shower.copyUser(users);
                    }else {
                        users = Shower.copyUser(copyQueries);
                    }
                }
            }
            if(!flag)
            {
                return users;
            }
        }
    }

    /**
     * SBP Algorithm
     * @param querySize
     * @param queryDensity
     * @param refineInterval
     * @param e
     * @param queryGetter
     * @return
     */
    public static ArrayList<Query> SBP_alg(int querySize, int queryDensity, int refineInterval , float e, QueriesSet queryGetter)
    {
        ArrayList<Query> allQueries = new ArrayList<Query>();
        int refineSize = queryDensity*refineInterval;
        int refineCount =  (querySize%refineSize == 0)? querySize/refineSize:querySize/refineSize+1;
        for(int i=0;i<refineCount;i++)
        {
            int curRefineSize = ((i+1)*refineSize>querySize)?querySize-i*refineSize:refineSize;
            HashMap<Edge, ArrayList<Flow>> nodeMaps = FlowsNetwork.generateQueryInfos(allQueries,new HashMap<>());
            long t1 = System.currentTimeMillis();
            ArrayList<Query> queries = init(queryGetter, refineSize,i,curRefineSize, nodeMaps);//Init each queries
            queries = refine(queries, e);
            long t2 = System.currentTimeMillis();
            sumTime += (t2-t1);
            allQueries.addAll(queries);

            FlowsNetwork.greedyQueryQueue.addAll(allQueries);
            FlowsNetwork.Update();
        }
        return allQueries;
    }


}
