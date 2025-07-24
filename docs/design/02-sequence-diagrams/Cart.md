### 장바구니 조회
    sequenceDiagram
    participant U as User
    participant CC as CartController
    participant US as UserService
    participant CS as CartService
    participant PS as ProductService

    U ->> CC: 내 장바구니 목록 조회 
    CC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패 (사용자 미존재, 헤더 미존재)
        US -->> CC: 401 Unauthorized
    else 인증 성공
        CC ->> CS: 장바구니 목록 요청 (userId)
        alt 항목 없음
            CS -->> CC: 빈 장바구니 반환
        else 항목 존재
            CS -->> CC: 장바구니 목록 반환
            loop 장바구니의 상품별 정보 요청
                CC->>PS: 상품정보 요청(productId)
                PS-->>CC: 상품 정보 반환
            end
        end
    end
    CC -->> U : 장바구니목록 반환
[![](https://mermaid.ink/img/pako:eNp1U1Fr2lAU_iuH--RAJZpo0zz0Jd1D32QhMIYvwdxqICbumoyt4sO6MoQ6GEOpFZQJXWmHZW5a6P5Sc_MfdpMYYzp7ns659_u-852Tmw6q2TpGEgIWbfzWxVYNHxpanWjNqhUctjTiGDWjpVkOqKC1QW1j8v-VLAd3MjuRbcshtmnuQqlKrKBg8s6o4R1CSiz0LKQSQirE1t1agopwKuQODpgbCbzTFdDpD28xeLyfe-c98H7eerMJ0NnCH_chgjPbAV5VJKCnczq-pdOvQCcP9HoC_uWQZZB5nVOVl69yR4cvIo5mOjGEnl_5_RvIJFzv1wOdreh0ngX_4sr7MkhO1vTQpQK52KfAFUC1NNdp2MQ4wXqEwmYbb7qc_X5c3ifstWk5ML1rwPGA_rmDjMvWfKRvdQ2M-8M7BgN68ZlO-slVKLtlyvvbe6K9GPmXo4QQ-ovFwvGeF9tl8qleEKZtt1JgOhkB_fTR_9bzlmdAvw-95Wo9XpoZbYX1qwQ7CRkpdKYVPZbUOuKoKIHT0GjIjBvtsogtfWsHcbFJgi8TvidIT50aGmVRnRg6ko41tsQsamLS1IIadQKVKnIauImrSGKpjo8113SqqGp1GY89_ze23USSQ1zGJLZbb8SF29I1J_53N-KEecNEtl3LQVKxJJRCESR10HtWi1y-uMcJxX2hyIs8L2bRByTxpbxQ5vdLfLlcKPCiUCx3s-gkbMvlRY4XeEEUxD2xJHCc0P0HEY2roQ?type=png)](https://mermaid.live/edit#pako:eNp1U1Fr2lAU_iuH--RAJZpo0zz0Jd1D32QhMIYvwdxqICbumoyt4sO6MoQ6GEOpFZQJXWmHZW5a6P5Sc_MfdpMYYzp7ns659_u-852Tmw6q2TpGEgIWbfzWxVYNHxpanWjNqhUctjTiGDWjpVkOqKC1QW1j8v-VLAd3MjuRbcshtmnuQqlKrKBg8s6o4R1CSiz0LKQSQirE1t1agopwKuQODpgbCbzTFdDpD28xeLyfe-c98H7eerMJ0NnCH_chgjPbAV5VJKCnczq-pdOvQCcP9HoC_uWQZZB5nVOVl69yR4cvIo5mOjGEnl_5_RvIJFzv1wOdreh0ngX_4sr7MkhO1vTQpQK52KfAFUC1NNdp2MQ4wXqEwmYbb7qc_X5c3ifstWk5ML1rwPGA_rmDjMvWfKRvdQ2M-8M7BgN68ZlO-slVKLtlyvvbe6K9GPmXo4QQ-ovFwvGeF9tl8qleEKZtt1JgOhkB_fTR_9bzlmdAvw-95Wo9XpoZbYX1qwQ7CRkpdKYVPZbUOuKoKIHT0GjIjBvtsogtfWsHcbFJgi8TvidIT50aGmVRnRg6ko41tsQsamLS1IIadQKVKnIauImrSGKpjo8113SqqGp1GY89_ze23USSQ1zGJLZbb8SF29I1J_53N-KEecNEtl3LQVKxJJRCESR10HtWi1y-uMcJxX2hyIs8L2bRByTxpbxQ5vdLfLlcKPCiUCx3s-gkbMvlRY4XeEEUxD2xJHCc0P0HEY2roQ)


### 장바구니 넣기(수량변경 포함)
    sequenceDiagram
    participant U as User
    participant CC as CartController
    participant US as UserService
    participant PS as ProductService
    participant CS as CartService
    participant CR as CartRepository

    U ->> CC: 장바구니 담기 요청 (productId, quantity)
    CC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패 (사용자 미존재, 헤더 미존재)
        US ->> CC: 401 Unauthorized
    else 인증 성공
        US -->> CC: 사용자 정보 반환
        CC ->> PS: 상품 상태 조회 (productId)
        alt 상품이 없음
            PS -->> CC: 404 Not Found
        else 판매중이 아님
            PS -->> CC: 409 Conflict
        else
            PS -->> CC: 상품 정보 반환
            CC ->> CS: 장바구니 처리 요청 (userId, productId, quantity)
            CS ->> CR: 장바구니 항목 조회 (userId, productId)
            alt 항목 존재
                CS ->> CR: 수량 증가 후 저장
            else 항목 없음
                CS ->> CR: 새 항목 추가 후 저장
            end
            alt 저장 실패 ( 사유 불문 )
                CR -->> CC: 500 Internal Server Error
            else 저장 성공
                CR -->> CC: 장바구니 반영 결과
            end
        end
    end
[![](https://mermaid.ink/img/pako:eNp9VGFP2kAY_itv7pMmaCotrPSDX9AlflkIhGRZ-NLQU5tAi8d1mRoTnWZhgSVugYhOnCbMzQUzpmj4sF9Er_9h10JbYGi_9O7e93ne97l77nZR3tQwUlAZb1nYyOMVXd0gajFnlFRC9bxeUg0KWVDLkC1jMrmcTLrrSb6SNA1KzEJhOiOb8ZEZTN7qeTwZTnnhFDE1K09nZiQzfonZ4bQfTuOSWdapSbZzRs4A_mVhYXmZ96gAu_hud-uDh45drYBd7Q36XWBndXZ3C3OlYfE1LQJbFqfU6fb8EM_VuQTZDCd432FnN-ziGFirz65b4Jw2-AjmXi9kM6vphbWVEUYtUD-FVdtO7SfMhVj7d59d9dhFJwLOSdv-VA9XRnCv7UzQtyQsQdZQLbppEn0Ha8MkXCjjoMjRn8H9wyQ4UB02fdmw73tgd5vOaTNMHglMuQIPD5wvFe93eA7squuc1cb2Zqw9T6GXzVo9YCcfWKsWRt0vNdaDJEjwyqTw0rQMLUzzJDi1Y_tHm7U_e0SNI7vafI4oAdxk6wU9Tyd5nsb4omarH9uBZGbKI-yuaV93Ao9Y3L-uQZ72SkA4Orz0FKHTuLV_3QQb-x_hFI27yQHE9cdkeLpSpWl_-wjcD4PuPjhfj7jkfV5-EjTc8xHpjGObJj0Mu36sP0s8frKBRbzE4BJ4Zjy_BPuxYnf6MD-jdDo8uJggwJpBMTHUArj3HhNYJcQkMxT5haYuwizayWeg22TNAxjcdQf3f58RFEz4AEXQBtE1pKyrvHQEFTEpqu4c7bo5OUQ3cRHnkMKHGl5XrQLNoZyxx3H8tXpjmkWkUGJxJDGtjU1_YpU0lfoPb0BOeEFMkvziUKQk5HjUI0HKLnqHlKgsLEZfCFI0IUVFWRTlCNpGihhblOJiIibG40tLoixF43sRtOOVFRZlQZRESZbkF3JMEgRp7x9JVD-Z?type=png)](https://mermaid.live/edit#pako:eNp9VGFP2kAY_itv7pMmaCotrPSDX9AlflkIhGRZ-NLQU5tAi8d1mRoTnWZhgSVugYhOnCbMzQUzpmj4sF9Er_9h10JbYGi_9O7e93ne97l77nZR3tQwUlAZb1nYyOMVXd0gajFnlFRC9bxeUg0KWVDLkC1jMrmcTLrrSb6SNA1KzEJhOiOb8ZEZTN7qeTwZTnnhFDE1K09nZiQzfonZ4bQfTuOSWdapSbZzRs4A_mVhYXmZ96gAu_hud-uDh45drYBd7Q36XWBndXZ3C3OlYfE1LQJbFqfU6fb8EM_VuQTZDCd432FnN-ziGFirz65b4Jw2-AjmXi9kM6vphbWVEUYtUD-FVdtO7SfMhVj7d59d9dhFJwLOSdv-VA9XRnCv7UzQtyQsQdZQLbppEn0Ha8MkXCjjoMjRn8H9wyQ4UB02fdmw73tgd5vOaTNMHglMuQIPD5wvFe93eA7squuc1cb2Zqw9T6GXzVo9YCcfWKsWRt0vNdaDJEjwyqTw0rQMLUzzJDi1Y_tHm7U_e0SNI7vafI4oAdxk6wU9Tyd5nsb4omarH9uBZGbKI-yuaV93Ao9Y3L-uQZ72SkA4Orz0FKHTuLV_3QQb-x_hFI27yQHE9cdkeLpSpWl_-wjcD4PuPjhfj7jkfV5-EjTc8xHpjGObJj0Mu36sP0s8frKBRbzE4BJ4Zjy_BPuxYnf6MD-jdDo8uJggwJpBMTHUArj3HhNYJcQkMxT5haYuwizayWeg22TNAxjcdQf3f58RFEz4AEXQBtE1pKyrvHQEFTEpqu4c7bo5OUQ3cRHnkMKHGl5XrQLNoZyxx3H8tXpjmkWkUGJxJDGtjU1_YpU0lfoPb0BOeEFMkvziUKQk5HjUI0HKLnqHlKgsLEZfCFI0IUVFWRTlCNpGihhblOJiIibG40tLoixF43sRtOOVFRZlQZRESZbkF3JMEgRp7x9JVD-Z)

### 장바구니 삭제
    sequenceDiagram
    participant U as User
    participant CC as CartController
    participant US as UserService
    participant CS as CartService
    participant CR as CartRepository

        U ->> CC: 장바구니 삭제 요청 (productId)
        CC ->> US: 사용자 인증 확인 (X-USER-ID)
        alt 인증 실패 (사용자 미존재, 헤더 미존재)
            US ->> CC: 401 Unauthorized
        else 인증 성공
            US -->> CC: 사용자 정보 반환
            CC ->> CS: 장바구니 처리 요청 (userId, productId)
            CS ->> CR: 장바구니 항목 조회 (userId, productId)
            alt 항목 없음
                CS ->> CC: 404 not found
            else 항목 있음
                CS ->> CR: 장바구니 항목 삭제(cartId)
            end
            alt 저장 실패 ( 사유 불문 )
                CR -->> CC: 500 Internal Server Error
            else 저장 성공
                CR -->> CC: 장바구니 반영 결과
        
            end
        end

[![](https://mermaid.ink/img/pako:eNqFVE1rGkEY_isvczKgsrqr2ewhF5ODV5eFUrws7pgs6I4dZ0uTILShFCEWelBiJBEDaWiKoTYmxUN_kTv-h85usq4ak-5hmZn3eZ73c-YIlYiFkYYAoI7fudgp4R3b3KNmteiIM6iZlNklu2Y6DAww62DUMX1uyuV8W06c5IjDKKlU1qEMPVTQMX1vl_AaIT0UehlSCCEFXCN1mxF68Ih6_PufAYntbRGVBrz_3Ru1pw9D76QJ_PiWD86B99r87hZiNUost8Ty1kbEFJn4VEMX1OMh793w_jfgFxN-fQGzs45YQexNwtB3C4n8zgLPrLAQxk-uZq0fEIv43q8Jv7zn_WEcZqdX3td2dLIgEQSuzyNXpBQYjumyfULtQ2xFQFyp47mzz7-n44fnIvP8oyQGHW98D96oOzvrLhOeks7pq_W663rXw3m9XNG5vBWHdXULZJ6CL6zIzDq33s8b4JejWa_1fxm_lCHl9Au_aC2bFz0FZVLAIQzKxHWsZWRQp1Cp33xV6aWYg4mJlcS0PYsTr_oLZmDwUejMZyCo__kAvD9NbziBjTUBFKJmZSQJ8g7D1DEr4F8ATGGXUkLX5BU6WtP_VdmlxET_efcTTO9G0_HfiPdKamKD4miP2hbSyqbwHUdVTKumv0dHPq6I2D6u4iLSxNLCZdOtsCIqOg3BE3f2LSFVpDHqCiYl7t5-uHFrlsnCB2cuToVDTHOinwxpW2pKDUSQdoQ-IC2tSsn0pqSkt5S0rMqyMB4gTc4klay8lZGz2VRKVpV0thFHh4FbKalKsiIrqqJuqhlFkpTGP1_ozis?type=png)](https://mermaid.live/edit#pako:eNqFVE1rGkEY_isvczKgsrqr2ewhF5ODV5eFUrws7pgs6I4dZ0uTILShFCEWelBiJBEDaWiKoTYmxUN_kTv-h85usq4ak-5hmZn3eZ73c-YIlYiFkYYAoI7fudgp4R3b3KNmteiIM6iZlNklu2Y6DAww62DUMX1uyuV8W06c5IjDKKlU1qEMPVTQMX1vl_AaIT0UehlSCCEFXCN1mxF68Ih6_PufAYntbRGVBrz_3Ru1pw9D76QJ_PiWD86B99r87hZiNUost8Ty1kbEFJn4VEMX1OMh793w_jfgFxN-fQGzs45YQexNwtB3C4n8zgLPrLAQxk-uZq0fEIv43q8Jv7zn_WEcZqdX3td2dLIgEQSuzyNXpBQYjumyfULtQ2xFQFyp47mzz7-n44fnIvP8oyQGHW98D96oOzvrLhOeks7pq_W663rXw3m9XNG5vBWHdXULZJ6CL6zIzDq33s8b4JejWa_1fxm_lCHl9Au_aC2bFz0FZVLAIQzKxHWsZWRQp1Cp33xV6aWYg4mJlcS0PYsTr_oLZmDwUejMZyCo__kAvD9NbziBjTUBFKJmZSQJ8g7D1DEr4F8ATGGXUkLX5BU6WtP_VdmlxET_efcTTO9G0_HfiPdKamKD4miP2hbSyqbwHUdVTKumv0dHPq6I2D6u4iLSxNLCZdOtsCIqOg3BE3f2LSFVpDHqCiYl7t5-uHFrlsnCB2cuToVDTHOinwxpW2pKDUSQdoQ-IC2tSsn0pqSkt5S0rMqyMB4gTc4klay8lZGz2VRKVpV0thFHh4FbKalKsiIrqqJuqhlFkpTGP1_ozis)