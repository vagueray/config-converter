package vc.plugins.configconverter.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vc.plugins.configconverter.gui.ConfigGui;
import vc.plugins.configconverter.utils.YamlUtil;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author xiaolei.fu
 * @version 1.0.0
 * @since 1.0.0
 */
public class ToYamlConfigEditor extends UserDataHolderBase implements FileEditor {
    private final Project project;
    private final VirtualFile file;
    private final ConfigGui gui;

    public ToYamlConfigEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        gui = new ConfigGui(project, file, YamlUtil::propertiesToYaml);
    }

    @Override
    public VirtualFile getFile() {
        return this.file;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return gui.getRootComponent();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return gui.getPreferredFocusedComponent();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title)
    @NotNull String getName() {
        return "ToYaml";
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    }


    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {

    }

    @Override
    public void dispose() {
        gui.dispose();
    }

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }
}
