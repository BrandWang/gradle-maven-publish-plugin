package com.vanniktech.maven.publish

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.assertj.core.api.Java6Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.MavenPlugin
import org.gradle.plugins.signing.SigningPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import java.io.File

class MavenPublishPluginTest {
  @Test fun javaPlugin() {
    val project = ProjectBuilder.builder().build()
    project.plugins.apply(JavaPlugin::class.java)
    assert(project)
  }

  @Test fun javaLibraryPlugin() {
    val project = ProjectBuilder.builder().build()
    project.plugins.apply(JavaLibraryPlugin::class.java)
    assert(project)
  }

  @Test fun javaLibraryPluginWithGroovy() {
    val project = ProjectBuilder.builder().build()
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply(GroovyPlugin::class.java)
    assert(project)

    assertThat(project.tasks.getByName("groovydocJar")).isNotNull()
  }

  @Test fun androidLibraryPlugin() {
    val project = ProjectBuilder.builder().build()
    project.plugins.apply(LibraryPlugin::class.java)

    prepareAndroidLibraryProject(project)

    assert(project)
  }

  @Test fun androidLibraryPluginWithKotlinAndroid() {
    val project = ProjectBuilder.builder().build()
    project.plugins.apply(LibraryPlugin::class.java)
    project.plugins.apply("kotlin-android")

    prepareAndroidLibraryProject(project)

    assert(project)
  }

  @Test fun javaLibraryPluginWithKotlin() {
    val project = ProjectBuilder.builder().withName("single").build() as DefaultProject
    project.plugins.apply(JavaLibraryPlugin::class.java)
    project.plugins.apply("kotlin")
    assert(project)
  }

  private fun prepareAndroidLibraryProject(project: Project) {
    val extension = project.extensions.getByType(LibraryExtension::class.java)
    extension.compileSdkVersion(27)

    val manifestFile = File(project.projectDir, "src/main/AndroidManifest.xml")
    manifestFile.parentFile.mkdirs()
    manifestFile.writeText("""<manifest package="com.foo.bar"/>""")
  }

  // This does not assert anything but it's a good start.
  private fun assert(project: Project) {
    project.plugins.apply(MavenPublishPlugin::class.java)

    val extension = project.extensions.getByType(MavenPublishPluginExtension::class.java)
    extension.repositoryUsername = "bar"
    extension.repositoryPassword = "foo"

    (project as DefaultProject).evaluate()

    assertThat(project.plugins.findPlugin(MavenPlugin::class.java)).isNotNull()
    assertThat(project.plugins.findPlugin(SigningPlugin::class.java)).isNotNull()
    assertThat(project.group).isNotNull()
    assertThat(project.version).isNotNull()

    val task = project.tasks.getByName("uploadArchives")
    assertThat(task.description).isEqualTo("Uploads all artifacts belonging to configuration ':archives'")
    assertThat(task.group).isEqualTo("upload")
  }
}
