plugins {
    java
    distribution
}

val archivaUserName: String by project
val archivaPassword: String by project

group = "io.sharptree"
version = "1.0.2"

val vendor = "Sharptree"
val product = "autoscript-dbc"
val distro = "autoscript-dbc"

project.version = "1.0.2"

tasks.compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

repositories {
    mavenCentral()
}

distributions {

    val distribution by configurations.creating {
        extendsFrom(configurations.implementation.get())
        isCanBeResolved = true
    }

    main {
        contents {
            into("applications/maximo/lib") {
                from("$buildDir/libs/${product.toLowerCase()}.jar")
            }

            into("tools/maximo/classes") {
                includeEmptyDirs = false
                from(layout.buildDirectory.dir("classes/java/main")) {
                    include("**/*.class")
                }
            }
        }
    }
}

// Configure the distribution task to tar and gzip the results.
tasks.distTar {
    compression = Compression.GZIP
    archiveExtension.set("tar.gz")
}

tasks.assembleDist {
    finalizedBy("fixzip")

}

tasks.register("fixzip") {
    dependsOn("rezip", "retar", "releaseZip","releaseTar")

    doLast {
        delete(layout.buildDirectory.asFile.get().path + File.separator + "distributions" + File.separator + "tmp")
        delete(layout.buildDirectory.asFile.get().path + File.separator + "distributions" + File.separator + "tmp2")
    }
}

tasks.register("unzip") {
    dependsOn("assembleDist")
    val archiveBaseName = project.name + "-" + project.version
    val distDir = layout.buildDirectory.asFile.get().path + File.separator + "distributions"

    doLast {
        copy {
            from(zipTree(tasks.distZip.get().archiveFile.get().asFile))
            into(distDir + File.separator + "tmp")
        }
    }
}

tasks.register<Zip>("rezip") {
    dependsOn("unzip")
    val archiveBaseName = project.name + "-" + project.version
    val distDir = layout.buildDirectory.asFile.get().path + File.separator + "distributions"
    val baseDir = File(distDir + File.separator + "tmp" + File.separator + archiveBaseName)

    archiveFileName.set("$archiveBaseName.zip")

    from(baseDir) {
        exclude("**/*.jar", "**/lib")
        into("/")
    }
}

tasks.register<Tar>("retar") {
    dependsOn("unzip")
    val archiveBaseName = project.name + "-" + project.version
    val distDir = layout.buildDirectory.asFile.get().path + File.separator + "distributions"
    val baseDir = File(distDir + File.separator + "tmp" + File.separator + archiveBaseName)

    compression = Compression.GZIP
    archiveExtension.set("tar.gz")

    from(baseDir) {
        exclude("**/*.jar", "**/lib")
        into("/")
    }
}

tasks.jar {
    archiveFileName.set("${product.toLowerCase()}.jar")
}

tasks.getByName("distTar").dependsOn("jar")
tasks.getByName("distZip").dependsOn("jar")

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to product,
                "Created-By" to vendor,
                "Implementation-Version" to project.version
            )
        )
    }
    archiveBaseName.set(product.toLowerCase())
}

tasks.register<Zip>("releaseZip") {
    dependsOn("unzip")
    val archiveBaseName = distro.toLowerCase() + "-" + project.version
    val distDir = layout.buildDirectory.asFile.get().path + File.separator + "distributions"
    val baseDir = File(distDir + File.separator + "tmp" + File.separator + archiveBaseName)

    archiveFileName.set("$archiveBaseName-release.zip")

    from(baseDir) {
        exclude("tmp/")
        exclude("applications/")
        exclude("tools/maximo/en/")
        into("/")
    }
}

tasks.register<Tar>("releaseTar") {
    dependsOn("unzip")

    val archiveBaseName = distro.toLowerCase() + "-" + project.version
    val distDir = layout.buildDirectory.asFile.get().path + File.separator + "distributions"
    val baseDir = File(distDir + File.separator + "tmp" + File.separator + archiveBaseName)

    compression = Compression.GZIP
    archiveFileName.set("$archiveBaseName-release.tar.gz")
    archiveExtension.set("tar.gz")

    from(baseDir) {
        exclude("tmp/")
        into("/")
    }
}


dependencies {

    /*
     * The javax.servlet-api is required to compile DataBean classes, but is otherwise provided by WebSphere / WebLogic.
     */
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    /*
     * The gson package is used to parse the scriptConfig JSON.
     */
    @Suppress("GradlePackageUpdate") // We are matching what Maximo uses
    implementation("com.google.code.gson:gson:2.2.4")

    /**
     * Maximo's libraries needed for compiling the application.
     *
     * asset-management - the businessobjects.jar
     * webclient - classes from the maximouiweb/WEB-INF/classes folder
     * tools - classes from the [SMP_HOME]/maximo/tools/maximo/classes folder
     *
     */
    compileOnly(fileTree("libs") { listOf("*.jar") })

    @Suppress("GradlePackageUpdate")
    compileOnly("org.jdom:jdom:2.0.2")
    @Suppress("GradlePackageUpdate")
    compileOnly("log4j:log4j:1.2.16")

}