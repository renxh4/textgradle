import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KaptTaskParam implements Serializable {

    String compileClassPath = null;
    HashSet<File> javaSourceRoots = null;
    List<String> processorOptions = null;
    Set<File> processingClassPath = null;
    Map<String, String> javacOptions = null;

    List<File> kotlinStdlibClasspath = null;
    List<String> annotationProcessorFqNames = null;
    File sourcesOutputDir = null;
    File classesOutputDir = null;
    String stubsOutputDir = null;
}
