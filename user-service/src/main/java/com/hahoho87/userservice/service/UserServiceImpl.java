package com.hahoho87.userservice.service;

import com.hahoho87.userservice.dto.UserDto;
import com.hahoho87.userservice.entity.UserEntity;
import com.hahoho87.userservice.exception.UserNotFoundException;
import com.hahoho87.userservice.repository.UserRepository;
import com.hahoho87.userservice.vo.OrderResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper mapper;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);

        encodePassword(userDto, userEntity);

        userRepository.save(userEntity);
        return null;
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(u -> mapper.map(u, UserDto.class)).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        Optional<UserEntity> byUserId = userRepository.findByUserId(userId);
        UserEntity userEntity = byUserId.orElseThrow(() -> new UserNotFoundException("Can not find User by UserId : " + userId));

        UserDto userDto = mapper.map(userEntity, UserDto.class);
        List<OrderResponse> orderResponses = new ArrayList<>();
        userDto.setOrderResponses(orderResponses);

        return userDto;
    }

    private void encodePassword(UserDto userDto, UserEntity userEntity) {
        String encodedPassword = passwordEncoder.encode(userDto.getPwd());
        userEntity.setEncryptedPwd(encodedPassword);
    }
}
