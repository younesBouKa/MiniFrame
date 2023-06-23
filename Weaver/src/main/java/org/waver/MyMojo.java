package org.waver;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.tools.ClassFinder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Mojo( name = "touch", defaultPhase = LifecyclePhase.PACKAGE )
public class MyMojo extends AbstractMojo {
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true, readonly = true)
    private File outputDirectory;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    public void execute() throws MojoExecutionException {
        //getLog() from AbstractMojo provides logger of maven so that we can see it in build log
        getLog().info("Execution for maven target modifier started");

        //Iterate dependencies & collect information about dependencies
        String dependenciesText = "";
        for (Object dep : project.getDependencies()) {
            Dependency dependency = (Dependency) dep;
            dependenciesText = dependenciesText + " Group = " + dependency.getGroupId() + " Artifact = "
                    + dependency.getArtifactId() + " Version = " + dependency.getVersion() + " \n";
        }

        //Create a new file in target & add above dependency information to that.
        File newFile = new File(outputDirectory, "generatedFile.txt");
        try {
            newFile.createNewFile();
            Files.write(newFile.toPath(), dependenciesText.getBytes(), StandardOpenOption.APPEND);
            getLog().info("Generated - " + newFile.getAbsolutePath());
        } catch (IOException e) {
            getLog().error("Failed to generate new file", e);
            throw new MojoExecutionException("Failed to generate new file", e);
        }

        //Modify existing file from target & append text to it. Assume that
        //test.properties exists in project for example purpose.
        File classes = new File(outputDirectory.getAbsolutePath()
                + System.getProperty("file.separator")
                + "classes");
        ClassFinder.addToClassPath(Collections.singleton(classes.getAbsolutePath()));
        Set<Class> classSet = ClassFinder.getClassesWithFilter(Objects::nonNull);
        getLog().info("ClassFinder Cache size: "+classSet.size());
        getLog().info("OutputDirectory: "+classes.getAbsolutePath());
        Arrays
                .stream(classes.listFiles())
                .forEach(file -> {
                    getLog().info(file.getName());
                });
    }
}
