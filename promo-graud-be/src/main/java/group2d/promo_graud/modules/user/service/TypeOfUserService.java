package group2d.promo_graud.modules.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import group2d.promo_graud.modules.user.dto.request.TypeOfUserRequest;
import group2d.promo_graud.modules.user.dto.response.TypeOfUserResponse;
import group2d.promo_graud.modules.user.entity.TypeOfUser;
import group2d.promo_graud.modules.user.error.TypeOfUserErrorCode;
import group2d.promo_graud.modules.user.repository.TypeOfUserRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
public class TypeOfUserService {
    private TypeOfUserRepository typeOfUserRepository;

    public TypeOfUserService(TypeOfUserRepository typeOfUserRepository) {
        this.typeOfUserRepository = typeOfUserRepository;
    }

    public TypeOfUserResponse createTypeOfUser(TypeOfUserRequest request) {
        TypeOfUser typeOfUser =
                TypeOfUser.builder()
                        .type(request.getType())
                        .threshold(request.getThreshold())
                        .build();
        return mapToTypeOfUserResponse(typeOfUserRepository.save(typeOfUser));
    }

    public TypeOfUserResponse updateTypeOfUser(TypeOfUserRequest request, Integer id) {
        TypeOfUser typeOfUser =
                typeOfUserRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                TypeOfUserErrorCode.TYPE_OF_USER_NOT_EXISTS));
        typeOfUser.setType(request.getType());
        typeOfUser.setThreshold(request.getThreshold());
        return mapToTypeOfUserResponse(typeOfUserRepository.save(typeOfUser));
    }

    public TypeOfUserResponse findById(Integer id) {
        TypeOfUser typeOfUser =
                typeOfUserRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                TypeOfUserErrorCode.TYPE_OF_USER_NOT_EXISTS));
        return mapToTypeOfUserResponse(typeOfUser);
    }

    public List<TypeOfUserResponse> findAll() {
        return typeOfUserRepository.findAll().stream().map(this::mapToTypeOfUserResponse).toList();
    }

    public TypeOfUserResponse mapToTypeOfUserResponse(TypeOfUser typeOfUser) {
        return TypeOfUserResponse.builder()
                .id(typeOfUser.getId())
                .type(typeOfUser.getType())
                .threshold(typeOfUser.getThreshold())
                .build();
    }
}
