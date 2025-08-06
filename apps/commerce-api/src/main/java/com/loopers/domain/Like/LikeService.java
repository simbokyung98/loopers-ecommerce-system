package com.loopers.domain.Like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public void like(Long userId, Long productId){
        likeRepository.findLike(userId, productId)
                .orElseGet(() -> {
                    LikeModel likeModel = new LikeModel(userId, productId);
                    return likeRepository.save(likeModel);
                });
    }

    @Transactional
    public void dislike(Long userId, Long productId){
        likeRepository.findLike(userId, productId)
                .ifPresent(likeRepository::delete);
    }

    public List<Long> getLikedProductIdsByUser(Long userId){
        return likeRepository.findAllProductIdByUserId(userId);
    }

}
