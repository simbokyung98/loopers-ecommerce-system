package com.loopers.infrastructure.like;


import com.loopers.domain.Like.LikeModel;
import com.loopers.domain.Like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;

    @Override
    public LikeModel save(LikeModel likeModel) {
        return likeJpaRepository.save(likeModel);
    }

    @Override
    public void delete(LikeModel likeModel) {
        likeJpaRepository.delete(likeModel);
    }

    @Override
    public Optional<LikeModel> findByUserIdAndProductId(Long userId, Long productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<LikeModel> findAllByUserId(Long userId) {
        return likeJpaRepository.findAllByUserId(userId);
    }
}
