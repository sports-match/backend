package com.srr.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Tests for enforcing service layer conventions
 */
public class ServiceLayerTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    public static void setupAll() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.srr"); // Only import com.srr packages that we can access
    }

    @Test
    public void serviceImplsShouldHaveServiceAnnotation() {
        classes()
                .that().haveNameMatching(".*ServiceImpl")
                .should().beAnnotatedWith(Service.class)
                .because("All service implementations should have @Service annotation")
                .check(importedClasses);
    }

    @Test
    public void serviceImplsShouldImplementServiceInterface() {
        classes()
                .that().haveNameMatching(".*ServiceImpl")
                .should().implement(DescribedPredicate.describe(
                        "A service interface", 
                        clazz -> clazz.getSimpleName().endsWith("Service") && clazz.isInterface()))
                .because("Service implementations should implement a service interface")
                .check(importedClasses);
    }

    @Test
    public void deleteMethodsShouldBeTransactional() {
        methods()
                .that().haveNameMatching("delete.*")
                .and().arePublic()
                .and().areDeclaredInClassesThat().haveNameMatching(".*ServiceImpl")
                .should().beAnnotatedWith(Transactional.class)
                .because("Public delete methods in services should be transactional")
                .check(importedClasses);
    }

    @Test
    public void createMethodsShouldBeTransactional() {
        methods()
                .that().haveNameMatching("create.*")
                .and().arePublic()
                .and().areDeclaredInClassesThat().haveNameMatching(".*ServiceImpl")
                .should().beAnnotatedWith(Transactional.class)
                .because("Public create methods in services should be transactional")
                .check(importedClasses);
    }

    @Test
    public void updateMethodsShouldBeTransactional() {
        methods()
                .that().haveNameMatching("update.*")
                .and().arePublic()
                .and().areDeclaredInClassesThat().haveNameMatching(".*ServiceImpl")
                .should().beAnnotatedWith(Transactional.class)
                .because("Public update methods in services should be transactional")
                .check(importedClasses);
    }
}
