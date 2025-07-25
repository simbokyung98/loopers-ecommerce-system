
```mermaid 
classDiagram
    class Brand{
    Long id
    String name
    }
    class Product{
    List Prouducts
    + addItem(productItem)
    }
    class ProductItem{
    Long id
    Long name
    Brand Brand
    Long Stock
    String status
    List Likes 
    }

    class Like{
    Long ProuductId
    }
    
    Prouduct --> "N" ProuductItem :소유
    ProuductItem --> Brand : 참조
    ProuductItem --> "N" Like : 참조
```
