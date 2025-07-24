### 브랜드 조회
    sequenceDiagram
    participant User
    participant BC as BrandController
    participant BS as BrandService
    
    User->>BC: 브랜드 조회 요청(brandId)
    BC->>BS: 브랜드 조회(brandId)
    alt 브랜드 없음
    BS-->>BC: 404 Not Found
    else 브랜드 존재
    BS-->>BC: 브랜드 정보
    end

[![](https://mermaid.ink/img/pako:eNptkU9PwjAYxr9K8540ATLWMkYPHDZj4sXL4sXsUtcCS_YHu86ohKsnbmpMCBK9a2KUg99p8zu4MSAI9tD0fd_f8zxNOwIv5gIooGIl4jIVkSeOfNaXLHSjsjlkUvmeP2SRQmeJkPtdy0YsQZZkEbfjSMk4CP7FnA3mCHnle6Jiqr20rne7lk1R9j3J5rPsfoby14-f6QTl04f88_3gopSe8MNKYNkl7uzjOxwL1DbydJc_T1YOTn2VSDSCTmOFjuM04tVQBIn4Y73I52-7uq35y2P2tVhJIw416EufA-2xwqcGoZAhK2sYlYwLaiBC4QItjlz0WBooF9xoXOiKtzqP4xCokmmhlHHaH6yLdMiZWn_PxlwWgULaxd0V0KZmGPrSBegIroHqptbQ2xrRO0THJsZmDW6A4laDGLjTwobRbGKT6Ma4BrfLXK1haphgYhKzbbaIpuHxL5QbxYw?type=png)](https://mermaid.live/edit#pako:eNptkU9PwjAYxr9K8540ATLWMkYPHDZj4sXL4sXsUtcCS_YHu86ohKsnbmpMCBK9a2KUg99p8zu4MSAI9tD0fd_f8zxNOwIv5gIooGIl4jIVkSeOfNaXLHSjsjlkUvmeP2SRQmeJkPtdy0YsQZZkEbfjSMk4CP7FnA3mCHnle6Jiqr20rne7lk1R9j3J5rPsfoby14-f6QTl04f88_3gopSe8MNKYNkl7uzjOxwL1DbydJc_T1YOTn2VSDSCTmOFjuM04tVQBIn4Y73I52-7uq35y2P2tVhJIw416EufA-2xwqcGoZAhK2sYlYwLaiBC4QItjlz0WBooF9xoXOiKtzqP4xCokmmhlHHaH6yLdMiZWn_PxlwWgULaxd0V0KZmGPrSBegIroHqptbQ2xrRO0THJsZmDW6A4laDGLjTwobRbGKT6Ma4BrfLXK1haphgYhKzbbaIpuHxL5QbxYw)

### 상품 목록 조회
    sequenceDiagram
    participant User
    participant PC as ProductController
    participant PS as ProductService
    participant LS as LikeService

    User->>PC: 상품목록 조회 요청(condition, pageable)
    PC->>PS: 상품목록 조회 (condition, pageable)
    alt 상품 목록이 비어 있음
    PS-->>PC: 빈 상품 목록 반환
    else 상품 존재
    loop 상품의 좋아요 수 요청(productId)
        PS->>LS: 상품 좋아요수 조회(productId)
        LS-->>PS: 상품 좋아요 수 정보
    end
    PS-->PC: 200 OK + 상품 목록 (+ 좋아요 수)
    end

[![](https://mermaid.ink/img/pako:eNp9kk9v0zAYxr-K9Z42LavSxM2yHHoJF0QlKlVcUC4m8bqIxA6ug4CqB8QOldgJMVQmdaLcQCBN0Ms-U9zvgPOnZaUVOVjx6-f3vO9jeQwhjyh4MKIvcspC-iAmQ0HSgCH9ZUTIOIwzwiR6MqJit9r3ERmhvuBRHkqfMyl4kuwVDu4JB1S8jEO6q-pVql78nG5J6rWc4Ljb7fseUu_erj5Mi-_fisUcqcXt6voSqeuP6tfPg5CzKJYxZ4Z2HlLyLKGHNd_3S3qwn_4PRxLZIKhm1HyJirsL9WmJ1M1UzS8b_8FxM15xN90mUHE7W32e1TqajOj6WC2W6uZHXU84z5q6ms-Q-vpeXV3oVEhNZ-t0mSjv72HUjNa07XZ7m1h_uQqr0mmM73C9etw9YN3wy1Xxe9lMzKJ7EcuElmmix4_Q0T8xD462XQ43PBgwFHEE3hnR8Q1IqUhJuYdxqQlAntOUBuDp34iekTyRAQRsojn9Mp5ynoInRa5JwfPh-XqTZxGR60e7MRe6IRU-z5kEr93G2KlcwBvDK_As12xZJya2TrFlu7btGvAaPLvTwo592rEdp922XWw5EwPeVH3Nlmva2MYudk_cDjZNPPkD6VZE4w?type=png)](https://mermaid.live/edit#pako:eNp9kk9v0zAYxr-K9Z42LavSxM2yHHoJF0QlKlVcUC4m8bqIxA6ug4CqB8QOldgJMVQmdaLcQCBN0Ms-U9zvgPOnZaUVOVjx6-f3vO9jeQwhjyh4MKIvcspC-iAmQ0HSgCH9ZUTIOIwzwiR6MqJit9r3ERmhvuBRHkqfMyl4kuwVDu4JB1S8jEO6q-pVql78nG5J6rWc4Ljb7fseUu_erj5Mi-_fisUcqcXt6voSqeuP6tfPg5CzKJYxZ4Z2HlLyLKGHNd_3S3qwn_4PRxLZIKhm1HyJirsL9WmJ1M1UzS8b_8FxM15xN90mUHE7W32e1TqajOj6WC2W6uZHXU84z5q6ms-Q-vpeXV3oVEhNZ-t0mSjv72HUjNa07XZ7m1h_uQqr0mmM73C9etw9YN3wy1Xxe9lMzKJ7EcuElmmix4_Q0T8xD462XQ43PBgwFHEE3hnR8Q1IqUhJuYdxqQlAntOUBuDp34iekTyRAQRsojn9Mp5ynoInRa5JwfPh-XqTZxGR60e7MRe6IRU-z5kEr93G2KlcwBvDK_As12xZJya2TrFlu7btGvAaPLvTwo592rEdp922XWw5EwPeVH3Nlmva2MYudk_cDjZNPPkD6VZE4w)


### 싱품 정보 조회

    sequenceDiagram
    participant User
    participant PC as ProductController
    participant PS as ProductService
    participant LS as LikeService

    User->>PC: 상품 조회 요청(productId)
    PC->>PS: 상품 조회 (productId)
    alt 상품 없음
    PS-->>PC: 404 Not Found
    else 상품 존재
    PS->>LS: 상품 좋아요수 조회(productId)
    LS-->>PS: 상품 좋아요 수 정보
    PS-->PC: 200 OK + 상품 목록 (+ 좋아요 수)
    end

[![](https://mermaid.ink/img/pako:eNptkkFLwzAYhv9K-E7K5siabNYcdqkI4tDB8CK9hDbOYtvMLBV17CCKCO4kiiIo6k1REPVfrf4H23Wbm1sOIV_yvN_7kqQNjnQFMGiJvUiEjlj2eEPxwA5RMppcac_xmjzUaLMl1PRuzUK8hWpKupGjLRlqJX1_JlgfA-tC7XuOmKaqfarq7YoJJJvTBAuVSs1iKD45_rk8R_HTx89dF8V3V_Hn-1wza77qzmd8zUrp-n96iuO-HiE3Z_F9dyCvLwzcKKZoXWq0IqPQzQ6F3xJ_fb_jh7eRqFKpjnk-X8TXp2nA89tBgCn_amY0Q4T6qsfr3tf3WKY0koEx2lhDuaGk9_rSe7pHc7lJ8cBBhC7koaE8F9g2T6LnIRAq4GkN7ZSxQe-IQNjAkqUrtnnkaxvssJPokpfZkjIAplWUKJWMGjvDImq6XA8_zai5SgyFspLr0sBoqd8CWBsOgBkmLhiLmBpL1CAmIWYeDoGRUoGWyVKJlMvFIjGpUe7k4ahvigsmJpRQk5qLZoliTDu_SJsAew?type=png)](https://mermaid.live/edit#pako:eNptkkFLwzAYhv9K-E7K5siabNYcdqkI4tDB8CK9hDbOYtvMLBV17CCKCO4kiiIo6k1REPVfrf4H23Wbm1sOIV_yvN_7kqQNjnQFMGiJvUiEjlj2eEPxwA5RMppcac_xmjzUaLMl1PRuzUK8hWpKupGjLRlqJX1_JlgfA-tC7XuOmKaqfarq7YoJJJvTBAuVSs1iKD45_rk8R_HTx89dF8V3V_Hn-1wza77qzmd8zUrp-n96iuO-HiE3Z_F9dyCvLwzcKKZoXWq0IqPQzQ6F3xJ_fb_jh7eRqFKpjnk-X8TXp2nA89tBgCn_amY0Q4T6qsfr3tf3WKY0koEx2lhDuaGk9_rSe7pHc7lJ8cBBhC7koaE8F9g2T6LnIRAq4GkN7ZSxQe-IQNjAkqUrtnnkaxvssJPokpfZkjIAplWUKJWMGjvDImq6XA8_zai5SgyFspLr0sBoqd8CWBsOgBkmLhiLmBpL1CAmIWYeDoGRUoGWyVKJlMvFIjGpUe7k4ahvigsmJpRQk5qLZoliTDu_SJsAew)