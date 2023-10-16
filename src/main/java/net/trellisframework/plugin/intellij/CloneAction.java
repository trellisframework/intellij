package net.trellisframework.plugin.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class CloneAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        FileDocumentManager.getInstance().saveAllDocuments();
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && virtualFile.isDirectory()) {
            String newFolderName = Messages.showInputDialog("Enter new folder name:", "New Folder Name", null);
            if (newFolderName != null) {
                WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
                    VirtualFile destination = clone(virtualFile, newFolderName);
                    if (destination != null) {
                        System.out.println("Path:" + destination.getPath() + " source: " + pascalCase(virtualFile.getName()) + " dest: " + pascalCase(newFolderName));
                        Utility.File.findAndReplace(new File(destination.getPath()), pascalCase(virtualFile.getName()), pascalCase(newFolderName));
                    }
                });
            }
        }
    }

    private VirtualFile clone(VirtualFile sourceFolder, String newFolderName) {
        if (newFolderName != null && !newFolderName.trim().isEmpty()) {
            VirtualFile parentFolder = sourceFolder.getParent();
            String parentFolderPath = parentFolder.getPath();
            String destinationPath = parentFolderPath + "/" + newFolderName;
            VirtualFile destinationFolder = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(destinationPath));
            try {
                if (destinationFolder == null) {
                    destinationFolder = parentFolder.createChildDirectory(this, newFolderName);
                }
                VfsUtil.copyDirectory(this, sourceFolder, destinationFolder, null);
                String sourcePackage = getPackageDeclaration(sourceFolder);
                String destinationPackage = getPackageDeclaration(destinationFolder);
                changePackageDeclaration(destinationFolder, sourcePackage, destinationPackage);
                return destinationFolder;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    private String getPackageDeclaration(VirtualFile directory) throws IOException {
        return directory.getPath().replaceAll(".*/java/", "").replace("/", ".");
    }

    private void changePackageDeclaration(VirtualFile directory, String oldPackage, String newPackage) throws IOException {
        if (directory.isDirectory()) {
            for (VirtualFile file : directory.getChildren()) {
                if (file.isDirectory()) {
                    changePackageDeclaration(file, oldPackage, newPackage);
                } else if (file.getName().endsWith(".java")) {
                    String content = new String(file.contentsToByteArray());
                    content = StringUtils.replace(content, oldPackage, newPackage);
                    file.setBinaryContent(content.getBytes());
                }
            }
        }
    }

    private String pascalCase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
