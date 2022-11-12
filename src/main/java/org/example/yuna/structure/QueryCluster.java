package org.example.yuna.structure;

import lombok.Data;

import java.util.LinkedList;

@Data
public class QueryCluster {
    private Integer index;
    private Query Center;
    private LinkedList<Query> queriesList;

    public QueryCluster() {
    }

    public QueryCluster(Integer index, Query center, LinkedList<Query> queriesList) {
        this.index = index;
        Center = center;
        this.queriesList = queriesList;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Query getCenter() {
        return Center;
    }

    public void setCenter(Query center) {
        Center = center;
    }

    public LinkedList<Query> getQueriesList() {
        return queriesList;
    }

    public void setQueriesList(LinkedList<Query> queriesList) {
        this.queriesList = queriesList;
    }
}
