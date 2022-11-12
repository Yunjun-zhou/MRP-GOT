package org.example.yuna.conf;

import org.example.yuna.data.FlowsNetwork;
import org.example.yuna.data.QueriesSet;

//Parameters Setting
public class Setting {

    public static boolean isSelfAware = true;
    public static boolean isRushHour = true;//rushHour flag
    public static boolean isPreCheck = true;

    public static String edgeFile = "roadFile_CD";
    public static String nodeFile = "nodeFile_CD";
    public static String miniFile = "MinTravelTimeFile_CD";
    public static String queriesFile = "queryRequestFilepath_CD";
    public static int[] userSizesNY = {10000,14000,18000,22000,26000};
    public static int[] userSizesTG = { 5000, 10000, 15000, 20000, 25000};
    public static int[] userSizesCD = { 4000, 8000, 12000, 16000, 20000};

    public static float[] es = { 0.01f, 0.02f, 0.05f, 0.08f,0.1f };
    public static int[] adaptSteps = {5,10,15,20,25};


    public  static int[] deSizes = {50,100,200,300,400};
    public static int[] angles = {30,45,60,90,105};

    public static double[] radius = {0.2,0.4,0.6,0.8,1.0};

    public static  float[] alphas = {1.0f,2.0f,3.0f,4.0f,5.0f};
    public static  float[] betas = {1.0f,1.5f,2.0f,2.5f,3.0f};

    /**
     * The default settings
     */
    public static int userSize = 12000;//TG:10000 NY:10000 Chengdu:12000
    public static int interval = 2;
    public static float e = 0.1f;
    public static float e_sbp = 0.001f;
    public static int step = 5;
    public static int queryDensity = 200;
    public static int deSize = 250;
    public static int angle = 60;
    public static double mu = 1.0;

    public static float alpha = 2.0f;
    public static float beta = 2.0f;
    public static int expNum = 5;
    public static long sumTime = 0;
    public static int swapCheckCount_valid = 0;
    public static int swapCheckCount = 0;


    /**
     * Reset parameters and queries
     * @param queriesPath queries path
     * @return
     */
    public static QueriesSet resetPara(String queriesPath){
        swapCheckCount_valid = 0;
        swapCheckCount = 0;
        return new QueriesSet(queriesPath,queryDensity,0);
    }
}
