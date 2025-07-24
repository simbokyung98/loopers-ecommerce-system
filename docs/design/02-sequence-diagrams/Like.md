### 상품 좋아요 등록/취소
        sequenceDiagram
        participant User
        participant LC as LikeController
        participant US as UserService
        participant PS as ProductService
        participant LS as LikeService
        participant LR as LikeRepository
        
        
            User->>LC: 상품 좋아요 등록/취소 요청(productId)
            LC->>US : 사용자 인증 확인 (X-USER-ID)
            alt 인증 실패(사용자 미존재, 헤더 미존재)
            US-->>LC: 401 UNAUTHORIZED
            else 인증 성공
            US-->>LC: 사용자 정보 반환
            LC->>PS: 상품상태 조회(productId)
            alt 존재하지 않는 상품
            PS-->LC: 404 not found
            else 판매중이지 않는 상품
            PS-->>LC : 409 CONFLICT
            else 
            PS-->>LC: 상품 정보
            LC->>LS : 좋아요 처리 요청(userId, productId)
            LS->>LR : 좋아요 항목 조회(userID, productId)
            alt 좋아요가 존재할 경우
            LS->>LR : 좋아요 삭제
            else 좋아요가 존재하지 않을 경우
            LS->>LR : 좋아요 등록(userId, productId)
            end
                alt 저장 실패 ( 사유 불문 )
                    LR -->> LC: 500 Internal Server Error
                else 저장 성공
                    LR -->> LC: 좋아요 반영 결과
            end
        end
    end

