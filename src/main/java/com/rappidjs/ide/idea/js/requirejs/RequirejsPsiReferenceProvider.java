package com.rappidjs.ide.idea.js.requirejs;

import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.rappidjs.ide.idea.RappidProject;
import org.jetbrains.annotations.NotNull;

/**
 * User: tony
 * Date: 26.08.14
 * Time: 10:40
 */
public class RequirejsPsiReferenceProvider extends PsiReferenceProvider {

    public static final String REQUIREJS_DEFINE_FUNCTION_NAME = "define";

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        RappidProject rappidProject = RappidProject.getFromPsiElement(element);

        if (!(rappidProject != null && rappidProject.exists())) {
            return PsiReference.EMPTY_ARRAY;
        }

        String path = element.getText();

        if ("require".equals(RequirejsConfig.unquote(path))) {
            // don't reference the require keyword in the imports
            return PsiReference.EMPTY_ARRAY;
        }

        if (isDefineFirstCollection(element)) {
            TextRange textRange = new TextRange(1, path.length() - 1);

            PsiReference ref = new RequirejsReference(element, textRange, rappidProject);
            return new PsiReference[]{ref};
        }


        return new PsiReference[0];
    }

    public static boolean isDefineFirstCollection(PsiElement element) {
        PsiElement jsArrayLiteral = element.getParent();
        if (null != jsArrayLiteral && jsArrayLiteral instanceof JSArrayLiteralExpression) {
            PsiElement jsArgumentList = jsArrayLiteral.getParent();
            if (null != jsArgumentList && jsArgumentList instanceof JSArgumentList) {
                PsiElement jsReferenceExpression = jsArgumentList.getPrevSibling();
                if (null != jsReferenceExpression && jsReferenceExpression instanceof JSReferenceExpression) {
                    if (jsReferenceExpression.getText().equals(REQUIREJS_DEFINE_FUNCTION_NAME)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
