package com.rappidjs.ide.idea.js.requirejs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;

/**
 * User: tony
 * Date: 26.08.14
 * Time: 10:25
 */
public class RequirejsPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(StandardPatterns.instanceOf(JSLiteralExpression.class), new RequirejsPsiReferenceProvider());
    }
}
