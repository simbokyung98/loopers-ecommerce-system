package com.loopers.application.like;

import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.like.dto.LikeInfo;
import com.loopers.domain.Like.LikeService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private LikeService likeService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private LikeFacade likeFacade;

    @DisplayName("내 좋아요 상품 정보를 조회할 떄 , ")
    @Nested
    class GetLikedProducts{

        @Test
        @DisplayName("좋아요한 상품이 있는 경우, 상품 정보를 반환한다.")
        void shouldReturnLikedProducts_whenUserHasLikedProducts() {
            // given
            Long userId = 1L;
            Long brandId = 1L;

            List<Long> likedProductIds = List.of(100L, 200L);

            ProductModel p1 =
                    new ProductModel(
                            "테스트 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, brandId);
            ProductModel p2 =
                    new ProductModel(
                            "루퍼스 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, brandId);

            List<ProductModel> products = List.of(p1, p2);

            when(likeService.getLikedProductIdsByUser(userId)).thenReturn(likedProductIds);
            when(productService.getListByIds(likedProductIds)).thenReturn(products);

            // act
            LikeInfo.LikeProducts result = likeFacade.getLikedProducts(userId);

            // assert
            assertThat(result.likeProducts()).hasSize(2);
        }

        @Test
        @DisplayName("좋아요한 상품이 없는 경우, 빈 리스트를 반환한다.")
        void shouldReturnEmptyList_whenUserHasNoLikedProducts() {

            Long userId = 1L;
            when(likeService.getLikedProductIdsByUser(userId)).thenReturn(List.of());

            // act
            LikeInfo.LikeProducts result = likeFacade.getLikedProducts(userId);

            // assert
            assertThat(result.likeProducts()).isEmpty();
            verify(productService, never()).getListByIds(any());
        }

    }

    @DisplayName("좋아요 할 때, ")
    @Nested
    class Like {
        @DisplayName("이미 좋아요 상태면 likeCount 증가 호출하지 않는다")
        @Test
        void NotIncreaseCount_whenAlreadyLiked() {

            Long userId = 1L, productId = 10L;
            when(likeService.like(userId, productId)).thenReturn(false); // 이미 좋아요였음

            // act
            likeFacade.like(new LikeCriteria.Like(userId, productId));

            // assert
            verify(userService).checkExistUser(userId);
            verify(productService).checkExistProduct(productId);
            verify(likeService).like(userId, productId);
            verify(productService, never()).increaseLikeCount(productId);
        }
    }
    @DisplayName("좋아요 취소 할 때, ")
    @Nested
    class Dislike {
        @DisplayName("좋아요 취소가 실제로 발생하지 않으면 likeCount 감소 호출하지 않는다")
        @Test
        void notDecreaseCount_whenNotLiked() {

            Long userId = 1L, productId = 10L;
            when(likeService.dislike(userId, productId)).thenReturn(false);

            // act
            likeFacade.dislike(new LikeCriteria.Dislike(userId, productId));

            // assert
            verify(userService).checkExistUser(userId);
            verify(likeService).dislike(userId, productId);
            verify(productService, never()).decreaseLikeCount(productId);
        }
    }
}
