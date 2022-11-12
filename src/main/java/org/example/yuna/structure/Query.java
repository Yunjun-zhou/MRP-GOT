package org.example.yuna.structure;


import lombok.Data;

import java.util.ArrayList;

@Data
public class Query {
    /**
     * query id
     */
    public int queryId;
    /**
     * departure time
     */
    public float departureTime;
    /**
     * the source vertex
     */
    public int startVertexId;
    /**
     * the destination vertex
     */
    public int endVertexId;
    /**
     * the current number of vertexes that the user has traveled
     */
    public int currentIndex;
    /**
     * the vertex that the user is traveling
     */
    public int currentVertex;
    /**
     * the current time
     */
    public float currentTime;

    /**
     * three lists are used to record the vertexes, times and edges that the user has traveled
     */
    public ArrayList<Integer> crossNodeIds = new ArrayList<Integer>();
    public ArrayList<Float> crossNodeTimes = new ArrayList<Float>();
    public 	ArrayList<Edge> crossEdges = new ArrayList<Edge>();

    public float maxDec = 0f;


    /**
     * The user generating 1 for our SBP algorithm
     *
     * @param queryId        query id
     * @param departureTime departure time
     * @param startVertexId the source vertex id
     * @param endVertexId   the destination id
     * @param paths         the vertexes the user has traveled
     * @param times         the times at each traveled vertex
     */

    public Query(int queryId, float departureTime, int startVertexId, int endVertexId, ArrayList<Integer> paths,
                ArrayList<Float> times, ArrayList<Edge> crossEdges) {
        // TODO Auto-generated constructor stub
        this.queryId = queryId;
        this.departureTime = departureTime;
        this.startVertexId = startVertexId;
        this.endVertexId = endVertexId;

        // set the route and corresponding records
        this.crossNodeIds = paths;
        this.crossNodeTimes = times;
        this.currentIndex = 0;
        this.currentVertex = crossNodeIds.get(currentIndex);
        this.currentTime = crossNodeTimes.get(currentIndex);
        this.crossEdges = crossEdges;
    }

    /**
     * The user generating 2 for exact algorithm
     *
     * @param queryId        query id
     * @param departureTime departure time
     * @param startVertexId the source vertex id
     * @param endVertexId   the destination id
     */
    public Query(int queryId, float departureTime, int startVertexId, int endVertexId) {
        // TODO Auto-generated constructor stub
        this.queryId = queryId;
        this.departureTime = departureTime;
        this.startVertexId = startVertexId;
        this.endVertexId = endVertexId;

        this.currentIndex = 0;
        this.currentTime = departureTime;
        this.currentVertex = startVertexId;

    }

    /**
     * user copy
     * @return a copied user
     */
    public Query copy() {
        return new Query(queryId, departureTime, startVertexId, endVertexId, new ArrayList<Integer>(crossNodeIds),
                new ArrayList<Float>(crossNodeTimes) , new ArrayList<Edge>(crossEdges));
    }



    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * set the current index of the user, denoting how many vertexes that the user has traveled
     * @param index the current index of the user
     */
    public void setCurrentIndex(int index) {
        currentIndex = index;
        currentVertex = crossNodeIds.get(currentIndex);
        currentTime = crossNodeTimes.get(currentIndex);
    }

    public void setCurrentTime(float currentTime) {
        this.currentTime = currentTime;
    }

    public void showInfo() {
        System.out.println(queryId + " " + departureTime + " " + startVertexId + "->" + endVertexId);
        System.out.println(crossNodeIds);
        System.out.println(crossNodeTimes);
    }

    /**
     * calculate the travel time from the source to destination
     * @return the travel time from the source to destination
     */
    public float totalTimeCost() {
        return crossNodeTimes.get(crossNodeTimes.size() - 1) - departureTime;
    }

}
