package com.loopers.infrastructure.like;


import com.loopers.domain.Like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;

}
