package com.renxh

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency;

class FatAarplugin implements Plugin< Project> {

    private Project project

    public static final String CONFIG_NAME = "renxh";
    public static final String CONFIG_SUFFIX = "Renxh";

    public static final String ARTIFACT_TYPE_AAR = "aar";

    public static final String ARTIFACT_TYPE_JAR = "jar";
    private final Collection<Configuration> embedConfigurations = new ArrayList<>();

    private RClassesTransform transform


    @Override
    void apply(Project project) {
        this.project = project;
        project.task("fataar").doLast {
            print("aaa")
        }

        LibraryExtension android = (LibraryExtension) project.getExtensions().getByName("android")
        //创建配置项 renxh
        createConfigurations()
        //添加transform
//        registerTransform()

        project.afterEvaluate {
            doAfterEvaluate()
        }

    }


    private registerTransform() {
        transform = new RClassesTransform(project)
        // register in project.afterEvaluate is invalid.
        project.android.registerTransform(transform)
    }

    /**
     * 创建配置项 renxh
     */
    private void createConfigurations() {
        //创建配置项 renxh
        Configuration embedConf = project.configurations.create(CONFIG_NAME)
        createConfiguration(embedConf)
        WinkLog.d("Creating configuration renxh")

        //创建配置项 debugRenxh releaseRenxh
        project.android.buildTypes.all { buildType ->
            String configName = buildType.name + CONFIG_SUFFIX
            Configuration configuration = project.configurations.create(configName)
            createConfiguration(configuration)
            WinkLog.d(" buildTypes Creating configuration " + configName)
        }

        project.android.productFlavors.all { flavor ->
            String configName = flavor.name + CONFIG_SUFFIX
            Configuration configuration = project.configurations.create(configName)
            createConfiguration(configuration)
            WinkLog.d("productFlavors Creating configuration " + configName)
            project.android.buildTypes.all { buildType ->
                String variantName = flavor.name + buildType.name.capitalize()
                String variantConfigName = variantName + CONFIG_SUFFIX
                Configuration variantConfiguration = project.configurations.create(variantConfigName)
                createConfiguration(variantConfiguration)
                WinkLog.d("productFlavors Creating configuration " + variantConfigName)
            }
        }
    }

    private void createConfiguration(Configuration embedConf) {
        embedConf.visible = false
        embedConf.transitive = false
        project.gradle.addListener(new EmbedResolutionListener(project, embedConf))
        embedConfigurations.add(embedConf)
    }

    private void doAfterEvaluate() {
//        embedConfigurations.each {
//            if (project.fataar.transitive) {
//                it.transitive = true
//            }
//        }
        //也是debug和release
        project.android.libraryVariants.all { LibraryVariant variant ->
            WinkLog.d("LibraryVariant getBuildType",variant.getBuildType().name + CONFIG_SUFFIX)
            WinkLog.d("LibraryVariant getFlavorName",variant.getFlavorName() + CONFIG_SUFFIX)
            WinkLog.d("LibraryVariant variantName",variant.name + CONFIG_SUFFIX)
            Collection<ResolvedArtifact> artifacts = new ArrayList()
            Collection<ResolvedDependency> firstLevelDependencies = new ArrayList<>()
            embedConfigurations.each { configuration ->
                WinkLog.d("configuration name",configuration.name)
                if (configuration.name == CONFIG_NAME
                        || configuration.name == variant.getBuildType().name + CONFIG_SUFFIX
                        || configuration.name == variant.getFlavorName() + CONFIG_SUFFIX
                        || configuration.name == variant.name + CONFIG_SUFFIX) {
                    Collection<ResolvedArtifact> resolvedArtifacts = resolveArtifacts(configuration)
                    artifacts.addAll(resolvedArtifacts)
                    artifacts.addAll(dealUnResolveArtifacts(configuration, variant, resolvedArtifacts))
                    firstLevelDependencies.addAll(configuration.resolvedConfiguration.firstLevelModuleDependencies)
                }
            }

            if (!artifacts.isEmpty()) {
                def processor = new VariantProcessor(project, variant)
                processor.processVariant(artifacts, firstLevelDependencies, transform)
            }
        }
    }

    /**
     * 其实就是解析出renxh com.xxx的具体数据，然后添加到集合中
     * @param configuration
     * @return
     */
    private Collection<ResolvedArtifact> resolveArtifacts(Configuration configuration) {
        def set = new ArrayList()
        if (configuration != null) {
            configuration.resolvedConfiguration.resolvedArtifacts.each { artifact ->
                if (ARTIFACT_TYPE_AAR == artifact.type || ARTIFACT_TYPE_JAR == artifact.type) {
                    //
                } else {
                    WinkLog.d("error artifact name = ${artifact.name}, type = ${artifact.type}")
                    throw new ProjectConfigurationException('Only support embed aar and jar dependencies!', null)
                }
                WinkLog.d("artifact name = ${artifact.name}, type = ${artifact.type}")
                set.add(artifact)
            }
        }
        return set
    }

    private Collection<ResolvedArtifact> dealUnResolveArtifacts(Configuration configuration, LibraryVariant variant, Collection<ResolvedArtifact> artifacts) {
        def artifactList = new ArrayList()
        configuration.resolvedConfiguration.firstLevelModuleDependencies.each { dependency ->
            def match = artifacts.any { artifact ->
                WinkLog.d("dealUnResolveArtifacts",dependency.moduleName+"/"+artifact.moduleVersion.id.name)
                dependency.moduleName == artifact.moduleVersion.id.name
            }

            WinkLog.d(String.valueOf(match))
//
//            if (!match) {
//                def flavorArtifact = FlavorArtifact.createFlavorArtifact(project, variant, dependency)
//                if (flavorArtifact != null) {
//                    artifactList.add(flavorArtifact)
//                }
//            }
        }
        return artifactList
    }
}