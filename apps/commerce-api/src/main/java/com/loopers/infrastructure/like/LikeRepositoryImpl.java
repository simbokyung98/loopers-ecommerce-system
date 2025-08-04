package com.loopers.infrastructure.like;


import com.loopers.domain.Like.LikeModel;
import com.loopers.domain.Like.LikeRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Long countByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }

    @Override
    public Map<Long, Long> countByProductIds(List<Long> productIds) {
        return jpaQueryFactory.select(likeModel.productId, likeModel.count())
                .from(likeModel)
                .where(likeModel.productId.in(productIds))
                .groupBy(likeModel.productId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(likeModel.productId),
                        tuple -> Optional.ofNullable(tuple.get(likeModel.count())).orElse(0L)
                ));
    }
}
