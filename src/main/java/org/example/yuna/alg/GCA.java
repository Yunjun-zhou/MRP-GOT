package org.example.yuna.alg;

import org.example.yuna.data.FlowsNetwork;
import org.example.yuna.data.MinTravelTimeData;
import org.example.yuna.data.QueriesSet;
import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Query;
import org.example.yuna.structure.QueryCluster;
import org.example.yuna.utils.Shower;

import java.util.*;

import static org.example.yuna.alg.Test.*;
import static org.example.yuna.conf.Setting.*;

public class GCA {

    public static double k = 0;


    public static ArrayList<Query> getAffectUsers(Query refiningQuery, List<Query> queries)
    {
        ArrayList<Query> affectedQueries = new ArrayList<>();
        for(int i=0;i<refiningQuery.crossEdges.size();i++)
        {
            Edge affectedEdge = refiningQuery.crossEdges.get(i);
            for(int j=0;j<queries.size();j++)
            {
                Query calUser  = queries.get(j);
                //Passing through the same edge is judged to be the affected
                if(calUser.crossEdges.contains(affectedEdge) && !affectedQueries.contains(calUser))
                {
                    for(int index = 0;index<calUser.crossEdges.size();index++)
                    {
                        if(calUser.crossEdges.get(index) == affectedEdge &&
                                calUser.crossNodeTimes.get(index)>refiningQuery.crossNodeTimes.get(i) &&
                                calUser.crossNodeTimes.get(index)<refiningQuery.crossNodeTimes.get(i+1))
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
     * RPOT-A* algorithm
     * @param queryGetter all queries package
     * @param batchSize  batch index
     * @param batchCount  one batch's size
     * @param refineSize  now batch's size
     * @param isAware  road network state
     * @param map  other flows
     * @return
     */
    public static ArrayList<Query> AStar(QueriesSet queryGetter, int batchSize, int batchCount, int refineSize, Boolean isAware,HashMap<Edge, ArrayList<Flow>> map) {

        int userSize = refineSize;
        ArrayList<Query> users = new ArrayList<Query>();
        for (int i = 0; i < userSize; i++) {
            int startId = queryGetter.getQueries().get(i+batchCount*batchSize).getStartVertexId();//get queries information
            int endId = queryGetter.getQueries().get(i+batchCount*batchSize).getEndVertexId();
            float stime = queryGetter.getQueries().get(i+batchCount*batchSize).getDepartureTime();
            g.reNodeNE(endId, MinTravelTimeData.dDist);//update graph node to end's distance (dDist)
            RouteSearch routeSearch = new RouteSearch();
            routeSearch.getPath(startId, endId, stime, g ,isAware,map);//path planning
            users.add(new Query(i+batchCount*batchSize, stime, startId, endId, routeSearch.paths, routeSearch.times, routeSearch.crossEdges));//add planning result
        }
        return users;
    }

    /**
     *
     * @param queries queries list
     * @param eplison sigma threshold
     * @param step steps threshold
     * @param othersQueries other flows
     * @return
     */
    public static List<Query> refineNE(List<Query> queries, float eplison,int step,List<Query> othersQueries)
    {
        k = -1;
        int epochs = 0;
        HashMap<Edge, ArrayList<Flow>> otherFlows = FlowsNetwork.generateQueryInfos(othersQueries, new HashMap<>());

        while(true)
        {
            int size =queries.size();
            if(epochs % step == 0){
                k++;
            }
            Float cost1 = Shower.time(queries);//Calculate the global traffic time under the current planning
            for (int queryCount = 0; queryCount < size; queryCount++) { //Select each path to game in order
                Query refiningQuery = queries.get(queryCount);//Choose users who want to game
                queries.remove(refiningQuery);//Remove the user

//              Re -plan the route for the current gaming user
                int startId = refiningQuery.startVertexId;
                int endId = refiningQuery.endVertexId;
                float stime = refiningQuery.departureTime;
                g.reNodeNE(endId, MinTravelTimeData.dDist);

                HashMap<Edge, ArrayList<Flow>> totalFlows = FlowsNetwork.generateQueryInfos(queries,otherFlows);


                RouteSearch routeSearch = new RouteSearch();
                boolean loop = routeSearch.getPath(startId, endId, stime, g,isSelfAware, totalFlows);//isStatic,isSelfAware,
                int size1 = refiningQuery.crossNodeIds.size();
                int size2 = routeSearch.paths.size();

                if ( (size1== size2 && refiningQuery.crossNodeIds.get(size1-2).equals(routeSearch.paths.get(size1-2)))) {
                    queries.add(queryCount, refiningQuery);//not change, restore the current user of the current user

                }else {//update the itinerary information of the Gaming user
                    refiningQuery.crossNodeIds = routeSearch.paths;
                    refiningQuery.crossNodeTimes = routeSearch.times;
                    refiningQuery.crossEdges = routeSearch.crossEdges;
                    queries.add(queryCount, refiningQuery);
                    swapCheckCount++;
                }
            }
            List<Query> affectedQueries = queries;
//          Add an affected user to re -calculate the time to reach each side
            FlowsNetwork.greedyQueryQueue.addAll(affectedQueries);
            if (othersQueries != null) {
                FlowsNetwork.greedyQueryQueue.addAll(othersQueries);
            }
            FlowsNetwork.Update();
            epochs++;
            Float cost2 = Shower.time(queries);//Calculate the time spent on the pass after gaming
            if((cost1-cost2) < eplison*cost1*Math.pow(1.2,k))//Determine whether the iteration of this round of iteration
            {
                return queries;
            }
        }
    }

    /**
     * RPOT-Game-CC and Game algorithm
     * @param querySize
     * @param e
     * @param step
     * @param queryGetter
     * @param label
     * @param maxNum
     * @param angle
     * @param isAware
     * @return
     */
    public static ArrayList<Query> testByNE(int querySize,float e,int step, QueriesSet queryGetter,int label,int maxNum,int angle,Boolean isAware)
    {
        ArrayList<Query> allQueries = new ArrayList<Query>();
        int refineSize = queryDensity*interval;
        int refineCount =  (querySize%refineSize == 0)? querySize/refineSize:querySize/refineSize+1;
        for(int i=0;i<refineCount;i++)
        {

            int curRefineSize = ((i+1)*refineSize>querySize)?querySize-i*refineSize:refineSize;

            HashMap<Edge, ArrayList<Flow>> nodeInfos = FlowsNetwork.generateQueryInfos(allQueries,new HashMap<>());
            ArrayList<Query> queries = AStar(queryGetter, refineSize,i,curRefineSize,isAware, nodeInfos);//Initialize a path for each user
            allQueries.addAll(queries);
            FlowsNetwork.greedyQueryQueue.addAll(allQueries);
            FlowsNetwork.Update();
        }



        if(label == 1) {
            List<QueryCluster> queryCluster = Decomposition.coClusteringDecomposition(allQueries,maxNum,angle);
            for(QueryCluster cluster:queryCluster){
                allQueries.removeAll(cluster.getQueriesList());
                ArrayList<Query> copyQueries = Shower.copyUser(allQueries);
                long t1 = System.currentTimeMillis();
                refineNE(cluster.getQueriesList(),e,step,copyQueries);
                long t2 = System.currentTimeMillis();
                sumTime += (t2-t1);
                allQueries.addAll(cluster.getQueriesList());
            }
        }else{
             long t1 = System.currentTimeMillis();
             refineNE(allQueries, e,step,null);
             long t2 = System.currentTimeMillis();
             sumTime += (t2-t1);
         }

        FlowsNetwork.greedyQueryQueue.addAll(allQueries);
        FlowsNetwork.Update();
        return allQueries;
    }


}
