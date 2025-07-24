### ERD
[![](https://mermaid.ink/img/pako:eNqtVkuPmzAQ_ivIp1YiUYGQEI7dVupKfR5yWSEhBzuJlWDTsdk2TfLfa_OKtYFsd1NOeF7-ZuabgQPKBKEoRhQ-MLwGnCf8PWBODglfsjXjymHEKbYJf8SQbTA4HOc04QQrqlhOnQyofk2xsmRlQS5khO5oIzsl_DsIUmbqmUsa3dLgSbXFatvJpBLZ-VQAy-jZWyqsStlpd2xL00yUXN2M-w7DJegORJVTDdR1yq2ToMXX-x-Lj86bUlLQCtcyepugzrVRv9jvZ4m5Ympv56URtmgT_g0IhSHA51stf2E8qgCNFc7ryrXFxYQAlfIsKDaC05SX-ZLC0w4-7ciN5a_SuVc0H0qpRt9XyVYzVMpr3Rv07aVrQ8YrLXpV7gvZ08n-aq8pJ6YXXaQlA7W5fWiFvvlwQR-r_C1X_sdFn5iecdj_C3kHmErYI5NM8NSsuJtBJfyz3iPPw3nZBL9madyciKGSUz_H42gkDo7Za073xGbIZZ-dKYFtZ1Zrr2E1qZZhscNZj-Xx6FTNtizFL94b0SaFsaMYuEwFpLpM0mRl5XASdei6bl1oxrNdSYy1pWlsq4-eBSPHhjPNyrlMzSyhi6iWZgAB0BUF2ZLpOlrBjR1y0RoYQfEK7yR1UU4hx-aMNBUdzRa1oXr2UaxfCYatIchJOxWYPwiRo1hBqd1AlOtNe6g50nzvu8hQbY07M0Mo9nwvjKooKD6g3ygeTebjyAungTd7N_Hm0WQ6ddFey_0oHIdBNA8jPwimEz84uehPdbM_9vxJMAvDyJtNvCAIXEQJ0_37Uv9yVH8ep7_X3wdJ?type=png)](https://mermaid.live/edit#pako:eNqtVkuPmzAQ_ivIp1YiUYGQEI7dVupKfR5yWSEhBzuJlWDTsdk2TfLfa_OKtYFsd1NOeF7-ZuabgQPKBKEoRhQ-MLwGnCf8PWBODglfsjXjymHEKbYJf8SQbTA4HOc04QQrqlhOnQyofk2xsmRlQS5khO5oIzsl_DsIUmbqmUsa3dLgSbXFatvJpBLZ-VQAy-jZWyqsStlpd2xL00yUXN2M-w7DJegORJVTDdR1yq2ToMXX-x-Lj86bUlLQCtcyepugzrVRv9jvZ4m5Ympv56URtmgT_g0IhSHA51stf2E8qgCNFc7ryrXFxYQAlfIsKDaC05SX-ZLC0w4-7ciN5a_SuVc0H0qpRt9XyVYzVMpr3Rv07aVrQ8YrLXpV7gvZ08n-aq8pJ6YXXaQlA7W5fWiFvvlwQR-r_C1X_sdFn5iecdj_C3kHmErYI5NM8NSsuJtBJfyz3iPPw3nZBL9madyciKGSUz_H42gkDo7Za073xGbIZZ-dKYFtZ1Zrr2E1qZZhscNZj-Xx6FTNtizFL94b0SaFsaMYuEwFpLpM0mRl5XASdei6bl1oxrNdSYy1pWlsq4-eBSPHhjPNyrlMzSyhi6iWZgAB0BUF2ZLpOlrBjR1y0RoYQfEK7yR1UU4hx-aMNBUdzRa1oXr2UaxfCYatIchJOxWYPwiRo1hBqd1AlOtNe6g50nzvu8hQbY07M0Mo9nwvjKooKD6g3ygeTebjyAungTd7N_Hm0WQ6ddFey_0oHIdBNA8jPwimEz84uehPdbM_9vxJMAvDyJtNvCAIXEQJ0_37Uv9yVH8ep7_X3wdJ)
### 코드값 관리(예상)
##### - Order 의 status  : [paid, preparing, shipped, delivered, cancelled,refunded ]
##### - PointHistory 의 division_code :[pay, add]
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
    datetime cart_at
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
    