[![](https://mermaid.ink/img/pako:eNqNlW1P2lAUx7_KyX2FCTjKU2lfmCzgMhKiBCRZDG8aelUyaNmlLHPGxKcsRlniNsmQiMGF-bBghg_bfLF9IXr7HXZbKYgWtS-ae9v_-Z37P_fcdgllVRkjEUHvKuE3ZaxkcTQnzROpkFHsF0WJaLlsrigpGqRLmDi_iUdAKkE89xpHVEUjaj4_SplOmUqTlMLkbS6LnWUJS5YgqlzOag8q4yk79cOypC1L4qJaymkqWRwonUbmZa7TMzERj4hA11eNz5tAv23T6gat74L-5Vw_bDyjf07ohwqwJ_TizFW8WXFMHhsGxSMMw7wzzlqb1k_pwQ7QxjU9aoCxV2UjcL3ypFOTSU8seidUymu2lG63jMqJa4DQf17Twyt60HaD8bWlf9wdPLlDSac8PSMBLwfpqefpmZfTydjsZHRYh_Ml3E-3cd69_DWKc8tIs6pfXoHeqRl7NQffiZRdPvO-vg_0sGPUKyOLZTm2TBjVGj1eAVrd0rd2e4xhbcJcz42tACiqBnNqWZEdPBmVHf24RVufaOPqKUwGBZMqQGR66kU8FplxgDrHDZrFKoxDReJWJ_RbiV7U9KO23UNl1nQx2Q0jeyllEpJDBKN6pv84tQtrEaKjCVaB7dhuZ6Vf7SZ0L_7ReufxhHTtjDb3nXrHiWvvIm1sPDnDzfl6tBr47m73DTZX6MH33pkBl9Wv-03Qf2_q7WsYux9kLSQJ5h6CuYlBrxdiioaJIuXB_LhgApOEqOR-6I3zXkKHU-OEv-W0U6O1VVaYTvfy7wPu-hM2QG40T3IyEuckltuNCpgUJHOOlkxNBmkLuIAzSGRDGc9J5byWQRllmcWx7-GsqhaQqJEyiyRqeX7BnpSLsqTZf4E-nLCEmETYydKQ6AsJnAVB4hJ6h0TOHxgP-nie9wtCiPcG-bAbLSLR4_Pz44EQ5w97haA3KPD8shu9t_Jy476wwAe5YCDkDYU5nhOW_wNO7mQN?type=png)](https://mermaid.live/edit#pako:eNqNlW1P2lAUx7_KyX2FCTjKU2lfmCzgMhKiBCRZDG8aelUyaNmlLHPGxKcsRlniNsmQiMGF-bBghg_bfLF9IXr7HXZbKYgWtS-ae9v_-Z37P_fcdgllVRkjEUHvKuE3ZaxkcTQnzROpkFHsF0WJaLlsrigpGqRLmDi_iUdAKkE89xpHVEUjaj4_SplOmUqTlMLkbS6LnWUJS5YgqlzOag8q4yk79cOypC1L4qJaymkqWRwonUbmZa7TMzERj4hA11eNz5tAv23T6gat74L-5Vw_bDyjf07ohwqwJ_TizFW8WXFMHhsGxSMMw7wzzlqb1k_pwQ7QxjU9aoCxV2UjcL3ypFOTSU8seidUymu2lG63jMqJa4DQf17Twyt60HaD8bWlf9wdPLlDSac8PSMBLwfpqefpmZfTydjsZHRYh_Ml3E-3cd69_DWKc8tIs6pfXoHeqRl7NQffiZRdPvO-vg_0sGPUKyOLZTm2TBjVGj1eAVrd0rd2e4xhbcJcz42tACiqBnNqWZEdPBmVHf24RVufaOPqKUwGBZMqQGR66kU8FplxgDrHDZrFKoxDReJWJ_RbiV7U9KO23UNl1nQx2Q0jeyllEpJDBKN6pv84tQtrEaKjCVaB7dhuZ6Vf7SZ0L_7ReufxhHTtjDb3nXrHiWvvIm1sPDnDzfl6tBr47m73DTZX6MH33pkBl9Wv-03Qf2_q7WsYux9kLSQJ5h6CuYlBrxdiioaJIuXB_LhgApOEqOR-6I3zXkKHU-OEv-W0U6O1VVaYTvfy7wPu-hM2QG40T3IyEuckltuNCpgUJHOOlkxNBmkLuIAzSGRDGc9J5byWQRllmcWx7-GsqhaQqJEyiyRqeX7BnpSLsqTZf4E-nLCEmETYydKQ6AsJnAVB4hJ6h0TOHxgP-nie9wtCiPcG-bAbLSLR4_Pz44EQ5w97haA3KPD8shu9t_Jy476wwAe5YCDkDYU5nhOW_wNO7mQN)


### 내가 좋아요 한 상품 목록 조회
        sequenceDiagram
        participant User
        participant LC as LikeController
        participant US as UserService
        participant PS as ProductService
        participant LS as LikeService
        
            User->>LC: 내 좋아요 목록 요청
            LC->>US : 사용자 인증 확인 (X-USER-ID)
            alt 인증 실패(사용자 미존재, 헤더 미존재)
            US-->>User: 401 UNAUTHORIZED
            else 인증 성공
            US-->>LC: 사용자 정보 반환
            LC->>LS: 내 좋아요 목록 조회(userId) 
            alt 좋아요 목록이 비어있는경우
            LS-->>LC: 200 OK + 빈 좋아요 목록
            else 좋아요 목록이 존재하는 경우
            LS-->>LC: 좋아요 목록 반환
            LC->>PS : 좋아요한 상품 별 상품정보 요청(좋아요한 상품 목록)
            PS-->>LC : 상품 목록 반환
                alt 조회 실패 ( 사유 불문 )
                    LS -->> LC: 500 Internal Server Error
                else 조회 성공
                    LS -->> LC: 좋아요 목록 반환
            end
        end
    end

[![](https://mermaid.ink/img/pako:eNqNVF1v0lAY_itvzhVEIKXlsxdLDJBIJI4MSczCTUPPGBFaPLRGJVw4uSBuJsaAMjKWYRbjDEYcaPDCP7Se_gdPy9eAsniu3tM-H-_zntPWUF6VMRIRzFYVP9OxksfxolQgUjmnzF9UJKIV88WKpGiQrWLi_CYVA6kKqeJTHFMVjail0jZkNmMhLaUMJs-LeewMS9uwNFFlPa_diUxl5tYbsGVlLcvTu7OTiolgHI2Bfj6m7QbttsD4dmX0e8BKev19lZOKMQZrWQR6NKDdK3r-HmhvQr_0wDxtswpcT7zZTGLPm4y7V6lSSZtD6fGlefLVtZQwfkxof0zPBx4wP10a71rLJ2sq2YzX6oC1LkKA80P20f3s4we7e8n9RHwViUtVvDBs_LwZ_XJSstLfinLRNkZjMIYd87TjkDyV2Tar_tDsnrh01ldSdoND8jUG7TGbPw36kWVsGm9bN9d_aXe4ZrnokOc42H0I9xiluSHlFNvBbTpOs91hbnC33Ua8rQNJ21dhDjfbZ0DfvDY_NMEYNWblbKbT2-RyxNomawednnVj6d-GOfaymLJ9DLP7BS77ZM8uwPjdNAYTcG-SprnBsgIreZANOqlomChSCazvBxNIEKKSTep00DNDh_vlJP9fg8WKvHyw2NhFTkEeVCBFGYkHEvP3oDImZcnao5qFyyHtEJdxDomslPGBpJe0HMopdcZjf4d9VS0jUSM6YxJVLxzON3pFlrT5z24hTpgpJjFVVzQk8hEuZIsgsYZeIFHwh31-PsqHolwk6g-FhYAHvWQwjvNFBS4gBAS2uEgwUPegV7Yv52OgIM8LnBDi_CGBD9f_AXv_Fl8?type=png)](https://mermaid.live/edit#pako:eNqNVF1v0lAY_itvzhVEIKXlsxdLDJBIJI4MSczCTUPPGBFaPLRGJVw4uSBuJsaAMjKWYRbjDEYcaPDCP7Se_gdPy9eAsniu3tM-H-_zntPWUF6VMRIRzFYVP9OxksfxolQgUjmnzF9UJKIV88WKpGiQrWLi_CYVA6kKqeJTHFMVjail0jZkNmMhLaUMJs-LeewMS9uwNFFlPa_diUxl5tYbsGVlLcvTu7OTiolgHI2Bfj6m7QbttsD4dmX0e8BKev19lZOKMQZrWQR6NKDdK3r-HmhvQr_0wDxtswpcT7zZTGLPm4y7V6lSSZtD6fGlefLVtZQwfkxof0zPBx4wP10a71rLJ2sq2YzX6oC1LkKA80P20f3s4we7e8n9RHwViUtVvDBs_LwZ_XJSstLfinLRNkZjMIYd87TjkDyV2Tar_tDsnrh01ldSdoND8jUG7TGbPw36kWVsGm9bN9d_aXe4ZrnokOc42H0I9xiluSHlFNvBbTpOs91hbnC33Ua8rQNJ21dhDjfbZ0DfvDY_NMEYNWblbKbT2-RyxNomawednnVj6d-GOfaymLJ9DLP7BS77ZM8uwPjdNAYTcG-SprnBsgIreZANOqlomChSCazvBxNIEKKSTep00DNDh_vlJP9fg8WKvHyw2NhFTkEeVCBFGYkHEvP3oDImZcnao5qFyyHtEJdxDomslPGBpJe0HMopdcZjf4d9VS0jUSM6YxJVLxzON3pFlrT5z24hTpgpJjFVVzQk8hEuZIsgsYZeIFHwh31-PsqHolwk6g-FhYAHvWQwjvNFBS4gBAS2uEgwUPegV7Yv52OgIM8LnBDi_CGBD9f_AXv_Fl8)