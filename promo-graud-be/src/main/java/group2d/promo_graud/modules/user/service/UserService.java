package group2d.promo_graud.modules.user.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.dto.request.UserRequest;
import group2d.promo_graud.modules.user.dto.response.UserResponse;
import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.error.TypeOfUserErrorCode;
import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.repository.TypeOfUserRepository;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TypeOfUserRepository typeOfUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserRequest request) {
        TypeOfUser typeOfUser =
                typeOfUserRepository
                        .findById(request.getTypeOfUser())
                        .orElseThrow(
                                () -> new AppException(TypeOfUserErrorCode.TYPE_OF_USER_EXISTS));
        User user =
                User.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .typeOfUser(typeOfUser)
                        .role(request.getRole())
                        .build();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return mapToUserResponse(userRepository.save(user));
    }

    public UserResponse updateUser(UserRequest request, Integer id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        return mapToUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::mapToUserResponse).toList();
    }

    @PostAuthorize("authentication.getName() == returnObject.username")
    public UserResponse findUserById(Integer id) {
        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        return mapToUserResponse(user);
    }

    @Cacheable(value = "users", key = "'username:' + #username")
    public User findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .isDelete(user.getIsDeleted())
                .typeOfUser(user.getTypeOfUser())
                .createdAt(user.getCreatedAt())
                .updateAt(user.getUpdatedAt())
                .build();
    }
}
