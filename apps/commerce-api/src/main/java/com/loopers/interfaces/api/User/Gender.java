package com.loopers.interfaces.api.User;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("M","남성"),
    FEMALE("F","여성");

    private final String code;
    private final String desc;


    public static Gender fromCode(String code) {

        if(code == null || code.isBlank()){
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 비어있을 수 없습니다");
        }

        for (Gender gender : values()) {
            if (gender.code.equalsIgnoreCase(code)) {
                return gender;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "없는 성별입니다.");
    }


}
