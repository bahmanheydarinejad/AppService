package ir.appservice_v1.view.beanComponents;

import ir.appservice_v1.model.entity.domain.Document;
import ir.appservice_v1.model.service.DocumentService;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
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
                imageStream = new DefaultStreamedContent();
            } else {
                Document document = documentService.get(documentId);
                imageStream =
                        new DefaultStreamedContent(new ByteArrayInputStream(document.getData()));
            }
        } catch (Exception e) {
            logger.error(String.format("Stream failed, Cause: {}", e.getMessage()));
        } finally {
            logger.trace("Streaming: {}", imageStream.getName());
            return imageStream;
        }
    }
}
