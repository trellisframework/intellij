package net.trellisframework.plugin.intellij;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.regex.Pattern;

public class Utility {

    public static class Generator {

        private final AnActionEvent event;
        private final String domain;
        private final String packages;
        private final PsiDirectory directory;

        public Generator(AnActionEvent event, String domain, String packages, PsiDirectory directory) {
            this.event = event;
            this.domain = domain;
            this.packages = packages;
            this.directory = directory;
        }

        static PsiDirectory createDirectory(PsiDirectory parent, String name) {
            PsiDirectory directory = parent.findSubdirectory(name);
            if (directory == null) {
                directory = parent.createSubdirectory(name);
            }
            return directory;
        }

        static void createFile(PsiDirectory directory, String fileName, String content) {
            PsiFileFactory fileFactory = PsiFileFactory.getInstance(directory.getProject());
            FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(fileName);
            PsiFile file = fileFactory.createFileFromText(fileName, fileType, content);
            directory.add(file);
        }

        public void generate() {
            WriteCommandAction.runWriteCommandAction(event.getProject(), () -> {
                String pascalCase = domain.substring(0, 1).toUpperCase() + domain.substring(1);
                PsiDirectory domainDir = Generator.createDirectory(directory, pascalCase.toLowerCase());
                Generator.Entity.generate(domainDir, packages, pascalCase);
                Generator.Constant.generate(domainDir, packages, pascalCase);
                Generator.Repository.generate(domainDir, packages, pascalCase);
                Generator.Payload.generate(domainDir, packages, pascalCase);
                Generator.Controller.generate(domainDir, packages, pascalCase);
                Generator.Action.generate(domainDir, packages, pascalCase);
                Generator.Task.generate(domainDir, packages, pascalCase);
            });

        }

