package com.loopers.domain.Like;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LikeRepository {

    LikeModel save(LikeModel likeModel);

    void delete(LikeModel likeModel);

    Optional<LikeModel> findLike(Long userId, Long productId);

    List<Long> findAllProductIdByUserId(Long userId);

    Long countByProductId(Long productId);

    Map<Long, Long> countByProductIds(List<Long> productIds);
}
