package com.magma.dmsdata.validation;

import com.magma.dmsdata.data.support.Acl;

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
