package com.google.idea.blaze.rust.sync;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.primitives.LanguageClass;
import com.google.idea.blaze.base.model.primitives.WorkspaceType;
import com.google.idea.blaze.base.plugin.PluginUtils;
import com.google.idea.blaze.base.projectview.ProjectView;
import com.google.idea.blaze.base.projectview.ProjectViewEdit;
import com.google.idea.blaze.base.projectview.ProjectViewSet;
import com.google.idea.blaze.base.projectview.ProjectViewSet.ProjectViewFile;
import com.google.idea.blaze.base.projectview.section.ListSection;
import com.google.idea.blaze.base.projectview.section.ScalarSection;
import com.google.idea.blaze.base.projectview.section.sections.AdditionalLanguagesSection;
import com.google.idea.blaze.base.projectview.section.sections.WorkspaceTypeSection;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.output.IssueOutput;
import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.blaze.base.sync.BlazeSyncManager;
import com.google.idea.blaze.base.sync.BlazeSyncPlugin;
import com.google.idea.blaze.base.sync.projectview.WorkspaceLanguageSettings;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.NavigatableAdapter;
import com.intellij.util.PlatformUtils;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Unlike most of the go-specific code, will be run even if the JetBrains Go plugin isn't enabled.
 */
public class AlwaysPresentRustSyncPlugin implements BlazeSyncPlugin {

    private static final String RUST_PLUGIN_ID = "org.rust.lang";

    @Override
    public Set<LanguageClass> getSupportedLanguagesInWorkspace(WorkspaceType workspaceType) {
        return ImmutableSet.of(LanguageClass.RUST);
    }

    @Override
    public ImmutableList<String> getRequiredExternalPluginIds(Collection<LanguageClass> languages) {
        return languages.contains(LanguageClass.RUST)
                ? ImmutableList.of(RUST_PLUGIN_ID)
                : ImmutableList.of();
    }

    @Override
    public boolean validate(
            Project project, BlazeContext context, BlazeProjectData blazeProjectData) {
        if (!blazeProjectData.getWorkspaceLanguageSettings().isLanguageActive(LanguageClass.RUST)
                || PluginUtils.isPluginEnabled(RUST_PLUGIN_ID)) {
            return true;
        }
        IssueOutput.error(
                "Bazel Rust support requires the Rust plugin. Click here to install/enable the JetBrains Rust "
                        + "plugin, then restart the IDE")
                .navigatable(PluginUtils.installOrEnablePluginNavigable(RUST_PLUGIN_ID))
                .submit(context);
        return true;
    }

    @Override
    public boolean validateProjectView(
            @Nullable Project project,
            BlazeContext context,
            ProjectViewSet projectViewSet,
            WorkspaceLanguageSettings workspaceLanguageSettings) {
        return true;
    }
}
