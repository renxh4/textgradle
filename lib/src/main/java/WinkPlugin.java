import com.android.build.gradle.AppExtension;
import com.android.builder.internal.ClassFieldImpl;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;

import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

public class WinkPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        new AA().aa();
        project.task("javaceshi").doLast(task -> {
            System.out.println("sdasdasd1");
        });

        createWinkTask(project);


        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        Settings.data.newVersion = System.currentTimeMillis() + "";
        appExtension.getDefaultConfig().addBuildConfigField(new ClassFieldImpl("String",
                "WINK_VERSION", "\"" + Settings.data.newVersion + "\""));
        appExtension.aaptOptions(aaptOptions -> {
            WinkLog.d("aaptOptions", "开始aapt配置 execute!");
            String stableIdPath = project.getRootDir() + "/.idea/" + "renxh" + "/stableIds.txt";
            String winkFolder = project.getRootDir() + "/.idea/" + "renxh";
            File file = new File(stableIdPath);
            File lbfolder = new File(winkFolder);
            if (!lbfolder.exists()) {
                lbfolder.mkdir();
            }
            if (file.exists()) {
                WinkLog.d("aaptOptions", "开始aapt配置 execute! 文件存在  " + file.getAbsolutePath());
                aaptOptions.additionalParameters("--stable-ids", file.getAbsolutePath());
            } else {
                WinkLog.d("aaptOptions", "开始aapt配置 execute! 文件不存在");
                aaptOptions.additionalParameters("--emit-ids", file.getAbsolutePath());
            }
        });


        project.getGradle().getTaskGraph().whenReady(new Action<TaskExecutionGraph>() {
            @Override
            public void execute(TaskExecutionGraph taskExecutionGraph) {
                project.getTasks().forEach(new Consumer<Task>() {
                    @Override
                    public void accept(Task task) {
                        WinkLog.d("task", task.getName());
                    }
                });

                // Embedded WINK_VERSION.
//                GradleUtils.getFlavorTask(project, "pre", "DebugBuild").doFirst(task -> {
//                    Settings.data.newVersion = System.currentTimeMillis() + "";
//                    ((AppExtension) project.getExtensions().getByName("android"))
//                            .getDefaultConfig().addBuildConfigField(new ClassFieldImpl("String",
//                            "WINK_VERSION", "\"" + Settings.data.newVersion + "\""));
//                });
                Task packageDebug = GradleUtils.getFlavorTask(project, "package", "Debug");
                packageDebug.doLast(task -> afterFullBuild(project));
            }
        });


    }


    private void afterFullBuild(Project project) {

        new InitEnvHelper().initEnv(project, true);
        // 产生快照
        DiffHelper.initAllSnapshot();

        //创建kotlin文件
        createKotlinFile(project);

    }

    public void createWinkTask(Project project) {
        project.getTasks().register("wink", task -> {
            task.doLast(task1 -> JavaEntrance.main(new String[]{"."}));
        }).get().setGroup(Settings.NAME);
    }


    // 创建 Kotlin 文件，用来 kapt 编译
    private void createKotlinFile(Project project) {
        String fileName = project.getRootDir() + "/.idea/" + Settings.NAME + "/KaptCompileFile.kt";
        String fileContent =
                "package com.immomo.wink.patch\n" +
                        "class KaptCompileFile {\n" +
                        "}";

        byte[] sourceByte = fileContent.getBytes();
        if (null != sourceByte) {
            try {
                File file = new File(fileName);        //文件路径（路径+文件名）
                if (!file.exists()) {    //文件不存在则创建文件，先创建目录
                    File dir = new File(file.getParent());
                    dir.mkdirs();
                    file.createNewFile();
                }
                FileOutputStream outStream = new FileOutputStream(file);    //文件输出流用于将数据写入文件
                outStream.write(sourceByte);
                outStream.close();    //关闭文件输出流
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
