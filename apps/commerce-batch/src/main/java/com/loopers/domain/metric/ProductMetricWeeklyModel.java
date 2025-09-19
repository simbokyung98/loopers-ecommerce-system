package com.loopers.domain.metric;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_metric_weekly",
        uniqueConstraints = @UniqueConstraint(name = "uq_weekly_product", columnNames = {"product_id", "week_start"}))
public class ProductMetricWeeklyModel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;
    @Column(name="week_start", nullable=false)
    private LocalDate weekStart;
    @Column(name="week_end", nullable=false)
    private LocalDate weekEnd;

    @Column(name="view_count",  nullable=false)
    private long viewCount;
    @Column(name="like_count",  nullable=false)
    private long likeCount;
    @Column(name="order_count", nullable=false)
    private long orderCount;

    @Column(name="score", nullable=false)
    private long score;
    @Column(name="rank_value")
    private Integer rankValue;


    public ProductMetricWeeklyModel(Long productId, LocalDate weekStart, LocalDate weekEnd,
                                    long viewCount, long likeCount, long orderCount,
                                    long score, Integer rankValue) {
        this.productId = productId;
        this.weekStart = weekStart;
        this.weekEnd   = weekEnd;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.orderCount= orderCount;
        this.score     = score;
        this.rankValue = rankValue;
    }
}
