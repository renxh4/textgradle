public class JavaEntrance {
    public static void main(String[] args) {

            WinkLog.d("mmm","wink");
            if (args == null || args.length == 0) {
                WinkLog.d("Java 命令需要指定参数：path");
                return;
            }

            WinkLog.d("====== 开始执行 Java 任务 ======");

            String path = args[0];
//        String func = args[1];

            WinkLog.d("====== path : " + path);

            InitEnvHelper helper = new InitEnvHelper();

            WinkLog.d("Wink start...");
            if (helper.isEnvExist(path)) {
                // Increment
                WinkLog.d("isEnvExist");
                helper.initEnvFromCache(path);
            }else {
                WinkLog.d("isEnvExistretrue");
                return;
            }

            // Diff file changed
            WinkLog.d("mmm","rundiff");
            runDiff();

            new ResourceHelper().checkResourceWithoutTask(); // 内部判断：Settings.data.hasResourceChanged
            new CompileHelper().compileCode();
            if (new IncrementPatchHelper().patchToApp()) {
                updateSnapShot();
            }

    }

    private static void updateSnapShot() {
        for (ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new DiffHelper(info).initSnapshotForRes();
            }
        }
    }

    public static boolean runDiff() {
        WinkLog.d("Diff start...");

        for (ProjectTmpInfo projectInfo : Settings.data.projectBuildSortList) {
            new DiffHelper(projectInfo).diff(projectInfo);

            if (projectInfo.hasResourceChanged) {
                WinkLog.i("遍历是否有资源修改, name=" + projectInfo.fixedInfo.dir);
                WinkLog.i("遍历是否有资源修改, changed=" + projectInfo.hasResourceChanged);
                Settings.data.hasResourceChanged = true;
            }

            if (projectInfo.hasAddNewOrChangeResName) {
                Settings.data.hasResourceAddOrRename = true;
            }


        }
        return Settings.data.hasResourceChanged;
    }
}
