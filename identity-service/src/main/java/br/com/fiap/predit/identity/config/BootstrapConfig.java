package br.com.fiap.predit.identity.config;

import br.com.fiap.predit.identity.domain.Role;
import br.com.fiap.predit.identity.domain.User;
import br.com.fiap.predit.identity.domain.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapConfig implements ApplicationRunner {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public BootstrapConfig(UserRepository repository, PasswordEncoder encoder,
                           @Value("${predit.bootstrap.admin-email}") String adminEmail,
                           @Value("${predit.bootstrap.admin-password}") String adminPassword) {
        this.repository = repository;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repository.existsByEmailIgnoreCase(adminEmail)) {
            repository.save(new User("Predit Administrator", adminEmail, encoder.encode(adminPassword), Role.ADMIN));
        }
    }
}
