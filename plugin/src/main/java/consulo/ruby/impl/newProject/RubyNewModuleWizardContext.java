package consulo.ruby.impl.newProject;

import consulo.content.bundle.Sdk;
import consulo.module.creation.NewModuleWizardContextBase;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyNewModuleWizardContext extends NewModuleWizardContextBase {
    private final RubyProjectTemplate myTemplate;
    @Nullable
    private Sdk mySdk;

    public RubyNewModuleWizardContext(boolean isNewProject, RubyProjectTemplate template) {
        super(isNewProject);
        myTemplate = template;
    }

    public RubyProjectTemplate getTemplate() {
        return myTemplate;
    }

    @Nullable
    public Sdk getSdk() {
        return mySdk;
    }

    public void setSdk(@Nullable Sdk sdk) {
        mySdk = sdk;
    }
}
