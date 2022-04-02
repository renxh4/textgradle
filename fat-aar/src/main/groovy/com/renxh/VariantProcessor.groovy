package com.renxh

import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.ResolvableDependency
import org.gradle.api.internal.tasks.CachingTaskDependencyResolveContext
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskProvider

class VariantProcessor {

    private final Project mProject

    private final LibraryVariant mVariant

    private VersionAdapter mVersionAdapter

    private Collection<File> mJarFiles = new ArrayList<>()


    VariantProcessor(Project project, LibraryVariant variant) {
        mProject = project
        mVariant = variant
        mVersionAdapter = new VersionAdapter(project, variant)
    }

    void processVariant(Collection<ResolvedArtifact> artifacts,
                        Collection<ResolvableDependency> dependencies,
                        RClassesTransform transform) {
        String taskPath = 'pre' + mVariant.name.capitalize() + 'Build'
        WinkLog.d("processVariant", taskPath)
        TaskProvider prepareTask = mProject.tasks.named(taskPath)
        if (prepareTask == null) {
            throw new RuntimeException("Can not find task ${taskPath}!")
        }
        TaskProvider bundleTask = VersionAdapter.getBundleTaskProvider(mProject, mVariant)
        preEmbed(artifacts, dependencies, prepareTask)
        processArtifacts(artifacts, prepareTask, bundleTask)
//        processClassesAndJars(bundleTask)
//        if (mAndroidArchiveLibraries.isEmpty()) {
//            return
//        }
//        processManifest()
//        processResources()
//        processAssets()
//        processJniLibs()
//        processConsumerProguard()
//        processGenerateProguard()
//        processDataBinding(bundleTask)
//        processRClasses(transform, bundleTask)
    }


    private void preEmbed(Collection<ResolvedArtifact> artifacts,
                          Collection<ResolvedDependency> dependencies,
                          TaskProvider prepareTask) {
        TaskProvider embedTask = mProject.tasks.register("pre${mVariant.name.capitalize()}Embed") {
            doFirst {
                printEmbedArtifacts(artifacts, dependencies)
            }
        }

        prepareTask.configure {
            dependsOn embedTask
        }
    }

    private static void printEmbedArtifacts(Collection<ResolvedArtifact> artifacts,
                                            Collection<ResolvedDependency> dependencies) {
        WinkLog.d("---------------------------------------------->")
        Collection<String> moduleNames = artifacts.stream().map { it.moduleVersion.id.name }.collect()
        for (String name : moduleNames) {
            WinkLog.d("each moduleName = ${name}")
        }
        dependencies.each { dependency ->
            WinkLog.d("dependency.moduleName=${dependency.moduleName}")
            if (!moduleNames.contains(dependency.moduleName)) {
                return
            }

            ResolvedArtifact self = dependency.allModuleArtifacts.find { module ->
                module.moduleVersion.id.name == dependency.moduleName
            }

            if (self == null) {
                return
            }

            WinkLog.d("[embed detected][$self.type]${self.moduleVersion.id}")
            moduleNames.remove(self.moduleVersion.id.name)

            dependency.allModuleArtifacts.each { artifact ->
                if (!moduleNames.contains(artifact.moduleVersion.id.name)) {
                    return
                }
                if (artifact != self) {
                    WinkLog.d("    - [embed detected][transitive][$artifact.type]${artifact.moduleVersion.id}")
                    moduleNames.remove(artifact.moduleVersion.id.name)
                }
            }
        }

        moduleNames.each { name ->
            ResolvedArtifact artifact = artifacts.find { it.moduleVersion.id.name == name }
            if (artifact != null) {
                FatUtils.logAnytime("[embed detected][$artifact.type]${artifact.moduleVersion.id}")
            }
        }
        WinkLog.d("<----------------------------------------------")

    }

    /**
     * exploded artifact files
     */
    private void processArtifacts(Collection<ResolvedArtifact> artifacts, TaskProvider<Task> prepareTask, TaskProvider<Task> bundleTask) {
        if (artifacts == null) {
            return
        }
        for (final ResolvedArtifact artifact in artifacts) {
            if (FatAarplugin.ARTIFACT_TYPE_JAR == artifact.type) {
                if (artifact.file.exists()) {
                    long len = artifact.file.length()
                    long size = len / 1024
                    String gp = artifact.getModuleVersion().getId().getGroup()
                    String na = artifact.getModuleVersion().getId().getName()
                    WinkLog.d("[zzzzzzzz][jar] group=${gp}:${na}, size=${size}")
                }
                addJarFile(artifact.file)

            } else if (FatAarPlugin.ARTIFACT_TYPE_AAR == artifact.type) {
                AndroidArchiveLibrary archiveLibrary = new AndroidArchiveLibrary(mProject, artifact, mVariant.name)
//                File jarFile = archiveLibrary.classesJarFile
//                if (jarFile.exists()) {
//                    long len = jarFile.length()
//                    long size = len / 1024
//                    FatUtils.printDebug("[zzzzzzzz][aar] group=${archiveLibrary.group}:${archiveLibrary.name}, size=${size}")
//                }
//                addAndroidArchiveLibrary(archiveLibrary)
//                Set<Task> dependencies
//
//                if (getTaskDependency(artifact) instanceof TaskDependency) {
//                    dependencies = artifact.buildDependencies.getDependencies()
//                } else {
//                    CachingTaskDependencyResolveContext context = new CachingTaskDependencyResolveContext()
//                    getTaskDependency(artifact).visitDependencies(context)
//                    if (context.queue.size() == 0) {
//                        dependencies = new HashSet<>()
//                    } else {
//                        dependencies = context.queue.getFirst().getDependencies()
//                    }
//                }
//                final def zipFolder = archiveLibrary.getRootFolder()
//                zipFolder.mkdirs()
//                def group = artifact.getModuleVersion().id.group.capitalize()
//                def name = artifact.name.capitalize()
//                String taskName = "explode${group}${name}${mVariant.name.capitalize()}"
//                Task explodeTask = mProject.tasks.create(taskName, Copy) {
//                    from mProject.zipTree(artifact.file.absolutePath)
//                    into zipFolder
//
//                    doFirst {
//                        // Delete previously extracted data.
//                        zipFolder.deleteDir()
//                    }
//                }
//
//                if (dependencies.size() == 0) {
//                    explodeTask.dependsOn(prepareTask)
//                } else {
//                    explodeTask.dependsOn(dependencies.first())
//                }
//                Task javacTask = mVersionAdapter.getJavaCompileTask()
//                javacTask.dependsOn(explodeTask)
//                bundleTask.configure {
//                    dependsOn(explodeTask)
//                }
//                mExplodeTasks.add(explodeTask)
            }
        }
    }

    void addJarFile(File jar) {
        mJarFiles.add(jar)
    }
}