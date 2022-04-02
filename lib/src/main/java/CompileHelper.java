import java.io.File;
import java.util.List;

public class CompileHelper {
    public void compileCode() {
        File file = new File(Settings.env.tmpPath + "/tmp_class");
        if (!file.exists()) {
            file.mkdirs();
        }

//        //变更注解的文件列表
//        List<String> changedAnnotationList = getChangedAnnotationList();
////        List<String> changedAnnotationList = new ArrayList<>();
////        changedAnnotationList.add("/Users/momo/Documents/MomoProject/wink/wink-demo-app/src/main/java/com/immomo/wink/MainActivity3.java");
//        if (changedAnnotationList.size() > 0) {
//            changedAnnotationList.add(Settings.env.tmpPath + "/KaptCompileFile.kt");
//        }
//        WinkLog.d("changedAnnotationList >>>>>>>>>>>>>>>>>>> : " + changedAnnotationList.toString());
//
//
//        compileKapt(changedAnnotationList);

        for (ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            compileKotlin(project);
        }

        for (ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            compileJava(project);
        }

//        if (changedAnnotationList.size() > 0) {
//            String classPathStr = getFullClasspathString();
//            compileKaptFile(classPathStr);
//        }
        createDexPatch();
    }

    private void compileKotlin(ProjectTmpInfo project) {
        if (project.changedKotlinFiles.size() <= 0) {
            WinkLog.d("LiteBuild: ================> 没有 Kotlin 文件变更。");
            return;
        }

        WinkLog.i("Compile " + project.changedKotlinFiles.size() + " kotlin files, module " + project.fixedInfo.name + ", " + project.changedKotlinFiles.toString());
        StringBuilder sb = new StringBuilder();
        for (String path : project.changedKotlinFiles) {
            sb.append(" ");
            sb.append(path);
        }

        String kotlinHome = System.getenv("KOTLIN_HOME");
        if (kotlinHome == null || kotlinHome.equals("")) {
            kotlinHome = "/Applications/Android Studio.app/Contents/plugins/Kotlin";
        }

        String kotlinc = getKotlinc();

        WinkLog.d("[LiteBuild] kotlincHome : " + kotlinc);
        WinkLog.d("[LiteBuild] projectName : " + project.fixedInfo.name);
        try {
            String mainKotlincArgs = project.fixedInfo.kotlincArgs;

            String javaHomePath = Settings.env.javaHome;
            javaHomePath = javaHomePath.replace(" ", "\\ ");

            String shellCommand = "sh " + kotlinc
                    // 兼容 kotlin internal 属性
                    + " -Xfriend-paths=" + project.fixedInfo.buildDir + "/tmp/kotlin-classes/debug "
                    + " -jdk-home " + javaHomePath
                    + mainKotlincArgs + sb.toString();

            WinkLog.d("[LiteBuild] kotlinc shellCommand : " + shellCommand);
            Utils.ShellResult result = Utils.runShells(shellCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Settings.data.classChangedCount += 1;
    }

    private String getKotlinc() {
        String kotlinc = System.getenv("KOTLINC_HOME");
        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            kotlinc = "/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc";
        }

        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            kotlinc = "/Applications/AndroidStudio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc";
        }

        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            WinkLog.d("\n\n================== 请配置 KOTLINC_HOME =================="
                    + "\n1. 打开：~/.bash_profile"
                    + "\n2. 添加：export KOTLINC_HOME=\"/Applications/Android\\ Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc\""
                    + "\n3. 执行：source ~/.bash_profile"
                    + "\n========================================================\n\n");

            return "";
        }

        // 如果路径包含空格，需要替换 " " 为 "\ "
        if (!kotlinc.contains("\\")) {
            kotlinc = kotlinc.replace(" ", "\\ ");
        }

        return kotlinc;
    }


    private int compileJava(ProjectTmpInfo project) {
        if (project.changedJavaFiles.size() <= 0) {
            return 0;
        }

        WinkLog.i("Compile " + project.changedJavaFiles.size() + " java files, module " + project.fixedInfo.name + ", " + project.changedJavaFiles.toString());
        StringBuilder sb = new StringBuilder();
        for (String path : project.changedJavaFiles) {
            sb.append(" ");
            sb.append(path);
        }

        String shellCommand = "javac" + project.fixedInfo.javacArgs
                + sb.toString();
        WinkLog.d("[LiteBuild] : javac shellCommand = " + shellCommand);
        WinkLog.d("[LiteBuild] projectName : " + project.fixedInfo.name);
        Utils.runShells(
                shellCommand
        );

        Settings.data.classChangedCount += 1;

        return project.changedJavaFiles.size();
    }

    private void createDexPatch() {
        if (Settings.data.classChangedCount <= 0) {
            // 没有数据变更
            return;
        }

        String patchName = Settings.env.version + "_patch.jar";
        String cmds = useD8(patchName);


        Utils.runShells(Utils.ShellOutput.NONE, cmds);
    }

    public String useD8(String patchName) {
        String cmds = "";
        String dest = Settings.env.tmpPath + "/tmp_class.zip";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "rm -rf " + dest;
        cmds += '\n' + "cd " + Settings.env.tmpPath + "/tmp_class";
        cmds += '\n' + "zip -r -o -q " + dest + " *";
        cmds += '\n' + "/Users/momo/Library/Android/sdk/build-tools/30.0.3"  + "/d8 --intermediate --output " + Settings.env.tmpPath + "/" + patchName
                + " " + Settings.env.tmpPath + "/tmp_class.zip";

        if (Settings.data.hasResourceAddOrRename) {
            // 不同版本 AGP 生成 R 文件路径不一致
            // 4.0.0+ 版本：R.jar
            // before 4.0.0：多个 R.class 文件
            cmds += " " + Settings.env.appProjectDir + "/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar/" + Settings.env.variantName + "/R.jar";
        }
        return cmds;
    }

}
