package com.loopers.application.ranking;

public record Weights(int view, int like, int order) {
    public static final Weights DEFAULT = new Weights(1, 5, 20);
    public int sum() { return view + like + order; }
}
