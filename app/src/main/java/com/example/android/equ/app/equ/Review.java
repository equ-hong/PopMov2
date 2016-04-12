package com.example.android.equ.app.equ;

import java.util.List;

/**
 * Created by i on 2016-03-30.
 */
public class Review {
    int id;
    List<ReviewResult> results;

    public List<ReviewResult> getResults() {
        return results;
    }
}

class ReviewResult {
    String author;
    String content;

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}

