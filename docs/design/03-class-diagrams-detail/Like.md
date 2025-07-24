### 좋아요 등록/취소
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

[![](https://mermaid.ink/img/pako:eNqNUL1ugzAQfhXrZoIwYIM9dGnGDF26VCwWdglKbEcGS2kRD9M3yNiHykPUQEmiTr3h9N19P2d5gNpKBRzqo-i6bSsaJ3RlUKh5g16c9dLX_bAsp9pZ06BWLovxUfzaKfcv4a49qAfheuQG7tQUiXxot5QFTAlos3m6ezm6Xr6vX5c__BywchBB41oJvHdeRaCV02IaYX5MBf1eaVUBD1AKd6igMmPwnIR5s1avtnCx2a-DP0nRq9-fA_4ujt0kUUYq92y96YFTzOYM4AOcgWeYxoxRgkmRUMKyPIIP4Cku4xIXBNOEhIZZMUbwOV9NYsJwSjHJSZpnJcnHH3eXh7g?type=png)](https://mermaid.live/edit#pako:eNqNUL1ugzAQfhXrZoIwYIM9dGnGDF26VCwWdglKbEcGS2kRD9M3yNiHykPUQEmiTr3h9N19P2d5gNpKBRzqo-i6bSsaJ3RlUKh5g16c9dLX_bAsp9pZ06BWLovxUfzaKfcv4a49qAfheuQG7tQUiXxot5QFTAlos3m6ezm6Xr6vX5c__BywchBB41oJvHdeRaCV02IaYX5MBf1eaVUBD1AKd6igMmPwnIR5s1avtnCx2a-DP0nRq9-fA_4ujt0kUUYq92y96YFTzOYM4AOcgWeYxoxRgkmRUMKyPIIP4Cku4xIXBNOEhIZZMUbwOV9NYsJwSjHJSZpnJcnHH3eXh7g)

### 내가 좋아요 한 상품 목록 조회
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

[![](https://mermaid.ink/img/pako:eNptUUtOwzAQvYo167Sym-ZTC3UBbJAqhITYoGys2qRRG6fyRwKqbFlxBBZwgy45VA6B43xapM5i5Dfz5r2xfYB1xQVQWO-Y1rcFyxUrM4lc-ApaFVtx6AqrQpurFt8ZUS5R4bLuOvX5xIOqLLdrM0xVMkcFPwOSlaKD14pJ3uWu8GhU4RjaMGMvaQ_uvXYbg914OLWetFDItumClG-e6fzbs2cO994KNJksUQb3GYw7INp8fDZf3yeSr7bEcSeKmuNv83O8wPH-pz4EkKuCA31hOy0CKIUqWYvB75iB2Qj3bEDdkTO1zSCTtRvaM_lcVSVQo6wbc8b5ZgB2z5kR_aeOykpILtRNZaUBGkUEexGgB3gFOkvJlCwW8zjEcZKmaRjAG9AknuIEJ2RG0iTEOJ7XAbx7VzLFuKWSMEoJTqNZ_Qc7YLqX?type=png)](https://mermaid.live/edit#pako:eNptUUtOwzAQvYo167Sym-ZTC3UBbJAqhITYoGys2qRRG6fyRwKqbFlxBBZwgy45VA6B43xapM5i5Dfz5r2xfYB1xQVQWO-Y1rcFyxUrM4lc-ApaFVtx6AqrQpurFt8ZUS5R4bLuOvX5xIOqLLdrM0xVMkcFPwOSlaKD14pJ3uWu8GhU4RjaMGMvaQ_uvXYbg914OLWetFDItumClG-e6fzbs2cO994KNJksUQb3GYw7INp8fDZf3yeSr7bEcSeKmuNv83O8wPH-pz4EkKuCA31hOy0CKIUqWYvB75iB2Qj3bEDdkTO1zSCTtRvaM_lcVSVQo6wbc8b5ZgB2z5kR_aeOykpILtRNZaUBGkUEexGgB3gFOkvJlCwW8zjEcZKmaRjAG9AknuIEJ2RG0iTEOJ7XAbx7VzLFuKWSMEoJTqNZ_Qc7YLqX)