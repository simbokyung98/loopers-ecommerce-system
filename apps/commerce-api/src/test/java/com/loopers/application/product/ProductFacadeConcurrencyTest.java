package com.loopers.application.product;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.Like.LikeRepository;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductFacadeConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeFacade likeFacade;


    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동시에 좋아요를 요청해도 상품의 좋아요 수가 정확히 반영되어야 한다.")
    @Test
    void concurrencyTest_likeCountShouldBeAccurate_whenMultipleUsersLikeSimultaneously() throws InterruptedException {

        BrandModel brandModel = new BrandModel("테스트 브랜드");
        BrandModel brand =
                brandRepository.saveBrand(brandModel);
        ProductModel product = productRepository.saveProduct(
                new ProductModel("비관적 락 테스트 상품", 100L, 1000L, ProductStatus.SELL, brand.getId())
        );

        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);


        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userSuffix = i;
            executorService.execute(() -> {
                try {
                    UserModel user = userRepository.save(new UserModel("user" + userSuffix, "F", "1990-01-01", "user" + userSuffix + "@example.com"));
                    likeFacade.like(LikeCriteria.Like.of(user.getId(), product.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // when
        ProductInfo.Product productInfo = productFacade.getProduct(product.getId());

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(productInfo.likeCount()).isEqualTo(threadCount);
    }
}
