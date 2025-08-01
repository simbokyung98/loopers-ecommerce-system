
## 주문/결제
### 주문 요청
* 시나리오 : “회원은 여러가지 상품을 한번에 주문 요청 할 수 있다”
* 기능명세
    * 주체(Actor) : 회원
    * 주요시나리오
        * 사용자가 사용자 id와 상품목록으로 주문을 요청한다
        * 시스템은 사용자가 요청한 주문을 처리한다
    * 시스템 동작
        * 사용자 정보를 검증한다
        * 사용자의 포인트가 충분한지 검증한다
        * 상품 재고 현황을 확인한다
        * 모든 상품의 재고가 있다면 상품의 재고를 차감한다
        * 사용자의 포인트를 차감한다
        * 주문정보를 기록한다
        * 주문요청 정보를 반환한다
    * 예외
        * 존재하지 않는 회원일 경우
            * 인증 헤더 없음 / 401 UNAUTHORIZED
        * 포인트가 충분하지 않을 경우
            * 비즈니스 룰 벗어남 / 400 BAD_REQUEST
        * 상품 재고가 충분하지 않을 경우
            * 비즈니스 룰 벗어남 / 400 BAD_REQUEST

### 유저의 주문 목록 조회
* 시나리오 : “회원은 자신의 주문 목록을 조회할 수 있다”
* 기능명세
    * 주체(Actor) :회원
    * 주요 시나리오
        * 회원이 사용자 id로 주문 목록를 요청한다
        * 시스템은 사용자 id에 맞는 주문 목록을 반환한다
    * 시스템 동작
        * 사용자 정보를 검증한다
        * 사용자 id로 db에서 주문 정보를 조회한다
        * 주문 목록을 반환한다
    * 예외
        * 존재하지 않는 회원일 경우
            * 인증 헤더 없음 / 401 UNAUTHORIZED
### 단일 주문 상세 조회
* 시나리오 : “회원은 자신의 주문 정보를 조회할 수 있다”
* 기능명세
    * 주체(Actor) :회원
    * 주요 시나리오
        * 사용자가 주문id로 주문 정보를 조회한다
        * 시스템은 주문id에 해당하는 주문 정보를 반환한다
    * 시스템 동작
        * 사용자 정보를 검증한다
        * 주문id로 db에서 주문 정보를 조회한다
        * 주문 정보를 반환한다
    * 예외
        * 존재하지 않는 회원일 경우
            * 인증 헤더 없음 / 401 UNAUTHORIZED
        * 존재하지 않는 주문정보일 경우
            * 잘못된 productId / 404 not found