package com.rappidjs.ide.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.util.HashMap;

/**
 * User: tony
 * Date: 25.08.14
 * Time: 12:01
 */
public class RappidProject {

    protected static HashMap<String, RappidProject> cache = new HashMap<String, RappidProject>();
    protected String basePath = null;

    protected RappidProject(String basePath) {
        this.basePath = basePath;
    }

    public static RappidProject get(String basePath) {
        basePath = new File(basePath).getAbsolutePath();
        if (!cache.containsKey(basePath)) {
            cache.put(basePath, new RappidProject(basePath));
        }

        return cache.get(basePath);
    }

    public static RappidProject getFromProject(Project project) {
        return RappidProject.get(project.getBasePath());
    }

    public static RappidProject getFromModule(Module module) {
        return RappidProject.get(new File(module.getModuleFilePath()).getParentFile().getAbsolutePath());
    }

    public String getBasePath() {
        return basePath;
    }

    public boolean exists() {
        return getBasePath() != null
                && new File(getBasePath()).exists()
                && new File(getConfigPath()).exists();
    }

    public String getPublicPath() {
        return getProjectRelativePath("public");
    }

    public String getServerPath() {
        return getProjectRelativePath("server");
    }

    public String getConfigPath() {
        return getProjectRelativePath("public/config.json");
    }

    public String getProjectRelativePath(String path) {
        return new File(getBasePath(), path).getAbsolutePath();
    }

    public boolean containsFile(String path) {
        return this.getBasePath() != null && path.startsWith(this.getBasePath());
    }
}
