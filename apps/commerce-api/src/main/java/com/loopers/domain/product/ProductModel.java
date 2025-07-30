package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.Like.LikeToggleResult;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "tb_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductModel extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "stock", nullable = false)
    private Long stock;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Column(name = "brand_id", nullable = false)
    private Long brandId;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;


    public ProductModel(String name, Long stock, Long price, ProductStatus status, Long brandId){
        if(name == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 이름은 null 일 수 없습니다.");
        }
        if(stock == null || stock < 0){
            throw new CoreException(ErrorType.BAD_REQUEST,"상품의 재고는 null 이거나 음수일 수 없습니다.");
        }

        if(price == null || price < 0){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 가격은 null 이거나 음수일 수 없습니다.");
        }

        if(status == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 상태는 null 일 수 없습니다.");
        }

        if(brandId == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "상품의 브랜드 아이디는 null 일 수 없습니다.");
        }

        this.name = name;
        this.stock = stock;
        this.price = price;
        this.status = status;
        this.brandId = brandId;
        this.likeCount = 0L;
    }

    public boolean isOnSell(){
        return status == ProductStatus.SELL;
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        this.likeCount -= 1;
    }

    public void applyLikeToggle(LikeToggleResult result) {
        if (result == LikeToggleResult.LIKED) {

            likeCount++;
        } else {
            if(likeCount == 0){
                throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 좋아요 취소 입니다.");
            }
            likeCount--;
        }
    }






}
