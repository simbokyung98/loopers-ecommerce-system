package com.loopers.application.like;


import com.loopers.application.like.dto.LikeToggleInfo;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 토글 할 때, ")
    @Nested
    class toggle {
        @DisplayName("좋아요 요청 시, 좋아요 결과가 반환된다")
        @Test
        void returnLikeResult_whenLikeIsToggled(){
            //arrange

            UserModel requestModel = new UserModel(
                    "testId",
                    Gender.MALE.getCode(),
                    "2024-05-22",
                    "loopers@test.com"
            );
            UserModel user = userRepository.save(requestModel);

            Long BRAND_ID = 1L;

            ProductModel productModel =
                    new ProductModel(
                            "테스트 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, BRAND_ID);
            ProductModel product = productRepository.save(productModel);

            //act
            LikeToggleInfo likeToggleInfo = likeFacade.toggle(user.getId(), product.getId());

            //assert
            assertAll(
                    () -> assertThat(likeToggleInfo).isNotNull(),
                    () -> assertThat(likeToggleInfo.totalLikeCount()).isEqualTo(product.getLikeCount()+1),
                    () -> assertThat(likeToggleInfo.liked()).isEqualTo(true)
            );



        }

            @DisplayName("좋아요 요청 취소 시, 좋아요 취소 결과가 반환된다")
            @Test
            void returnUnlikeResult_whenLikeIsCancelled(){
                //arrange

                UserModel requestModel = new UserModel(
                        "testId",
                        Gender.MALE.getCode(),
                        "2024-05-22",
                        "loopers@test.com"
                );
                UserModel user = userRepository.save(requestModel);

                Long BRAND_ID = 1L;

                ProductModel productModel =
                        new ProductModel(
                                "테스트 상품",
                                0L,
                                0L,
                                ProductStatus.SELL, BRAND_ID);
                ProductModel product = productRepository.save(productModel);

                //act
                likeFacade.toggle(user.getId(), product.getId());
                LikeToggleInfo likeToggleInfo = likeFacade.toggle(user.getId(), product.getId());

                //assert
                assertAll(
                        () -> assertThat(likeToggleInfo).isNotNull(),
                        () -> assertThat(likeToggleInfo.totalLikeCount()).isEqualTo(product.getLikeCount()),
                        () -> assertThat(likeToggleInfo.liked()).isEqualTo(false)
                );


            }
        }


}
