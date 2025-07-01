package com.srr.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * General architecture tests to ensure the codebase follows consistent patterns
 */
public class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    public static void setupAll() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.srr"); // Only import com.srr packages that we can access
    }

    @Test
    public void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        noClasses()
                .that().resideInAnyPackage("..service..", "..repository..")
                .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..rest..")
                .because("Services and repositories should not depend on web layer")
                .check(importedClasses);
    }

    @Test
    public void servicesShouldOnlyBeAccessedByControllersOrOtherServicesOrEventListeners() {
        classes()
                .that().resideInAnyPackage("..service..")
                .should().onlyBeAccessed().byAnyPackage("..service..", "..rest..", "..controller..", "..event.listener..")
                .because("Services should only be accessed by controllers, other services, or event listeners")
                .check(importedClasses);
    }

    @Test
    public void repositoriesShouldOnlyBeAccessedByServices() {
        classes()
                .that().resideInAnyPackage("..repository..")
                .should().onlyBeAccessed().byAnyPackage("..service..", "..repository..")
                .because("Repositories should only be accessed by services")
                .check(importedClasses);
    }

    @Test
    public void layeredArchitectureShouldBeRespected() {
        classes()
                .that().resideInAPackage("..rest..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..rest..",
                        "..service..",
                        "..dto..",
                        "..domain..",
                        "..enumeration..",
                        "java..",
                        "javax..",
                        "org.springframework..",
                        "org.slf4j..",
                        "lombok..",
                        "io.swagger..",
                        "me.zhengjie.utils..",
                        "me.zhengjie.annotation..",
                        "me.zhengjie.base..")
                .because("Controllers should only depend on approved service layers and utility classes")
                .check(importedClasses);
    }
}
