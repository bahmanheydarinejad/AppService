package ir.appservice.model.service;

import ir.appservice.configuration.exception.NotFoundEntityException;
import ir.appservice.model.entity.BaseEntity;
import ir.appservice.model.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CrudService<T extends BaseEntity> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    protected CrudRepository crudRepository;
    protected Class<T> clazz;

    @PersistenceContext
    private EntityManager entityManager;

    public CrudService(CrudRepository crudRepository, Class<T> clazz) {
        this.crudRepository = crudRepository;
        this.clazz = clazz;
    }

    public boolean existById(String id) {
        return this.crudRepository.existsById(id);
    }

    public List<T> addAll(Collection<T> items) {
        return this.crudRepository.saveAll(items);
    }

    public List<T> editAll(Collection<T> items) {
        List<T> editedItems = new ArrayList<>();

        items.forEach(t -> editedItems.add(edit(t)));
        return editedItems;
    }

    public T add(T t) {
        return (T) crudRepository.save(t);
    }

    @SuppressWarnings("unchecked")
    private T findOrThrow(String id) {
        T entity = (T) crudRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new NotFoundEntityException("Entity not found: " + id);
        }
        return entity;
    }

    public T remove(String id) {
        T temp = findOrThrow(id);
        temp.setDeleteDate(new Date());
        return (T) crudRepository.save(temp);
    }

    public T remove(T t) {
        return remove(t.getId());
    }

    public T recycle(String id) {
        T temp = findOrThrow(id);
        temp.setDeleteDate(null);
        return (T) crudRepository.save(temp);
    }

    public T activate(String id) {
        T temp = findOrThrow(id);
        temp.setStatue(T.ACTIVE_STATUS);
        return (T) crudRepository.save(temp);
    }

    public T deActivate(String id) {
        T temp = findOrThrow(id);
        temp.setStatue(T.DE_ACTIVE_STATUS);
        return (T) crudRepository.save(temp);
    }

    public T recycle(T t) {
        return recycle(t.getId());
    }

    public T permanentRemove(String id) {
        T temp = findOrThrow(id);
        crudRepository.deleteById(id);
        return temp;
    }

    public T permanentRemove(T t) {
        return permanentRemove(t.getId());
    }

    public T edit(T t) {

        if (!crudRepository.existsById(t.getId())) {
            throw new NotFoundEntityException("Entity Not Found Exception: " + t.getId());
        }
        return (T) crudRepository.save(t);
    }

    public T get(String id) {
        return (T) crudRepository.findById(id).orElse(null);
    }

    public T lazyGet(String id, String... attributes) {
        logger.trace("Fetching: {} of {}", Arrays.asList(attributes), clazz);
        EntityGraph<T> graph = entityManager.createEntityGraph(clazz);
        for (String att : attributes) {
            String[] split = att.split("\\.");
            logger.trace("Att: {} => {}", att, Arrays.asList(split));
            Subgraph sg = null;
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    logger.trace("Root Node: {}", split[i]);
                    sg = graph.addSubgraph(split[i]);
                } else {
                    logger.trace("Node: {}", split[i]);
                    sg = sg.addSubgraph(split[i]);
                }
            }
        }

        graph.getAttributeNodes().stream().forEach(attributeNode -> logger.trace("Graph:[{},{}," +
                        "{}]",
                attributeNode.getAttributeName(), attributeNode.getKeySubgraphs(), attributeNode.getSubgraphs()));

        Map<String, Object> hints = new HashMap();
        hints.put("jakarta.persistence.loadgraph", graph);

        return entityManager.find(clazz, id, hints);
    }

    public T fullLazyGet(String id) {
        List<String> fieldNames = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isAssignableFrom(BaseEntity.class)) {
                fieldNames.add(field.getName());
            }
        }

        logger.trace("Full lazy load: {}", fieldNames);
        return lazyGet(id, fieldNames.toArray(new String[]{}));
    }

    private void addSubgraph() {

    }

    public List<T> list() {
        return crudRepository.findAllByDeleteDateIsNull();
    }

    public List<T> list(Pageable pageable) {
        return crudRepository.findAllByDeleteDateIsNull(pageable).getContent();
    }

    public Page pagedList(Pageable pageable, Map<String, Object> filters) {
        return crudRepository.findAll(getFilterSpecification(filters), pageable);
    }

    public Page pagedActiveList(Pageable pageable, Map<String, Object> filters) {
        return crudRepository.findAll(getNotDeletedSpecifications().and(getFilterSpecification(filters)), pageable);
    }

    public Page pagedDeletedList(Pageable pageable, Map<String, Object> filters) {
        return crudRepository.findAll(getDeletedSpecifications().and(getFilterSpecification(filters)), pageable);
    }

    public List<T> listDeleted() {
        return crudRepository.findAllByDeleteDateIsNotNull();
    }

    public List<T> listDeleted(Pageable pageable) {
        return crudRepository.findAllByDeleteDateIsNotNull(pageable).getContent();
    }

    private Specification<T> getDeletedSpecifications() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder.isNotNull(root.get("deleteDate"));
    }

    private Specification<T> getNotDeletedSpecifications() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> builder.isNull(root.get("deleteDate"));
    }

    private Specification<T> getFilterSpecification(
            Map<String, Object> filterValues) {

        Specification<T> specification = (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
            Optional<Predicate> predicate = filterValues.entrySet().stream()
                    .filter(v -> v.getValue() != null)
                    .map(entry -> {
                        Path<?> path = root;
                        String key = entry.getKey();
                        if (entry.getKey().contains(".")) {
                            String[] splitKey = entry.getKey().split("\\.");
                            path = root.join(splitKey[0]);
                            key = splitKey[1];
                        }
                        return builder.like(path.get(key).as(String.class),
                                "%" + entry.getValue() + "%");
                    })
                    .collect(Collectors.reducing((a, b) -> builder.and(a, b)));
            return predicate.orElseGet(() -> alwaysTrue(builder));
        };

        return specification;
    }

    private Predicate alwaysTrue(CriteriaBuilder builder) {
        return builder.isTrue(builder.literal(true));
    }

    private Specification<T> generateSpecificationFilter(Map<String, Object> filters) {
        return (Specification<T>) (root, query, criteriaBuilder) -> {
            filters.entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .forEach(entry -> {
                        try {
                            if (root.get(entry.getKey()).getJavaType() == String.class) {
                                Predicate like = criteriaBuilder.like(root.get(entry.getKey()),
                                        "%" + entry.getValue() + "%");
                                query.where(like);
                            } else {
                                Predicate equal = criteriaBuilder.equal(root.get(entry.getKey()),
                                        entry.getValue());
                                query.where(equal);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    });
            query.where(criteriaBuilder.isNull(root.get("deleteDate")));
            return query.getGroupRestriction();
        };
    }
}
