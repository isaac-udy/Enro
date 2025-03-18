import org.gradle.api.Project
import java.io.FileInputStream
import java.util.*


val Project.enroVersionName: String get() {
    val versionPropertiesFile = rootProject.file("version.properties")
    val versionProperties = Properties()
    versionProperties.load(FileInputStream(versionPropertiesFile))
    return versionProperties.getProperty("versionName")
}