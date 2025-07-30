package com.loopers.infrastructure.like;


import com.loopers.domain.Like.LikeModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeModel, Long> {

    Optional<LikeModel> findByUserIdAndProductId(Long userId, Long productId);

    Boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<LikeModel> findAllByUserId(Long userId);

}
