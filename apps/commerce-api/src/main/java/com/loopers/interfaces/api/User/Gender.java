package com.loopers.interfaces.api.User;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

import java.lang.reflect.GenericDeclaration;

/**
 * Class Description
 *
 * @author simbokyung
 * @version 1.0
 * @class Gender
 * @modification <pre>
 * since        author	        description
 * ----------   ------------    ---------------------
 * 7/15/25   최초 작성
 * </pre>
 * @copyRight COPYRIGHT © OSSTEM IMPLANT CO., LTD. ALL RIGHTS RESERVED.\n
 */
@Getter
public enum Gender {
    MALE("M","남성"),
    FEMALE("F","여성");

    private final String code;
    private final String desc;


    Gender(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


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
