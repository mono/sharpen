package sharpen.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.JavaModelException;


public class JavaProjectCmd {
	private String projectName;
	private String sourceFolder;
	private String projectPath;
	private List<String> classPath = new ArrayList<String>();
	
	
	public JavaProjectCmd(String args)
	{
		sourceFolder = args;
		projectPath =args.substring(0,args.lastIndexOf("/"));
		projectName = projectPath.substring(projectPath.lastIndexOf("/")+1);
	}
	
	public JavaProjectCmd()
	{
		sourceFolder = "";
		projectPath ="";
		projectName ="";
	}
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

	public void setSourceFolder(Iterable<String> sourceFolders) {
		for (String srcFolder : sourceFolders) {
			sourceFolder =projectPath + "/" + srcFolder;
		}
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) throws JavaModelException
	{
		if (!new File(projectPath).exists()) throw new IllegalArgumentException("'" + projectPath + "' not found.");
		this.projectPath = projectPath;
	}

	public List<String> getclassPath() {
		return classPath;
	}

	public void setclassPath(Iterable<String> classPaths) {
		for (String cPath : classPaths) {
			classPath.add(cPath);
		}
	}
	List<String> getAllCompilationUnits() throws JavaModelException
	{
		String pathForsourcefile = sourceFolder;	
		if (!new File(pathForsourcefile).exists()) throw new IllegalArgumentException("'" + pathForsourcefile + "' not found.");
		List<String> units =  new ArrayList<String>();
		getAllfile(pathForsourcefile,units);
		return units;		
	}
	
	void getAllfile(String projectPath,List<String> files)
	{
		
		File root = new File(projectPath );
        File[] list = root.listFiles();

        for ( File f : list ) {
          if ( f.isDirectory() ) {
        	  getAllfile(f.getAbsolutePath(),files);
          }
          else {
        	  String fileName = f.getAbsoluteFile().getName();
        	  
        	  if (fileName.substring(fileName.lastIndexOf(".")+1).equalsIgnoreCase("java"))
        		  files.add(f.getAbsoluteFile().toString().replace("\\","/"));            	  
          }
        }		
  	}
	
	public String createCompilationUnit(String packageName, String cuName, String source) throws IOException 
	{
		String pathForsourcefile = packageName;
		File srcPath = new File(pathForsourcefile);
		if (!srcPath.exists()){
			srcPath.mkdir();
		}
		String sourcefileName = pathForsourcefile + "/" + cuName;
		projectPath =packageName.substring(0,packageName.lastIndexOf("/src"));
		sourceFolder = projectPath + "/src";
		projectName = projectPath.substring(projectPath.lastIndexOf("/")+1);
		File sourcefile = new File(sourcefileName);
		sourcefile.createNewFile();
        FileWriter fw = new FileWriter(sourcefile);
        fw.write(source);
        fw.close();
		return	sourcefileName;
	}

	private void delete(File f) throws IOException {
		  if (f.isDirectory()) {
		    for (File c : f.listFiles())
		      delete(c);
		  }
		  if (!f.delete())
		    throw new FileNotFoundException("Failed to delete file: " + f);
		}
	
	public void deleteProject() {
			String target = projectPath;
			File targetfile = new File(target);
			if (targetfile.exists()) {
				try {
					delete(targetfile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
	}
}
