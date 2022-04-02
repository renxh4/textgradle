

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IncrementPatchHelper {

        String RESOURCE_APK_SUFFIX = "_resources-debug.png";


    public boolean patchToApp() {
        List<String> devicesList = DeviceUtils.getConnectingDevices();
        if (Settings.data.classChangedCount <= 0 && !Settings.data.hasResourceChanged) {
            WinkLog.i("No changed, nothing to do.");
//            installAppIfDiffVersion(devicesList);
            return false;
        }

        WinkLog.d("[IncrementPatchHelper]->[patchToApp] \n是否有资源变动：" + Settings.data.classChangedCount + "，是否新增改名：" + Settings.data.hasResourceChanged);

        createPatchFile(devicesList);
        patchDex(devicesList);
        patchResources(devicesList);
        restartApp(devicesList);

        WinkLog.i("Patch finish in " + (System.currentTimeMillis() - Settings.data.beginTime) / 1000 + "s.");
        WinkLog.i(Settings.data.classChangedCount + " file changed, "
                + (Settings.data.hasResourceChanged ? "has" : "no") + " resource changed.");
        return true;
    }


    public void createPatchFile(@NotNull List<String> devicesList) {
        for (int i = 0; i < devicesList.size(); i++) {
            String deviceId = devicesList.get(i);
            String patch = "/sdcard/Android/data/" + Settings.env.debugPackageName;
            Utils.ShellResult result = Utils.runShells("source ~/.bash_profile\nadb -s " + deviceId + " shell ls " + patch);
            boolean noPermission = false;
            Utils.runShells(Utils.ShellOutput.NONE, "source ~/.bash_profile\nadb -s " + deviceId + " shell mkdir " + patch);
            for (String error : result.getErrorResult()) {
                if (error.contains("Permission denied")) {
                    // 标志没文件权限
                    noPermission = true;
                    break;
                }
            }

            if (noPermission) {
                Settings.data.patchPath = "/sdcard/" + Settings.NAME + "/patch_file/";
            } else {
                Settings.data.patchPath = "/sdcard/Android/data/" + Settings.env.debugPackageName + "/patch_file/";
            }

            String mkdirStr = "source ~/.bash_profile\nadb -s " + deviceId + " shell mkdir " + Settings.data.patchPath;
            Utils.runShells(Utils.ShellOutput.NONE, "source ~/.bash_profile", mkdirStr);

            String lsStr = "source ~/.bash_profile\nadb -s " + deviceId + " shell ls " + Settings.data.patchPath;
            result = Utils.runShells(lsStr);
            if (result.getErrorResult().size() > 0) {
                WinkLog.d("Can not create patch file " + Settings.data.patchPath);
            }
        }
    }

    public void patchDex(@NotNull List<String> devicesList) {
        if (Settings.data.classChangedCount <= 0) {
            return;
        }

        WinkLog.i("Dex patching...");

        for (int i = 0; i < devicesList.size(); i++) {
            String patchName = Settings.env.version + "_patch.jar";
            Utils.runShells("source ~/.bash_profile\n" + "adb -s " + devicesList.get(i) + " push " + Settings.env.tmpPath + "/" + patchName
                    + " " + Settings.data.patchPath + Settings.env.version + "_patch.png");
        }

        WinkLog.d("version到底是啥",Settings.env.version+"/");

    }

    public void patchResources(@NotNull List<String> devicesList) {
        if (!Settings.data.hasResourceChanged) {
            return;
        }

        WinkLog.i("Resources patching...");

        for (String deviceId : devicesList) {
            String patchName = Settings.env.version + ResourceHelper.apk_suffix;
            Utils.runShells("source ~/.bash_profile\n" +
                    "adb -s " + deviceId + " shell rm -rf " + Settings.data.patchPath + "apk\n" +
                    "adb -s " + deviceId + " shell mkdir " + Settings.data.patchPath + "apk\n" +
                    "adb -s " + deviceId + " push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath + "apk/" +
                    Settings.env.version + RESOURCE_APK_SUFFIX);
        }
    }

    public void restartApp(@NotNull List<String> devicesList) {
        for (String deviceId : devicesList) {
//            if (isDiffVersion(deviceId)) {
//                installLocalApk_Version(deviceId);
//                continue;
//            }
            String cmds = "";
            cmds += "source ~/.bash_profile";
            cmds += '\n' + "adb -s " + deviceId + " shell am force-stop " + Settings.env.debugPackageName;
            cmds += '\n' + "adb -s " + deviceId + " shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity;
            Utils.runShells(cmds);
        }
    }
}
