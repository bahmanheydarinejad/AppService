package ir.appservice_v1.configuration.initializer;

import ir.appservice_v1.model.entity.domain.Document;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
@ConfigurationPropertiesBinding
public class DocumentFeederConverter implements Converter<String, Document> {
    protected static final Logger logger = LoggerFactory.getLogger(DocumentFeederConverter.class);

    @Override
    public Document convert(String source) {

        try {
            File tempFile = new ClassPathResource(source).getFile();
            Document avatar = new Document();
            avatar.setDisplayName(tempFile.getName());
            avatar.setType(Files.probeContentType(tempFile.toPath()));
            avatar.setData(FileUtils.readFileToByteArray(tempFile));
            logger.info(source);
            return avatar;
        } catch (IOException e) {
            e.printStackTrace();
            logger.info(null);
            return null;
        }
    }

}
