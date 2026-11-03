package ir.appservice_v1.model.converter;

import ir.appservice_v1.model.entity.domain.Document;
import ir.appservice_v1.model.service.DocumentService;
import org.springframework.stereotype.Component;

@Component
public class DocumentConverter extends BaseConverter<Document> {

    public DocumentConverter(DocumentService documentService) {
        super(Document.class, documentService);
    }
}
