package com.crearcode.leads;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

	private static final String DOMINIO = "com.crearcode.leads.dominio..";
	private static final String APLICACION = "com.crearcode.leads.aplicacion..";
	private static final String INFRAESTRUCTURA = "com.crearcode.leads.infraestructura..";

	private final JavaClasses clases = new ClassFileImporter()
			.importPackages("com.crearcode.leads");

	@Test
	void elDominioNoDependeDeSpringNiDeJpa() {
		ArchRule regla = noClasses()
				.that().resideInAPackage(DOMINIO)
				.should().dependOnClassesThat()
				.resideInAnyPackage("org.springframework..", "jakarta.persistence..");

		regla.check(clases);
	}

	@Test
	void elDominioNoDependeDeInfraestructura() {
		ArchRule regla = noClasses()
				.that().resideInAPackage(DOMINIO)
				.should().dependOnClassesThat().resideInAPackage(INFRAESTRUCTURA);

		regla.check(clases);
	}

	@Test
	void laAplicacionNoDependeDeInfraestructura() {
		ArchRule regla = noClasses()
				.that().resideInAPackage(APLICACION)
				.should().dependOnClassesThat().resideInAPackage(INFRAESTRUCTURA);

		regla.check(clases);
	}

}
