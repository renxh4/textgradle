package com.renxh

import com.android.build.gradle.api.LibraryVariant
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider

class VersionAdapter{
    private Project mProject

    private LibraryVariant mVariant

    VersionAdapter(Project project, LibraryVariant variant) {
        mProject = project
        mVariant = variant
    }

    static TaskProvider<Task> getBundleTaskProvider(Project project, LibraryVariant variant) throws UnknownTaskException {
        def taskPath = "bundle" + variant.name.capitalize()
        TaskProvider bundleTask
        try {
            bundleTask = project.tasks.named(taskPath)
        } catch (UnknownTaskException ignored) {
            taskPath += "Aar"
            bundleTask = project.tasks.named(taskPath)
        }
        WinkLog.d("[getBundleTaskProvider] taskPath=" + taskPath)
        return bundleTask
    }
}