package com.loopers.domain.brand;

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
@Table(name = "tb_brand")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandModel extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String  name;

    public BrandModel(String name){
        if(name == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드의 이름은 null 일 수 없습니다.");
        }
        this.name = name;
    }
}
