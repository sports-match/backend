package com.srr.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

/**
 * Tests for enforcing entity and repository conventions
 */
public class EntityRepositoryPatternTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    public static void setupAll() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.srr"); // Only import com.srr packages that we can access
    }

    @Test
    public void entitiesShouldBeAnnotatedWithEntity() {
        classes()
                .that().resideInAnyPackage("..domain..", "..entity..")
                .and().areNotEnums()
                .and().areNotInterfaces()
                .and().haveSimpleNameNotEndingWith("Dto")
                .and().haveSimpleNameNotEndingWith("Converter")
                .and().resideOutsideOfPackage("..converter..")
                .should().beAnnotatedWith(Entity.class)
                .because("Domain/entity classes should be annotated with @Entity")
                .check(importedClasses);
    }

    @Test
    public void entitiesShouldHaveTableAnnotation() {
        classes()
                .that().areAnnotatedWith(Entity.class)
                .should().beAnnotatedWith(Table.class)
                .because("Entities should have @Table annotation to specify the table name")
                .check(importedClasses);
    }

    @Test
    public void entitiesShouldHaveIdField() {
        fields()
                .that().areAnnotatedWith(Id.class)
                .should().beDeclaredInClassesThat().areAnnotatedWith(Entity.class)
                .because("@Id fields should be declared in @Entity classes")
                .check(importedClasses);
    }

    @Test
    public void repositoriesShouldExtendJpaRepository() {
        classes()
                .that().haveNameMatching(".*Repository")
                .and().areInterfaces()
                .should().beAssignableTo(JpaRepository.class)
                .because("Repository interfaces should extend JpaRepository")
                .check(importedClasses);
    }

    @Test
    public void repositoriesShouldExtendJpaSpecificationExecutor() {
        classes()
                .that().haveNameMatching(".*Repository")
                .and().areInterfaces()
                .should().beAssignableTo(JpaSpecificationExecutor.class)
                .because("Repository interfaces should extend JpaSpecificationExecutor for dynamic querying")
                .check(importedClasses);
    }

    @Test
    public void repositoriesShouldEndWithRepositoryName() {
        classes()
                .that().areAssignableTo(JpaRepository.class)
                .should().haveNameMatching(".*Repository")
                .because("Repository interfaces should have names ending with 'Repository'")
                .check(importedClasses);
    }

    @Test
    public void dtosShouldFollowNamingConvention() {
        classes()
                .that().haveSimpleNameEndingWith("Dto")
                .should().resideInAnyPackage("..dto..")
                .because("DTO classes should be placed in dto packages")
                .check(importedClasses);
    }
}
