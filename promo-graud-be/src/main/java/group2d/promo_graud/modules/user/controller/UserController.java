package group2d.promo_graud.modules.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import group2d.promo_graud.modules.user.dto.request.UserRequest;
import group2d.promo_graud.modules.user.dto.response.UserResponse;
import group2d.promo_graud.modules.user.service.UserService;
import group2d.promo_graud.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/users")
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
            @RequestBody UserRequest request, @PathVariable Integer id) {
        UserResponse userResponse = userService.updateUser(request, id);
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .code(200)
                        .message("Updated user success")
                        .result(userResponse)
                        .build());
    }
}
