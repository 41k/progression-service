package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "root")
public class ArchitectureTest {

	@ArchTest
	static final ArchRule application_layer_should_not_depend_on_infrastructure_layer =
			noClasses().that().resideInAPackage("..application..")
					.should().dependOnClassesThat().resideInAPackage("..infrastructure..");
}
