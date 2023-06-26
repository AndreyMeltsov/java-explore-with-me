package ru.practicum.ewmservice.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.user.dto.UserDto;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getUsers(Long[] ids, Pageable pageable) {
        List<UserDto> users;
        if (ids == null) {
            users = userMapper.toUserDto(userRepository.findAll(pageable));
        } else {
            users = userMapper.toUserDto(userRepository.findByIdIn(ids, pageable));
        }
        log.info("Users quantity is: {}", users.size());
        return users;
    }

    @Transactional
    public User createUser(UserDto userDto) {
        User user = User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
        if (userRepository.existsByName(userDto.getName())) {
            throw new ConflictException("User with such name has already existed in DB");
        }
        User actualUser = userRepository.save(user);
        log.info("User is added: {}", actualUser);
        return actualUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("User with id{} was deleted", id);
    }
}

