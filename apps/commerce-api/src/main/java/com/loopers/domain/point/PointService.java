package com.loopers.domain.point;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public PointModel getPoint(Long userId){
        return pointRepository.findByUserId(userId)
                .orElse(null);
    }

    public PointModel create(Long userId){
        PointModel pointModel = new PointModel(userId);
        return pointRepository.save(pointModel);
    }

    public PointModel charge(Long userId, Long amount){
        PointModel pointModel = pointRepository.findByUserId(userId)
                        .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "사용자 포인트 정보가 존재하지 않습니다.."));

        pointModel.charge(amount);

        return pointRepository.save(pointModel);

    }



    public void spend(Long userId, Long amount){
        System.out.println("1️⃣ 락 획득 직전: " + Thread.currentThread().getName());
        PointModel pointModel = pointRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() ->  new CoreException(ErrorType.BAD_REQUEST, "사용자 포인트 정보가 존재하지 않습니다.."));
        System.out.println("2️⃣ 락 획득 완료: " + Thread.currentThread().getName());
        System.out.println("3️⃣ 차감 전 amount: " + pointModel.getTotalAmount() + " / 포인트 ID: " + pointModel.getId() + " / Thread: " + Thread.currentThread().getName());

        pointModel.spand(amount);
        System.out.println("4️⃣ 차감 후 amount: " + pointModel.getTotalAmount() + " / 포인트 ID: " + pointModel.getId() + " / Thread: " + Thread.currentThread().getName());

//       pointRepository.save/(pointModel);

    }
}
