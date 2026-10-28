package group2d.promo_graud.modules.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import group2d.promo_graud.modules.user.dto.request.UserRequest;
import group2d.promo_graud.modules.user.dto.request.UserUpdateRequest;
import group2d.promo_graud.modules.user.dto.response.UserResponse;
import group2d.promo_graud.modules.user.service.UserService;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.dto.PaginatedResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .code(201)
                        .message("Thêm người dùng thành công")
                        .result(userResponse)
                        .build());
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) Integer userTypeId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int page) {
        Page<UserResponse> pages = userService.getAllUser(userTypeId, name, size, page);
        PaginatedResponse<UserResponse> result =
                PaginatedResponse.<UserResponse>builder()
                        .data(pages.getContent())
                        .page(pages.getNumber() + 1)
                        .totalItems(pages.getTotalElements())
                        .totalPages(pages.getTotalPages())
                        .build();

        return ResponseEntity.ok(
                ApiResponse.<PaginatedResponse<UserResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách người dùng thành công")
                        .result(result)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAll() {
        List<UserResponse> listUser = userService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<UserResponse>>builder()
                        .code(200)
                        .message("Get user roster success")
                        .result(listUser)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findUserById(@PathVariable Integer id) {
        UserResponse userResponse = userService.findUserById(id);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .code(200)
                        .message("Find user by id success")
                        .result(userResponse)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @RequestBody UserUpdateRequest request, @PathVariable Integer id) {
        UserResponse userResponse = userService.updateUser(request, id);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .code(200)
                        .message("Updated user success")
                        .result(userResponse)
                        .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = ((Number) jwt.getClaim("userId")).intValue();
        UserResponse userResponse = userService.getMyProfile(userId);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .code(200)
                        .message("Lấy thông tin user thành công")
                        .result(userResponse)
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder().code(200).message("Xóa người dùng thành công").build());
    }
}
