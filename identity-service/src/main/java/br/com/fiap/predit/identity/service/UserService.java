package br.com.fiap.predit.identity.service;

import br.com.fiap.predit.identity.api.CreateUserRequest;
import br.com.fiap.predit.identity.api.UserResponse;
import br.com.fiap.predit.identity.domain.User;
import br.com.fiap.predit.identity.domain.UserRepository;
import br.com.fiap.predit.identity.error.ConflictException;
import br.com.fiap.predit.identity.error.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(request.name(), request.email(), encoder.encode(request.password()), request.role());
        return UserResponse.from(repository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse find(UUID id) {
        return repository.findById(id).map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repository.findAll().stream().map(UserResponse::from).toList();
    }
}
