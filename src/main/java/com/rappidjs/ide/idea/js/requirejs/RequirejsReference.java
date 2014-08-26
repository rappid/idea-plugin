package com.rappidjs.ide.idea.js.requirejs;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import com.rappidjs.ide.idea.RappidProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: tony
 * Date: 26.08.14
 * Time: 11:01
 */
public class RequirejsReference implements PsiReference {

    protected PsiElement element;
    protected TextRange textRange;
    protected RappidProject rappidProject;

    public RequirejsReference(PsiElement element, TextRange textRange, RappidProject rappidProject) {
        this.element = element;
        this.textRange = textRange;
        this.rappidProject = rappidProject;
    }

    @Override
    public PsiElement getElement() {
        return this.element;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return rappidProject.getRequirejsConfig().requireResolve(element);
    }

    @Override
    public String toString() {
        return getCanonicalText();
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        ArrayList<LookupElement> completionResultSet = new ArrayList<LookupElement>();

        return completionResultSet.toArray();
//
//        List<String> files = element
//                .getProject()
//                .getComponent(RequirejsProjectComponent.class)
//                .getCompletion(element);
//
//        for (String file : files) {
//            completionResultSet.add(
//                    LookupElementBuilder
//                            .create(element, file)
//                            .withInsertHandler(
//                                    RequirejsInsertHandler.getInstance()
//                            )
//            );
//        }
//
//        return completionResultSet.toArray();
    }

    @Override
    public boolean isReferenceTo(PsiElement psiElement) {
        return false;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public PsiElement handleElementRename(String s) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public TextRange getRangeInElement() {
        return textRange;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return element.getText();
    }
}
