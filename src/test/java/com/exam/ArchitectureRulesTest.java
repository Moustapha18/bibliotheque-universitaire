package com.exam;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Règles complémentaires à Spring Modulith (règles 2 et 3 du sujet, et DTO obligatoires). */
@AnalyzeClasses(packages = "com.exam", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule jpaRelationsStayInsideTheirModule = fields()
            .that().areAnnotatedWith(ManyToOne.class)
            .or().areAnnotatedWith(OneToMany.class)
            .or().areAnnotatedWith(OneToOne.class)
            .or().areAnnotatedWith(ManyToMany.class)
            .should(referenceTypesOfTheirOwnModule());

    @ArchTest
    static final ArchRule apiPackagesNeverDependOnInternals = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..internal..");

    @ArchTest
    static final ArchRule controllersNeverTouchEntities = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class);

    private static ArchCondition<JavaField> referenceTypesOfTheirOwnModule() {
        return new ArchCondition<>("reference only types of the same module") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                String owner = moduleOf(field.getOwner().getPackageName());
                String target = moduleOf(field.getRawType().getPackageName());
                if (!owner.equals(target)) {
                    events.add(SimpleConditionEvent.violated(field, field.getDescription() + " traverse les modules"));
                }
            }
        };
    }

    /** com.exam.<module>.... -> <module> */
    private static String moduleOf(String packageName) {
        String[] parts = packageName.split("\\.");
        return parts.length > 2 ? parts[2] : packageName;
    }
}
