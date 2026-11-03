package ir.appservice_v1.controller;

import ir.appservice_v1.model.entity.domain.Document;
import ir.appservice_v1.model.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequestMapping("/document")
@Controller
public class DocumentController extends BaseController {

    @Autowired
    private DocumentService documentService;

    @GetMapping()
    public List<Document> listDocuments() {

        List<Document> documents = documentService.list();
        return documents;

    }

    @GetMapping("/{id}")
    public void getDocument(@PathVariable String id, HttpServletResponse response) throws IOException {

        Document document = documentService.get(id);
        logger.trace(String.format("Document: %s, %s, %s", document.getDisplayName(), document.getType(), document.getSize()));

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(document.getType());
        ServletOutputStream responseOutputStream = response.getOutputStream();
        responseOutputStream.write(document.getData());
        responseOutputStream.flush();
        responseOutputStream.close();
    }
}
