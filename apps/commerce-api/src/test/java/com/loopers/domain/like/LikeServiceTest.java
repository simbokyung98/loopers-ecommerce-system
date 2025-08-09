package com.loopers.domain.like;

import com.loopers.domain.Like.LikeModel;
import com.loopers.domain.Like.LikeRepository;
import com.loopers.domain.Like.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @DisplayName("좋아요를 요청할 떄, ")
    @Nested
    class Like {
        @DisplayName("좋아요가 없을 경우, 새로 좋아요를 생성한다")
        @Test
        void like_savesNewLike_whenNotExists() {

            Long userId = 1L;
            Long productId = 1L;

            when(likeRepository.findLike(userId, productId))
                    .thenReturn(Optional.empty());

            //act
            Boolean created = likeService.like(userId, productId);

            //assert
            assertTrue(created);
            verify(likeRepository).save(argThat(model ->
                    model.getUserId().equals(userId) && model.getProductId().equals(productId)));

        }

        @DisplayName("좋아요가 이미 존재하면 저장하지 않는다.")
        @Test
        void like_doesNotSave_whenAlreadyExists() {

            Long userId = 1L;
            Long productId = 100L;
            LikeModel existing = new LikeModel(userId, productId);

            when(likeRepository.findLike(userId, productId))
                    .thenReturn(Optional.of(existing));

            //act
            Boolean created = likeService.like(userId, productId);

            // assert
            assertFalse(created);
            assertNull(existing.getDeletedAt());
            verify(likeRepository, never()).save(any(LikeModel.class));
        }

        @Test
        @DisplayName("soft-delete 된 좋아요가 있으면 restore 한다")
        void restores_whenSoftDeletedExists() {
            Long userId = 1L;
            Long productId = 100L;
            LikeModel existing = new LikeModel(userId, productId);

            existing.delete();

            when(likeRepository.findLike(userId, productId))
                    .thenReturn(Optional.of(existing));

            boolean result = likeService.like(userId, productId);

            assertTrue(result);
            assertNull(existing.getDeletedAt());
            verify(likeRepository, never()).save(any());
        }

    }


    @DisplayName("좋아요 취소를 요청할 떄, ")
    @Nested
    class Dislike {
        @Test
        @DisplayName("좋아요가 존재하면 삭제한다.")
        void dislike_deletesLike_whenExists() {

            Long userId = 1L;
            Long productId = 100L;
            LikeModel existing = new LikeModel(userId, productId);

            when(likeRepository.findLike(userId, productId))
                    .thenReturn(Optional.of(existing));


            Boolean deleted = likeService.dislike(userId, productId);

            assertTrue(deleted);
            assertNotNull(existing.getDeletedAt());
        }

        @Test
        @DisplayName("이미 취소된 좋아요면 아무 것도 하지 않고 false를 반환한다.")
        void dislike_noop_whenAlreadyDeleted() {

            Long userId = 1L;
            Long productId = 100L;
            LikeModel existing = new LikeModel(userId, productId);
            existing.delete();

            when(likeRepository.findLike(userId, productId))
                    .thenReturn(Optional.of(existing));


            Boolean deleted = likeService.dislike(userId, productId);


            assertFalse(deleted);
            assertNotNull(existing.getDeletedAt());
        }

        @Test
        @DisplayName("좋아요가 존재하지 않으면 false 반환하고 아무 것도 하지 않는다.")
        void returnFalse_whenNotExists() {
            Long userId = 1L;
            Long productId = 100L;

            when(likeRepository.findLike(userId, productId)).thenReturn(Optional.empty());

            boolean deleted = likeService.dislike(userId, productId);

            assertFalse(deleted);

        }



    }



}
