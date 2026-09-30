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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import group2d.promo_graud.modules.user.dto.request.TypeOfUserRequest;
import group2d.promo_graud.modules.user.dto.response.TypeOfUserResponse;
import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.enums.UserTypeEnum;
import group2d.promo_graud.modules.user.repository.TypeOfUserRepository;
import group2d.promo_graud.modules.user.service.TypeOfUserService;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
public class TypeOfUserServiceTest {

    @Mock private TypeOfUserRepository typeOfUserRepository;

    @InjectMocks private TypeOfUserService typeOfUserService;

    @Test
    @DisplayName("TC1: Thêm loại người dùng thành công")
    public void createTypeOfUserSuccess() {
        TypeOfUserRequest request =
                TypeOfUserRequest.builder()
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(3500000))
                        .build();

        TypeOfUser savedTypeOfUser =
                TypeOfUser.builder()
                        .id(1)
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(3500000))
                        .build();
        when(typeOfUserRepository.save(any(TypeOfUser.class))).thenReturn(savedTypeOfUser);
        TypeOfUserResponse response = typeOfUserService.createTypeOfUser(request);
        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(UserTypeEnum.GOLD, response.getType());
        assertEquals(BigDecimal.valueOf(3500000), response.getThreshold());
        verify(typeOfUserRepository).save(any(TypeOfUser.class));
    }

    @Test
    @DisplayName("TC2: Tìm loại người dùng theo ID thành công")
    public void findByIdSuccess() {
        Integer id = 1;
        TypeOfUser typeOfUser =
                TypeOfUser.builder()
                        .id(id)
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(3500000))
                        .build();
        when(typeOfUserRepository.findById(id)).thenReturn(Optional.of(typeOfUser));
        TypeOfUserResponse response = typeOfUserService.findById(id);
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(UserTypeEnum.GOLD, response.getType());
        assertEquals(BigDecimal.valueOf(3500000), response.getThreshold());
        verify(typeOfUserRepository).findById(id);
    }

    @Test
    @DisplayName("TC3: Tìm loại người dùng không tồn tại")
    public void findByIdNotFound() {
        Integer id = 999;
        when(typeOfUserRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> typeOfUserService.findById(id));
        verify(typeOfUserRepository).findById(id);
    }

    @Test
    @DisplayName("TC4: Lấy tất cả loại người dùng thành công")
    public void findAllSuccess() {
        TypeOfUser typeOfUser1 =
                TypeOfUser.builder()
                        .id(1)
                        .type(UserTypeEnum.NORMAL)
                        .threshold(BigDecimal.valueOf(0))
                        .build();

        TypeOfUser typeOfUser2 =
                TypeOfUser.builder()
                        .id(2)
                        .type(UserTypeEnum.SILVER)
                        .threshold(BigDecimal.valueOf(2000000))
                        .build();

        TypeOfUser typeOfUser3 =
                TypeOfUser.builder()
                        .id(3)
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(3500000))
                        .build();

        when(typeOfUserRepository.findAll())
                .thenReturn(List.of(typeOfUser1, typeOfUser2, typeOfUser3));
        List<TypeOfUserResponse> responses = typeOfUserService.findAll();
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals(UserTypeEnum.NORMAL, responses.get(0).getType());
        assertEquals(UserTypeEnum.SILVER, responses.get(1).getType());
        assertEquals(UserTypeEnum.GOLD, responses.get(2).getType());
        verify(typeOfUserRepository).findAll();
    }

    @Test
    @DisplayName("TC5:Lấy tất cả loại người dùng khi danh sách rỗng")
    public void findAllEmpty() {
        when(typeOfUserRepository.findAll()).thenReturn(List.of());
        List<TypeOfUserResponse> responses = typeOfUserService.findAll();
        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(typeOfUserRepository).findAll();
    }

    @Test
    @DisplayName("TC6: Cập nhật loại người dùng thành công")
    public void updateTypeOfUserSuccess() {
        Integer id = 1;
        TypeOfUserRequest request =
                TypeOfUserRequest.builder()
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(5000000))
                        .build();

        TypeOfUser existingTypeOfUser =
                TypeOfUser.builder()
                        .id(id)
                        .type(UserTypeEnum.SILVER)
                        .threshold(BigDecimal.valueOf(2000000))
                        .build();

        TypeOfUser updatedTypeOfUser =
                TypeOfUser.builder()
                        .id(id)
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(5000000))
                        .build();

        when(typeOfUserRepository.findById(id)).thenReturn(Optional.of(existingTypeOfUser));
        when(typeOfUserRepository.save(any(TypeOfUser.class))).thenReturn(updatedTypeOfUser);

        TypeOfUserResponse response = typeOfUserService.updateTypeOfUser(request, id);
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(UserTypeEnum.GOLD, response.getType());
        assertEquals(BigDecimal.valueOf(5000000), response.getThreshold());
        verify(typeOfUserRepository).findById(id);
        verify(typeOfUserRepository).save(any(TypeOfUser.class));
    }

    @Test
    @DisplayName("TC7: Cập nhật loại người dùng không tồn tại")
    public void updateTypeOfUserNotFound() {
        Integer id = 999;
        TypeOfUserRequest request =
                TypeOfUserRequest.builder()
                        .type(UserTypeEnum.GOLD)
                        .threshold(BigDecimal.valueOf(5000000))
                        .build();
        when(typeOfUserRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> typeOfUserService.updateTypeOfUser(request, id));
        verify(typeOfUserRepository).findById(id);
    }
}
