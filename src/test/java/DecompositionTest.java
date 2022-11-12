import org.example.yuna.alg.Decomposition;
import org.example.yuna.data.GraphData;
import org.example.yuna.data.QueriesSet;
import org.example.yuna.structure.Query;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.example.yuna.conf.Setting.*;

public class DecompositionTest {

    /**
     *  test calculating angle function
     */
    @Test
    public void AngleTest(){
        GraphData graphData = GraphData.getInstance(edgeFile,nodeFile,miniFile,false);
        QueriesSet queriesSet = new QueriesSet("queryRequestFilepath",queryDensity,0);
        ArrayList<Query> queries = queriesSet.getQueries();
        for (int i = 0;i<100;i++){
            Query o1 = queries.get(new Random().nextInt(200));
            Query o2 = queries.get(new Random().nextInt(400));
            System.out.println("o1 start mapping："+Decomposition.millerXY(graphData.nodes.get(o1.startVertexId).getLocation()));
            System.out.println("o1 end mapping："+Decomposition.millerXY(graphData.nodes.get(o1.endVertexId).getLocation()));
            System.out.println("o2 start mapping："+Decomposition.millerXY(graphData.nodes.get(o2.startVertexId).getLocation()));
            System.out.println("o2 end mapping："+Decomposition.millerXY(graphData.nodes.get(o2.endVertexId).getLocation()));
            System.out.println(Decomposition.getAngle(graphData.nodes.get(o1.startVertexId).getLocation(),
                    graphData.nodes.get(o1.endVertexId).getLocation(),
                    graphData.nodes.get(o2.startVertexId).getLocation(),
                    graphData.nodes.get(o2.endVertexId).getLocation(),false));
            System.out.println();
        }


    }
}
