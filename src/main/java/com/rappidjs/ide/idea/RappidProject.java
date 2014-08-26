package com.rappidjs.ide.idea;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.rappidjs.ide.idea.js.requirejs.RequirejsConfig;

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
    protected Module module = null;
    protected RequirejsConfig requirejsConfig = null;

    protected RappidProject(String basePath, Module module) {
        this.basePath = basePath;
        this.module = module;
    }

    public static RappidProject get(String basePath, Module module) {
        String cacheId = new File(basePath).getAbsolutePath() + module.getName();

        if (!cache.containsKey(cacheId)) {
            cache.put(cacheId, new RappidProject(basePath, module));
        }

        return cache.get(cacheId);
    }

    public static RappidProject getFromModule(Module module) {
        return RappidProject.get(new File(module.getModuleFilePath()).getParentFile().getAbsolutePath(), module);
    }

    public String getBasePath() {
        return basePath;
    }

    public Module getModule() {
        return module;
    }

    public boolean exists() {
        return getBasePath() != null && new File(getBasePath()).exists() &&
                (
                        (isApplicationProject() && new File(getConfigPath()).exists())
                                || (isLibraryProject() && new File(getPackagePath()).exists())
                );

    }

    public String getPublicPath() {
        return getProjectRelativePath("public");
    }

    public String getConfigPath() {
        return getProjectRelativePath("public/config.json");
    }

    private String getPackagePath() {
        return getProjectRelativePath("package.json");
    }

    public String getProjectRelativePath(String path) {
        return new File(getBasePath(), path).getAbsolutePath();
    }

    public boolean containsFile(String path) {
        return this.getBasePath() != null && path.startsWith(this.getBasePath());
    }

    public static RappidProject getFromPsiElement(PsiElement element) {

        Module module = ModuleUtil.findModuleForPsiElement(element);

        if (module != null) {
            return RappidProject.getFromModule(module);
        }

        return null;
    }

    public boolean isLibraryProject() {
        return !new File(getPublicPath()).exists() && new File(getPackagePath()).exists();
    }

    public boolean isApplicationProject() {
        return new File(getPublicPath()).exists();
    }

    public RequirejsConfig getRequirejsConfig() {

        if (requirejsConfig == null) {
            requirejsConfig = new RequirejsConfig(this);
        }

        return requirejsConfig;

    }
}
