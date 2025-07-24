### 주문 요청
    sequenceDiagram
    participant User
    participant OC as OrderController
    participant US as UserService
    participant PS as PointService
    participant PTS as PoductService
    participant OS as OrderService
  
    
    User->>OC: 주문요청(order, productList)   
    OC->>US : 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패(사용자 미존재, 헤더 미존재)
        US-->>User: 401 UNAUTHORIZED
    else 인증 성공
        US-->>OC: 사용자 정보 반환
        OC->>PS: 포인트사용 가능 여부 조회
        alt 포인트 부족
            PS-->>OC : 400 BAD_REQUEST
        else 포인트 충분
            PS-->>OC : 포인트 정보
            loop 재고 확인
            OC->> PTS: 상품 재고 확인 요청(productId)
            alt 상품이 없음
                PTS -->> OC: 404 Not Found
            else 판매중이 아님 or 재고가 충분하지 않음
                PTS -->> OC: 409 Conflict
            else 모든 재고 존재 확인
                PTS -->> OC: 상품 정보 반환
            end
            end
                OC ->> PS : 포인트 차감(userId, amount)
                OC ->> PTS : 재고차감(상품정보)
                OC ->> OS : 주문정보 등록(order)
                OS -->> OC : 주문정보 반환
            alt 저장 실패 ( 사유 불문 )
                OS -->> OC: 500 Internal Server Error
            else 저장 성공
                OS -->> OC: 주문요청 반영
            end
        end 
    end

