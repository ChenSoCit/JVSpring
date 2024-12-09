

package TayJVLearn.StartJV.Demo.service.impl;


import TayJVLearn.StartJV.Demo.dto.request.AddressDTO;
import TayJVLearn.StartJV.Demo.dto.request.UserRequestDTO;
import TayJVLearn.StartJV.Demo.dto.response.PageResponse;
import TayJVLearn.StartJV.Demo.dto.response.UserDetailResponse;
import TayJVLearn.StartJV.Demo.exception.ResourceNotFoundException;
import TayJVLearn.StartJV.Demo.model.Address;
import TayJVLearn.StartJV.Demo.model.User;
import TayJVLearn.StartJV.Demo.repository.SearchRepository;
import TayJVLearn.StartJV.Demo.repository.UserRepository;
import TayJVLearn.StartJV.Demo.service.UserService;
import TayJVLearn.StartJV.Demo.util.UserStatus;
import TayJVLearn.StartJV.Demo.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    /**
     * Save new user to DB
     *
     * @param request
     * @return userId
     */
    @Override
    public long saveUser(UserRequestDTO request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .addresses(convertToAddress(request.getAddresses()))
                .build();
        request.getAddresses().forEach(a ->
                user.saveAddress(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build()));
        userRepository.save(user);

        log.info("User has added successfully, userId={}", user.getId());

        return user.getId();
    }



    /**
     * Update user by userId
     *
     * @param userId
     * @param request
     */
    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        User user = getUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            // check email from database if not exist then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setAddresses(convertToAddress(request.getAddresses()));
        userRepository.save(user);

        log.info("User has updated successfully, userId={}", userId);
    }

    @Override
    public void changeUser(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("User changed successfull, userId = {}", userId);
    }

    /**
     * Change status of user by userId
     *
     * @param userId
     * @param status
     */

    /**
     * Delete user by userId
     *
     * @param userId
     */
    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
        log.info("User has deleted permanent successfully, userId={}", userId);
    }

    /**
     * Get user detail by userId
     *
     * @param userId
     * @return
     */
    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(userId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .phone(user.getPhone())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus())
                .type(user.getType().name())
                .build();
    }

    @Override
    public PageResponse getALlUsersWithSortBy(int pageNo, int pageSize, String sortBy) {
        if(pageNo > 0){
            pageNo = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();
        // Neu co gia tri
        if(StringUtils.hasLength(sortBy)){
            //firstname:desc|asc
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)"); // bieu thuc co dang regex (\w+?) group1 (:)group2 (.*) group3
            Matcher matcher = pattern.matcher(sortBy); //
            if(matcher.find()){ // tim kiem
                if (matcher.group(3).equalsIgnoreCase("asc")){ // so sanh{
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Page<User> page = userRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by(sorts)));

        List<UserDetailResponse> list = page.stream().map(user -> UserDetailResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .dateOfBirth(user.getDateOfBirth())
                        .gender(user.getGender())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .status(user.getStatus())
                        .type(user.getType().name())
                        .build())
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .items(list)
                .build();
    }
    @Override
    public PageResponse<?> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sorts != null) {
            for (String sortBy : sorts) {
                log.info("sortBy: {}", sortBy);
                // firstName:asc|desc
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sortBy);
                if (matcher.find()) {
                    if (matcher.group(3).equalsIgnoreCase("asc")) {
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    } else {
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }

        // Page<User> page = userRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by(sorts)));
        //Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));

        Page<User> users = userRepository.findAll(pageable);
       //  Page<User> users = userRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by(orders)));
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .gender(user.getGender())
                .username(user.getUsername())
                .type(user.getType().name())
                .status(user.getStatus())
                .build()).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy) {
        return searchRepository.searchUser(pageNo, pageSize, search, sortBy);
    }

    @Override
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy,String address, String... search) {
        return searchRepository.advanceSearchUserByCriteria(pageNo, pageSize,sortBy, address , search);
    }

    /**
     * Get all user per pageNo and pageSize
     *
     * @param pageNo
     * @param pageSize
     * @return
     */


    /**
     * Get user by userId
     *
     * @param userId
     * @return User
     */
    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(("user.not.found")));
    }

    /**
     * Covert Set<AddressDTO> to Set<Address>
     *
     * @param addresses
     * @return Set<Address>
     */
    private Set<Address> convertToAddress(Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(a.getAddressType())
                        .build())
        );
        return result;
    }
}