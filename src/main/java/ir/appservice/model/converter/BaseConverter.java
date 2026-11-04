package ir.appservice.model.converter;

import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

public abstract class BaseConverter<T extends BaseEntity> implements Converter<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private CrudService<T> crudService;
    private Class<T> clazz;

    public BaseConverter(Class<T> clazz, CrudService<T> crudService) {
        this.crudService = crudService;
        this.clazz = clazz;
    }

    @Override
    public T getAsObject(FacesContext context, UIComponent component, String id) {

        component.getAttributes().entrySet().stream().forEach(entry -> logger.trace("{}:{}", entry.getKey(), entry.getValue()));
        logger.trace("getAsObject: {}", id);

        if (id == null || id.trim().isEmpty()) {
            return null;
        }

        try {
            T temp = crudService.get(id);
            logger.trace("To Object: {} -> {}", temp.getId(), clazz.getSimpleName());
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            String message = String.format("Can't Convert <%s> to <%s>, Cause: %s", id, clazz.getSimpleName(), e.getMessage());
            logger.error(message);
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, T item) {
        logger.trace("getAsObject: {}", item);

        if (item == null) {
            return "";
        }

        try {
            logger.trace("To String {} -> {}", clazz.getSimpleName(), item.getId());
            return item.getId();
        } catch (Exception e) {
            e.printStackTrace();
            String message = String.format("Can't Convert <%s> to String, Cause: %s", item, e.getMessage());
            logger.error(message);
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_WARN, message, message));
        }
    }
}