        public static class Entity {
            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "model");
                String clazz = domain + "Entity";
                String content =
                        "import lombok.AllArgsConstructor;\n" +
                                "import lombok.Getter;\n" +
                                "import lombok.NoArgsConstructor;\n" +
                                "import lombok.Setter;\n" +
                                "\n" +
                                "import jakarta.persistence.Column;\n" +
                                "import jakarta.persistence.Entity;\n" +
                                "import jakarta.persistence.Table;\n" +
                                "\n" +
                                "@Getter\n" +
                                "@Setter\n" +
                                "@NoArgsConstructor\n" +
                                "@AllArgsConstructor(staticName = \"of\")\n" +
                                "@Table(name = \"t_" + domain.toLowerCase() + "\")\n" +
                                "@Entity\n" +
                                "public class " + domain + "Entity extends BaseEntity {\n" +
                                "    @Column(name = \"name\", length = 100, nullable = false)\n" +
                                "    private String name;\n" +
                                "}";
                createFile(directory, clazz + ".java", content);
            }
        }

        public static class Payload {
            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "payload");
                browse(directory, path, domain);
                read(directory, path, domain);
                edit(directory, path, domain);
                modify(directory, path, domain);
                add(directory, path, domain);
            }

            private static String imports() {
                return "import com.example.action.*;\n" +
                        "import com.example.payload.*;\n" +
                        "import net.trellisframework.ui.web.controller.Api;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "import org.springframework.http.ResponseEntity;\n" +
                        "import org.springframework.data.domain.Page;\n\n";
            }

            private static void browse(PsiDirectory dir, String path, String domain) {
                String clazz = "Browse" + domain + "Request";
                String content = "import lombok.AllArgsConstructor;\n" +
                        "import lombok.Data;\n" +
                        "import lombok.NoArgsConstructor;\n" +
                        "import net.trellisframework.core.payload.Payload;\n" +
                        "import net.trellisframework.data.core.util.DefaultPageRequest;\n" +
                        "import org.springframework.data.domain.Pageable;\n" +
                        "\n" +
                        "import java.util.List;\n" +
                        "\n" +
                        "@Data\n" +
                        "@NoArgsConstructor\n" +
                        "@AllArgsConstructor(staticName = \"of\")\n" +
                        "public class Browse" + domain + "Request implements Payload {\n" +
                        "    private String id;\n" +
                        "    private String name;\n" +
                        "    private List<String> sort = List.of(\"created:desc\");\n" +
                        "    private Integer page;\n" +
                        "    private Integer size;\n" +
                        "    \n" +
                        "    public Pageable getPageable() {\n" +
                        "        return DefaultPageRequest.of(page, size, sort);\n" +
                        "    } \n" +
                        "    \n" +
                        "}";
                createFile(dir, clazz + ".java", content);
            }

            private static void read(PsiDirectory dir, String path, String domain) {
                String clazz = domain;
                String content = "import lombok.AllArgsConstructor;\n" +
                        "import lombok.Data;\n" +
                        "import lombok.NoArgsConstructor;\n" +
                        "import net.trellisframework.core.payload.Payload;\n" +
                        "\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "@Data\n" +
                        "@NoArgsConstructor\n" +
                        "@AllArgsConstructor(staticName = \"of\")\n" +
                        "public class " + clazz + " implements Payload {\n" +
                        "    private String id;\n" +
                        "    private Date created;\n" +
                        "    private String name;\n" +
                        "}";
                createFile(dir, domain + ".java", content);
            }

            private static void edit(PsiDirectory dir, String path, String domain) {
                String clazz = "Edit" + domain + "Request";
                String content = "import lombok.AllArgsConstructor;\n" +
                        "import lombok.Data;\n" +
                        "import lombok.NoArgsConstructor;\n" +
                        "import net.trellisframework.core.payload.Payload;\n" +
                        "\n" +
                        "@Data\n" +
                        "@NoArgsConstructor\n" +
                        "@AllArgsConstructor(staticName = \"of\")\n" +
                        "public class Edit" + domain + "Request implements Payload {\n" +
                        "    private String id;\n" +
                        "    private String name;\n" +
                        "}";
                createFile(dir, clazz + ".java", content);
            }

            private static void modify(PsiDirectory dir, String path, String domain) {
                String clazz = "Modify" + domain + "Request";
                String content = "import com.fasterxml.jackson.annotation.JsonInclude;\n" +
                        "import lombok.AllArgsConstructor;\n" +
                        "import lombok.Data;\n" +
                        "import lombok.NoArgsConstructor;\n" +
                        "import net.trellisframework.core.payload.Payload;\n" +
                        "\n" +
                        "\n" +
                        "@Data\n" +
                        "@NoArgsConstructor\n" +
                        "@AllArgsConstructor(staticName = \"of\")\n" +
                        "@JsonInclude(JsonInclude.Include.NON_NULL)\n" +
                        "public class Modify" + domain + "Request implements Payload {\n" +
                        "    private String id;\n" +
                        "    private String name;\n" +
                        "}";
                createFile(dir, clazz + ".java", content);
            }

            private static void add(PsiDirectory dir, String path, String domain) {
                String clazz = "Add" + domain + "Request";
                String content = "import lombok.AllArgsConstructor;\n" +
                        "import lombok.Data;\n" +
                        "import lombok.NoArgsConstructor;\n" +
                        "import net.trellisframework.core.payload.Payload;\n" +
                        "\n" +
                        "@Data\n" +
                        "@NoArgsConstructor\n" +
                        "@AllArgsConstructor(staticName = \"of\")\n" +
                        "public class Add" + domain + "Request implements Payload {\n" +
                        "    private String name;\n" +
                        "}";
                createFile(dir, clazz + ".java", content);
            }
        }

        public static class Constant {

            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "constant");
                String clazz = "Messages";
                String content = "import net.trellisframework.core.message.MessageHandler;\n\n" +
                        "public enum Messages implements MessageHandler {\n" +
                        "    " + domain.toUpperCase() + "_NOT_FOUND;\n" +
                        "}";
                createFile(directory, clazz + ".java", content);

            }

            private static void messages(PsiDirectory dir, String path, String domain) {
                String clazz = "Edit" + domain + "Request";
                String content = "public class " + clazz + " implements Payload {\n\n}";
                createFile(dir, clazz + ".java", content);
            }
        }

        public static class Repository {

            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "repository");
                String clazz = domain + "Repository";
                String content = "import " + path + ".model.*;\n" +
                        "import " + path + ".payload.Browse" + domain + "Request;\n" +
                        "import net.trellisframework.data.sql.data.repository.GenericJpaRepository;\n" +
                        "import org.springframework.data.domain.Page;\n" +
                        "import org.springframework.stereotype.Repository;\n" +
                        "\n" +
                        "@Repository\n" +
                        "public interface " + domain + "Repository extends GenericJpaRepository<" + domain + "Entity, String> {\n" +
                        "\n" +
                        "    default Page<" + domain + "Entity> findAll(Browse" + domain + "Request request) {\n" +
                        "        return findAll(request.getPageable());\n" +
                        "    }\n\n" +
                        "}";
                createFile(directory, clazz + ".java", content);
            }
        }

        public static class Controller {

            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "api").createSubdirectory("rest");
                generate(directory, path, domain, "UM");
                generate(directory, path, domain, "SM");
            }


            public static void generate(PsiDirectory dir, String path, String domain, String prefix) {
                String clazz = domain + prefix + "Controller";
                String content = imports(path) +
                        "@RestController\n" +
                        "@RequestMapping(value = \"/" + prefix.toLowerCase() + "/" + domain.toLowerCase() + "s" + "\", produces = MediaType.APPLICATION_JSON_VALUE)\n" +
                        "@Validated\n" +
                        "public class " + clazz + " implements Api {\n\n" +
                        browse(domain) +
                        read(domain) +
                        edit(domain) +
                        modify(domain) +
                        add(domain) +
                        delete(domain) +
                        "}";
                createFile(dir, clazz + ".java", content);
            }

            private static String imports(String path) {
                return "import " + path + ".action.*;\n" +
                        "import " + path + ".payload.*;\n" +
                        "import net.trellisframework.ui.web.bind.RequestParams;\n" +
                        "import net.trellisframework.ui.web.controller.Api;\n" +
                        "import org.springframework.http.MediaType;\n" +
                        "import org.springframework.validation.annotation.Validated;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "import org.springframework.http.ResponseEntity;\n" +
                        "import org.springframework.data.domain.Page;\n\n";
            }

            private static String browse(String domain) {
                return "    @GetMapping\n" +
                        "    public ResponseEntity<Page<" + domain + ">> browse(@RequestParams Browse" + domain + "Request request) {\n" +
                        "        return ResponseEntity.ok(call(Browse" + domain + "Action.class, request));\n" +
                        "    }\n\n";
            }

            private static String read(String domain) {
                return "    @GetMapping(\"/{id}\")\n" +
                        "    public ResponseEntity<" + domain + "> read(@PathVariable String id) {\n" +
                        "        return ResponseEntity.ok(call(Read" + domain + "Action.class, id));\n" +
                        "    }\n\n";
            }

            private static String edit(String domain) {
                return "    @PutMapping(\"/{id}\")\n" +
                        "    public ResponseEntity<" + domain + "> edit(@Validate @RequestBody Edit" + domain + "Request request) {\n" +
                        "        return ResponseEntity.ok(call(Edit" + domain + "Action.class, request));\n" +
                        "    }\n\n";
            }

            private static String modify(String domain) {
                return "    @PatchMapping(\"/{id}\")\n" +
                        "    public ResponseEntity<" + domain + "> modify(@Validate @RequestBody Modify" + domain + "Request request) {\n" +
                        "        return ResponseEntity.ok(call(Modify" + domain + "Action.class, request));\n" +
                        "    }\n\n";
            }

            private static String add(String domain) {
                return "    @PostMapping\n" +
                        "    public ResponseEntity<" + domain + "> add(@Validate @RequestBody Add" + domain + "Request request) {\n" +
                        "        return ResponseEntity.ok(call(Add" + domain + "Action.class, request));\n" +
                        "    }\n\n";
            }

            private static String delete(String domain) {
                return "    @DeleteMapping(\"/{id}\")\n" +
                        "    public ResponseEntity<Void> delete(@PathVariable String id) {\n" +
                        "        return ResponseEntity.ok(call(Delete" + domain + "Action.class, id));\n" +
                        "    }\n\n";
            }
        }

        public static class Action {
            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "action");
                browse(directory, path, domain);
                read(directory, path, domain);
                edit(directory, path, domain);
                modify(directory, path, domain);
                add(directory, path, domain);
                delete(directory, path, domain);
                getById(directory, path, domain);
            }

            private static void browse(PsiDirectory dir, String path, String domain) {
                String clazz = "Browse" + domain + "Action";
                String content = "import " + path + ".payload.Browse" + domain + "Request;\n" +
                        "import " + path + ".payload." + domain + ";\n" +
                        "import " + path + ".task.Browse" + domain + "Task;\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import org.springframework.data.domain.Page;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<Page<" + domain + ">, Browse" + domain + "Request> {\n\n" +
                        "    public Page<" + domain + "> execute(Browse" + domain + "Request request) {\n" +
                        "        return call(Browse" + domain + "Task.class, request);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void read(PsiDirectory dir, String path, String domain) {
                String clazz = "Read" + domain + "Action";
                String content = "import " + path + ".payload." + domain + ";\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<" + domain + ", String> {\n\n" +
                        "    public " + domain + " execute(String id) {\n" +
                        "        return plainToClass(call(Get" + domain + "ByIdAction.class, id), " + domain + ".class);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void edit(PsiDirectory dir, String path, String domain) {
                String clazz = "Edit" + domain + "Action";
                String content = "import " + path + ".payload.Edit" + domain + "Request;\n" +
                        "import " + path + ".payload." + domain + ";\n" +
                        "import " + path + ".task.Save" + domain + "Task;\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<" + domain + ", Edit" + domain + "Request> {\n\n" +
                        "    public " + domain + " execute(Edit" + domain + "Request request) {\n" +
                        "        return plainToClass(call(Save" + domain + "Task.class, plainToClass(request, call(Get" + domain + "ByIdAction.class, request.getId()))), " + domain + ".class);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void modify(PsiDirectory dir, String path, String domain) {
                String clazz = "Modify" + domain + "Action";
                String content = "import " + path + ".payload.Modify" + domain + "Request;\n" +
                        "import " + path + ".payload." + domain + ";\n" +
                        "import " + path + ".task.Save" + domain + "Task;\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<" + domain + ", Modify" + domain + "Request> {\n\n" +
                        "    public " + domain + " execute(Modify" + domain + "Request request) {\n" +
                        "        return plainToClass(call(Save" + domain + "Task.class, plainToClass(request, call(Get" + domain + "ByIdAction.class, request.getId()))), " + domain + ".class);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void add(PsiDirectory dir, String path, String domain) {
                String clazz = "Add" + domain + "Action";
                String content = "import " + path + ".model." + domain + "Entity;\n" +
                        "import " + path + ".payload.Add" + domain + "Request;\n" +
                        "import " + path + ".payload." + domain + ";\n" +
                        "import " + path + ".task.Save" + domain + "Task;\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<" + domain + ", Add" + domain + "Request> {\n\n" +
                        "    public " + domain + " execute(Add" + domain + "Request request) {\n" +
                        "        return plainToClass(call(Save" + domain + "Task.class, plainToClass(request, " + domain + "Entity.class)), " + domain + ".class);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void delete(PsiDirectory dir, String path, String domain) {
                String clazz = "Delete" + domain + "Action";
                String content = "import " + path + ".task.Delete" + domain + "Task;\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<Void, String> {\n\n" +
                        "    public Void execute(String id) {\n" +
                        "        return call(Delete" + domain + "Task.class, call(Get" + domain + "ByIdAction.class, id));\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void getById(PsiDirectory dir, String path, String domain) {
                String clazz = "Get" + domain + "ByIdAction";
                String content = "import " + path + ".constant.Messages;\n" +
                        "import " + path + ".model." + domain + "Entity;\n" +
                        "import " + path + ".task.Find" + domain + "ByIdTask;\n" +
                        "import net.trellisframework.context.action.Action1;\n" +
                        "import net.trellisframework.http.exception.NotFoundException;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements Action1<" + domain + "Entity, String> {\n\n" +
                        "    public " + domain + "Entity execute(String id) {\n" +
                        "        return call(Find" + domain + "ByIdTask.class, id).orElseThrow(() -> new NotFoundException(Messages." + domain.toUpperCase() + "_NOT_FOUND));\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

        }

        public static class Task {
            public static void generate(PsiDirectory dir, String path, String domain) {
                PsiDirectory directory = createDirectory(dir, "task");
                browse(directory, path, domain);
                save(directory, path, domain);
                delete(directory, path, domain);
                findById(directory, path, domain);
            }

            private static void browse(PsiDirectory dir, String path, String domain) {
                String clazz = "Browse" + domain + "Task";
                String content = "import " + path + ".payload.Browse" + domain + "Request;\n" +
                        "import " + path + ".payload." + domain + ";\n" +
                        "import " + path + ".repository." + domain + "Repository;\n" +
                        "import net.trellisframework.data.core.task.RepositoryTask1;\n" +
                        "import org.springframework.data.domain.Page;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements RepositoryTask1<" + domain + "Repository, Page<" + domain + ">, Browse" + domain + "Request> {\n\n" +
                        "    public Page<" + domain + "> execute(Browse" + domain + "Request request) {\n" +
                        "        return plainToClass(getRepository().findAll(request), " + domain + ".class);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void save(PsiDirectory dir, String path, String domain) {
                String clazz = "Save" + domain + "Task";
                String content = "import " + path + ".model." + domain + "Entity;\n" +
                        "import " + path + ".repository." + domain + "Repository;\n" +
                        "import net.trellisframework.data.core.task.RepositoryTask1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements RepositoryTask1<" + domain + "Repository, " + domain + "Entity, " + domain + "Entity> {\n\n" +
                        "    public " + domain + "Entity execute(" + domain + "Entity entity) {\n" +
                        "        return getRepository().save(entity);\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void delete(PsiDirectory dir, String path, String domain) {
                String clazz = "Delete" + domain + "Task";
                String content = "import " + path + ".model." + domain + "Entity;\n" +
                        "import " + path + ".repository." + domain + "Repository;\n" +
                        "import net.trellisframework.data.core.task.RepositoryTask1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements RepositoryTask1<" + domain + "Repository, Void, " + domain + "Entity> {\n\n" +
                        "    public Void execute(" + domain + "Entity entity) {\n" +
                        "        getRepository().delete(entity);\n" +
                        "        return null;\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

            private static void findById(PsiDirectory dir, String path, String domain) {
                String clazz = "Find" + domain + "ByIdTask";
                String content = "import " + path + ".model." + domain + "Entity;\n" +
                        "import " + path + ".repository." + domain + "Repository;\n" +
                        "import net.trellisframework.data.core.task.RepositoryTask1;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "import java.util.Optional;\n\n" +
                        "@Service\n" +
                        "public class " + clazz + " implements RepositoryTask1<" + domain + "Repository, Optional<" + domain + "Entity>, String> {\n\n" +
                        "    public Optional<" + domain + "Entity> execute(String id) {\n" +
                        "        return Optional.ofNullable(id).flatMap(x -> getRepository().findById(id));\n" +
                        "    }\n\n}";
                createFile(dir, clazz + ".java", content);
            }

        }

    }

    public static class File {

        public static void findAndReplace(java.io.File folder, String oldString, String newString) {
            if (folder.isDirectory()) {
                java.io.File[] files = folder.listFiles();
                if (files != null) {
                    for (java.io.File file : files) {
                        findAndReplace(file, oldString, newString);
                    }
                }
            } else if (folder.isFile()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(folder));
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append(System.lineSeparator());
                    }
                    reader.close();
                    String fileContent = content.toString();
                    fileContent = replace(oldString, newString, fileContent);

                    BufferedWriter writer = new BufferedWriter(new FileWriter(folder));
                    writer.write(fileContent);
                    writer.close();

                    String fileName = folder.getName();
                    if (fileName.contains(oldString)) {
                        String newFileName = replace(oldString, newString, fileName);
                        java.io.File newFile = new java.io.File(folder.getParentFile(), newFileName);
                        folder.renameTo(newFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static String replace(String searchString, String replacement, String text) {
            return StringUtils.replaceIgnoreCase(replaceWords(searchString, replacement, text), searchString.toLowerCase(), replacement.toLowerCase());
        }

        private static String replaceWords(String searchString, String replacement, String text) {
            // Replace for Pascal Case
            String quotedSearchValue = Pattern.quote(searchString);
            String pascalCase = Character.toUpperCase(quotedSearchValue.charAt(0)) + quotedSearchValue.substring(1);
            String pascalCaseReplace = Character.toUpperCase(replacement.charAt(0)) + replacement.substring(1);
            text = text.replaceAll(pascalCase, pascalCaseReplace);

            // Replace for Upper Case
            String upperCase = quotedSearchValue.toUpperCase();
            String upperCaseReplace = replacement.toUpperCase();
            text = text.replaceAll(upperCase, upperCaseReplace);

            // Replace for Camel Case
            String camelCase = Character.toLowerCase(quotedSearchValue.charAt(0)) + quotedSearchValue.substring(1);
            String camelCaseReplace = Character.toLowerCase(replacement.charAt(0)) + replacement.substring(1);
            text = text.replaceAll(camelCase, camelCaseReplace);

            return text;
        }

    }
}
