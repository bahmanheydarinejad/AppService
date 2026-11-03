package ir.appservice_v1.view.beanComponents.panelBean.crudBean;

import ir.appservice_v1.model.entity.domain.Document;
import ir.appservice_v1.model.service.DocumentService;
import ir.appservice_v1.view.beanComponents.panelBean.BaseLazyCrudBean;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import java.io.ByteArrayInputStream;


@Setter
@Getter
@Component
@SessionScope
public class DocumentCrudBean extends BaseLazyCrudBean<Document> {

    private StreamedContent imageStream;
    private String documentRequestId = "documentRequestId";

    public DocumentCrudBean(DocumentService documentService) {
        super(documentService, Document.class);
    }

    public void uploadDocument(FileUploadEvent event) {
        logger.trace("Uploading file: " + event.getFile().getFileName());
        this.appLazyDataModel.setItem(((DocumentService) crudService).uploadDocument(event.getFile()));
    }

    public StreamedContent imageStream() {
        try {
            if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
                // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
                imageStream = new DefaultStreamedContent();
            } else {
                imageStream =
                        new DefaultStreamedContent(new ByteArrayInputStream(this.appLazyDataModel.getItem().getData()));
            }
        } catch (Exception e) {
            logger.error(String.format("Stream failed, Cause: {}", e.getMessage()));
        } finally {
            logger.trace("Streaming: {}", imageStream.getName());
            return imageStream;
        }
    }

}
