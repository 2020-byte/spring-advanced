package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String DIGIT_PATTERN = ".*\\d.*";
    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserResponse(user.getId(), user.getEmail()))
                .orElseThrow(() -> new InvalidRequestException("User not found"));
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        validateNewPassword(request.getNewPassword());
        validatePasswordChange(user, request.getOldPassword(), request.getNewPassword());

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword.length() < MIN_PASSWORD_LENGTH ||
                !newPassword.matches(DIGIT_PATTERN) ||
                !newPassword.matches(UPPERCASE_PATTERN)) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    private void validatePasswordChange(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }
    }
}
