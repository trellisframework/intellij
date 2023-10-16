package net.trellisframework.plugin.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class FindReplaceAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        FileDocumentManager.getInstance().saveAllDocuments();
        FindReplaceDialog dialog = new FindReplaceDialog();
        if (dialog.showAndGet()) {
            VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
            File baseDir = new File(virtualFile.getPath());
            WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
                Utility.File.findAndReplace(baseDir, dialog.getOldString(), dialog.getNewString());
                Messages.showMessageDialog(event.getProject(), "Find and Replace completed!", "Information", Messages.getInformationIcon());
            });
        }
    }
}
