classDiagram
class Cart{
- List<CartItem> items
+ addItem(Product, quantity)

}
class CartItem{
- Product product
- int quantity

        + increaseQuantity()
        + deleteItem(Product)
  }
  class Product {
    + id
    + status
      }

  Cart --> "N" CartItem : 소유
  CartItem --> Product : 참조
  [![](https://mermaid.ink/img/pako:eNpVUUtOwzAQvYo1q1ZNqzppfhbqpmyQEIIt8saKTRvROMV2JEqULSuOwAJu0CWH6iGwkzQEL-x5M--9Gds1ZCUXQCDbM62vc7ZVrKCyRWjDlKmpnKPbXJsrh26MKNYot7umcoYY5y4zuVclrzLjoZeKSZOb45RKKpuxj-O1Xj0XHbrTpXJpBqUTon7NbCVTgmnx0Fcn03GVi70wYjxBX266o-t-aVj_M-ZjpA0zlR60XeCmRvP5GlG4ozBcAhF0fv84f379sdq0Y15aWcrp5_x9Ag-2KudAntheCw8KoQrmMLTDUDA7UQgKxIacqWcK9tWs6MDkY1kWQIyqrEyV1XZ3AdWBMyP6nxqclZBcqE1ZSQMkCCLcmgCp4RUIxukiwAFepn7oRyuc-h4cLc1mYz_FSZxGKz9dNR68tV2XiySIojQOcZL4YRjg5heVkrDR?type=png)](https://mermaid.live/edit#pako:eNpVUUtOwzAQvYo1q1ZNqzppfhbqpmyQEIIt8saKTRvROMV2JEqULSuOwAJu0CWH6iGwkzQEL-x5M--9Gds1ZCUXQCDbM62vc7ZVrKCyRWjDlKmpnKPbXJsrh26MKNYot7umcoYY5y4zuVclrzLjoZeKSZOb45RKKpuxj-O1Xj0XHbrTpXJpBqUTon7NbCVTgmnx0Fcn03GVi70wYjxBX266o-t-aVj_M-ZjpA0zlR60XeCmRvP5GlG4ozBcAhF0fv84f379sdq0Y15aWcrp5_x9Ag-2KudAntheCw8KoQrmMLTDUDA7UQgKxIacqWcK9tWs6MDkY1kWQIyqrEyV1XZ3AdWBMyP6nxqclZBcqE1ZSQMkCCLcmgCp4RUIxukiwAFepn7oRyuc-h4cLc1mYz_FSZxGKz9dNR68tV2XiySIojQOcZL4YRjg5heVkrDR)
- 
