package com.rappidjs.ide.idea.js.requirejs;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.rappidjs.ide.idea.RappidProject;
import com.rappidjs.ide.idea.utils.FileUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * User: tony
 * Date: 26.08.14
 * Time: 11:16
 */
public class RequirejsConfig {

    private static final Map<String, String> pluginToExtensionMap = ImmutableMap.<String, String>builder()
            .put("xaml", ".xml")
            .put("json", ".json")
            .put("raw", "")
            .build();

    protected RappidProject rappidProject;
    private Map<String, String> requirejsConfigPaths = null;

    public RequirejsConfig(RappidProject rappidProject) {
        this.rappidProject = rappidProject;
    }

    public static String getFileExtensionFromPlugin(String plugin) {
        if (pluginToExtensionMap.containsKey(plugin)) {
            return pluginToExtensionMap.get(plugin);
        }

        return null;
    }

    public PsiElement requireResolve(PsiElement element) {

        String path = unquote(element.getText());
        String fileName;
        String fileExtension = ".js";

        if (path.contains("!")) {
            String[] exclamationMarkSplit = path.split("!");
            if (exclamationMarkSplit.length == 2) {
                path = exclamationMarkSplit[1];
                fileExtension = getFileExtensionFromPlugin(exclamationMarkSplit[0]);
            } else {
                // more than one ! contained, what does this mean?
                return null;
            }
        }

        if (fileExtension == null) {
            // without file extension I cannot load a references file
            return null;
        }

        fileName = path + fileExtension;

        // TODO: relative and absolute files
        /*
        if (fileName.startsWith("/")) {
            targetFile = FileUtils.findFileByPath(getWebDir(), fileName);
            if (null != targetFile) {
                return PsiManager.getInstance(element.getProject()).findFile(targetFile);
            } else {
                return null;
            }
        } else if (fileName.startsWith(".")) {
            PsiDirectory fileDirectory = element.getContainingFile().getContainingDirectory();
            if (null != fileDirectory) {
                targetFile = FileUtils.findFileByPath(fileDirectory.getVirtualFile(), fileName);
                if (null != targetFile) {
                    return PsiManager.getInstance(element.getProject()).findFile(targetFile);
                }
            }
        }
        */

        Project project = element.getProject();

        VirtualFile targetFile = FileUtil.findFileInProject(project, fileName);

        if (targetFile != null) {
            return PsiManager.getInstance(project).findFile(targetFile);
        }

        // search by path mapping
        targetFile = FileUtil.findFileInProject(project, getMappedPath(path) + fileExtension);

        if (targetFile != null) {
            return PsiManager.getInstance(project).findFile(targetFile);
        }

        return null;

    }

    private String getMappedPath(String path) {
        if (requirejsConfigPaths == null) {
            parseRequirejsConfig();
        }

        if (requirejsConfigPaths != null) {
            return requirejsConfigPaths.get(path);
        }

        return null;
    }

    private boolean parseRequirejsConfig() {

        VirtualFile configFile = FileUtil.findFileInModule(rappidProject.getModule(), "config.json");

        if (configFile == null) {
            return false;
        }

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(configFile.getPath()));
            JSONObject jsonObject = new JSONObject(new String(encoded, configFile.getCharset()));
            JSONObject paths = jsonObject.getJSONObject("paths");

            requirejsConfigPaths = new HashMap<String, String>();

            Iterator iterator = paths.keys();

            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                requirejsConfigPaths.put(key, paths.getString(key));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            return false;
        }


        return true;

    }

    public static String unquote(String text) {
        return text.replace("\"", "").replace("'", "");
    }

}
