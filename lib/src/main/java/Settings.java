import java.util.ArrayList;

public class Settings {
    public static Env env = new Env();

    public static KaptTaskParam kaptTaskParam;

    public static String NAME = "renxh";

    public static void storeEnv(Env env, String filePath) {
        LocalCacheUtil.save2File(env, filePath);
        Settings.env = env;
    }

    public static Data initData() {
        data = new Data();
        ArrayList<ProjectFixedInfo> projectBuildSortList = env.projectBuildSortList;

        for (ProjectFixedInfo projectFixedInfo : projectBuildSortList) {
            data.projectBuildSortList.add(new ProjectTmpInfo(projectFixedInfo));
        }


        data.beginTime = System.currentTimeMillis();
        // todo apt
        data.processorMapping = LocalCacheUtil.getCache(env.tmpPath + "/annotation/mapping");

        return data;

    }


    public static Data data = new Data();


    public static Env restoreEnv(String filePath) {

        Object cache = LocalCacheUtil.getCache(filePath);
        env = (Env) cache;
        WinkLog.d("restoreEnv",env.version+env.launcherActivity);
        return env;
    }



    public static ProjectFixedInfo getProjectTreeRoot() {
        return env.projectTreeRoot;
    }

    public static void setProjectTreeRoot(ProjectFixedInfo projectTreeRoot) {
        Env.projectTreeRoot = projectTreeRoot;
    }

    public static String getVariantName() {
        return env.variantName;
    }

    public static void setVariantName(String variantName) {
        Env.variantName = variantName;
    }





}
