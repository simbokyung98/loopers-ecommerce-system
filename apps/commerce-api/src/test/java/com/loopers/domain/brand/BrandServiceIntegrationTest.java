package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class BrandServiceIntegrationTest {

    @Autowired
    BrandService brandService;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("브랜드 정보를 조회할 때, ")
    @Nested
    class Get {

        @DisplayName("브랜드가 존재하지 않는 경우,NOT_FOUND 예외가 발생하며 실패한다")
        @Test
        void throwsNotFoundException_whenDoNotExist(){
            Long notExistBrandId = 999L;

            assertThatException()
                    .isThrownBy(() -> brandService.getBrand(notExistBrandId))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("브랜드 id로 브랜드 정보를 오쳥하면, 브랜드 정보를 반환한다.")
        @Test
        void returnBrand_whenBrandExist(){

            String name = "브랜드 이름";

            BrandModel brandModel = new BrandModel(name);

            BrandModel response =
                    brandRepository.saveBrand(brandModel);

            //act
            BrandModel result = brandService.getBrand(response.getId());

            //assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getName()).isEqualTo(name)
            );

        }
    }
}
