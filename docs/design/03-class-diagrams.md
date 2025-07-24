### 전체 클래스 다이어그램

```mermaid 

    classDiagram
    class User {
    - Long id
      - String name
      - String birth
      - String gender
      + signUp(name, birth, gender)
      }
    
    class Product {
    - Long id
      - String name
      - int price
      - int stock
      - Brand brand
      + create(name, price, stock, brand)
      }
    
    class Brand {
    - Long id
      - String name
      + create(name)
      }
    
    class Order {
    - Long id
      - User user
      - List<OrderItem> orderItems
      - String address
      - String phoneNumber
      - Point point
      - String status
      + void addItem(orderItem)
      }
    
    class OrderItem {
    - Long id
      - User user
      - Product product
      - int quantity
      + create(user, product, quantity)
      }
    
    class Cart {
    - Long id
      - Product product
      - int quantity
      - create(user, product, quantity)
      - delete(user, product)
    
    }
    
    class Like {
    - Long id
      - User user
      - Product product
      + create(user, product)
      + delete(user, product)
      }
    
    class Point {
    - Long id
      - User user
      - int amount
      + charge(amount, user)
      + add(amount, user)
      }
    
    class PointHistory {
    - Long id
      - User user
      - int amount
      - String divisionCode
      - Date createdAt
      + create(user, amount, divisionCode, createAt)
      }

    
    Order "1" --> "N" OrderItem : 소유
    OrderItem --> Product : 참조
    Order --> User : 참조
    Order --> Point : 참조
    Product --> Brand : 소속
    Cart --> User : 담은 사람
    Cart --> Product : 담긴 상품
    Like --> User : 누른 사람
    Like --> Product : 대상 상품
    Point --> User : 소유자
    PointHistory --> User : 대상

```