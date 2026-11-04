package ir.appservice.model.service;

import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.repository.DocumentRepository;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@ApplicationScope
public class DocumentService extends CrudService<Document> {

    public DocumentService(DocumentRepository documentRepository) {
        super(documentRepository, Document.class);
    }

    public DocumentRepository getDocumentRepository() {
        return (DocumentRepository) this.crudRepository;
    }

    public StreamedContent streamImage() {
        String document_id = null;
        try {
            if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
                // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
                logger.trace("DefaultStreamedContent");
                return DefaultStreamedContent.builder().build();
            } else {
                logger.trace("imageId: " + FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("imageId"));
                document_id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("imageId");
                logger.trace(String.format("Streaming document: %s", document_id));
                Document doc = getDocumentRepository().findById(document_id).orElse(null);
                if (doc != null && doc.getData() != null) {
                    return DefaultStreamedContent.builder()
                            .stream(() -> new ByteArrayInputStream(doc.getData()))
                            .build();
                }
                return DefaultStreamedContent.builder().build();
            }
        } catch (Exception e) {
            logger.error(String.format("Could not serve document: %s => %s", document_id, e.getMessage()));
            return DefaultStreamedContent.builder().build();
        }
    }

    public Document uploadDocument(UploadedFile uploadedFile) {
        logger.trace("Uploading file: {}, size: {}", uploadedFile.getFileName(),
                uploadedFile.getSize());

        Document document = new Document();
        document.setDisplayName(uploadedFile.getFileName());
        document.setData(uploadedFile.getContent());
        document.setType(uploadedFile.getContentType());
        document.setSize(uploadedFile.getSize());

        logger.trace("Successful file upload {} is uploaded. Size {} (KB): ",
                document.getDisplayName(),
                document.getSize() / 1024f);

        return document;
    }

    public Document convertToDocument(File file) throws IOException {
        logger.trace("File: {}, size: {}", file, file.length());

        Document document = new Document();
        document.setDisplayName(Paths.get(file.toURI()).getFileName().toString());
        document.setData(Files.readAllBytes(Paths.get(file.toURI())));
        document.setType(Files.probeContentType(Paths.get(file.toURI())));
        document.setSize(file.length());

        logger.trace("Successful file upload", document.getDisplayName() + " is uploaded. Size (KB): " + document.getSize() / 1024f, null);

        return document;
    }


}