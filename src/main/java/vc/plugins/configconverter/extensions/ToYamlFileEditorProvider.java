package vc.plugins.configconverter.extensions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import vc.plugins.configconverter.editor.ToYamlConfigEditor;

import java.util.Arrays;
import java.util.List;

/**
 * @author xiaolei.fu
 * @version 1.0.0
 * @since 1.0.0
 */
public class ToYamlFileEditorProvider implements FileEditorProvider, DumbAware {
    private static final Logger LOG = Logger.getInstance("#vc.plugins.configconverter.extensions.FileEditorProvider");
    private static final List<String> SUPPORT_EXT_NAME = Arrays.asList("properties", "toml");


    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return isConfigFile(project, virtualFile);
    }

    private boolean isConfigFile(@NotNull Project project, @NotNull VirtualFile file) {
        String fileName = file.getName();
        if (!fileName.contains(".")) {
            return false;
        }
        String fileExtName = fileName.substring(fileName.lastIndexOf(".") + 1);
        return SUPPORT_EXT_NAME.contains(fileExtName);
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new ToYamlConfigEditor(project, virtualFile);
    }

    @Override
    public @NotNull
    @NonNls String getEditorTypeId() {
        return "ConfigConverter";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
