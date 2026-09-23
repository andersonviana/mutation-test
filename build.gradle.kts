plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Necessário para o PIT conseguir executar testes JUnit 5.
    pitest("org.pitest:pitest-junit5-plugin:1.2.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// -----------------------------------------------------------------------
// PIT (Pitest) - Mutation Testing configuration
// -----------------------------------------------------------------------
// Mutation testing complementa a cobertura de linha (line coverage):
// enquanto a cobertura de linha só diz "esse código foi executado",
// o PIT modifica (muta) o bytecode de produção e verifica se os testes
// REALMENTE detectam (matam) essas mutações. Se um teste passa mesmo
// com o código alterado, o mutante "sobrevive" - sinal de teste fraco.
pitest {
    // Suporte a "./gradlew pitest -PtargetClass=com.example.pacote.MinhaClasse":
    // durante o desenvolvimento assistido por IA, roda o PIT só para a classe
    // (e teste correspondente) que acabou de ser gerada/alterada, em vez do
    // projeto inteiro — feedback muito mais rápido a cada iteração.
    val targetClassProp = project.findProperty("targetClass") as String?

    // Pacotes de produção que serão mutados pelo PIT
    targetClasses.set(targetClassProp?.let { listOf(it) } ?: setOf("com.example.mutationtest.*"))

    // As implementações InMemory* existem apenas para permitir que o contexto
    // Spring suba fora dos testes (main); nos testes unitários os repositórios
    // são mockados, então essas classes não têm cobertura própria e poluiriam
    // as métricas dos 3 cenários didáticos.
    excludedClasses.set(setOf("com.example.mutationtest.scenario3.complex.InMemory*"))

    // Pacotes de teste que serão executados contra os mutantes
    targetTests.set(
        targetClassProp?.let { listOf("$it*Test", "$it*Tests") }
            ?: setOf("com.example.mutationtest.*")
    )

    // STRONGER: grupo de mutadores mais agressivo do PIT, incluindo
    // mutações de boundary conditions, negation, arithmetic, return values,
    // increments, invert negatives, etc. Ideal para fins didáticos.
    mutators.set(setOf("STRONGER"))

    outputFormats.set(setOf("HTML", "XML"))

    // Facilita o acesso ao relatório sempre no mesmo caminho:
    // build/reports/pitest/index.html
    timestampedReports.set(false)

    // Não falha o build caso algum módulo/pacote não tenha mutações
    // (evita quebrar a demo caso um cenário específico seja rodado isolado)
    failWhenNoMutations.set(false)

    threads.set(4)

    // Análise incremental: o PIT guarda o "histórico" de mutantes já
    // analisados e, nas próximas execuções, só reprocessa o que mudou
    // (classe de produção ou teste). Essencial no loop "IA gera teste ->
    // PIT valida": cada iteração fica muito mais rápida.
    historyInputLocation.set(layout.buildDirectory.file("pitest/history.bin").get().asFile)
    historyOutputLocation.set(layout.buildDirectory.file("pitest/history.bin").get().asFile)

    // Threshold pensado para a POC: mostra números reais sem quebrar a demo.
    mutationThreshold.set(0)
    coverageThreshold.set(0)

    jvmArgs.set(listOf("-Xmx512m"))
    verbose.set(false)
}

// -----------------------------------------------------------------------
// Task customizada: pitestAll
// -----------------------------------------------------------------------
// Roda o pitest garantindo que os 3 cenários (simple, intermediate, complex)
// sejam sempre analisados juntos, e imprime no console o caminho do relatório
tasks.register("pitestAll") {
    group = "verification"
    description = "Roda o PIT (Pitest) para todos os cenários da POC (1 - simples, 2 - intermediário, 3 - complexo) e mostra o caminho do relatório."
    dependsOn("pitest")

    doLast {
        val reportPath = layout.buildDirectory.file("reports/pitest/index.html").get().asFile
        println()
        println("======================================================================")
        println(" Mutation Testing concluído!")
        println(" Relatório HTML: file://${reportPath.absolutePath}")
        println("======================================================================")
    }
}
