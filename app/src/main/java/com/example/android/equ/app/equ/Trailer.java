package com.example.android.equ.app.equ;

import java.util.List;

/**
 * Created by i on 2016-03-30.
 */
public class Trailer {
    int id;
    List<TrailerResult> results;

    public List<TrailerResult> getResults() {
        return results;
    }
}

class TrailerResult {
    String key;

    public String getKey(){
        return key;
    }

}
