package org.example.yuna.alg;

import org.example.yuna.data.FlowsNetwork;
import org.example.yuna.data.GraphData;
import org.example.yuna.data.QueriesSet;
import org.example.yuna.structure.Edge;
import org.example.yuna.structure.Flow;
import org.example.yuna.structure.Query;
import org.example.yuna.utils.Shower;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.example.yuna.alg.GCA.*;
import static org.example.yuna.conf.Setting.*;


public class Test {
    public static GraphData g = GraphData.getInstance(edgeFile,nodeFile,miniFile,true);
    public static float minCost = Float.MAX_VALUE;


    public static void main(String[] args) {

      // vary the trip count (user count) for Ind algorithm, remember set self-aware = true
        System.out.println("Chengdu Results：");
        varyUserSize(userSizesCD);


//
        System.out.println("Game-CC:");
        varyUserSizeByNE(userSizesCD,1,true);
        System.out.println();
        System.out.println("Game:");
        varyUserSizeByNE(userSizesCD,2,true);



        System.out.println();
        System.out.println();
        System.out.println("----------Parameters Results--------------");

        varyPara_e(es,1);

        varyDeSize(deSizes,1);
        varyDeAngle(angles,1);
        varyRadius(radius,1);


        varyAlpha_GCA(alphas,1);
        varyAlpha_SBP(alphas);

        varyBeta_GCA(betas,1);
        varyBeta_SBP(betas);
        System.out.println();
        System.out.println();



    }

