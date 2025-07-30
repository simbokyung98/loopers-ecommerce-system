package com.loopers.domain.Like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    public void likeToggle(Long userId, Long productId){
        Optional<LikeModel> existing = likeRepository.findByUserIdAndProductId(userId, productId);

        if(existing.isPresent()){
            likeRepository.delete(existing.get());
        }else{
            LikeModel likeModel = new LikeModel(userId, productId);
            likeRepository.save(likeModel);

        }
    }

    public List<LikeModel> getList(Long userId){
        return likeRepository.findAllByUserId(userId);
    }
}
