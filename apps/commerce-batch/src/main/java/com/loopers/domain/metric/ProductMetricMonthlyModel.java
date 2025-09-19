package com.loopers.domain.metric;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_metric_monthly",
        uniqueConstraints = @UniqueConstraint(name = "uq_monthly_product", columnNames = {"product_id", "month_start"}))
public class ProductMetricMonthlyModel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_id", nullable=false)
    private Long productId;
    @Column(name="month_start", nullable=false)
    private LocalDate monthStart;
    @Column(name="month_end", nullable=false)
    private LocalDate monthEnd;

    @Column(name="view_count", nullable=false)
    private long viewCount;
    @Column(name="like_count", nullable=false)
    private long likeCount;
    @Column(name="order_count", nullable=false)
    private long orderCount;

    @Column(name="score", nullable=false)
    private long score;
    @Column(name="rank_value")
    private Integer rankValue;

    public ProductMetricMonthlyModel(Long productId, LocalDate monthStart, LocalDate monthEnd,
                                     long viewCount, long likeCount, long orderCount,
                                     long score, Integer rankValue) {
        this.productId = productId;
        this.monthStart= monthStart;
        this.monthEnd  = monthEnd;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.orderCount= orderCount;
        this.score     = score;
        this.rankValue = rankValue;
    }
}
