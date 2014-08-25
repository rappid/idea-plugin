package com.rappidjs.ide.idea.xaml;

import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlElementDescriptorProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlElementsGroup;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.util.XmlUtil;
import com.rappidjs.ide.idea.RappidProject;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * User: tony
 * Date: 25.08.14
 * Time: 11:30
 */
public class ComponentXmlElementDescriptorProvider implements XmlElementDescriptorProvider {
    @Nullable
    @Override
    public XmlElementDescriptor getDescriptor(XmlTag xmlTag) {

        if (xmlTag.isValid() && xmlTag.getContainingFile().getName().endsWith("xml")) {
            Module module = ModuleUtil.findModuleForPsiElement(xmlTag);
            RappidProject rappidProject;

            rappidProject = RappidProject.getFromModule(module);

            if (rappidProject.exists() && rappidProject.containsFile(xmlTag.getContainingFile().getVirtualFile().getPath())) {
                // valid xaml document in an rappidjs project
                XmlNSDescriptor xmlNSDescriptor = xmlTag.getNSDescriptor(xmlTag.getNamespace(), false);
                XmlElementDescriptor xmlElementDescriptor = null;

                if (xmlNSDescriptor != null) {
                    xmlElementDescriptor = xmlNSDescriptor.getElementDescriptor(xmlTag);
                }

                if (xmlElementDescriptor == null) {
                    xmlElementDescriptor = XmlUtil.findXmlDescriptorByType(xmlTag);
                }

                return new ComponentXmlElementDescriptor(xmlElementDescriptor, xmlTag, rappidProject);

            }

        }

        return null;

    }

    public static class ComponentXmlElementDescriptor implements XmlElementDescriptor {

        protected RappidProject rappidProject = null;
        protected XmlTag xmlTag = null;
        protected XmlElementDescriptor xmlElementDescriptor = null;

        @SuppressWarnings({"UnusedDeclaration"})
        public ComponentXmlElementDescriptor() {
            super();
        }

        public ComponentXmlElementDescriptor(XmlElementDescriptor xmlElementDescriptor, XmlTag xmlTag, RappidProject rappidProject) {
            this.xmlElementDescriptor = xmlElementDescriptor;
            this.xmlTag = xmlTag;
            this.rappidProject = rappidProject;

            logger().debug(String.format("Create Descriptor for %s", xmlTag.getName()));
        }

        private static VirtualFile findFileForClass(Project project, String fqClassName) {

            String baseFileName = fqClassName.replace(".", "/");
            String[] fileNames = new String[]{
                    baseFileName + ".xml",
                    baseFileName + ".js"
            };

            Module[] modules = ModuleManager.getInstance(project).getModules();
            VirtualFile symlinkedFile = null;

            for (Module module : modules) {
                for (String fileName : fileNames) {

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

                    VirtualFile virtualFile = findFile(roots, fileName);

                    if (virtualFile != null && virtualFile.exists()) {

                        if (!virtualFile.getPath().equals(virtualFile.getCanonicalPath())) {
                            // symbolic linked file -> check if we can get the native one
                            symlinkedFile = virtualFile;
                        } else {
                            return virtualFile;
                        }
                    }
                }
            }

            // didn't found a non symlinked file, use the symlinked file if available
            if (symlinkedFile != null) {
                return symlinkedFile;
            }

            return null;
        }

        private static VirtualFile findFile(VirtualFile[] sourceRoots, String fileName) {

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

        protected com.intellij.openapi.diagnostic.Logger logger() {
            return IdeaLogger.getInstance(this.getClass());
        }

        public PsiElement getDeclaration() {

            logger().debug(String.format("Get declaration for %s", xmlTag.getName()));

            String fqClassName = xmlTag.getNamespace() + "." + xmlTag.getLocalName();
            VirtualFile file = findFileForClass(xmlTag.getProject(), fqClassName);

            if (file != null) {
                PsiFile psiFile = PsiManager.getInstance(xmlTag.getProject()).findFile(file);

                if (psiFile != null && psiFile.isValid()) {
                    return psiFile;
                }
            }

            return xmlElementDescriptor.getDeclaration();
        }

        @Override
        public XmlElementDescriptor[] getElementsDescriptors(XmlTag xmlTag) {
            return xmlElementDescriptor.getElementsDescriptors(xmlTag);
        }

        @Nullable
        @Override
        public XmlElementDescriptor getElementDescriptor(XmlTag xmlTag, XmlTag xmlTag2) {
            return xmlElementDescriptor.getElementDescriptor(xmlTag, xmlTag2);
        }

        @Override
        public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag xmlTag) {
            return xmlElementDescriptor.getAttributesDescriptors(xmlTag);
        }

        @Override
        public String getName(PsiElement context) {
            return xmlElementDescriptor.getName(context);
        }

        @Override
        public String getName() {
            return xmlElementDescriptor.getName();
        }

        @Override
        public void init(PsiElement element) {
            xmlElementDescriptor.init(element);
        }

        @Override
        public Object[] getDependences() {
            return xmlElementDescriptor.getDependences();
        }

        @Override
        public String getQualifiedName() {
            return xmlElementDescriptor.getQualifiedName();
        }

        @Override
        public String getDefaultName() {
            return xmlElementDescriptor.getDefaultName();
        }

        @Nullable
        @Override
        public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String s, @Nullable XmlTag xmlTag) {
            return xmlElementDescriptor.getAttributeDescriptor(s, xmlTag);
        }

        @Nullable
        @Override
        public XmlAttributeDescriptor getAttributeDescriptor(XmlAttribute xmlAttribute) {
            return xmlElementDescriptor.getAttributeDescriptor(xmlAttribute);
        }

        @Override
        public XmlNSDescriptor getNSDescriptor() {
            return xmlElementDescriptor.getNSDescriptor();
        }

        @Nullable
        @Override
        public XmlElementsGroup getTopGroup() {
            return xmlElementDescriptor.getTopGroup();
        }

        @Override
        public int getContentType() {
            return xmlElementDescriptor.getContentType();
        }

        @Nullable
        @Override
        public String getDefaultValue() {
            return xmlElementDescriptor.getDefaultValue();
        }

    }
}
