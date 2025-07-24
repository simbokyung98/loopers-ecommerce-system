    classDiagram
    class Brand{
    Long id
    String name
    }
    class Prouduct{
    List Prouducts
    }
    class ProuductItem{
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


[![](https://mermaid.ink/img/pako:eNp9UjFOwzAUvYr157SqkyaNPTAAC1KFkLqhLFbsplEbu7IdCaiyMnEEBrhBRw6VQ-DENKQC1YOV9_5__71v5QC54gIoIHfyHTPmtmSFZlUmBwZdayb5wTNLJQtUcg9WVpcOSlYJTzRj2YNWNa9ze1KWxg6cudB_Z0X1n1sPfr36VP4e1VdW5duzdMYyW5tRhmW5FQYNAcYRutLYeojEz_L6uzunBjSZXKEM7jM4WwPR9vWtff_4K-irncivQVF7_Go_jxca_fQu4agbAih0yYGu2c6IACqhK9Zh6NfIwG6EezCg7pMzvc0gk40T7Zl8VKoCanXtZM6r2JxAvefMip__YJisheRC36haWqARnuF-CNADPAHFmEwj7FgSxmEyxyQM4Nm1OXYREpwuSDIPybwJ4KV3nU3TKEnIIsZpGsZxhJtvZirMyg?type=png)](https://mermaid.live/edit#pako:eNp9UjFOwzAUvYr157SqkyaNPTAAC1KFkLqhLFbsplEbu7IdCaiyMnEEBrhBRw6VQ-DENKQC1YOV9_5__71v5QC54gIoIHfyHTPmtmSFZlUmBwZdayb5wTNLJQtUcg9WVpcOSlYJTzRj2YNWNa9ze1KWxg6cudB_Z0X1n1sPfr36VP4e1VdW5duzdMYyW5tRhmW5FQYNAcYRutLYeojEz_L6uzunBjSZXKEM7jM4WwPR9vWtff_4K-irncivQVF7_Go_jxca_fQu4agbAih0yYGu2c6IACqhK9Zh6NfIwG6EezCg7pMzvc0gk40T7Zl8VKoCanXtZM6r2JxAvefMip__YJisheRC36haWqARnuF-CNADPAHFmEwj7FgSxmEyxyQM4Nm1OXYREpwuSDIPybwJ4KV3nU3TKEnIIsZpGsZxhJtvZirMyg)

