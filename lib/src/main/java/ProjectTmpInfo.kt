import java.io.Serializable
import java.util.ArrayList

class ProjectTmpInfo(
    @JvmField var fixedInfo: ProjectFixedInfo
) : Serializable {
    @JvmField
    var changedJavaFiles: MutableList<String> = ArrayList()
    @JvmField
    var changedKotlinFiles: MutableList<String> = ArrayList()
    @JvmField
    var hasResourceChanged: Boolean = false
    @JvmField
    var hasAddNewOrChangeResName: Boolean = false
}