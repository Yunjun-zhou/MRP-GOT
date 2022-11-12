import org.example.yuna.conf.DataFileConfig;
import org.example.yuna.structure.Location;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SelectQueryNode {

    public List<Integer> getNodes(Location l1, Location l2) throws IOException {
        List<Integer> nodes = new LinkedList<>();
        String nodePath = "nodeFile";
        BufferedReader reader = new BufferedReader(new FileReader(DataFileConfig.getSetting(nodePath)));
        String line;
        while ((line = reader.readLine()) != null){
            String[] temp = line.split(" ");
            int id = Integer.parseInt(temp[0]);
            Double lon = Double.parseDouble(temp[1]);
            Double lat = Double.parseDouble(temp[2]);
            if(lon>=l1.getLongitude() && lon<=l2.getLongitude() && lat>=l1.getLatitude() && lat <= l2.getLatitude()){
                nodes.add(id);
            }
        }
        reader.close();
        return nodes;
    }


    public void getAllNodes(List<Location> ls1,List<Location> ls2,int max) throws IOException {
        String resPath = "ODFilepath";
        BufferedWriter writer = new BufferedWriter(new FileWriter(DataFileConfig.getSetting(resPath)));

        for (int i = 0;i<ls1.size();i++){
            List<Integer> nodes = getNodes(ls1.get(i),ls2.get(i));
            int len = Math.min(nodes.size(),max);
            for (int j = 0; j < len; j++) {
                writer.write(nodes.get(j)+"\r\n");
            }
            writer.write("end\r\n");
        }
        writer.close();
    }

    @Test
    public void testNodes(){
        List<Location> ls1 = new LinkedList<>();
        List<Location> ls2 = new LinkedList<>();

        ls1.add(new Location(-74.0177778,40.6205556));
        ls2.add(new Location(-74.0077778,40.6230556));

        ls1.add(new Location(-74.0041667,40.6205556));
        ls2.add(new Location(-73.9886111,40.6266667));

        ls1.add(new Location(-73.9872222,40.6116667));
        ls2.add(new Location(-73.9744444,40.6163889));

        ls1.add(new Location(-74.0311111,40.6266667));
        ls2.add(new Location(-74.0211111,40.6313889));

        ls1.add(new Location(-73.9980556,40.6155556));
        ls2.add(new Location(-73.9880556,40.6205556));

        ls1.add(new Location(-74.0094444,40.6294444));
        ls2.add(new Location(-73.9925,40.6313889));

        ls1.add(new Location(-73.9838889,40.6194444));
        ls2.add(new Location(-73.975,40.6238889));

        ls1.add(new Location(-74.0327778,40.6194444));
        ls2.add(new Location(-74.0216667,40.6225));

        try {
            getAllNodes(ls1,ls2,50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
