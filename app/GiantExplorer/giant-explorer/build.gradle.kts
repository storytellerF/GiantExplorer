import com.android.build.api.dsl.VariantDimension
import com.storyteller_f.version_manager.Versions
import com.storyteller_f.version_manager.baseApp
import com.storyteller_f.version_manager.constraintCommonUIListVersion
import com.storyteller_f.version_manager.fileSystemDependency
import com.storyteller_f.version_manager.implModule
import com.storyteller_f.version_manager.networkDependency
import com.storyteller_f.version_manager.setupDataBinding
import com.storyteller_f.version_manager.setupGeneric
import com.storyteller_f.version_manager.setupPreviewFeature
import com.storyteller_f.version_manager.workerDependency

val versionManager: String by project

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.storyteller_f.version_manager")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
//    id("com.starter.easylauncher") version "6.2.0"
    id("androidx.room") version "2.6.1"
}

android {

    val id = "com.storyteller_f.giant_explorer"
    defaultConfig {
        applicationId = id
    }

    namespace = "com.storyteller_f.giant_explorer"
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            val suffix = ".$name"
            registerProviderKey("file-provider", id, suffix)
            registerProviderKey("file-system-provider", id, suffix)
            registerProviderKey("file-system-encrypted-provider", id, suffix)
        }
        release {
            registerProviderKey("file-provider", id)
            registerProviderKey("file-system-provider", id)
            registerProviderKey("file-system-encrypted-provider", id)
        }
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {

    implementation("androidx.constraintlayout:constraintlayout:${Versions.CONSTRAINTLAYOUT}")
    fileSystemDependency()
    networkDependency()
    workerDependency()

    handleSu()
    handleShun()
    implementation(project(":giant-explorer-plugin-core"))

    implementation("com.j256.simplemagic:simplemagic:1.17")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.webkit:webkit:1.10.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.window:window:1.2.0")

    androidTestImplementation("androidx.room:room-testing:2.6.1")

    val liPluginModule = findProject(":li-plugin")
    if (liPluginModule != null) {
        implementation(liPluginModule)
    }
    implementation("com.github.tony19:logback-android:3.0.0")
}

baseApp()
implModule(":slim-ktx")
constraintCommonUIListVersion(versionManager)
fileSystemDependency()
setupGeneric()
setupDataBinding()
setupPreviewFeature()

fun DependencyHandlerScope.handleShun() {
    //filter & sort
    val filterArtifact = listOf("config-core", "sort-core", "filter-core", "filter-ui", "sort-ui")

    val filterModules = filterArtifact.mapNotNull {
        findProject(":filter:$it")
    }
    if (filterModules.size == filterArtifact.size) {
        filterModules.forEach {
            implementation(it)
        }
    } else {
        filterArtifact.forEach {
            implementation("com.github.storytellerF.Shun:$it:1.0.0")
        }
    }
}

fun DependencyHandlerScope.handleSu() {
    val libsuVersion = "5.0.3"

    // The core module that provides APIs to a shell
    implementation("com.github.topjohnwu.libsu:core:${libsuVersion}")

    // Optional: APIs for creating root services. Depends on ":core"
    implementation("com.github.topjohnwu.libsu:service:${libsuVersion}")

    // Optional: Provides remote file system support
    implementation("com.github.topjohnwu.libsu:nio:${libsuVersion}")
}

/**
 * 同时设置manifest和buildConfig
 */
fun VariantDimension.registerProviderKey(
    identification: String,
    applicationId: String?,
    valueSuffix: String? = null
) {
    val placeholderKey = placeholderKey(identification)
    val buildConfigKey = buildConfigKey(placeholderKey)
    val authorityValue =
        "${applicationId}.$identification${if (valueSuffix == null) "" else "$valueSuffix"}"

    // Now we can use ${documentsAuthority} in our Manifest
    manifestPlaceholders[placeholderKey] = authorityValue
    // Now we can use BuildConfig.DOCUMENTS_AUTHORITY in our code
    buildConfigField(
        "String",
        buildConfigKey.toString(),
        "\"${authorityValue}\""
    )
}

fun buildConfigKey(placeholderKey: String): StringBuilder {
    val configKey = StringBuilder()
    placeholderKey.forEachIndexed { index, c ->
        configKey.append(
            when {
                index == 0 -> c.uppercase()
                c.isUpperCase() && placeholderKey[index - 1].isLowerCase() -> "_$c"
                else -> c.uppercase()
            }
        )
    }
    return configKey
}

fun placeholderKey(identification: String): String {
    val identifyString = StringBuilder()
    var i = 0
    while (i < identification.length) {
        val c = identification[i++]
        if (c == '-') {
            identifyString.append(identification[i++].uppercase())
        } else {
            identifyString.append(c)
        }
    }
    return identifyString.append("Authority").toString()
}

room {
    schemaDirectory("$projectDir/schemas")
}