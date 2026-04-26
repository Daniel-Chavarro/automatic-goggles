package org.java_avanzado.taller.config;

import lombok.RequiredArgsConstructor;
import org.java_avanzado.taller.domain.model.User;
import org.java_avanzado.taller.domain.model.enums.UserRole;
import org.java_avanzado.taller.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartAminUserData implements ApplicationRunner {

    private final UserService userService;

    @Value("${app.bootstrap.first-name}")
    private String firstName;

    @Value("${app.bootstrap.last-name}")
    private String lastName;

    @Value("${app.bootstrap.email}")
    private String email;

    @Value("${app.bootstrap.password}")
    private String password;

    @Value("${app.bootstrap.phone}")
    private String phone;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .phone(phone)
                .role(UserRole.ADMIN)
                .build();

        userService.createUser(user);
    }
}
