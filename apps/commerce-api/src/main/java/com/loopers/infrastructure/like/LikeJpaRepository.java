package com.loopers.infrastructure.like;


import com.loopers.domain.Like.LikeModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeJpaRepository extends JpaRepository<LikeModel, Long> {

}
