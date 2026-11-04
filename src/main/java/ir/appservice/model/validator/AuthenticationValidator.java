package ir.appservice.model.validator;

import ir.appservice.model.entity.application.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

public class AuthenticationValidator implements Validator<Account> {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationValidator.class);

    @Override
    public void validate(FacesContext context, UIComponent component, Account value) throws ValidatorException {

    }

}
