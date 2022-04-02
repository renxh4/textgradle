import java.io.Serializable
import java.util.ArrayList

data class Data(
    @JvmField var projectBuildSortList: MutableList<ProjectTmpInfo> = ArrayList(),
    @JvmField var hasResourceChanged: Boolean = false,
    @JvmField var hasResourceAddOrRename: Boolean = false,
    @JvmField var classChangedCount: Int = 0,
    @JvmField var needProcessDebugResources: Boolean = false,
    @JvmField var newVersion: String = "",
    @JvmField var patchPath: String = "",
    @JvmField var processorMapping: ProcessorMapping? = null,
    @JvmField var beginTime: Long? = null,
    @JvmField var logLevel: Int = 3
) : Serializable

