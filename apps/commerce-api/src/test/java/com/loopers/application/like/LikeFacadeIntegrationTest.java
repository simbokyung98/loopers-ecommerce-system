package com.loopers.application.like;


import com.loopers.application.like.dto.LikeCommand;
import com.loopers.application.like.dto.LikeInfo;
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
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("내 좋아요 상품 정보를 조회할 떄 , ")
    @Nested
    class GetLikedProducts{

        @BeforeEach
        void setup(){
            UserModel requestModel = new UserModel(
                    "testId",
                    Gender.MALE.getCode(),
                    "2024-05-22",
                    "loopers@test.com"
            );
            UserModel user = userRepository.save(requestModel);

            Long BRAND_ID = 1L;

            ProductModel p1 =
                    new ProductModel(
                            "테스트 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, BRAND_ID);
            ProductModel p2 =
                    new ProductModel(
                            "루퍼스 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, BRAND_ID);
            ProductModel product1 = productRepository.save(p1);
            ProductModel product2 = productRepository.save(p2);

            likeFacade.like(LikeCommand.Like.of(user.getId(), product1.getId()));
            likeFacade.like(LikeCommand.Like.of(user.getId(), product2.getId()));
        }

        @DisplayName("내 유저 정보로 좋아요 상품을 조회할 경우, 상품 정보를 반환한다")
        @Test
        void returnProductInfo_whenMyUserId(){

            Long userId = 1L;

            LikeInfo.LikeProducts result=
                likeFacade.getLikedProducts(userId);

            assertAll(
                    () -> assertThat(result.likeProducts()).isNotNull(),
                    () -> assertThat(result.likeProducts().size()).isEqualTo(2)
            );

        }
    }





}
