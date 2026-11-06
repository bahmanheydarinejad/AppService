package ir.appservice.view.beanComponents;

import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.service.DocumentService;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.io.ByteArrayInputStream;

@Getter
@Setter
@Component
@ApplicationScope
public class DocumentStreamer {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private DocumentService documentService;
    private StreamedContent imageStream;
    private String documentRequestId = "documentRequestId";

    public DocumentStreamer(DocumentService documentService) {
        this.documentService = documentService;
    }

    public StreamedContent imageStream() {
        String documentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(documentRequestId);
        logger.trace("Document Request ID: " + documentId);
        try {
            if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
                // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
                imageStream = DefaultStreamedContent.builder().build();
            } else {
                Document document = documentService.get(documentId);
                imageStream = DefaultStreamedContent.builder()
                        .stream(() -> new ByteArrayInputStream(document.getData()))
                        .contentType(document.getType())
                        .name(document.getDisplayName())
                        .build();
            }
        } catch (Exception e) {
            logger.error(String.format("Stream failed, Cause: {}", e.getMessage()));
        } finally {
            logger.error("Streaming: {}", documentId);
            return imageStream;
        }
    }
}
