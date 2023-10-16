package net.trellisframework.plugin.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import org.apache.commons.lang3.StringUtils;

public class InsertBreadAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiDirectory directory = (PsiDirectory) e.getData(CommonDataKeys.PSI_ELEMENT);
        if (directory != null) {
            String domain = Messages.showInputDialog("Enter domain name", "Generate Domain", null);
            if (StringUtils.isNotBlank(domain)) {
                String packages = directory.getVirtualFile().getPath().replaceAll(".*/java/", "").replace("/", ".") + "." + domain.toLowerCase();
                new Utility.Generator(e, domain, packages, directory).generate();
            }
        }
    }
}