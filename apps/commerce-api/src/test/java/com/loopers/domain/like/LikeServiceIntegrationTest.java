package com.loopers.domain.like;

import com.loopers.domain.Like.LikeModel;
import com.loopers.domain.Like.LikeService;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 토글 할 때, ")
    @Nested
    class toggle {
        @DisplayName("좋아요가 없을 경우, 새로 좋아요를 생성한다")
        @Test
        void createLike_whenLikeDoesNotExist() {

            Long userId = 1L;
            Long productId = 1L;

            //act
            likeService.likeToggle(userId, productId);

            Boolean exists = likeJpaRepository.existsByUserIdAndProductId(userId, productId);
            assertThat(exists).isTrue();




        }

        @DisplayName("좋아요가 있을경우, 존재하는 좋아요를 삭제한다.")
        @Test
        void deleteLike_whenLikeAlreadyExists() {
            Long userId = 1L;
            Long productId = 1L;
            LikeModel likeModel = new LikeModel(userId, productId);
            likeJpaRepository.save(likeModel);

            likeService.likeToggle(userId, productId);

            Boolean exists = likeJpaRepository.existsByUserIdAndProductId(userId, productId);
            assertThat(exists).isFalse();

        }
    }



}
