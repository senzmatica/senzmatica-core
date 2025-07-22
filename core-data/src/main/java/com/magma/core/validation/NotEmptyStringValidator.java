package com.magma.core.validation;

import com.magma.core.data.support.Acl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class NotEmptyStringValidator implements ConstraintValidator<NotEmptyString, List<Acl>> {

    @Override
    public void initialize(NotEmptyString notEmptyString) {

    }

    @Override
    public boolean isValid(List<Acl> value, ConstraintValidatorContext context) {
        return value != null && !value.isEmpty();
    }
}
