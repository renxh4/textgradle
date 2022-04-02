package com.renxh

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency

/**
 * 这个其实就是要解析renxh，debugRenxh releaseRenxh 的数据然后用CompileOnly加上依赖
 */
class EmbedResolutionListener implements DependencyResolutionListener{

    private final Project project

    private final Configuration configuration

    private final String compileOnlyConfigName

    EmbedResolutionListener(Project project, Configuration configuration) {
        this.project = project
        this.configuration = configuration
        String prefix = getConfigNamePrefix(configuration.name)
        WinkLog.d("prefix",prefix)
        if (prefix != null) {
            this.compileOnlyConfigName = prefix + "CompileOnly"
        } else {
            this.compileOnlyConfigName = "compileOnly"
        }
    }

    private String getConfigNamePrefix(String configurationName) {
        if (configurationName.endsWith(FatAarplugin.CONFIG_SUFFIX)) {
            return configurationName.substring(0, configuration.name.length() - FatAarplugin.CONFIG_SUFFIX.length())
        } else {
            return null
        }
    }

    @Override
    void beforeResolve(ResolvableDependencies resolvableDependencies) {
        configuration.dependencies.each { dependency ->
            if (dependency instanceof DefaultProjectDependency) {
                if (dependency.targetConfiguration == null) {
                    dependency.targetConfiguration = "default"
                }
                // support that the module can be indexed in Android Studio 4.0.0
                DefaultProjectDependency dependencyClone = dependency.copy()
                dependencyClone.targetConfiguration = null;
                // The purpose is to support the code hints
                project.dependencies.add(compileOnlyConfigName, dependencyClone)
                WinkLog.d("beforeResolve",compileOnlyConfigName+"=/"+dependency.toString())
            } else {
                // The purpose is to support the code hints
                project.dependencies.add(compileOnlyConfigName, dependency)
                WinkLog.d("beforeResolve",compileOnlyConfigName+"=/"+dependency.toString())
            }
        }
        project.gradle.removeListener(this)
    }

    @Override
    void afterResolve(ResolvableDependencies resolvableDependencies) {

    }
}