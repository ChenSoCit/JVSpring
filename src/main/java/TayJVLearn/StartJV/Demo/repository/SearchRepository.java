package TayJVLearn.StartJV.Demo.repository;
import TayJVLearn.StartJV.Demo.dto.response.PageResponse;
import TayJVLearn.StartJV.Demo.model.Address;
import TayJVLearn.StartJV.Demo.model.User;
import TayJVLearn.StartJV.Demo.repository.criteria.SearchCriteria;
import TayJVLearn.StartJV.Demo.repository.criteria.UserSearchQueryCriteriaConsumer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Repository;

import static TayJVLearn.StartJV.Demo.util.AppConst.SEARCH_OPERATOR;
import static TayJVLearn.StartJV.Demo.util.AppConst.SORT_BY;

@Repository
@Component
@Slf4j
public class SearchRepository {
    /*
    * 1. query ra 1 list user
    * 2. query ra so record
     */
    @PersistenceContext
    private EntityManager entityManager;

    private static final String LIKE_FORMAT = "%%%s%%";

    /**
     * Search user using keyword and
     *
     * @param pageNo
     * @param pageSize
     * @param search
     * @param sortBy
     * @return user list with sorting and paging
     */
    public PageResponse<?> searchUser(int pageNo, int pageSize, String search, String sortBy) {
        log.info("Execute search user with keyword={}", search);

        StringBuilder sqlQuery = new StringBuilder("select new TayJVLearn.StartJV.Demo.dto.response.UserDetailResponse(u.id, u.firstName, u.lastName) from User u Where 1=1");
        if (StringUtils.hasLength(search)) { // noi chuoi query
            sqlQuery.append("and lower(u.firstName) like lower(:firstName)");
            sqlQuery.append("or lower(u.lastName )like lower(:lastName)");
            sqlQuery.append("or lower(u.email) like lower(:email)");
        }
        // noi chuoi xu ly SortBy
        if (StringUtils.hasLength(sortBy)){
            //firstname:desc|esc
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()){
                sqlQuery.append(String.format("order by u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }

        Query selectQuery = entityManager.createQuery(sqlQuery.toString());

        // phan trang
        selectQuery.setFirstResult(pageNo); // vi tri
        selectQuery.setMaxResults(pageSize); // so luong
        if (StringUtils.hasLength(search)) { // fill tham so cho noi chuoi query
            selectQuery.setParameter("firstName", String.format("%%%s%%", search));
            selectQuery.setParameter("lastName", String.format("%%%s%%", search));
            selectQuery.setParameter("email", String.format("%%%s%%", search));
        }

        List users = selectQuery.getResultList(); // xem kq tra ve tu cau lenh sqlQuery tra r 1 list user

        System.out.println(users); // kiem tra lai da tra ve kq chua

        // query ra so record
        StringBuilder sqlCountQuery = new StringBuilder("select count(*) from User u Where 1=1");
        if (StringUtils.hasLength(search)) { // noi chuoi query
            sqlCountQuery.append("and lower(u.firstName) like lower(?1)"); //  theo thu tu muc search
            sqlCountQuery.append("or lower(u.lastName )like lower(?2)");
            sqlCountQuery.append("or lower(u.email) like lower(?3)");
        }
        Query selectCountQuery = entityManager.createQuery(sqlCountQuery.toString());

        if (StringUtils.hasLength(search)) { // fill tham so cho noi chuoi query
            selectCountQuery.setParameter(1, String.format("%%%s%%", search));
            selectCountQuery.setParameter(2, String.format("%%%s%%", search));
            selectCountQuery.setParameter(3, String.format("%%%s%%", search));
        }

        Long totalElements = (Long) selectCountQuery.getSingleResult(); // tong so cac phan tu phia tren
        System.out.println(totalElements);

        // xu ly sortBy

        // Neu co gia tri
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<?> page = new PageImpl<Object>(users,pageable, totalElements);

        return PageResponse.builder() // tra ve ket qua cuoi
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(users)
                .build();
    }

    public PageResponse<Object> advanceSearchUserByCriteria(int offset, int pageSize, String sortBy, String address, String... search){
        // 1. Lay ra ds user
        log.info("Search user with search={} and sortBy={}", search, sortBy);

        List<SearchCriteria> criteriaList = new ArrayList<>();

        if (search.length > 0) {
            Pattern pattern = Pattern.compile(SEARCH_OPERATOR);
            for (String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            for (String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }
        //2. Lay ra so luong ban ghi

        List<User> users = getUsers(offset, criteriaList, sortBy);

        Long totalElements = getTotalElements(criteriaList);

        Page<User> page = new PageImpl<>(users, PageRequest.of(offset, pageSize), totalElements);

        return PageResponse.builder()
                .pageNo(offset)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(users)
                .build();
    }

    private List<User> getUsers(int pageSize, List<SearchCriteria> criteriaList, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Predicate userPredicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(userPredicate, criteriaBuilder, userRoot);

        if (StringUtils.hasLength(address)) {
            Join<Address, User> userAddressJoin = userRoot.join("addresses");
            Predicate addressPredicate = criteriaBuilder.equal(userAddressJoin.get("city"), address);
            query.where(userPredicate, addressPredicate);
        } else {
            criteriaList.forEach(searchConsumer);
            userPredicate = searchConsumer.getPredicate();
            query.where(userPredicate);
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                String direction = matcher.group(3);
                if (direction.equalsIgnoreCase("asc")) {
                    query.orderBy(criteriaBuilder.asc(userRoot.get(fieldName)));
                } else {
                    query.orderBy(criteriaBuilder.desc(userRoot.get(fieldName)));
                }
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }
    private Long getTotalElements(List<SearchCriteria> params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(predicate, criteriaBuilder, root);
        params.forEach(searchConsumer);
        predicate = searchConsumer.getPredicate();
        query.select(criteriaBuilder.count(root));
        query.where(predicate);

        return entityManager.createQuery(query).getSingleResult();
    }
    /**
     * Advance search user by criterias
     *
     * @param offset
     * @param pageSize
     * @param sortBy
     * @param search
     * @return
     */
    /*
    public PageResponse<?> searchUserByCriteria(int offset, int pageSize, String sortBy, String address, String... search) {
        log.info("Search user with search={} and sortBy={}", search, sortBy);

        List<SearchCriteria> criteriaList = new ArrayList<>();

        if (search.length > 0) {
            Pattern pattern = Pattern.compile(SEARCH_OPERATOR);
            for (String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            for (String s : search) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        List<User> users = getUsers(offset, pageSize, criteriaList, address, sortBy);

        Long totalElements = getTotalElements(criteriaList);

        Page<User> page = new PageImpl<>(users, PageRequest.of(offset, pageSize), totalElements);

        return PageResponse.builder()
                .pageNo(offset)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(users)
                .build();
    }
    */
    /**
     * Get all users with conditions
     *
     * @param offset
     * @param pageSize
     * @param criteriaList
     * @param sortBy
     * @return
     */
    /*
    private List<User> getUsers(int offset, int pageSize, List<SearchCriteria> criteriaList, String address, String sortBy) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
        Root<User> userRoot = query.from(User.class);

        Predicate userPredicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(userPredicate, criteriaBuilder, userRoot);

        if (StringUtils.hasLength(address)) {
            Join<Address, User> userAddressJoin = userRoot.join("addresses");
            Predicate addressPredicate = criteriaBuilder.equal(userAddressJoin.get("city"), address);
            query.where(userPredicate, addressPredicate);
        } else {
            criteriaList.forEach(searchConsumer);
            userPredicate = searchConsumer.getPredicate();
            query.where(userPredicate);
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                String direction = matcher.group(3);
                if (direction.equalsIgnoreCase("asc")) {
                    query.orderBy(criteriaBuilder.asc(userRoot.get(fieldName)));
                } else {
                    query.orderBy(criteriaBuilder.desc(userRoot.get(fieldName)));
                }
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();
    }
    */
    /**
     * Count users with conditions
     *
     * @param params
     * @return
     */
    /*
    private Long getTotalElements(List<SearchCriteria> params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchQueryCriteriaConsumer searchConsumer = new UserSearchQueryCriteriaConsumer(predicate, criteriaBuilder, root);
        params.forEach(searchConsumer);
        predicate = searchConsumer.getPredicate();
        query.select(criteriaBuilder.count(root));
        query.where(predicate);

        return entityManager.createQuery(query).getSingleResult();
    }
    */
}

