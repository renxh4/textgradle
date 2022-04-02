import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.AnnotationProcessorOptions;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.utils.FileUtils;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.tasks.compile.JavaCompile;
import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask;
import org.jetbrains.kotlin.gradle.tasks.CompilerPluginOptions;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class InitEnvHelper {

    private Project project;

    public void initEnv(Project project, boolean b) {
        createEnv(project);
    }

    private void createEnv(Project project) {
        this.project = project;

        AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");

        Env env = Settings.env;
        env.javaHome = getJavaHome();
        env.sdkDir = androidExt.getSdkDirectory().getPath();
        env.buildToolsVersion = androidExt.getBuildToolsVersion();
        env.buildToolsDir = FileUtils.join(androidExt.getSdkDirectory().getPath(),
                "build-tools", env.buildToolsVersion);
        env.compileSdkVersion = androidExt.getCompileSdkVersion();
        env.compileSdkDir = FileUtils.join(env.sdkDir, "platforms", env.compileSdkVersion);

        env.rootDir = project.getRootDir().getAbsolutePath();
        if (androidExt.getProductFlavors() != null && androidExt.getProductFlavors().getNames().size() > 0) {
            SortedSet<String> names = androidExt.getProductFlavors().getNames();
            for (String s:names) {
                WinkLog.d("getProductFlavors",s);
            }
            env.defaultFlavor = androidExt.getProductFlavors().getNames().first();
            env.variantName = env.defaultFlavor + "Debug";
        }

        env.appProjectDir = project.getProjectDir().getAbsolutePath();
        env.tmpPath = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/" + Settings.NAME;

        env.packageName = androidExt.getDefaultConfig().getApplicationId();
        Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
        while (itApp.hasNext()) {
            ApplicationVariant variant = itApp.next();
            WinkLog.d("variantName",variant.getName());
            WinkLog.d("variantName",variant.getApplicationId());
            if (variant.getName().equals(env.variantName)) {
                env.debugPackageName = variant.getApplicationId();
                break;
            }
        }

        String manifestPath = androidExt.getSourceSets().getByName("main").getManifest().getSrcFile().getPath();
        env.launcherActivity = AndroidManifestUtils.findLauncherActivity(manifestPath, env.packageName);


//        initKaptTaskParams(env);

        findModuleTree2(project, "");


        if (!Settings.data.newVersion.isEmpty()) {
            WinkLog.d("version","有vesion");
            env.version = Settings.data.newVersion;
            Settings.data.newVersion = "";
            WinkLog.d("version","查看version"+Settings.env.version);
        }else {
            WinkLog.d("version","无vesion"+Settings.data.newVersion);
        }

        Settings.storeEnv(env, project.getRootDir() + "/.idea/" + Settings.NAME + "/env");

        initData(project);
    }

    private void initData(Project project) {
        Settings.initData();

        WinkLog.d("env", Settings.env.toString());
    }

    public void findModuleTree2(Project project, String productFlavor) {
        Settings.env.projectTreeRoot = new ProjectFixedInfo();
        initProjectData(Settings.env.projectTreeRoot, project, true);

        HashSet<String> hasAddProject = new HashSet<>();
        hasAddProject.add(project.getName());

        for (Project item : project.getRootProject().getSubprojects()) {
            String name = item.getName();
            WinkLog.d("Subproject",name);
            if (name.equals("wink-gradle-plugin")
                    || name.equals("wink-patch-lib")
                    || hasAddProject.contains(name)) {
                continue;
            }

            ProjectFixedInfo childNode = new ProjectFixedInfo();
            initProjectData(childNode, item, false);
            Settings.env.projectTreeRoot.children.add(childNode);
            hasAddProject.add(item.getName());
        }

        Settings.env.projectBuildSortList.clear();
        sortBuildList(Settings.env.projectTreeRoot, Settings.env.projectBuildSortList);
    }


    private void sortBuildList(ProjectFixedInfo node, List<ProjectFixedInfo> out) {
        for (ProjectFixedInfo child : node.children) {
            sortBuildList(child, out);
        }

        if (!node.isProjectIgnore) {
            out.add(node);

            WinkLog.d("mmm", node.toString());
        }
    }

    private void initProjectData(ProjectFixedInfo fixedInfo, Project project) {
        initProjectData(fixedInfo, project, false);
    }

    Set<String> compilePath = new HashSet<>();
    Set<String> processingPath = new HashSet<>();

    private void initProjectData(ProjectFixedInfo fixedInfo, Project project, boolean foreInit) {
        WinkLog.d("initProjectData",foreInit+"/");
        long findModuleEndTime = System.currentTimeMillis();
        fixedInfo.name = project.getName();
//        fixedInfo.isProjectIgnore = isIgnoreProject(fixedInfo.name);
        WinkLog.d("initProjectData","0");
        if (fixedInfo.isProjectIgnore && !foreInit) {
            return;
        }

        WinkLog.d("initProjectData","1");

        fixedInfo.dir = project.getProjectDir().getAbsolutePath();
        fixedInfo.buildDir = project.getBuildDir().getPath();

        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> kotlinArgs = new ArrayList<>();

        WinkLog.d("ywb 2222222 initProjectData 1111 耗时：" + (System.currentTimeMillis() - findModuleEndTime) + " ms");

        Object extension = project.getExtensions().findByName("android");
        JavaCompile javaCompile = null;
        String processorArgs = "";
        if (extension == null) {
            return;
        } else if (extension instanceof AppExtension) {
            processorArgs = getProcessorArgs(((AppExtension) extension).getDefaultConfig()
                    .getJavaCompileOptions().getAnnotationProcessorOptions().getArguments());

            Iterator<ApplicationVariant> itApp = ((AppExtension) extension).getApplicationVariants().iterator();
            while (itApp.hasNext()) {
                ApplicationVariant variant = itApp.next();
                if (variant.getName().equals(Settings.env.variantName)) {
                    javaCompile = variant.getJavaCompileProvider().get();
                    break;
                }
            }
        } else if (extension instanceof LibraryExtension) {
            processorArgs = getProcessorArgs(((LibraryExtension) extension).getDefaultConfig()
                    .getJavaCompileOptions().getAnnotationProcessorOptions().getArguments());

            Iterator<LibraryVariant> it = ((LibraryExtension) extension).getLibraryVariants().iterator();
            while (it.hasNext()) {
                LibraryVariant variant = it.next();
                if (variant.getName().equals(Settings.env.variantName)) {
                    javaCompile = variant.getJavaCompileProvider().get();
                    break;
                }
            }
        }

        WinkLog.d("==================== processorArgs : " + processorArgs);

        if (javaCompile == null) {
            return;
        }

        args.add("-source");
        args.add(javaCompile.getTargetCompatibility());

        args.add("-target");
        args.add(javaCompile.getTargetCompatibility());

        args.add("-encoding");
        args.add(javaCompile.getOptions().getEncoding());

        args.add("-bootclasspath");
        args.add(javaCompile.getOptions().getBootstrapClasspath().getAsPath());

        args.add("-g");

        StringBuilder processorpath = new StringBuilder(javaCompile.getOptions().getAnnotationProcessorPath().getAsPath());
        // todo apt
        if (Settings.env.kaptTaskParam != null &&
                Settings.env.kaptTaskParam.processorOptions != null) {
            for (File file : Settings.env.kaptTaskParam.processingClassPath) {
                if (processorpath != null && (processorpath.length() > 0)) {
                    processorpath.append(":");
                }

                processorpath.append(file.getAbsolutePath());
            }
        }

        WinkLog.d(" ==============> processorpath : " + processorpath);

        //注解处理器参数
        if (extension instanceof BaseExtension) {
            AnnotationProcessorOptions annotationProcessorOptions = ((BaseExtension) extension).getDefaultConfig().getJavaCompileOptions().getAnnotationProcessorOptions();
            annotationProcessorOptions.getArguments().forEach((k, v) -> Settings.env.annotationProcessorOptions.put(k, v));
        }


        args.add("-classpath");
        Settings.env.javaCommandPre = new ArrayList<>(args);

        WinkLog.d(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + javaCompile.getClasspath().getAsPath());

        fixedInfo.classPath = javaCompile.getClasspath().getAsPath() + ":"
                + project.getProjectDir().toString() + "/build/intermediates/javac/" + Settings.env.variantName + "/classes"
                + ":" + Settings.env.tmpPath + "/tmp_class"
                + ":" + project.getProjectDir().toString() + "/build/generated/not_namespaced_r_class_sources/" + Settings.env.variantName + "/r";

        args.add(fixedInfo.classPath);

        args.add("-d");
        args.add(Settings.env.tmpPath + "/tmp_class");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            sb.append(" ");
            sb.append(args.get(i));
        }

        fixedInfo.javacArgs = sb.toString();

        kotlinArgs.add("-classpath");


        WinkLog.d("projectDir : " + project.getProjectDir().toString());
        kotlinArgs.add(javaCompile.getOptions().getBootstrapClasspath().getAsPath() + ":"
                + fixedInfo.classPath);

        KaptTaskParam kaptTaskParam = Settings.env.kaptTaskParam;
        addJavaCompilePath(javaCompile);
//        addKaptCompilePath(kaptTaskParam);
        StringBuilder compilePathSb = new StringBuilder();
        for (String s : compilePath) {
            compilePathSb.append(":");
            compilePathSb.append(s);
        }
        Settings.env.kaptCompileClasspath = compilePathSb.toString().replaceFirst(":", "");
        StringBuilder apSb = new StringBuilder();
        for (String s : processingPath) {
            apSb.append(":");
            apSb.append(s);
        }
        Settings.env.kaptProcessingClasspath = apSb.toString().replaceFirst(":", "");


        Settings.env.jvmTarget = "-jvm-target " + getSupportVersion(javaCompile.getTargetCompatibility());
        kotlinArgs.add("-jvm-target");
        kotlinArgs.add(getSupportVersion(javaCompile.getTargetCompatibility()));

        kotlinArgs.add("-d");
        kotlinArgs.add(Settings.env.tmpPath + "/tmp_class");

        StringBuilder sbKotlin = new StringBuilder();
        for (int i = 0; i < kotlinArgs.size(); i++) {
            sbKotlin.append(" ");
            sbKotlin.append(kotlinArgs.get(i));

        }

        fixedInfo.kotlincArgs = sbKotlin.toString();
    }

    private String getSupportVersion(String jvmVersion) {
        if ("1.7".equals(jvmVersion)) {
            return "1.8";
        }

        return jvmVersion;
    }

    private void addKaptCompilePath(KaptTaskParam kaptTaskParam) {
        if (kaptTaskParam.compileClassPath != null) {
            String[] kaptCompilePaths = kaptTaskParam.compileClassPath.split(":");
            for (String kaptCompilePath : kaptCompilePaths) {
                compilePath.add(kaptCompilePath);
            }
        }

        if (kaptTaskParam.processingClassPath != null) {
            for (File path : kaptTaskParam.processingClassPath) {
                if (path.getAbsolutePath().contains("org.projectlombok")
                        || path.getAbsolutePath().contains("wink-compiler-hook-lib")
                        || path.getAbsolutePath().contains("butterknife-compiler")) {
                    continue;
                }
                processingPath.add(path.getAbsolutePath());
            }
        }
    }

    private void addJavaCompilePath(JavaCompile javaCompile) {
        Set<File> javaCompileFiles = javaCompile.getClasspath().getFiles();
        Set<File> apfiles = javaCompile.getOptions().getAnnotationProcessorPath().getFiles();
        for (File javaCompileFile : javaCompileFiles) {
            if (javaCompileFile.getAbsolutePath().contains("org.projectlombok")
                    || javaCompileFile.getAbsolutePath().contains("wink-compiler-hook-lib")
                    || javaCompileFile.getAbsolutePath().contains("butterknife-compiler")) {
                continue;
            }
            compilePath.add(javaCompileFile.getAbsolutePath());
        }
        Set<File> bootstrapPaths = javaCompile.getOptions().getBootstrapClasspath().getFiles();
        for (File bootstrapPath : bootstrapPaths) {
            compilePath.add(bootstrapPath.getAbsolutePath());
        }

        compilePath.add(project.getProjectDir().toString() + "/build/intermediates/javac/" + Settings.env.variantName + "/classes");
        compilePath.add(Settings.env.tmpPath + "/tmp_class");
        compilePath.add(project.getProjectDir().toString() + "/build/generated/not_namespaced_r_class_sources/" + Settings.env.variantName + "/r");

        for (File processingFile : apfiles) {
            if (processingFile.getAbsolutePath().contains("org.projectlombok")
                    || processingFile.getAbsolutePath().contains("wink-compiler-hook-lib")
                    || processingFile.getAbsolutePath().contains("butterknife-compiler")) {
                continue;
            }
            processingPath.add(processingFile.getAbsolutePath());
        }
    }

    private void initKaptTaskParams(Env env) {
        KaptTaskParam param = new KaptTaskParam();
        try {
            Task kaptDebug = project.getTasks().getByName("kaptDebugKotlin");
            if (kaptDebug instanceof KaptWithoutKotlincTask) {
                KaptWithoutKotlincTask ktask = (KaptWithoutKotlincTask) kaptDebug;
                param.compileClassPath = ktask.getClasspath().getAsPath();
                param.javacOptions = ktask.getJavacOptions();
                param.javaSourceRoots = null;
                WinkLog.d("kapt!!! --- ktask.getClasspath().getAsPath() ===>>> " + ktask.getClasspath().getAsPath());
                WinkLog.d("kapt!!! --- ktask.getJavacOptions() ===>>> " + ktask.getJavacOptions());
//            param.processorOptions = ktask.processorOptions.getArguments();
                try {
                    Field processorOptions = ShareReflectUtil.findField(ktask, "processorOptions");
                    CompilerPluginOptions options = (CompilerPluginOptions) processorOptions.get(ktask);
                    param.processorOptions = options.getArguments();

                    Field configurationContainer = ShareReflectUtil.findField(((DefaultProject) project), "configurationContainer");
                    ConfigurationContainer conf = (ConfigurationContainer) configurationContainer.get(((DefaultProject) project));
                    if (conf != null) {
                        param.processingClassPath = conf.getByName("kapt").resolve();
                        WinkLog.d("kapt!!! --- processingClassPath ===>>> " + param.processingClassPath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        env.kaptTaskParam = param;
    }

    private String getProcessorArgs(Map<String, String> argsA) {
        StringBuilder sb = new StringBuilder();
        if (argsA.size() > 0) {
            boolean firstA = true;
            for (String key : argsA.keySet()) {
                if (!firstA) {
                    sb.append(".");
                }

                if (argsA.get(key) == null || argsA.get(key).isEmpty()) {
                    sb.append("-A" + key);
                } else {
                    sb.append("-A" + key + "=" + argsA.get(key));
                }
            }
        }

        return sb.toString();
    }

    public String getJavaHome() {
        String javaHomeProp = System.getProperty("java.home");
        if (javaHomeProp != null && !javaHomeProp.equals("")) {
            int jreIndex = javaHomeProp.lastIndexOf("${File.separator}jre");
            if (jreIndex != -1) {
                return javaHomeProp.substring(0, jreIndex);
            } else {
                List<String> rets = new ArrayList<>();
                rets.toArray();
                Set<Integer> has = new HashSet<>();

                String str = "";
                new String(str.toCharArray());

                return javaHomeProp;
            }
        } else {
            return System.getenv("JAVA_HOME");
        }
    }

    public boolean isEnvExist(String path) {
        String envFilePath = path + "/.idea/" + Settings.NAME + "/env";
        File envFile = new File(envFilePath);
        return envFile.exists();
    }


    public void initEnvFromCache(String rootPath) {
        Settings.restoreEnv(rootPath
                + "/.idea/" + Settings.NAME + "/env");

        // Data每次初始化
        Settings.initData();
    }
}
