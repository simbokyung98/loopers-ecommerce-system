package com.loopers.domain.Like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeToggleResult likeToggle(Long userId, Long productId){
        Optional<LikeModel> existing = likeRepository.findByUserIdAndProductId(userId, productId);

        if(existing.isPresent()){
            likeRepository.delete(existing.get());
            return LikeToggleResult.UNLIKED;
        }else{
            LikeModel likeModel = new LikeModel(userId, productId);
            likeRepository.save(likeModel);
            return LikeToggleResult.LIKED;
        }
    }

    public List<Long> getLikedProductIdsByUser(Long userId){
        return likeRepository.findAllProductIdByUserId(userId);
    }
}
