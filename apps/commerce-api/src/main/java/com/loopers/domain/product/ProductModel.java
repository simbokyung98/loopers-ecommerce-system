package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
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
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductModel extends BaseEntity {

    @Column
    private String name;
    @Column
    private Long stock;
    @Column
    private Long price;
    @Column
    private ProductStatus status;

    public ProductModel(String name, Long stock, Long price, ProductStatus status){
        if(name == null){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if(stock == null || stock < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        if(price == null || price < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        if(status == null){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        this.name = name;
        this.stock = stock;
        this.price = price;
        this.status = status;
    }





}
