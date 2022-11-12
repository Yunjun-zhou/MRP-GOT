package org.example.yuna.data;

import org.example.yuna.conf.DataFileConfig;
import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Node;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.io.*;
import java.util.ArrayList;

import static org.example.yuna.conf.Setting.*;

public class MinTravelTimeData {

    public static float[][] dDist ;//Store mini distance matrix

    /**
     * Create mini Matrix from file
     * @param nodeSize node size
     * @param miniFile mini file location
     * @throws IOException
     */
    public static void read(int nodeSize,String miniFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(DataFileConfig.getSetting(miniFile)));
        String lineStr;

        dDist = new float[nodeSize][nodeSize];
        int index = 0;
        while ((lineStr = in.readLine()) != null) {
            String[] oneLineDis = lineStr.split(" ");
            for (int i = 0; i < oneLineDis.length; i++) {
                dDist[index][i] = Float.parseFloat(oneLineDis[i]);
            }
            index++;
        }
        in.close();
    }


    /*
    Use the Dijkstra algorithm to calculate the shortest distance
    */
    public void write() {
        GraphData graphData = GraphData.getInstance(edgeFile,nodeFile,miniFile,false);
        ArrayList<Node> vertices = graphData.nodes;
        Graph<Node, Edge> graph = graphData.graph;
        DijkstraShortestPath<Node, Edge> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
        try (FileWriter minTravelTimeFile = new FileWriter(DataFileConfig.getSetting("MinTravelTimeFile"))) {
            vertices.forEach(i -> {
                try {
                    ShortestPathAlgorithm.SingleSourcePaths<Node, Edge> paths = dijkstraShortestPath.getPaths(i);
                    vertices.forEach(j -> {
                        try {
                            if(paths.getWeight(j) > 1000000){
                                System.out.println(i+"-->" +j.getNodeId()+"not connect");
                            }
                            minTravelTimeFile.write(paths.getWeight(j)+" ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    minTravelTimeFile.write("\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static  void getDistMapOnce() throws IOException {

            int size = GraphData.getInstance(edgeFile,nodeFile,miniFile,false).nodes.size();
            File file = new File(DataFileConfig.getSetting("MinTravelTimeFile"));
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            int currLine = 0;
            while((lineString = reader.readLine()) != null)
            {
                String[] dist = lineString.split(" ");
                for (int i = 0; i < dist.length; i++) {
                }
                currLine++;
            }
            reader.close();
    }
}
