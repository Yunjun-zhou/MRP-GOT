package org.example.yuna.utils;

import org.example.yuna.data.GraphData;
import org.example.yuna.structure.Location;
import org.example.yuna.structure.Node;
import org.example.yuna.structure.Query;

import static org.example.yuna.conf.Setting.*;

public class DistanceUtil {
    private static double EARTH_RADIUS = 6381.372;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * Get distance by latitude and longitude (in meters)
     *
     * @param lat1 latitude of start
     * @param lng1 longitude of start
     * @param lat2 latitude of end
     * @param lng2 longitude of end
     * @return
     */
    public static double getDistance(double lng1, double lat1,
                               double lng2, double lat2,boolean flag) {
        if(flag){
            return Math.sqrt(Math.pow(lng1-lng2,2.0)+Math.pow(lat1-lat2,2.0));
        }
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = s * 1000;
        return s;
    }

    public static double getDistance(Query query) {
        GraphData graphData = GraphData.getInstance(edgeFile,nodeFile,miniFile,false);
        Node start = graphData.nodes.get(query.getStartVertexId());
        Node end = graphData.nodes.get(query.getEndVertexId());
        return getDistance(start, end);
    }

    public static double getDistance(Node o, Node d) {
        return getDistance(o.getLocation(), d.getLocation());
    }

    public static double getDistance(Location l1, Location l2) {
        return getDistance(l1.getLongitude(), l1.getLatitude(), l2.getLongitude(), l2.getLatitude(),l1.isXY());
    }

    public static double getTravelTime(Node o, Node d) {
    //  average speed 45 km/h=12.5 m/s
        return getDistance(o, d) / 12.5;
    }




}
