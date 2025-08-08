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
    public boolean like(Long userId, Long productId){


        return likeRepository.findLike(userId, productId)
                .map(like -> {
                    if(like.getDeletedAt() != null){
                        like.restore();
                        return true;
                    }else {
                        return false;
                    }
                })//이미 존재
                .orElseGet(() -> {
                    LikeModel likeModel = new LikeModel(userId, productId);
                    likeRepository.save(likeModel);
                    return true;
                });

    }

    @Transactional
    public Boolean dislike(Long userId, Long productId){

        return likeRepository.findLike(userId, productId)
                .map(like -> {
                    if(like.getDeletedAt() != null){
                      return false;
                    }else {
                        like.delete();
                        return true;
                    }
                })//좋아요 한 적 없음
                .orElseGet(() -> {
                  return false;
                });

    }

    public List<Long> getLikedProductIdsByUser(Long userId){
        return likeRepository.findAllProductIdByUserId(userId);
    }

}
