package com.example.portfolio.repository;

import com.example.portfolio.model.Project;
import com.example.portfolio.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findBySlugReturnsProjectWhenSlugExists() {
        var owner = userRepository.save(buildUser("olga@example.com"));
        var project = projectRepository.save(buildProject("Portfolio API", "portfolio-api", owner));

        var actual = projectRepository.findBySlug(project.getSlug());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(project.getId());
        assertThat(actual.get().getName()).isEqualTo(project.getName());
        assertThat(actual.get().getSlug()).isEqualTo(project.getSlug());
    }

    @Test
    void findBySlugReturnsEmptyWhenSlugDoesNotExist() {
        var actual = projectRepository.findBySlug("missing-slug");

        assertThat(actual).isEmpty();
    }

    @Test
    void findAllByOwnerIdReturnsOnlyOwnersProjects() {
        var firstOwner = buildUser("anna@example.com");
        var secondOwner = buildUser("maria@example.com");

        entityManager.persist(firstOwner);
        entityManager.persist(secondOwner);
        entityManager.flush();

        entityManager.persist(buildProject("CRM", "crm", firstOwner));
        entityManager.persist(buildProject("Landing", "landing", firstOwner));
        entityManager.persist(buildProject("Mobile App", "mobile-app", secondOwner));
        entityManager.flush();
        entityManager.clear();

        List<Project> actual = projectRepository.findAllByOwnerId(firstOwner.getId());

        assertThat(actual)
                .hasSize(2)
                .extracting(Project::getSlug)
                .containsExactlyInAnyOrder("crm", "landing");
    }

    private User buildUser(String email) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .bio("Repository test user")
                .build();
    }

    private Project buildProject(String name, String slug, User owner) {
        return Project.builder()
                .name(name)
                .slug(slug)
                .description("Repository test project")
                .owner(owner)
                .build();
    }
}