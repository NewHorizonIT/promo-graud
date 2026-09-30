package group2d.promo_graud.modules.user;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import group2d.promo_graud.modules.user.dto.request.UserRequest;
import group2d.promo_graud.modules.user.dto.response.UserResponse;
import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.modules.user.repository.TypeOfUserRepository;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.service.UserService;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private TypeOfUserRepository typeOfUserRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    @DisplayName("TC1: Tạo User thành công")
    void createUserSuccess() {
        UserRequest request =
                UserRequest.builder()
                        .username("huy")
                        .email("huy@gmail.com")
                        .password("123456")
                        .typeOfUser(1)
                        .role(RoleEnum.USER)
                        .build();

        TypeOfUser typeOfUser =
                TypeOfUser.builder()
                        .id(1)
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(3500000))
                        .build();

        User savedUser =
                User.builder()
                        .id(1)
                        .username("huy")
                        .email("huy@gmail.com")
                        .password("encodedPassword")
                        .typeOfUser(typeOfUser)
                        .role(RoleEnum.USER)
                        .build();

        when(typeOfUserRepository.findById(1)).thenReturn(Optional.of(typeOfUser));
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("huy", response.getUsername());
        assertEquals("huy@gmail.com", response.getEmail());
        assertEquals("encodedPassword", response.getPassword());
        assertEquals(RoleEnum.USER, response.getRole());
        assertEquals(typeOfUser, response.getTypeOfUser());

        verify(typeOfUserRepository).findById(1);
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("TC2: Tạo User khi TypeOfUser không tồn tại")
    void createUserTypeOfUserNotFound() {
        UserRequest request =
                UserRequest.builder()
                        .username("huy")
                        .email("huy@gmail.com")
                        .password("123456")
                        .typeOfUser(999)
                        .role(RoleEnum.USER)
                        .build();

        when(typeOfUserRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> userService.createUser(request));
        verify(typeOfUserRepository).findById(999);
    }

    @Test
    @DisplayName("TC3: Cập nhật User thành công")
    void updateUserSuccess() {
        Integer id = 1;
        UserRequest request =
                UserRequest.builder()
                        .username("huy123")
                        .email("new@gmail.com")
                        .password("newPassword")
                        .role(RoleEnum.USER)
                        .build();

        User existingUser =
                User.builder()
                        .id(id)
                        .username("huy")
                        .email("old@gmail.com")
                        .password("oldPassword")
                        .role(RoleEnum.USER)
                        .build();

        User updatedUser =
                User.builder()
                        .id(id)
                        .username("huy123")
                        .email("new@gmail.com")
                        .password("encodedNewPassword")
                        .role(RoleEnum.USER)
                        .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse response = userService.updateUser(request, id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("huy123", response.getUsername());
        assertEquals("new@gmail.com", response.getEmail());
        assertEquals("encodedNewPassword", response.getPassword());
        assertEquals(RoleEnum.USER, response.getRole());

        verify(userRepository).findById(id);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("TC4: Cập nhật User không tồn tại")
    void updateUserNotFound() {
        Integer id = 999;
        UserRequest request =
                UserRequest.builder()
                        .username("huy")
                        .email("huy@gmail.com")
                        .password("123456")
                        .role(RoleEnum.USER)
                        .build();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> userService.updateUser(request, id));
        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("TC5: Lấy tất cả User thành công")
    void findAllSuccess() {
        User user1 =
                User.builder()
                        .id(1)
                        .username("huy")
                        .email("huy@gmail.com")
                        .role(RoleEnum.USER)
                        .build();

        User user2 =
                User.builder()
                        .id(2)
                        .username("admin")
                        .email("admin@gmail.com")
                        .role(RoleEnum.ADMIN)
                        .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> responses = userService.findAll();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("huy", responses.get(0).getUsername());
        assertEquals("admin", responses.get(1).getUsername());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("TC6: Lấy tất cả User khi danh sách rỗng")
    void findAllEmpty() {

        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> responses = userService.findAll();

        assertNotNull(responses);
        assertEquals(0, responses.size());

        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("TC7: Tìm User theo ID thành công")
    void findUserByIdSuccess() {
        Integer id = 1;
        User user =
                User.builder()
                        .id(id)
                        .username("huy")
                        .email("huy@gmail.com")
                        .role(RoleEnum.USER)
                        .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = userService.findUserById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals("huy", response.getUsername());
        assertEquals("huy@gmail.com", response.getEmail());
        assertEquals(RoleEnum.USER, response.getRole());

        verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("TC8: Tìm User theo ID không tồn tại")
    void findUserByIdNotFound() {
        Integer id = 999;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> userService.findUserById(id));
        verify(userRepository).findById(id);
    }
}
