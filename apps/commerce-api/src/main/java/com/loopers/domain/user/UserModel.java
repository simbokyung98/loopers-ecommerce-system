package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;


@Entity
@Table(name ="user")
@Getter
public class UserModel extends BaseEntity {


    private static final String PATTERN_LOGIN_ID = "^[\\s\\S]{1,9}$";
    private static final String PATTERN_BRITH = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
    private static final String PATTERN_EMAIL  = "^\\w+@\\w+\\.\\w{2,}$";


    private String loginId;
    private String gender;
    private String brith;
    private String email;

    public UserModel() {}

    public UserModel(String loginId, String gender,String brith , String email) {

        if(loginId == null || !loginId.matches(PATTERN_LOGIN_ID)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "로그인id는 비어있거나 9자를 넘을 수 없습니다.");
        }

        if(brith == null || !brith.matches(PATTERN_BRITH)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일의 형식과 다릅니다.");
        }

        if(email == null || !email.matches(PATTERN_EMAIL)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일의 형식과 다릅니다.");
        }


        this.loginId = loginId;
        this.gender = gender;
        this.brith = brith;
        this.email = email;
    }
}
