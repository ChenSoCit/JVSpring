package TayJVLearn.StartJV.Demo.service;

import TayJVLearn.StartJV.Demo.dto.request.UserRequestDTO;
import TayJVLearn.StartJV.Demo.dto.response.PageResponse;
import TayJVLearn.StartJV.Demo.dto.response.UserDetailResponse;
import TayJVLearn.StartJV.Demo.util.UserStatus;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    long saveUser(UserRequestDTO request);
    void updateUser(long userId, UserRequestDTO request);
    void changeUser(long userId, UserStatus status);
    void deleteUser(long userId);
    UserDetailResponse getUser(long userId);
    PageResponse getALlUsersWithSortBy(int pageNo, int pageSize, String sortBy);
    PageResponse getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);
    PageResponse getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy);
    PageResponse advanceSearchByCriteria(int pageNo, int pageSize, String sortBy,String address, String... search );
}
