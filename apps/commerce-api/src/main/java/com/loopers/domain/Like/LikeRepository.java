package com.loopers.domain.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {

    LikeModel save(LikeModel likeModel);

    void delete(LikeModel likeModel);


    Optional<LikeModel> findLike(Long userId, Long productId);

    List<Long> findAllProductIdByUserId(Long userId);

}

