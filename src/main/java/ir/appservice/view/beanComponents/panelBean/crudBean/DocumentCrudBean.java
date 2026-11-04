package ir.appservice.view.beanComponents.panelBean.crudBean;

import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.service.DocumentService;
import ir.appservice.view.beanComponents.panelBean.BaseLazyCrudBean;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
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
                imageStream = DefaultStreamedContent.builder().build();
            } else {
                imageStream = DefaultStreamedContent.builder()
                        .stream(() -> new ByteArrayInputStream(this.appLazyDataModel.getItem().getData()))
                        .contentType(this.appLazyDataModel.getItem().getType())
                        .name(this.appLazyDataModel.getItem().getDisplayName())
                        .build();
            }
        } catch (Exception e) {
            logger.error(String.format("Stream failed, Cause: {}", e.getMessage()));
        } finally {
            logger.trace("Streaming: {}", imageStream.getName());
            return imageStream;
        }
    }

}
