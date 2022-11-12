package org.example.yuna.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Flow implements Comparable<Flow>{
    private float arriveTime;
    private float processTime;

    private int location;

    public Flow(float arriveTime, float processTime) {
        this.arriveTime = arriveTime;
        this.processTime = processTime;
        location = -1;
    }

    public void showInfo()
    {
        System.out.println( getArriveTime()+"+"+getProcessTime()+" nextTime: "+(arriveTime+processTime));
    }

    @Override
    public int compareTo(Flow o) {
        // TODO Auto-generated method stub
        return this.arriveTime>o.arriveTime?1:-1;
    }
}
