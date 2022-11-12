package org.example.yuna.alg;

import javafx.util.Pair;
import org.example.yuna.data.GraphData;
import org.example.yuna.structure.Location;
import org.example.yuna.structure.Node;
import org.example.yuna.structure.Query;
import org.example.yuna.structure.QueryCluster;
import org.example.yuna.utils.DistanceUtil;
import org.example.yuna.utils.MyComparator;

import java.util.*;

import static org.example.yuna.conf.Setting.*;

public class Decomposition {

    /*
      miller mapping
     */
    public static Pair<Double,Double> millerXY(Location location){
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        double L = 6381372*Math.PI*2;
        double mill = 2.3;
        double x = longitude*Math.PI/180;
        double y = latitude*Math.PI/180;
        y = 1.25*Math.log((Math.tan(0.25*Math.PI+0.4*y)));
        x = (L/2) + (L/(2*Math.PI))*x;
        y = (L/4) - (L/(4*mill))*y;
        return new Pair<>(x,y);
    }

    public static double getAngle(Location s1,Location d1,Location s2,Location d2,boolean flag){
        if(flag){
            double x1 = s1.getLatitude()- d1.getLatitude();
            double y1 = s1.getLongitude()-d1.getLongitude();

            double x2 = s2.getLatitude()- d2.getLatitude();
            double y2 = s2.getLongitude()-d2.getLongitude();

            double cos = (x1*x2+y1*y2)/(Math.sqrt(x1*x1+y1*y1)*Math.sqrt(x2*x2+y2*y2));
            return Math.acos(cos);
        }else{
            Pair<Double, Double> coordinate1 = millerXY(s1);
            Pair<Double, Double> coordinate2 = millerXY(d1);
            Pair<Double, Double> coordinate3 = millerXY(s2);
            Pair<Double, Double> coordinate4 = millerXY(d2);

            double x1 = coordinate2.getKey()-coordinate1.getKey();
            double y1 = coordinate2.getValue()-coordinate1.getValue();

            double x2 = coordinate4.getKey()-coordinate3.getKey();
            double y2 = coordinate4.getValue()-coordinate3.getValue();

            double cos = (x1*x2+y1*y2)/(Math.sqrt(x1*x1+y1*y1)*Math.sqrt(x2*x2+y2*y2));
            return Math.acos(cos);
        }
    }

    public static boolean isCross(Location s1,Location d1,Location s2,Location d2,boolean flag){
        if(flag){
            double x1 = s1.getLatitude()- d1.getLatitude();
            double y1 = s1.getLongitude()-d1.getLongitude();

            double x2 = s1.getLatitude()- s2.getLatitude();
            double y2 = s1.getLongitude()-s2.getLongitude();

            double x3 = s1.getLatitude()- d2.getLatitude();
            double y3 = s1.getLongitude()-d2.getLongitude();

            double vector1 = x1*y2-y1*x2;
            double vector2 = x1*y3-y1*x3;
            if(vector1*vector2<0){
                return true;
            }else{
                return false;
            }
        }else {
            Pair<Double, Double> coordinate1 = millerXY(s1);
            Pair<Double, Double> coordinate2 = millerXY(d1);
            Pair<Double, Double> coordinate3 = millerXY(s2);
            Pair<Double, Double> coordinate4 = millerXY(d2);

            double x1 = coordinate1.getKey()-coordinate2.getKey();
            double y1 = coordinate1.getValue()-coordinate2.getValue();

            double x2 = coordinate1.getKey()-coordinate3.getKey();
            double y2 = coordinate1.getValue()-coordinate3.getValue();

            double x3 = coordinate1.getKey()-coordinate4.getKey();
            double y3 = coordinate1.getValue()-coordinate4.getValue();

            double vector1 = x1*y2-y1*x2;
            double vector2 = x1*y3-y1*x3;
            if(vector1*vector2<0){
                return true;
            }else{
                return false;
            }
        }

    }

    /**
     * get radius length
     * @param query center query to calculate radius
     * @return
     */
    public static double getRstar(Query query) {
        return  mu * DistanceUtil.getDistance(query) ;///(8 + 4 * mu)
    }

    /**
     * Co-Cluster method
     * @param queries queriesSet
     * @param maxNum maxsize
     * @param angle  angle threshold
     * @return clusters
     */
    public static List<QueryCluster> coClusteringDecomposition(List<Query> queries,int maxNum,int angle) {
        PriorityQueue<Query> queryPriorityQueue = new PriorityQueue<>(MyComparator.disComparator);
        for (Query query:queries){
            queryPriorityQueue.add(query);
        }
        List<Query> orderQueries = new LinkedList<>();
        while (!queryPriorityQueue.isEmpty()){
            orderQueries.add(queryPriorityQueue.poll());
        }
        ArrayList<QueryCluster> queryClusters = new ArrayList<>();
        int index = 0;
        for (Query query : orderQueries) {
            if (index == 0) {
                QueryCluster queryCluster = new QueryCluster();
                queryCluster.setIndex(index);
                queryCluster.setCenter(query);
                queryCluster.setQueriesList(new LinkedList<>());
                queryCluster.getQueriesList().add(query);
                queryClusters.add(queryCluster);
                index++;
            } else {
                boolean add = false;
                for (QueryCluster queryCluster : queryClusters) {
                    Query center = queryCluster.getCenter();
                    double rstar = getRstar(center);
                    GraphData g = GraphData.getInstance(edgeFile,nodeFile,miniFile,false);
                    Node s1 = g.nodes.get(query.getStartVertexId());
                    Node d1 = g.nodes.get(query.getEndVertexId());

                    Node s2 = g.nodes.get(center.getStartVertexId());
                    Node d2 = g.nodes.get(center.getEndVertexId());
                    if (DistanceUtil.getDistance(s1, s2) <= rstar &&
                            DistanceUtil.getDistance(d1, d2) <= rstar &&
                            getAngle(s1.location,d1.location,s2.location,d2.location,s1.location.isXY())<(Math.PI/180*angle)&&
                            queryCluster.getQueriesList().size()<=maxNum
                          ) {
                        queryCluster.getQueriesList().add(query);
                        add = true;
                        break;
                    }
                }
                if (!add) {
                    QueryCluster queryCluster = new QueryCluster();
                    queryCluster.setIndex(index);
                    queryCluster.setCenter(query);
                    queryCluster.setQueriesList(new LinkedList<>());
                    queryCluster.getQueriesList().add(query);
                    queryClusters.add(queryCluster);
                    index++;
                }
            }
        }
        return queryClusters;
    }


}
