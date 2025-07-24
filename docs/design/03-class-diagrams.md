### 전체 클래스 다이어그램
[![](https://mermaid.ink/img/pako:eNqlVU1v1DAQ_SuWT63IVptkP7oRqlTaA0ilVEK9oFzctclabezFcSpKVakFVFWiSIgzVHDgxqGHHvY30fAf8EeczUZbWCCHxJ558-Z5ZpIcwyHHBEZweICybJOiRKA0ZmYHdjMiwHHMgLpaYIuzBFDstk-loMrAUEoapj0q5KhhSwjDRFjjPZDRhO2Ol3SsZ-FeiViO2UnMnIAdwXE-lH-jgTIJxoIOZwyZ5MN9Z3ggEMNgT9-dnqEgSJJSj4n2bIxncbOqLMHCmmboZ5meCPybCpvy55krm_LTTN43MY8kSdcAd8uskRxhLEjWtI5HnJHtPN2bMu5wUy99b4AziWSeuRMccoo1q062VKWdcxhtXvxArr9j-6y37EWOmKTyqFFDHe05vFehZpVsIHH3zCycs_XnnBaGyQFpwpa1mpqkLbpP_rMu82uw7Jx3qKi_TabZC4vQYJTynE3zj5BIyJI1egZcpVfD0XQ0Uz9U48vF0b8rqIYT00OaUc421LfLuTZVacoK4fX5JXP66uFeCVmvimVfyhj6MQSt1ppabavVdLojUJxfFp--lEhj0zjXNOW_nhRfrx2T9pmjzXPYnkw9jkT77HfGpjt_HzMz1jW223c3xedTULz-fnt1WXNPhSjEj8kNKN6c_fx4ETMzhHWCi7e33yYVQeWuEVyequCKwKqtH8hUorj6UPpci-tJDAX0YCIohtFzdJARD6ZEpEjvoZmGGMoRUR9MGKklRmI_hqobKmiM2DPOUxhJkaswwfNk5Db5GKvGlf-silmY_8iG7jSM_G7QDwwLjI7hSxi1_F5_ZdDpr_p-uxe2O2E_9OCRsgdBuOJ3-v2w2_aDQdDpnnjwlcnsr4SDgR-uDrrdsBf0eh0VQTBV53xc_jr14-QXbVt1qA?type=png)](https://mermaid.live/edit#pako:eNqlVU1v1DAQ_SuWT63IVptkP7oRqlTaA0ilVEK9oFzctclabezFcSpKVakFVFWiSIgzVHDgxqGHHvY30fAf8EeczUZbWCCHxJ558-Z5ZpIcwyHHBEZweICybJOiRKA0ZmYHdjMiwHHMgLpaYIuzBFDstk-loMrAUEoapj0q5KhhSwjDRFjjPZDRhO2Ol3SsZ-FeiViO2UnMnIAdwXE-lH-jgTIJxoIOZwyZ5MN9Z3ggEMNgT9-dnqEgSJJSj4n2bIxncbOqLMHCmmboZ5meCPybCpvy55krm_LTTN43MY8kSdcAd8uskRxhLEjWtI5HnJHtPN2bMu5wUy99b4AziWSeuRMccoo1q062VKWdcxhtXvxArr9j-6y37EWOmKTyqFFDHe05vFehZpVsIHH3zCycs_XnnBaGyQFpwpa1mpqkLbpP_rMu82uw7Jx3qKi_TabZC4vQYJTynE3zj5BIyJI1egZcpVfD0XQ0Uz9U48vF0b8rqIYT00OaUc421LfLuTZVacoK4fX5JXP66uFeCVmvimVfyhj6MQSt1ppabavVdLojUJxfFp--lEhj0zjXNOW_nhRfrx2T9pmjzXPYnkw9jkT77HfGpjt_HzMz1jW223c3xedTULz-fnt1WXNPhSjEj8kNKN6c_fx4ETMzhHWCi7e33yYVQeWuEVyequCKwKqtH8hUorj6UPpci-tJDAX0YCIohtFzdJARD6ZEpEjvoZmGGMoRUR9MGKklRmI_hqobKmiM2DPOUxhJkaswwfNk5Db5GKvGlf-silmY_8iG7jSM_G7QDwwLjI7hSxi1_F5_ZdDpr_p-uxe2O2E_9OCRsgdBuOJ3-v2w2_aDQdDpnnjwlcnsr4SDgR-uDrrdsBf0eh0VQTBV53xc_jr14-QXbVt1qA)


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