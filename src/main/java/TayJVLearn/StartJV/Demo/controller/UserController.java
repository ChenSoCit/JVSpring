package TayJVLearn.StartJV.Demo.controller;

import TayJVLearn.StartJV.Demo.dto.request.UserRequestDTO;
import TayJVLearn.StartJV.Demo.dto.response.PageResponse;
import TayJVLearn.StartJV.Demo.dto.response.ResponseData;
import TayJVLearn.StartJV.Demo.dto.response.ResponseError;

import TayJVLearn.StartJV.Demo.dto.response.UserDetailResponse;
import TayJVLearn.StartJV.Demo.service.UserService;
import TayJVLearn.StartJV.Demo.util.UserStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
    @RequestMapping("/user")
    @RequiredArgsConstructor
    @Slf4j
    public  class UserController {
        private final UserService userService;

        private static final String ERROR_MESSAGE = "errorMessage={}";


        @PostMapping(value = "/")
        public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO request) {
            log.info("Request add user, {} {}", request.getFirstName(), request.getLastName());

            try {
                long userId = userService.saveUser(request);
                return new ResponseData<>(HttpStatus.CREATED.value(), ("user.add.success"), userId);
            } catch (Exception e) {
                log.error("fail to added: ");
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Add user fail" + e.getMessage());
            }
        }
        @PutMapping("/{userId}")
        public ResponseData<Void> updateUser(@PathVariable @Min(1) long userId, @Valid @RequestBody UserRequestDTO request) {
            log.info("Request update userId={}", userId);

            try {
                userService.updateUser(userId, request);
                return new ResponseData<>(HttpStatus.ACCEPTED.value(), ("user.upd.success"));
            } catch (Exception e) {
                log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Update user fail");
            }
        }

        @PatchMapping("/{userId}")
        public ResponseData<Void> updateStatus(@Min(1) @PathVariable long userId, @RequestParam UserStatus status) {
            log.info("Request change status, userId={}", userId);
            try {
                userService.changeUser(userId,status);
                return new ResponseData<>(HttpStatus.ACCEPTED.value(), ("user.change.success"));
            }catch (Exception e){
                log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Change user fail");
            }
        }

        @DeleteMapping("/{userId}")
        public ResponseData<Void> deleteUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") long userId) {
            log.info("Request delete userId={}", userId);
            try {
                userService.deleteUser(userId);
                return new ResponseData<>(HttpStatus.NO_CONTENT.value(), ("user.del.success"));
            }catch (Exception e){
                log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Delete user fail");
            }
        }
        @GetMapping("/{userId}")
        public ResponseData<UserDetailResponse> getUser(@PathVariable @Min(1) Long userId){
            log.info("Request get user detail by orderId = " + userId);
            try {
                UserDetailResponse user = userService.getUser(userId);
                return new ResponseData<>(HttpStatus.OK.value(), "user", user);
            }catch (Exception  e){
                log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            }
        }
        @GetMapping("/list")
        public ResponseData<PageResponse> getALlUsersWithSortBy(
                @RequestParam( defaultValue = "0", required = false ) int pageNo,@Min(10)
                @RequestParam(defaultValue = "10", required = false) int pageSize,
                @RequestParam( required = false) String sortBy
        ) {
            log.info("Request get user list, pageNo={}, pageSize={}", pageNo, pageSize, sortBy);
            try {
                PageResponse<?> users = userService.getALlUsersWithSortBy(pageNo, pageSize, sortBy);
                return new ResponseData<>(HttpStatus.OK.value(), "user",users);
            }catch (Exception e){
                log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
                return new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            }

        }


        @GetMapping("/list-with-sort-by-multiple-columns")
        public ResponseData<?> getAllUsersWithSortByMultipleColumns(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                    @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                    @RequestParam(required = false) String... sorts) {
            log.info("Request get all of users with sort by multiple columns");
            return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUsersWithSortByMultipleColumns(pageNo, pageSize, sorts));
        }

    @GetMapping("/list-user-and-search-with-paging-and-sorting")
    public ResponseData<?> getAllUsersAndSearchWithPagingAndSorting(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                                    @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                                    @RequestParam(required = false) String search,
                                                                    @RequestParam(required = false) String sortBy) {
        log.info("Request get list of users and search with paging and sorting");
        return new ResponseData<>(HttpStatus.OK.value(), "users", userService.getAllUsersAndSearchWithPagingAndSorting(pageNo, pageSize, search, sortBy));
    }

    @GetMapping("")
    public  ResponseData<?> advanceSearchByCriteria(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                    @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                    @RequestParam(required = false) String sortBy,
                                                    @RequestParam(required = false) String address,
                                                    @RequestParam(required = false) String search){
            log.info("Request get list of users and search with paging and sorting by Criteria");
            return new ResponseData<>(HttpStatus.OK.value(), "user", userService.advanceSearchByCriteria(pageNo, pageSize, sortBy, address, search));
    }
}
