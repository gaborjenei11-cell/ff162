import javax.tools.*;
import com.sun.source.util.JavacTask;
import java.io.File;
import java.util.*;

/** Java syntax-only check, including Android classes, without Android SDK dependencies. */
public final class ParseSources {
    public static void main(String[] args)throws Exception {
        JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> errors=new DiagnosticCollector<>();
        StandardJavaFileManager manager=compiler.getStandardFileManager(errors,null,null);
        List<File> sources=new ArrayList<>();for(String name:args)sources.add(new File(name));
        Iterable<? extends JavaFileObject> units=manager.getJavaFileObjectsFromFiles(sources);
        JavacTask task=(JavacTask)compiler.getTask(null,manager,errors,Arrays.asList("-proc:none"),null,units);
        task.parse();
        if(!errors.getDiagnostics().isEmpty()){
            for(Diagnostic<?> error:errors.getDiagnostics())if(error.getKind()==Diagnostic.Kind.ERROR){System.err.println(error);System.exit(1);}
        }
        manager.close();System.out.println("PASS: Android and portable Java sources parse without syntax errors ("+sources.size()+" files)");
    }
}
