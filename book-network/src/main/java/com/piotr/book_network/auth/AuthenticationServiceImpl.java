package com.piotr.book_network.auth;

import com.piotr.book_network.email.EmailService;
import com.piotr.book_network.email.EmailTemplateName;
import com.piotr.book_network.role.Role;
import com.piotr.book_network.role.RoleRepository;
import com.piotr.book_network.security.JwtService;
import com.piotr.book_network.user.Token;
import com.piotr.book_network.user.TokenRepository;
import com.piotr.book_network.user.User;
import com.piotr.book_network.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    @Override
    public void register(RegistrationRequest request) throws MessagingException {
        Role userRole = getUserRole();
        User user = createUser(request, userRole);
        userRepository.save(user);
        sendValidationEmail(user);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = (User) auth.getPrincipal();
        claims.put("fullName", user.getFullName());
        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Override
    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = validateToken(token);
        enableUser(savedToken.getUser());
        updateToken(savedToken);
    }

    private Token validateToken(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired. A new token has been sent to the same email address");
        }
        return savedToken;
    }

    private void enableUser(User user) {
        var userEntity = userRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
    }

    private void updateToken(Token token) {
        token.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    private Role getUserRole() {
        return roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role not found"));
    }

    private User createUser(RegistrationRequest request, Role userRole) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();
    }

    private void sendValidationEmail(User user) throws MessagingException {
        String token = generateAndSaveActivationToken(user);
        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                token,
                "Account activation"
        );
    }

    private String generateAndSaveActivationToken(User user) {
        String generatedToken = generateActivationToken(6);
        saveActivationToken(user, generatedToken);
        return generatedToken;
    }

    private String generateActivationToken(int length) {
        String characters = "0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            stringBuilder.append(characters.charAt(randomIndex));
        }
        return stringBuilder.toString();
    }

    private void saveActivationToken(User user, String token) {
        Token tokenEntity = Token.builder()
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(tokenEntity);
    }
}