import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProjectFixedInfo implements Serializable {

    public List<ProjectFixedInfo> children = new ArrayList<ProjectFixedInfo>();
    public Boolean isProjectIgnore = false;
    public String name = "";
    public String dir = "";
    public String buildDir = null;
    public String manifestPath = null;
    public String javacArgs = null;
    public String classPath = null;
    public String kotlincArgs = null;
}
