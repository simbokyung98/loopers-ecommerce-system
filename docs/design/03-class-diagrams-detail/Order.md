### 

    classDiagram
    class Order{
    - int id
      - User user
      - List orderitems
      - string address
      - string phoneNumber
      - point point
    
           + addItem(orderItem)
          }
          class OrderItem{
           - int id
           - ProductItem
           - int quantity
          }
          class Product{
           int id
           int name
           int price
           int stock
          }
          class User {
              Long id
              Long name
          }
          class Point{
              - int id
              - int userId
              - int amount
    
              + pay(amount)
          }
        
          Order --> "N" OrderItem :소유
          OrderItem --> Product : 참조
          Order --> User : 참조
          Order --> Point : 참조

[![](https://mermaid.ink/img/pako:eNp1k79OwzAQxl_FuglEWyUN-eeBBZZKCFhYUBYTm9aisYPtSJSqKxOPwABvwMhD9SGwnTakLXiwfN_dfT7_oiyhlJQBhnJOtL7gZKpIVQhkl1fQtaJMLVsFDREXBnHahbeaKdTYrVMuuTZIuiZuWKU7XRvFxRQRShXTB3I9k4JdNdV9z6mW7rJ2L8RGPnEOE-t85O9wp-M2tTqY2iX_nfxGSdqUxtXslDw1RBhuFn-Yblq2ljuGLhCkYr2wVrzsx9rI8vEPW89wa2rXpbRAOuOt8Gu-O5PD02vee2enuG80OVBJJRthOrieb00WR62-C7bdPVg0HJ6hAq4K-AWN8Pr1bf3-0SvzsivdgEMYrb--159f-1aewH9J_8K9bCFgAFPFKeAHMtdsABVTFXExeBgFmBmzwADbIyXqsYBCrGxTTcSdlBVgoxrbpmQznW2DpqbEsM0_0DkrJuwo5w4I4Gx8mnsTwEt4BhyPw1GUhFk8DtIoSrJ4AAvASTTK4yDLk9M8i6M0y1cDePG3BqMsDZM0TaI0DIJxmKerH2yGFJk?type=png)](https://mermaid.live/edit#pako:eNp1k79OwzAQxl_FuglEWyUN-eeBBZZKCFhYUBYTm9aisYPtSJSqKxOPwABvwMhD9SGwnTakLXiwfN_dfT7_oiyhlJQBhnJOtL7gZKpIVQhkl1fQtaJMLVsFDREXBnHahbeaKdTYrVMuuTZIuiZuWKU7XRvFxRQRShXTB3I9k4JdNdV9z6mW7rJ2L8RGPnEOE-t85O9wp-M2tTqY2iX_nfxGSdqUxtXslDw1RBhuFn-Yblq2ljuGLhCkYr2wVrzsx9rI8vEPW89wa2rXpbRAOuOt8Gu-O5PD02vee2enuG80OVBJJRthOrieb00WR62-C7bdPVg0HJ6hAq4K-AWN8Pr1bf3-0SvzsivdgEMYrb--159f-1aewH9J_8K9bCFgAFPFKeAHMtdsABVTFXExeBgFmBmzwADbIyXqsYBCrGxTTcSdlBVgoxrbpmQznW2DpqbEsM0_0DkrJuwo5w4I4Gx8mnsTwEt4BhyPw1GUhFk8DtIoSrJ4AAvASTTK4yDLk9M8i6M0y1cDePG3BqMsDZM0TaI0DIJxmKerH2yGFJk)