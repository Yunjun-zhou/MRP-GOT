import org.example.yuna.data.GraphData;
import org.example.yuna.data.QueriesSet;
import org.example.yuna.structure.Query;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.example.yuna.conf.Setting.*;

public class QueriesTest {
    @Test
    public void isInroad(){
        GraphData graph = GraphData.getInstance(edgeFile,nodeFile,miniFile,false);
        QueriesSet queriesSet = new QueriesSet("queryRequestFilepath",25,0);
        ArrayList<Query> queries = queriesSet.getQueries();
        for(int num = 0;num<queries.size();num++){
            int start = queries.get(num).startVertexId;
            int end = queries.get(num).endVertexId;
            if(start < graph.nodes.size() && end < graph.nodes.size()){
                continue;
            }
        }
    }

    @Test
    public void isQueryTest(){
        Query query = new Query(0,0,2,15);
        Query query1 = new Query(2,0,26,89);
        ArrayList<Query> queries = new ArrayList<>();
        System.out.println(query);
        System.out.println(query1);
        System.out.println();
        queries.add(query1);
        queries.add(query);
        dealQuery(queries.get(0));
        dealQuery(queries.get(1));
        System.out.println(query);
        System.out.println(query1);
    }

    public void dealQuery(Query query){
        query.departureTime = (float) 2.3;
        query.queryId+=1;
    }
}
