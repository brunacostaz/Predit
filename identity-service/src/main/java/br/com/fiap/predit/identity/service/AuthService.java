package br.com.fiap.predit.identity.service;

import br.com.fiap.predit.identity.api.LoginRequest;
import br.com.fiap.predit.identity.api.LoginResponse;
import br.com.fiap.predit.identity.api.UserResponse;
import br.com.fiap.predit.identity.domain.User;
import br.com.fiap.predit.identity.domain.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = repository.findByEmailIgnoreCase(request.email())
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        JwtService.IssuedToken token = jwtService.issue(user);
        return new LoginResponse(token.value(), "Bearer", token.expiresAt(), UserResponse.from(user));
    }
}
