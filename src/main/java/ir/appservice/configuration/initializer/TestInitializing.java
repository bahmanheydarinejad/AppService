package ir.appservice.configuration.initializer;


import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

//@Configuration
public class TestInitializing implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TestInitializing.class);

    @Autowired
    private DocumentService documentService;

//    @Autowired
//    private EntityManager entityManager;

    @Autowired
    private JavaMailSender javaMailSender;

//    private Marshaller marshaller;
//    private Unmarshaller unmarshaller;

    @Override
    public void afterPropertiesSet() throws Exception {
        f3();
    }

    private void f3() throws MessagingException {
        logger.trace("Sending Test Email...");
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo("gharne_25@yahoo.com");
        helper.setText("<html><body>Here is a cat picture! <img src='cid:id101'/><body></html>", true);
        helper.setSubject("Hi");

        Document attachment = documentService.get("10c16a0f-744c-402d-9a5f-4f5d6ce659fd");
        helper.addAttachment(attachment.getDisplayName(), new ByteArrayResource(attachment.getData()));

        logger.trace("Test Email {} was sent.", mimeMessage);
        javaMailSender.send(mimeMessage);
    }

//    private void f2() {
//        logger.trace("f2");
//        EntityGraph<Account> graph = entityManager.createEntityGraph(Account.class);
//        Subgraph roleGraph = graph.addSubgraph("roles");
//        roleGraph.addSubgraph("panels");
////        roleGraph.addAttributeNodes("panels");
//
//        Map<String, Object> hints = new HashMap();
//        hints.put("jakarta.persistence.loadgraph", graph);
//        logger.trace("Graph: {}", graph.getAttributeNodes());
//
//        Account account = entityManager.find(Account.class, UUID.fromString("XXX"), hints);
//        logger.trace("Account: {}", account);
//        logger.trace("Roles: {}", account.getRoles());
//        account.getRoles().forEach(role -> logger.trace("panel: {}", role.getPanels()));
//    }

    //
//    private void initializeApplication() {
//        if (account != null && !accountService.existsByAccountNameIgnoreCase(account.getAccountName())) {
//            account.getRoles().stream().forEach(role -> logger.trace("Account Access Panel By " +
//                    "Role: {}=> {}", role.getDisplayName(), role.getPanels()));
//
//            account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));
//            account.setAvatar(account.getNaturalPerson().getAvatar());
////            account = accountService.add(account);
//            logger.trace(String.format("New account %s was added.", account.getAccountName()));
//
//
//            List<Class> entityClasses = new ArrayList<>();
//            ClassPathScanningCandidateComponentProvider scanner =
//                    new ClassPathScanningCandidateComponentProvider(false);
//            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
//            for (BeanDefinition bd : scanner.findCandidateComponents(BaseEntity.class.getPackage().getName())) {
//                try {
//                    entityClasses.add(Class.forName(bd.getBeanClassName()));
//                    logger.trace("Entity: {}", Class.forName(bd.getBeanClassName()).getName());
//                } catch (ClassNotFoundException e) {
//                    logger.error("Class not found for Entity: {}", bd.getBeanClassName());
//                }
//            }
//
//            try {
//                JAXBContext jaxbContext = JAXBContext.newInstance(entityClasses.toArray(new Class[]{}));
//                marshaller = jaxbContext.createMarshaller();
//                logger.trace("Account: {}", account);
//                marshaller.marshal(account, new StreamResult(initializer.getFile()));
////            unmarshaller = jaxbContext.createUnmarshaller();
////            account =
////                    (Account) unmarshaller.unmarshal(feeder.getFile());
////            accountService.add(account);
//            } catch (JAXBException e) {
//                e.printStackTrace();
//                logger.error(e.getMessage());
//            } catch (IOException e) {
//                e.printStackTrace();
//                logger.error(e.getMessage());
//            }
//
//        }
//    }
}
