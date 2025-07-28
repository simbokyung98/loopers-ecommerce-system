### ERD

```mermaid 
  erDiagram
    Brand{
    bigint id pk
    varchar name
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    Product{
    bigint id pk
    varchar name
    bigint brand_id fk
    bigint stock
    bigint price
    varchar status
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    Cart{
    bigint id pk
    bigint product_id fk, uk "UNIQUE (user_id, product_id)"
    bigint user_id fk, uk "UNIQUE (user_id, product_id)"
    bigint quantity
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    
    Order{
    bigint id pk
    bigint user_id fk
    datetime order_at
    bigint amount
    varchar address
    varchar phone_number
    varchar name
    varchar status
    bigint like_count
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    OrderItem{
    bigint id pk
    bigint order_id fk, uk "UNIQUE (order_id, product_id)"
    bigint product_id fk, uk "UNIQUE (order_id, product_id)"
    varchar name
    bigint price
    bigint quantity
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    User{
    bigint id pk
    varchar name
    varchar gender
    datetime birth
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    Point{
    bigint user_id pk
    bigint amount
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    PointHistory{
    bigint id pk
    bigint user_id fk
    bigint amount
    varchar division_code
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    
    Like{
    bigint id pk
    bigint user_id fk, uk "UNIQUE (user_id, product_id)"
    bigint product_id fk, uk "UNIQUE (user_id, product_id)"
    datetime create_at
    datetime update_at
    datetime delete_at
    }
    
    User       ||--o{ Cart          : adds
    User       ||--o{ Like          : likes
    User       ||--o{ Order         : places
    User       ||--|| Point         : owns
    User       ||--o{ PointHistory  : earns_or_uses
    
    Cart       }o--|| Product       : includes
    Product    }o--|| Brand         : made
    
    Order      ||--o{ OrderItem     : includes
    OrderItem  }o--|| Product       : refers
    
    Like       }o--|| Product       : on
    
```

### 코드값 관리(예상)
##### - Order 의 status  : [paid, preparing, shipped, delivered, cancelled,refunded ]
##### - PointHistory 의 division_code :[pay, add]
##### - Product 의 status : [available, outOfStock, discontinued, hidden]