    /**
     * vary queries number function of SBP
     * @param userSizes Q nums
     */
    public static void varyUserSize(int[] userSizes)
    {
        System.out.println("**************SBP result********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        ArrayList<Integer> swapValidCounts = new ArrayList<>();
        for(int i = 0;i<userSizes.length;i++)
        {
            float avgTime = 0;
            ArrayList<Query> allQueries;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = SBP.SBP_alg(userSizes[i], queryDensity, interval, e_sbp, queryGetter);
                avgTime  +=  Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            swapValidCounts.add(swapCheckCount_valid);
            sumTime = 0;
        }
        System.out.println("QuerySize: "+ Arrays.toString(userSizes));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
        System.out.println("SwapValidCounts:"+swapValidCounts);
    }

    /**
     * vary queries number function of A*
     * @param userSizes Q nums
     */
    public static void varyUserSizeForAstar(int[] userSizes)
    {
        System.out.println("\n************Init result**********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        for(int i = 0;i<userSizes.length;i++)
        {
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                QueriesSet queryGetter = resetPara(queriesFile);
                ArrayList<Query> allQueries = new ArrayList<Query>();
                int refineSize = queryDensity*interval;
                int refineCount = (userSizes[i]%refineSize == 0)? userSizes[i]/refineSize:userSizes[i]/refineSize+1;
                for(int m=0;m<refineCount;m++)
                {
                    int curRefineSize = ((m+1)*refineSize>userSizes[i])?userSizes[i]-m*refineSize:refineSize;
                    HashMap<Edge, ArrayList<Flow>> nodeMap = FlowsNetwork.generateQueryInfos(allQueries,new HashMap<>());
                    ArrayList<Query> queries;
                    long t1 = System.currentTimeMillis();
                    queries = GCA.AStar(queryGetter, refineSize,m,curRefineSize,isSelfAware, nodeMap);//FlowsNetwork.roadFlows
                    long t2 = System.currentTimeMillis();
                    sumTime += (t2-t1);
                    allQueries.addAll(queries);
                    FlowsNetwork.greedyQueryQueue.addAll(allQueries);
                    FlowsNetwork.Update();
                }
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            sumTime = 0;
        }
        System.out.println("QuerySize: "+ Arrays.toString(userSizes));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
    }

    /**
     * vary queries number function of Game-CC
     * @param userSizes Q nums
     */
    public static void varyUserSizeByNE(int[] userSizes,int label,Boolean isAware)
    {
        System.out.println("**************NE by Myself********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        for(int i = 0;i<userSizes.length;i++)
        {
                float avgTime = 0;
                for(int expCount = 0; expCount<expNum;expCount++)
                {
                    ArrayList<Query> allQueries;
                    QueriesSet queryGetter = resetPara(queriesFile);
                    allQueries = testByNE(userSizes[i], e,step, queryGetter,label,deSize,angle,isAware);
                    avgTime += Shower.time(allQueries);
                }
                runtime.add(sumTime/expNum);
                travelTime.add(avgTime/expNum);
                swapCounts.add(swapCheckCount);
                sumTime = 0;
        }
        System.out.println("Query Size: "+ Arrays.toString(userSizes));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
    }


    /** Vary sigma parameter function
     * The performance of algorithms as we vary sigma parameter.
     * @param es  The  refining parameter epsilon
     * @param label alg label
     */
    public static void varyPara_e(float[] es,int label)
    {
        System.out.println("*********es experiment**************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        for(int i = 0;i<es.length;i++)
        {
            ArrayList<Query> allQueries = new ArrayList<Query>();
            QueriesSet queryGetter = resetPara(queriesFile);
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                allQueries = testByNE(userSize, es[i],step, queryGetter,label,deSize,angle,isSelfAware);
                avgTime+=Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }
        System.out.println("es size: "+ Arrays.toString(es));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
    }

    /**
     * Vary maxSize parameter function
     * @param deSizes maxSize
     * @param label alg label
     */
    public static void varyDeSize(int[] deSizes, int label){
        System.out.println("**************maxSizes experiment********************");

        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        for(int i = 0;i<deSizes.length;i++)
        {
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                ArrayList<Query> allQueries;
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = testByNE(userSize, e, step,queryGetter,label,deSizes[i],angle,isSelfAware);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }
        System.out.println("Divide Size: "+ Arrays.toString(deSizes));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
    }

    /**
     * Vary angle parameter function
     * @param angles angle
     * @param label alg label
     */
    public static void varyDeAngle(int[] angles, int label){
        System.out.println("**************Angle experiment********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();

        for(int i = 0;i<angles.length;i++)
        {
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                ArrayList<Query> allQueries;
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = testByNE(userSize, e, step,queryGetter,label,deSize,angles[i],isSelfAware);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }

        System.out.println("Angles: "+ Arrays.toString(angles));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
    }

    /**
     * Vary radius parameter function
     * @param radius radius
     * @param label alg label
     */
    public static void varyRadius(double[] radius, int label){
        System.out.println("**************Radius experiment********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();

        for(int i = 0;i<radius.length;i++)
        {
            mu = radius[i];
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                ArrayList<Query> allQueries;
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = testByNE(userSize, e, step,queryGetter,label,deSize,angle,isSelfAware);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }
        System.out.println("Radius: "+ Arrays.toString(radius));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
    }

    public static void varyParaStep(int[] steps,int label)
    {
        System.out.println("*********adaptSteps experiment**************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        for(int i = 0;i<steps.length;i++)
        {
            ArrayList<Query> allQueries = new ArrayList<Query>();
            QueriesSet queryGetter = resetPara(queriesFile);
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                allQueries = testByNE(userSize, e,steps[i], queryGetter,label,deSize,angle,isSelfAware);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(Shower.time(allQueries));
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }
        System.out.println("Step size: "+ Arrays.toString(steps));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
    }


    /**
     * Vary Alpha parameter of Gaming-CC
     * @param alphas alpha
     * @param label alg label
     */
    public static void varyAlpha_GCA(float[] alphas, int label){
        System.out.println("**************Alpha number result********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        for(int i = 0;i<alphas.length;i++)
        {
            alpha = alphas[i];
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                ArrayList<Query> allQueries;
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = testByNE(userSize, e,step, queryGetter,label,deSize,angle,isSelfAware);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }
        System.out.println("Alpha number: "+ Arrays.toString(alphas));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
        alpha = 2.0f;
    }

    /**
     * Vary Alpha parameter of SBP
     * @param alphas alg label
     */
    public static void varyAlpha_SBP(float[] alphas)
    {
        System.out.println("**************SBP result of Alpha********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        ArrayList<Integer> swapValidCounts = new ArrayList<>();
        for(int i = 0;i<alphas.length;i++)
        {
            float avgTime = 0;
            alpha = alphas[i];
            ArrayList<Query> allQueries;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = SBP.SBP_alg(userSize, queryDensity, interval, e_sbp, queryGetter);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            swapValidCounts.add(swapCheckCount_valid);
            sumTime = 0;
        }
        System.out.println("Alphas number: "+ Arrays.toString(alphas));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
        System.out.println("SwapValidCounts:"+swapValidCounts);
        alpha = 2.0f;
    }

//    /**
//     * Vary Alpha parameter of A*
//     * @param alphas alg label
//     */
//    public static void varyAlpha_TAstar(float[] alphas)
//    {
//        System.out.println("************Init result of Alpha**********************");
//        ArrayList<Long> runtime = new ArrayList<>();
//        ArrayList<Float> travelTime = new ArrayList<>();
//        for(int i = 0;i<alphas.length;i++)
//        {
//            alpha = alphas[i];
//            float avgTime = 0;
//            for(int expCount = 0; expCount<expNum;expCount++)
//            {
//                QueriesSet queryGetter = resetPara(queriesFile);
//                ArrayList<Query> allQueries = new ArrayList<Query>();
//                int refineSize = queryDensity*interval;
//                int refineCount = (userSize%refineSize == 0)? userSize/refineSize:userSize/refineSize+1;
//                for(int m=0;m<refineCount;m++)
//                {
//                    int curRefineSize = ((m+1)*refineSize>userSize)?userSize-m*refineSize:refineSize;
//                    HashMap<Edge, ArrayList<Flow>> nodeMap = FlowsNetwork.generateQueryInfos(allQueries,new HashMap<>());
//                    long t1 = System.currentTimeMillis();
//                    ArrayList<Query> queries = TAStar.AStar_Traffic(queryGetter, refineSize,m,curRefineSize,isSelfAware, nodeMap);//FlowsNetwork.roadFlows
//                    long t2 = System.currentTimeMillis();
//                    sumTime += (t2-t1);
//                    allQueries.addAll(queries);
//                    FlowsNetwork.greedyQueryQueue.addAll(allQueries);
//                    FlowsNetwork.Update();
//                }
//                avgTime += Shower.time(allQueries);
//            }
//            runtime.add(sumTime/expNum);
//            travelTime.add(avgTime/expNum);
//            sumTime = 0;
//        }
//        System.out.println("Alpha Number: "+ Arrays.toString(alphas));
//        System.out.println("runtime: "+ runtime);
//        System.out.println("travelTime: "+ travelTime);
//        alpha = 2.0f;
//    }

    /**
     * Vary Beta parameter of Gaming-CC
     * @param betas beta
     * @param label alg label
     */
    public static void varyBeta_GCA(float[] betas, int label){
        System.out.println("**************Beta number result********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        for(int i = 0;i<betas.length;i++)
        {
            beta = betas[i];
            float avgTime = 0;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                ArrayList<Query> allQueries;
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = testByNE(userSize, e,step, queryGetter,label,deSize,angle,isSelfAware);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            sumTime = 0;
        }
        System.out.println("Betas number: "+ Arrays.toString(betas));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
        beta = 2.0f;
    }

    /**
     * Vary Beta parameter of SBP
     * @param betas beta
     */
    public static void varyBeta_SBP(float[] betas)
    {
        System.out.println("**************SBP result of Beta********************");
        ArrayList<Long> runtime = new ArrayList<>();
        ArrayList<Float> travelTime = new ArrayList<>();
        ArrayList<Integer> swapCounts = new ArrayList<>();
        ArrayList<Integer> swapValidCounts = new ArrayList<>();
        for(int i = 0;i<betas.length;i++)
        {
            float avgTime = 0;
            beta = betas[i];
            ArrayList<Query> allQueries;
            for(int expCount = 0; expCount<expNum;expCount++)
            {
                QueriesSet queryGetter = resetPara(queriesFile);
                allQueries = SBP.SBP_alg(userSize, queryDensity, interval, e_sbp, queryGetter);
                avgTime += Shower.time(allQueries);
            }
            runtime.add(sumTime/expNum);
            travelTime.add(avgTime/expNum);
            swapCounts.add(swapCheckCount);
            swapValidCounts.add(swapCheckCount_valid);
            sumTime = 0;
        }
        System.out.println("Betas number: "+ Arrays.toString(betas));
        System.out.println("runtime: "+ runtime);
        System.out.println("travelTime: "+ travelTime);
        System.out.println("SwapCounts:"+swapCounts);
        System.out.println("SwapValidCounts:"+swapValidCounts);
        beta = 2.0f;
    }

//    /**
//     * Vary Beta parameter of A*
//     * @param betas beta
//     */
//    public static void varyBeta_TAstar(float[] betas)
//    {
//        System.out.println("\n************Init result of Beta**********************");
//        ArrayList<Long> runtime = new ArrayList<>();
//        ArrayList<Float> travelTime = new ArrayList<>();
//        for(int i = 0;i<betas.length;i++)
//        {
//            beta = betas[i];
//            float avgTime = 0;
//            for(int expCount = 0; expCount<expNum;expCount++)
//            {
//                QueriesSet queryGetter = resetPara(queriesFile);
//                ArrayList<Query> allQueries = new ArrayList<Query>();
//                int refineSize = queryDensity*interval;
//                int refineCount = (userSize%refineSize == 0)? userSize/refineSize:userSize/refineSize+1;
//                for(int m=0;m<refineCount;m++)
//                {
//                    int curRefineSize = ((m+1)*refineSize>userSize)?userSize-m*refineSize:refineSize;
//                    HashMap<Edge, ArrayList<Flow>> nodeMap = FlowsNetwork.generateQueryInfos(allQueries,new HashMap<>());
//                    long t1 = System.currentTimeMillis();
//                    ArrayList<Query> queries = TAStar.AStar_Traffic(queryGetter, refineSize,m,curRefineSize,isSelfAware, nodeMap);//FlowsNetwork.roadFlows
//                    long t2 = System.currentTimeMillis();
//                    sumTime += (t2-t1);
//                    allQueries.addAll(queries);
//                    FlowsNetwork.greedyQueryQueue.addAll(allQueries);
//                    FlowsNetwork.Update();
//                }
//                avgTime += Shower.time(allQueries);
//            }
//            runtime.add(sumTime/expNum);
//            travelTime.add(avgTime/expNum);
//            sumTime = 0;
//        }
//        System.out.println("Beta Number: "+ Arrays.toString(betas));
//        System.out.println("runtime: "+ runtime);
//        System.out.println("travelTime: "+ travelTime);
//        beta = 2.0f;
//    }

}
