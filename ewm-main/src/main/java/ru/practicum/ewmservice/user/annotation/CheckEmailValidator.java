package ru.practicum.ewmservice.user.annotation;

import ru.practicum.ewmservice.user.dto.UserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckEmailValidator implements ConstraintValidator<EmailMaxLengthValid, UserDto> {
    @Override
    public boolean isValid(UserDto userDto, ConstraintValidatorContext constraintValidatorContext) {
        String email = userDto.getEmail();
        String[] emailParts = email.split("@");
        String localPart = emailParts[0];
        String domainPart = emailParts[1].split("\\.")[0];
        return localPart.length() <= 64 && domainPart.length() <= 63;
    }
}