[![](https://mermaid.ink/img/pako:eNqNVW1r2lAU_iuH-0lBS3yp1nwodOqYMKqrCqMII5i0FTRx12RsK4V2K6O0jnVrpbbMosyu7XDMrl3xw36RufkPu7mJplYty4eQe_M855znvNy7jvKKKCEeAX0q0ktNkvNSrCCsYqGUk83NsoDVQr5QFmQVshUJj-8moyBUIIlFCUcVWcVKsTgJlk2bMNNEWsKvCnlpHJJikJRSkNXpmIwNErX8dFQyPYzpDsbCWW8zEO_8fDLKA_n2V-_0yMkh-f3TpZgcD5Qxc_C0UFHdQ04yShlUB6W865CTS3K6D6TRI98bYBzX6Be4nnuz6fiSNxFzWxShqA4gZK9tVC9cDlX_1SOtG3La8YBx1NY_Hjo7NpsFmvaaXmm4PAQ5H2QXF7KZJ8mlxHI8ZqGkYkUaOtm-6l__uc9mKp2QmzX9-gb0bt04rjtQpi6V5sH41KHWjN2eRYF-d1PfPQNy1NFvN4G0usZJ1aGZCocMoAjSunL-mk_KjgFMARw8Woi9WIo_y8bTGQfHNDhmyO2lfrs91cwdINMyCiwqShloEvvXTbsuo_-ZULORaFLebxlfdkbBYHeC3QIJ0T1KZyVlPNK4oVn5QBrVUQSLlvapGS-YuQ9yQVhUVHisaLI4irWEV_f18zZpf2YWa9v6Xh0UbIdF02_nw6jVyTld1Hb_x2UE6DiuFAt5dYJH_ceFftAcCLeabmKyxgwPUja5iZiDMY33N6wqACtDerSe3Yt-d9-l0XZPiB4QSjRjqns6O8OmkamwqXZpWHjTiUnGswbfVnJwpbca1vxP4g1TMEackALWI81Ncnpmjz242AB-bdIJ2aFceNAFD7N0UBKyKmFZKIJ5hEkY4hgreEItB47ujf4ks3ePOjNwUt96oFR0YZ98bBt50CouiIhfEahbDypJuCSYa7TOTlekrkklKYd4-ilKK4JWVHMoJ29QHj2UlxWlhHgVa5SJFW11bbDQyqKgDi6doXFMXZoXCi0_4gORgJ8ZQfw6eo14X8Q_44tEgqGQP-ILBf2BOQ96g3j_3AwX5sL-UDgwFwmF5vyhDQ96y_z6ZjguwIXCPl8wHIiEZ8Mb_wA5fJ2D?type=png)](https://mermaid.live/edit#pako:eNqNVW1r2lAU_iuH-0lBS3yp1nwodOqYMKqrCqMII5i0FTRx12RsK4V2K6O0jnVrpbbMosyu7XDMrl3xw36RufkPu7mJplYty4eQe_M855znvNy7jvKKKCEeAX0q0ktNkvNSrCCsYqGUk83NsoDVQr5QFmQVshUJj-8moyBUIIlFCUcVWcVKsTgJlk2bMNNEWsKvCnlpHJJikJRSkNXpmIwNErX8dFQyPYzpDsbCWW8zEO_8fDLKA_n2V-_0yMkh-f3TpZgcD5Qxc_C0UFHdQ04yShlUB6W865CTS3K6D6TRI98bYBzX6Be4nnuz6fiSNxFzWxShqA4gZK9tVC9cDlX_1SOtG3La8YBx1NY_Hjo7NpsFmvaaXmm4PAQ5H2QXF7KZJ8mlxHI8ZqGkYkUaOtm-6l__uc9mKp2QmzX9-gb0bt04rjtQpi6V5sH41KHWjN2eRYF-d1PfPQNy1NFvN4G0usZJ1aGZCocMoAjSunL-mk_KjgFMARw8Woi9WIo_y8bTGQfHNDhmyO2lfrs91cwdINMyCiwqShloEvvXTbsuo_-ZULORaFLebxlfdkbBYHeC3QIJ0T1KZyVlPNK4oVn5QBrVUQSLlvapGS-YuQ9yQVhUVHisaLI4irWEV_f18zZpf2YWa9v6Xh0UbIdF02_nw6jVyTld1Hb_x2UE6DiuFAt5dYJH_ceFftAcCLeabmKyxgwPUja5iZiDMY33N6wqACtDerSe3Yt-d9-l0XZPiB4QSjRjqns6O8OmkamwqXZpWHjTiUnGswbfVnJwpbca1vxP4g1TMEackALWI81Ncnpmjz242AB-bdIJ2aFceNAFD7N0UBKyKmFZKIJ5hEkY4hgreEItB47ujf4ks3ePOjNwUt96oFR0YZ98bBt50CouiIhfEahbDypJuCSYa7TOTlekrkklKYd4-ilKK4JWVHMoJ29QHj2UlxWlhHgVa5SJFW11bbDQyqKgDi6doXFMXZoXCi0_4gORgJ8ZQfw6eo14X8Q_44tEgqGQP-ILBf2BOQ96g3j_3AwX5sL-UDgwFwmF5vyhDQ96y_z6ZjguwIXCPl8wHIiEZ8Mb_wA5fJ2D)

### 유저의 주문 목록 조회
    sequenceDiagram
    participant User
    participant OC as OrderController
    participant US as UserService
    participant OS as OrderService
    participant OR as OrderRepository
    
    User->>OC: 내 주문 목록 조회 요청
    OC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패 (사용자 미존재, 헤더 미존재)
        US -->> OC: 401 Unauthorized
    else 인증 성공
        US -->> OC: 사용자 정보 반환
        OC ->> OS: 주문 목록 조회 요청(userId)
        OS -->> OC : 주문목록 정보 반환
    end

[![](https://mermaid.ink/img/pako:eNp1kk9r2zAYxr-K0CkDJ9hO4tg69JLu0JMhJjCGLyJWE4P_ZLI81oZcym7rYIeWtYGUhpWxjox1awY57BPN8neYFNdOoakOQnr5Pe_z6M8EDmKPQASBGAl5k5JoQPZ9PKQ4dCNZHGPK_IE_xhED_YTQp1W7C3ACbOoR2o0jRuMg2IX1HYnJFg6hb_0B2dHJqTo9z_QqpkfGceKzmB4VWDFLh_rent1FIDtZAf7lb7Zcg-z7bbaYA764y2engM_O-O8fBS_iC1zEQ4CfLPnsll99Any-5l_nIL88FytQe1XvOy979YP9F4UGB6xE-Ieb_PQbqG212c81X6z41VIB-eeb7OPZtvIg38R0QF36ypwtVQP9CKdsFFP_mHgFRYKEVC7vf_27_7Nb_Sj19Xl2vwLZ3UV-ebGFH05oyxM-fxu1VFzcgfcool2ZgFJZCp8akUjEhgocUt-D6BCL8AoMCQ2x3MOJpFzIRiQkLkRi6ZFDnAbMhW40FTrxuK_jOISI0VQoaZwOR-UmHXuYld-yak6FpfxyacQgaqsdfdMEogl8B5Fm6Q3NslqGoVua0dKbpgKPINLNhtoRqNFpmpZhmLoxVeDxxldrqGpTNTpas21qqtnWp_8Bsf00Sw?type=png)](https://mermaid.live/edit#pako:eNp1kk9r2zAYxr-K0CkDJ9hO4tg69JLu0JMhJjCGLyJWE4P_ZLI81oZcym7rYIeWtYGUhpWxjox1awY57BPN8neYFNdOoakOQnr5Pe_z6M8EDmKPQASBGAl5k5JoQPZ9PKQ4dCNZHGPK_IE_xhED_YTQp1W7C3ACbOoR2o0jRuMg2IX1HYnJFg6hb_0B2dHJqTo9z_QqpkfGceKzmB4VWDFLh_rent1FIDtZAf7lb7Zcg-z7bbaYA764y2engM_O-O8fBS_iC1zEQ4CfLPnsll99Any-5l_nIL88FytQe1XvOy979YP9F4UGB6xE-Ieb_PQbqG212c81X6z41VIB-eeb7OPZtvIg38R0QF36ypwtVQP9CKdsFFP_mHgFRYKEVC7vf_27_7Nb_Sj19Xl2vwLZ3UV-ebGFH05oyxM-fxu1VFzcgfcool2ZgFJZCp8akUjEhgocUt-D6BCL8AoMCQ2x3MOJpFzIRiQkLkRi6ZFDnAbMhW40FTrxuK_jOISI0VQoaZwOR-UmHXuYld-yak6FpfxyacQgaqsdfdMEogl8B5Fm6Q3NslqGoVua0dKbpgKPINLNhtoRqNFpmpZhmLoxVeDxxldrqGpTNTpas21qqtnWp_8Bsf00Sw)

### 단일 주문 상세 조회
     sequenceDiagram
    participant User
    participant OC as OrderController
    participant US as UserService
    participant OS as OrderService
    
    User->>OC: 내 단일주문 조회 요청
    OC ->> US: 사용자 인증 확인 (X-USER-ID)
    alt 인증 실패 (사용자 미존재, 헤더 미존재)
        US -->> OC: 401 Unauthorized
    else 인증 성공
        US -->> OC: 사용자 정보 반환
        OC ->> OS: 주문 정보 조회 요청(userId, orderId)
        alt 존재하지 않는 주문
            OS -->> OC : 404 not found
        else 존재하는 주문
            OS -->> OC : 주문 정보 반환
        end
    end


[![](https://mermaid.ink/img/pako:eNqNUk2L2kAY_isvc7IQJR8aYw57cXvwJFSEUrwMZtSASew4Ke2KUJaedrfQg7Ku4NKlS7sFS7fdLXjYX9RM_kNnEqNr66E5hJmX5_NlRqgdOATZCMQ3JC9D4rfJoYu7FHstXw4HmDK37Q6wz6A5JPTfab0KeAh16hBaDXxGg35_H6zZkDAp0SD0ldsme5QaG6UdTPqX1PzBQb1qQ3R8D9HpDV888E8P0XIF_Oo2np8Bn0_4z28pXMQSaGFrAz9e8vlXfvkB-GLFPy8gvpiKE-Se55uNp8_ytcMnKQf3WQbhp9fx2Q3kttzo-4pf3fPLpQLx-XX0frKdrOlJygbkpa-MWVQ1aPo4ZL2AukfESVGkPyQbl3c_ft_92s9-lPrjNLoTjW9n8cVsC143rMuG6zWkwJ1t5EKxt5qjQCD3WnMeZU3qJgXi6Yx_eQt8ehKdTNZqW1xitkkGslgR_IBBJwh9Z4tLm2WC_6W0G_zvhiRTTw5IQV3qOsjuYGGkII9QD8s7GklUC7Ee8UgL2eLokA4O-6yFWv5Y8MTzehEEHrIZDQWTBmG3l13CgYNZ9ug34lRYygcd-gzZJcMqJiLIHqHXyNYqekGrVIqmqVc0s6gbloLeIFu3CmpZLetm2bAqpmnp5lhBR4mvVlBVQzXLmlGyNNUq6eM_OvxXZQ?type=png)](https://mermaid.live/edit#pako:eNqNUk2L2kAY_isvc7IQJR8aYw57cXvwJFSEUrwMZtSASew4Ke2KUJaedrfQg7Ku4NKlS7sFS7fdLXjYX9RM_kNnEqNr66E5hJmX5_NlRqgdOATZCMQ3JC9D4rfJoYu7FHstXw4HmDK37Q6wz6A5JPTfab0KeAh16hBaDXxGg35_H6zZkDAp0SD0ldsme5QaG6UdTPqX1PzBQb1qQ3R8D9HpDV888E8P0XIF_Oo2np8Bn0_4z28pXMQSaGFrAz9e8vlXfvkB-GLFPy8gvpiKE-Se55uNp8_ytcMnKQf3WQbhp9fx2Q3kttzo-4pf3fPLpQLx-XX0frKdrOlJygbkpa-MWVQ1aPo4ZL2AukfESVGkPyQbl3c_ft_92s9-lPrjNLoTjW9n8cVsC143rMuG6zWkwJ1t5EKxt5qjQCD3WnMeZU3qJgXi6Yx_eQt8ehKdTNZqW1xitkkGslgR_IBBJwh9Z4tLm2WC_6W0G_zvhiRTTw5IQV3qOsjuYGGkII9QD8s7GklUC7Ee8UgL2eLokA4O-6yFWv5Y8MTzehEEHrIZDQWTBmG3l13CgYNZ9ug34lRYygcd-gzZJcMqJiLIHqHXyNYqekGrVIqmqVc0s6gbloLeIFu3CmpZLetm2bAqpmnp5lhBR4mvVlBVQzXLmlGyNNUq6eM_OvxXZQ)