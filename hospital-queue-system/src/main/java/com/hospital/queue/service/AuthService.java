package com.hospital.queue.service;

import com.hospital.queue.dto.AuthDto;
import com.hospital.queue.entity.User;
import com.hospital.queue.enums.Role;
import com.hospital.queue.exception.BadRequestException;
import com.hospital.queue.repository.UserRepository;
import com.hospital.queue.security.JwtUtils;
import com.hospital.queue.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number is already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_PATIENT)
                .address(request.getAddress())
                .age(request.getAge())
                .bloodGroup(request.getBloodGroup())
                .enabled(true)
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return buildAuthResponse(authentication);
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return buildAuthResponse(authentication);
    }

    public AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtils.validateJwtToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }
        String email = jwtUtils.getUserNameFromJwtToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String newAccessToken = jwtUtils.generateRefreshToken(email);

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(token);
        response.setUserId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    private AuthDto.AuthResponse buildAuthResponse(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getEmail());

        User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow();

        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setAccessToken(jwt);
        response.setRefreshToken(refreshToken);
        response.setUserId(userDetails.getId());
        response.setFirstName(userDetails.getFirstName());
        response.setLastName(userDetails.getLastName());
        response.setEmail(userDetails.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}
