package com.loopers.infrastructure.like;


import com.loopers.domain.Like.LikeModel;
import com.loopers.domain.Like.LikeRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.loopers.domain.Like.QLikeModel.likeModel;

@RequiredArgsConstructor
@Repository
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public LikeModel save(LikeModel likeModel) {
        return likeJpaRepository.save(likeModel);
    }

    @Override
    public void delete(LikeModel likeModel) {
        likeJpaRepository.delete(likeModel);
    }


    @Override
    public Optional<LikeModel> findLike(Long userId, Long productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<Long> findAllProductIdByUserId(Long userId) {

        return jpaQueryFactory.select(likeModel.productId)
                .from(likeModel)
                .where(likeModel.userId.eq(userId))
                .fetch();
    }


}
