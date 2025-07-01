package com.srr.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Tests for enforcing security conventions
 */
public class SecurityConventionTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    public static void setupAll() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.srr"); // Only import com.srr packages that we can access
    }

    @Test
    public void getMethodsShouldHavePreAuthorizeAnnotation() {
        methods()
                .that().areAnnotatedWith(GetMapping.class)
                .and().areDeclaredInClassesThat().haveNameMatching(".*Controller")
                .should().beAnnotatedWith(PreAuthorize.class)
                .because("GET methods in controllers should have @PreAuthorize annotation")
                .check(importedClasses);
    }

    @Test
    public void postMethodsShouldHavePreAuthorizeAnnotation() {
        methods()
                .that().areAnnotatedWith(PostMapping.class)
                .and().areDeclaredInClassesThat().haveNameMatching(".*Controller")
                .should().beAnnotatedWith(PreAuthorize.class)
                .because("POST methods in controllers should have @PreAuthorize annotation")
                .check(importedClasses);
    }

    @Test
    public void putMethodsShouldHavePreAuthorizeAnnotation() {
        methods()
                .that().areAnnotatedWith(PutMapping.class)
                .and().areDeclaredInClassesThat().haveNameMatching(".*Controller")
                .should().beAnnotatedWith(PreAuthorize.class)
                .because("PUT methods in controllers should have @PreAuthorize annotation")
                .check(importedClasses);
    }

    @Test
    public void deleteMethodsShouldHavePreAuthorizeAnnotation() {
        methods()
                .that().areAnnotatedWith(DeleteMapping.class)
                .and().areDeclaredInClassesThat().haveNameMatching(".*Controller")
                .should().beAnnotatedWith(PreAuthorize.class)
                .because("DELETE methods in controllers should have @PreAuthorize annotation")
                .check(importedClasses);
    }
}
