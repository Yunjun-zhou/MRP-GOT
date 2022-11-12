package org.example.yuna.data;


import org.example.yuna.conf.DataFileConfig;
import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Location;
import org.example.yuna.structure.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static org.example.yuna.conf.Setting.alpha;
import static org.example.yuna.utils.DistanceUtil.getTravelTime;

public class GraphData {
    private static volatile GraphData instance;//Singular
    private String configField;//File to store edges
    private String configField2;//File to store nodes
    private String configField3;//File to store miniTime

    public Graph<Node, Edge> graph;


    /**
     * the mirror graph, edge wight a->b become b->a
     */

    public ArrayList<Node> nodes = new ArrayList<>();
    public HashMap<Node,ArrayList<Edge>> edges = new HashMap<>();

    public ArrayList<Integer> queryIdRange = new ArrayList<>();
    int sum = 0;

    public static GraphData getInstance(String edgeFile,String nodeFile,String miniTime,Boolean flag) {

         synchronized (GraphData.class) {
               if (instance == null) {
                   instance = new GraphData(edgeFile,nodeFile,miniTime);
               } else if (flag == true) {
                   instance = new GraphData(edgeFile,nodeFile,miniTime);
               }
//                instance = new GraphData("roadFile","nodeFile");
        }
        return instance;
    }

    private GraphData(String configField,String configField2,String configField3) {
        this.configField = configField;
        this.configField2 = configField2;
        this.configField3 = configField3;
        buildGraph();
    }

    public void buildGraph() {
        try {
            // build edges network
            File fileNode = new File(DataFileConfig.getSetting(configField2));
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(fileNode));
                String line = null;
                while ((line = bufferedReader.readLine())!=null ){
                    int id = Integer.parseInt(line.split(" ")[0]);
                    double lon =Double.parseDouble(line.split(" ")[1]);
                    double lat= Double.parseDouble(line.split(" ")[2]);
                    Location location = new Location(lon,lat);
                    Node newNode =  new Node(id,100000);
                    newNode.setLocation(location);
                    nodes.add(newNode);
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // build nodes network
            File fileEdge = new File(DataFileConfig.getSetting(configField));
            BufferedReader reader = new BufferedReader(new FileReader(fileEdge));
            String lineString = null;

            while((lineString = reader.readLine()) != null)
            {
                int start = Integer.parseInt(lineString.split(" ")[1]);
                int end =Integer.parseInt(lineString.split(" ")[2]);
                float weight= Float.parseFloat(lineString.split(" ")[3]);
                int capacity = Integer.parseInt(lineString.split(" ")[4]);

                Node startNode = nodes.get(start);
                Node endNode = nodes.get(end);

                if(!edges.containsKey(startNode)){
                    edges.put(startNode,new ArrayList<>());
                }
                ArrayList<Edge> edges1 = edges.get(startNode);
                boolean flag = true;
                for (Edge temp : edges1) {
                    if (temp.getEndNode() == endNode) {
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    edges1.add(new Edge(startNode,endNode,weight,capacity));
                    edges.put(startNode,edges1);
                    sum++;
                }

                flag = true;
                if(!edges.containsKey(endNode)){
                    edges.put(endNode,new ArrayList<>());
                }
                ArrayList<Edge> edges2 = edges.get(endNode);
                for (Edge temp : edges2) {
                    if (temp.getEndNode() == startNode) {
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    edges2.add(new Edge(endNode,startNode,weight,capacity));
                    edges.put(endNode,edges2);
                    sum++;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            // build miniTime matrix
            MinTravelTimeData.read(nodes.size(),configField3);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //This graph is based JGraphT,used calculate shortest length
        graph = new DefaultUndirectedWeightedGraph<>(Edge.class);
        nodes.forEach((s) -> {
            graph.addVertex(s);
        });
        edges.values().forEach((s) -> {
            s.forEach((t)->{
                if (graph.addEdge(t.getStartNode(), t.getEndNode(), t)) {
                    graph.setEdgeWeight(t,t.getMinCrossTime());
                }
            });
        });
    }

    /**
     * update the node records for SBP
     * @param endId the destination id
     * @param shortestDist the evaluated shortest distances
     */
    public void reNode(int endId, float[][] shortestDist) {
        for(int i=0;i< nodes.size();i++)
        {
            float len = shortestDist[endId][i];
            nodes.get(i).dDist = len;
            nodes.get(i).pre = -1;
            nodes.get(i).sDist = 1000000;
            nodes.get(i).currentTime = -1;
        }
    }

    /**
     * update the node records for our algorithm
     * @param endId the destination id
     * @param shortestDist the evaluated shortest distances
     */
    public void reNodeNE(int endId, float[][] shortestDist) {
        for(int i=0;i< nodes.size();i++)
        {
            float len = shortestDist[endId][i];
            nodes.get(i).dDist = (float) (len*(0.09*alpha+1.0f));//Our Heuristic function for searching
            nodes.get(i).pre = -1;
            nodes.get(i).sDist = 1000000;
        }
    }
    public void reNodeTraffic(int endId) {
        for(int i=0;i< nodes.size();i++)
        {
            double cost = getTravelTime(nodes.get(i),nodes.get(endId));
            nodes.get(i).dDist = (float) cost;
            nodes.get(i).pre = -1;
            nodes.get(i).sDist = 1000000;
        }
    }

}
