package group2d.promo_graud.modules.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import group2d.promo_graud.modules.user.dto.request.TypeOfUserRequest;
import group2d.promo_graud.modules.user.dto.response.TypeOfUserResponse;
import group2d.promo_graud.modules.user.service.TypeOfUserService;
import group2d.promo_graud.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/type-of-users")
public class TypeOfUserController {
    private TypeOfUserService typeOfUserService;

    public TypeOfUserController(TypeOfUserService typeOfUserService) {
        this.typeOfUserService = typeOfUserService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TypeOfUserResponse>> createTypeOfUser(
            @Valid @RequestBody TypeOfUserRequest request) {
        TypeOfUserResponse typeOfUserResponse = typeOfUserService.createTypeOfUser(request);
        return ResponseEntity.ok(
                ApiResponse.<TypeOfUserResponse>builder()
                        .code(201)
                        .message("Create type-of-user success")
                        .result(typeOfUserResponse)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TypeOfUserResponse>>> findAll() {
        List<TypeOfUserResponse> listTypeOfUser = typeOfUserService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<TypeOfUserResponse>>builder()
                        .code(200)
                        .message("Create type-of-user success")
                        .result(listTypeOfUser)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TypeOfUserResponse>> findById(@PathVariable Integer id) {
        TypeOfUserResponse typeOfUserResponse = typeOfUserService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<TypeOfUserResponse>builder()
                        .code(200)
                        .message("Find type-of-user by id success")
                        .result(typeOfUserResponse)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TypeOfUserResponse>> updateTypeOfUser(
            @Valid @RequestBody TypeOfUserRequest request, @PathVariable Integer id) {
        TypeOfUserResponse typeOfUserResponse = typeOfUserService.updateTypeOfUser(request, id);
        return ResponseEntity.ok(
                ApiResponse.<TypeOfUserResponse>builder()
                        .code(200)
                        .message("Update type-of-user success")
                        .result(typeOfUserResponse)
                        .build());
    }
}
