package vc.plugins.configconverter.gui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author xiaolei.fu
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigGui implements Disposable {
    private final Project project;
    private final VirtualFile file;
    private final Function<Properties, String> func;

    private JBPanel<?> rootPanel;

    private JBTextArea jbTextArea;

    public ConfigGui(Project project, VirtualFile file, Function<Properties, String> func) {
        this.project = project;
        this.file = file;
        this.func = func;

        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
                new BulkFileListener() {
                    @Override
                    public void after(@NotNull List<? extends VFileEvent> events) {
                        events.stream()
                                .filter(v -> file.equals(v.getFile()))
                                .findAny()
                                .ifPresent(v -> refreshTextContent());
                    }
                });

        initPanel();
    }

    private void initPanel() {
        rootPanel = new JBPanel<>();
        rootPanel.setAutoscrolls(true);
        rootPanel.setAlignmentX(0);
        rootPanel.setAlignmentY(0);

        GridLayout layout = new GridLayout(1, 1);
        rootPanel.setLayout(layout);

        jbTextArea = new JBTextArea();
        jbTextArea.setEditable(false);
        jbTextArea.setAlignmentX(0);
        jbTextArea.setAlignmentY(0);
        rootPanel.add(jbTextArea);

        refreshTextContent();
    }

    private void refreshTextContent() {
        try {
            InputStream inputStream = this.file.getInputStream();
            Properties properties = new Properties();
            properties.load(inputStream);

            String apply = this.func.apply(properties);
            jbTextArea.setText(apply);
        } catch (Exception e) {
            jbTextArea.setText(e.toString());
        }
    }


    public JComponent getRootComponent() {
        return rootPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return rootPanel;
    }

    @Override
    public void dispose() {
    }
}
