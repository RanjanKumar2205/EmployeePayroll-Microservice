package org.example.authservice.services;

import org.example.authservice.config.JwtService;
import org.example.authservice.dtos.AuthResponseDto;
import org.example.authservice.dtos.LoginRequestDto;
import org.example.authservice.dtos.RegisterRequestDto;
import org.example.authservice.entities.User;
import org.example.authservice.exceptions.ResourceNotFoundException;
import org.example.authservice.exceptions.UserAlreadyExistsException;
import org.example.authservice.mappers.AuthMapper;
import org.example.authservice.repositories.UserRepository;
import org.example.authservice.security.UserPrincipal;
import org.example.authservice.utils.GlobalConstantsUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final AuthMapper authMapper;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       AuthenticationManager authManager,
                       UserDetailsServiceImpl userDetailsServiceImpl,
                       AuthMapper authMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.authMapper = authMapper;
    }

    public AuthResponseDto register(RegisterRequestDto req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username already taken: " + req.getUsername());
        }

        User user = authMapper.toEntity(req);

        User savedUser = userRepository.save(user);

        UserPrincipal userPrincipal = userDetailsServiceImpl.loadUserByUsername(user.getUsername());

        Map<String, Object> claims = new HashMap<>();
        claims.put(GlobalConstantsUtil.ROLE_KEY, user.getRole());

        String token = jwtService.generateToken(claims, userPrincipal);

        return authMapper.toResponse(savedUser, token);
    }

    public AuthResponseDto login(LoginRequestDto req) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        UserPrincipal userPrincipal = userDetailsServiceImpl.loadUserByUsername(req.getUsername());
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(!user.getEnabled()) throw new DisabledException("User is disabled");

        Map<String, Object> claims = new HashMap<>();
        claims.put(GlobalConstantsUtil.ROLE_KEY, user.getRole());

        String token = jwtService.generateToken(claims, userPrincipal);

        return authMapper.toResponse(user, token);
    }
}
