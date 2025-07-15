package com.loopers.application.example;

import com.loopers.domain.example.ExampleModel;
import com.loopers.interfaces.api.User.UserV1Dto;

public record ExampleInfo(Long id, String name, String description) {
    public static ExampleInfo from(ExampleModel model) {
        return new ExampleInfo(
            model.getId(),
            model.getName(),
            model.getDescription()
        );
    }


}
