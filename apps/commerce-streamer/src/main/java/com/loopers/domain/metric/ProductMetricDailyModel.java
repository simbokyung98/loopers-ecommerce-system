package com.loopers.domain.metric;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "product_metric_daily",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetricDailyModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private LocalDate date;

    private long likeCount = 0;
    private long saleCount = 0;
    private long viewCount = 0;


    public ProductMetricDailyModel(Long productId, LocalDate date) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date must not be null");
        }
        this.productId = productId;
        this.date = date;
    }


    //집계테이블에 맞게 메소드명은 기술적 관점으로 작성
    public void updateLikeCount(long delta) {
        long newValue = this.likeCount + delta;
        validateNonNegative(newValue, "likeCount");
        this.likeCount = newValue;
    }

    public void updateSaleCount(long delta) {
        long newValue = this.saleCount + delta;
        validateNonNegative(newValue, "saleCount");
        this.saleCount = newValue;
    }

    public void updateViewCount(long delta) {
        long newValue = this.viewCount + delta;
        validateNonNegative(newValue, "viewCount");
        this.viewCount = newValue;
    }

    // ===== 공통 밸리데이션 =====

    private void validateNonNegative(long newValue, String fieldName) {
        if (newValue < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative (value=" + newValue + ")");
        }
    }
}
