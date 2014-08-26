package com.rappidjs.ide.idea.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;

/**
 * User: tony
 * Date: 26.08.14
 * Time: 13:36
 */
public class FileUtil {


    public static VirtualFile findFileInProject(Project project, String fileName) {


        Module[] modules = ModuleManager.getInstance(project).getModules();
        VirtualFile symlinkedFile = null;

        for (Module module : modules) {

            VirtualFile virtualFile = findFileInModule(module, fileName);

            if (virtualFile != null && virtualFile.exists()) {

                if (!virtualFile.getPath().equals(virtualFile.getCanonicalPath())) {
                    // symbolic linked file -> check if we can get the native one
                    symlinkedFile = virtualFile;
                } else {
                    return virtualFile;
                }
            }
        }


        // didn't found a non symlinked file, use the symlinked file if available
        if (symlinkedFile != null) {
            return symlinkedFile;
        }

        return null;
    }

    public static VirtualFile findFileInModule(Module module, String fileName) {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] roots = moduleRootManager.getSourceRoots();

        if (roots.length == 0) {
            // web module doesn't have source roots
            VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
            ArrayList<VirtualFile> virtualSourceRoots = new ArrayList<VirtualFile>(contentRoots.length * 2);

            for (VirtualFile contentRoot : contentRoots) {

                if (contentRoot.exists()) {
                    virtualSourceRoots.add(contentRoot);
                }

                VirtualFile tmpPublicRoot;
                tmpPublicRoot = contentRoot.findChild("public");
                if (tmpPublicRoot != null && tmpPublicRoot.exists() && tmpPublicRoot.isValid()) {
                    virtualSourceRoots.add(tmpPublicRoot);
                }

                tmpPublicRoot = contentRoot.findChild("server");
                if (tmpPublicRoot != null && tmpPublicRoot.exists() && tmpPublicRoot.isValid()) {
                    virtualSourceRoots.add(tmpPublicRoot);
                }

            }

            roots = virtualSourceRoots.toArray(new VirtualFile[virtualSourceRoots.size()]);

        }

        return findFile(roots, fileName);
    }


    public static VirtualFile findFile(VirtualFile[] sourceRoots, String fileName) {

        for (VirtualFile sourceRoot : sourceRoots) {

            if (sourceRoot == null) {
                // strange, but even if I saw the array itself filled, the
                // object was null -> threading problem?
                continue;
            }

            VirtualFile virtualFile = sourceRoot.findFileByRelativePath(fileName);

            if (virtualFile != null) {
                return virtualFile;
            }
        }

        return null;
    }

    private static VirtualFile findFile(VirtualFile root, String fileName) {
        return findFile(new VirtualFile[]{root}, fileName);
    }

}
