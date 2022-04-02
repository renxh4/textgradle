import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Env implements Serializable {

    public static ProjectFixedInfo projectTreeRoot = null;

    public static String variantName="debug";


    String javaHome = null;
    String rootDir = null;
    String version = null;
    String sdkDir = null;
    String buildToolsVersion = null;
    String buildToolsDir = null;
    String compileSdkVersion = null;
    String compileSdkDir = null;
    String debugPackageName = null;
    String launcherActivity = null;
    String appProjectDir = null;
    String tmpPath = "";
    String packageName = null;
    ArrayList<ProjectFixedInfo> projectBuildSortList = new ArrayList();
    String defaultFlavor = "";
    String kaptCompileClasspath = null;
    String kaptProcessingClasspath = null;
    Map<String, String> annotationProcessorOptions = new HashMap<String, String>();
    String jvmTarget = null;
    String branch = "debug";
    ArrayList<String> javaCommandPre = new ArrayList();

    KaptTaskParam kaptTaskParam =null;


    @Override
    public String toString() {
        return "Env{" +
                "javaHome='" + javaHome + '\'' +
                ", rootDir='" + rootDir + '\'' +
                ", version='" + version + '\'' +
                ", sdkDir='" + sdkDir + '\'' +
                ", buildToolsVersion='" + buildToolsVersion + '\'' +
                ", buildToolsDir='" + buildToolsDir + '\'' +
                ", compileSdkVersion='" + compileSdkVersion + '\'' +
                ", compileSdkDir='" + compileSdkDir + '\'' +
                ", debugPackageName='" + debugPackageName + '\'' +
                ", launcherActivity='" + launcherActivity + '\'' +
                ", appProjectDir='" + appProjectDir + '\'' +
                ", tmpPath='" + tmpPath + '\'' +
                ", packageName='" + packageName + '\'' +
                ", projectBuildSortList=" + projectBuildSortList +
                ", defaultFlavor='" + defaultFlavor + '\'' +
                ", kaptCompileClasspath='" + kaptCompileClasspath + '\'' +
                ", kaptProcessingClasspath='" + kaptProcessingClasspath + '\'' +
                ", annotationProcessorOptions=" + annotationProcessorOptions +
                ", jvmTarget='" + jvmTarget + '\'' +
                ", branch='" + branch + '\'' +
                ", javaCommandPre=" + javaCommandPre +
                ", kaptTaskParam=" + kaptTaskParam +
                '}';
    }
}


