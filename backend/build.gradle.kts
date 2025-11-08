plugins {
    id("java")
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.5"
}
group = "com.example"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test { useJUnitPlatform() }

springBoot {
    mainClass.set("com.example.ainote.AinoteApplication")
}

// 모든 Java 컴파일을 UTF-8로 강제
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// (선택) 테스트 런타임도 UTF-8 강제
tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

sourceSets {
    named("main") {
        java.setSrcDirs(listOf("src/main/java"))
        resources.setSrcDirs(listOf("src/main/resources"))
    }
}