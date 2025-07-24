### 상품 좋아요 등록/취소
```mermaid 
        sequenceDiagram
        participant User
        participant LC as LikeController
        participant US as UserService
        participant PS as ProductService
        participant LS as LikeService
        participant LR as LikeRepository
        
        
            User->>LC: 상품 좋아요 등록/취소 요청(productId)
            LC->>US : 사용자 인증 확인 (X-USER-ID)
            alt 인증 실패(사용자 미존재, 헤더 미존재)
            US-->>LC: 401 UNAUTHORIZED
            else 인증 성공
            US-->>LC: 사용자 정보 반환
            LC->>PS: 상품상태 조회(productId)
            alt 존재하지 않는 상품
            PS-->LC: 404 not found
            else 판매중이지 않는 상품
            PS-->>LC : 409 CONFLICT
            else 
            PS-->>LC: 상품 정보
            LC->>LS : 좋아요 처리 요청(userId, productId)
            LS->>LR : 좋아요 항목 조회(userID, productId)
            alt 좋아요가 존재할 경우
            LS->>LR : 좋아요 삭제
            else 좋아요가 존재하지 않을 경우
            LS->>LR : 좋아요 등록(userId, productId)
            end
                alt 저장 실패 ( 사유 불문 )
                    LR -->> LC: 500 Internal Server Error
                else 저장 성공
                    LR -->> LC: 좋아요 반영 결과
            end
        end
    end
```



### 내가 좋아요 한 상품 목록 조회
```mermaid 
        sequenceDiagram
        participant User
        participant LC as LikeController
        participant US as UserService
        participant PS as ProductService
        participant LS as LikeService
        
            User->>LC: 내 좋아요 목록 요청
            LC->>US : 사용자 인증 확인 (X-USER-ID)
            alt 인증 실패(사용자 미존재, 헤더 미존재)
            US-->>User: 401 UNAUTHORIZED
            else 인증 성공
            US-->>LC: 사용자 정보 반환
            LC->>LS: 내 좋아요 목록 조회(userId) 
            alt 좋아요 목록이 비어있는경우
            LS-->>LC: 200 OK + 빈 좋아요 목록
            else 좋아요 목록이 존재하는 경우
            LS-->>LC: 좋아요 목록 반환
            LC->>PS : 좋아요한 상품 별 상품정보 요청(좋아요한 상품 목록)
            PS-->>LC : 상품 목록 반환
                alt 조회 실패 ( 사유 불문 )
                    LS -->> LC: 500 Internal Server Error
                else 조회 성공
                    LS -->> LC: 좋아요 목록 반환
            end
        end
    end
```