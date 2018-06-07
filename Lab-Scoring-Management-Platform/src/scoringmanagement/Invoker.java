package scoringmanagement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import chn.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.*;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.io.*;
import java.util.Scanner;
 
/**
 * The Invoker class handles compilation, execution, and file output for submitted lab files
 * Full execution of the class allows for a java file to be compiled, executed, and for the console output to be logged in a log file
 *
 * @author Max & Darshan
 * @version June 4
 */
public class Invoker
{
    private static final File DRIVER = new File("C:/Users/Akul/Desktop/Lab-Scoring-Management-Platform/Lab-Scoring-Management-Platform/build/classes/scoringmanagement/Driver.java");
    private static final String OUTPUT_FILE = "C:/Users/Akul/Desktop/Lab-Scoring-Management-Platform/Lab-Scoring-Management-Platform/build/classes/scoringmanagement/output.txt";

    /**
     * The compileLab method takes in lab submission data (LabName, and LabData)
     * A temp directory is setup that holds (.class, .java, and .txt files)
     * Allows files to be compiled, executed, and redirect output to a txt file
     * 
     * Compilation of the student's labs is done by the Java provided library  https://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html
     * Compilation section is forked from  https://github.com/0416354917/Algorithms/blob/master/src/util/InlineCompiler.java 
     *        
     * @param String labName
     * @param String[] labFiles
     */
    public static boolean compileLab(String labName, String labFile)
    {
        boolean testCompile = false;
        StringBuilder labString = new StringBuilder(64);
        labString.append(labFile);//add the data into the string builder
        labName = "C:/Users/Akul/Desktop/Lab-Scoring-Management-Platform/Lab-Scoring-Management-Platform/build/classes/scoringmanagement/" + labName;
        File Driver = new File(labName);
        if (Driver.getParentFile().exists() || Driver.getParentFile().mkdirs())
        {
            try
            {
                Writer writer = null;
                try
                {
                    writer = new FileWriter(Driver);
                    writer.write(labString.toString());//Writes the labString to the writer
                    writer.flush();
                } 
                finally 
                {
                    try 
                    {
                        writer.close();
                    } 
                    catch (Exception e) 
                    {
                        System.out.println("writer can't be closed");
                    }
                }
            }
            catch (Exception e) 
            {
                System.out.println("Writer not executed correctly");//deal with multiple exceptions
            }

            /** Compilation Requirements *********************************************************************************************/
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            // This sets up the class path that the compiler will use
            List<String> optionList = new ArrayList<String>();
            optionList.add("-classpath");
            optionList.add(System.getProperty("java.class.path") + ";dist/InlineCompiler1.jar");

            Iterable<? extends JavaFileObject> compilationUnit
                    = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(Driver));
            JavaCompiler.CompilationTask task = compiler.getTask(
                null, 
                fileManager, 
                diagnostics, 
                optionList, 
                null, 
                compilationUnit);
            /********************************************************************************************* Compilation Requirements **/
            if (task.call()) 
            {
                /********************************************************************************************* Load/Execute **/
                // Create a new custom class loader, pointing to the directory that contains the compiled
                // classes, this should point to the top of the package structure!
                //@SuppressWarnings("resource")
                try
                {
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});//create .class file
                    testCompile = true;
                }
                catch(Exception e)
                {
                    System.out.print("classLoader did not load correctly");
                }
                /************************************************************************************************* Load and execute */
            } 
            else 
            {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) 
                {
                    System.out.format("Error on line %d in %s%n",//if the program could not be loaded into a class
                        diagnostic.getLineNumber(),
                        diagnostic.getSource().toUri());
                }
            }
        }
        
        return testCompile;
    }
    
    /**
     * Runs the program inputted and outputs into a txt file
     * 
     * Modified from:
     * https://github.com/AlmasB/CodeSamplesJava/blob/master/src/demo/InvokerDemo.java
     * 
     * @param String classFileName
     */
    public static boolean runProgram(String classFileName)
    {
        outputConsole();//Redirect from console stream to txt
        boolean testExec = false;
        
        Class[] argTypes = new Class[1];
        argTypes[0] = String[].class;//Enter the classFileName into the array
       
        try
        {
            Method mainMethod = Class.forName
            (classFileName).getDeclaredMethod("main",argTypes);
            Object[] argListForInvokedMain = new Object[1];
            argListForInvokedMain[0] = new String[0];
 
            mainMethod.invoke(null, argListForInvokedMain);
            
            testExec = true;
            // This is the instance on which you invoke
            // the method; since main is static, you can pass
            // null in.
        }  
        catch (ClassNotFoundException ex)
        {
            System.err.println("Class "+ classFileName +" not found in classpath.");//File not found
        }
        catch (NoSuchMethodException ex)
        {
            System.err.println("Class "+ classFileName +" does not define public static void main(String[])");//no main
        }
        catch (InvocationTargetException ex)
        {
            System.err.println("Exception while executing "+ classFileName +":"+ex.getTargetException());//cant run
        }
        catch (IllegalAccessException ex)
        {
            System.err.println("main(String[]) in class "+ classFileName +" is not public");//main not public
        }
       
        return testExec;
    }
    
    /**
     * Sets the Output Stream to be linked to a txt file instead of console
     */
    public static void outputConsole()
    {
        try
        {//set the output stream
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("C:/Users/Akul/Desktop/Lab-Scoring-Management-Platform/Lab-Scoring-Management-Platform/build/classes/scoringmanagement/output.txt")), true));
        }//set the directory for the output.txt // does not handle variables
        catch(Exception e)
        {
            System.err.print("Output stream could not be setup correctly");
        }
    }
}