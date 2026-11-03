package ir.appservice_v1.view.beanComponents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;

public abstract class BaseBean implements Serializable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public void info(String message, String detail, String parentId) {
        FacesContext.getCurrentInstance().addMessage(parentId, new FacesMessage(FacesMessage.SEVERITY_INFO, message, detail));
    }

    public void warn(String message, String detail, String parentId) {
        FacesContext.getCurrentInstance().addMessage(parentId, new FacesMessage(FacesMessage.SEVERITY_WARN, message, detail));
    }

    public void error(String message, String detail, String parentId) {
        FacesContext.getCurrentInstance().addMessage(parentId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, detail));
    }

    public void fatal(String message, String detail, String parentId) {
        FacesContext.getCurrentInstance().addMessage(parentId, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, detail));
    }

}
