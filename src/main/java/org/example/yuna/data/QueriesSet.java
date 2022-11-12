package org.example.yuna.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.yuna.conf.DataFileConfig;
import org.example.yuna.structure.Query;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Data
@AllArgsConstructor
public class QueriesSet {
    // The query arrival rate
    int queryDensity = 100;
    // The departure time of the first trip query
    float minDepartureTime  = 0f;

    // The file that stores the simulated trip queries
    String queryRequestFilepath = "queryRequestFilepath";
    String ODFilepath = "ODFilepath";

    // The starts,ends and departure time of trips
    ArrayList<Query> queries = new ArrayList<Query>();


    /**
     * Init queries set from file
     * @param queryRequestFilepath
     * @param queryDensity
     * @param minDepartureTime
     */
    public QueriesSet(String queryRequestFilepath, int queryDensity, float minDepartureTime) {
        // TODO Auto-generated constructor stub
        this.queryRequestFilepath = queryRequestFilepath;
        this.queryDensity = queryDensity;
        this.minDepartureTime = minDepartureTime;
        getInfo();
    }

    /**
     * Get simulated trip queries (String format)
     * @return String[i] is the trip query i
     */
    public ArrayList<String> getODInfo( )
    {
        ArrayList<String> queryRequest = new ArrayList<String>();
        try {
            File file = new File(DataFileConfig.getSetting(queryRequestFilepath));
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String lineString = null;
            while((lineString = reader.readLine()) != null)
            {
                queryRequest.add(lineString);
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Read Error");
            e.printStackTrace();
        }
        return queryRequest;
    }


    /**
     * Get the starts,ends and departure time of trips
     */
    public void getInfo()
    {
        ArrayList<String> queryRequest = getODInfo( );
        // update the departure time
        try {
            File file = new File(DataFileConfig.getSetting(queryRequestFilepath));
            Writer out = new FileWriter(file);
            for (int i = 0; i < queryRequest.size(); i++) {
                if(i % queryDensity == 0)
                {
                    minDepartureTime += 1;
                }
                int start = Integer.parseInt(queryRequest.get(i).split(" ")[2]);
                int end = Integer.parseInt(queryRequest.get(i).split(" ")[3]);
                this.queries.add(new Query(i,minDepartureTime,start,end));
                String data = i + " "  + minDepartureTime +" "+ start + " " + end + "\n";
                out.write(data);
            }
            out.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * Random generate queries function
     * @param range ranges
     * @param sum  number of queries
     * @return
     */
    public List<Integer> generateOD(int range, int sum){
        List<Integer> OD = new LinkedList<>();

        File file = new File(DataFileConfig.getSetting(ODFilepath));
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            int lineCountMax = range;
            String line = null;
            while ((line = bufferedReader.readLine())!=null && lineCountMax>0){
                OD.add(Integer.parseInt(line));
                lineCountMax--;
            }
            bufferedReader.close();

            File fileIn = new File(DataFileConfig.getSetting(queryRequestFilepath));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileIn));
            Random random = new Random();
            for (int i = 0; i < sum; i++) {
                int start_c = random.nextInt(range);
                int end_c = random.nextInt(range);
                if(start_c == end_c){
                    end_c = (end_c+10)%range;
                }
                String data = i + " "  + 0 +" "+ OD.get(start_c) + " " + OD.get(end_c) + "\n";
                bufferedWriter.write(data);
            }
            bufferedWriter.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return OD;
    }



}
