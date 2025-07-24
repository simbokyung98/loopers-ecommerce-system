### 좋아요 등록/취소
```mermaid 
    classDiagram
    class Prouduct{
    Long id
    }
    class User{
    Long id
    }
    class Like{
    Prouduct Prouduct
    User user
    }
    
        Like --> Prouduct : 참조
        Like --> User : 참조
```


### 내가 좋아요 한 상품 목록 조회
```mermaid 
    classDiagram
    class Like{
    List<LikeItem> items
    }
    class Prouduct{
    Long id
    Long name
    Brand Brand
    String status
    }
    class LikeItem{
    Prouduct Prouduct
    User user
    }
    class User {
    Long id
    }

    Like --> "N" LikeItem :소유
    LikeItem --> Prouduct : 참조
    LikeItem --> User : 참조
```