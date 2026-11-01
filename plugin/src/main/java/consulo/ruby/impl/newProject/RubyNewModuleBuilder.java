package consulo.ruby.impl.newProject;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.creation.scratch.NewModuleBuilder;
import consulo.module.creation.scratch.NewModuleContext;
import consulo.module.creation.scratch.NewModuleContextGroup;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.api.localize.RubyApiLocalize;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl(id = "ruby")
public class RubyNewModuleBuilder implements NewModuleBuilder {
    @Override
    public void setupContext(NewModuleContext context) {
        NewModuleContextGroup group = context.addGroup("ruby", RubyApiLocalize.newProjectGroup(), RubyApiIconGroup.rubyRuby());
        RubyProjectTemplate[] templates = RubyProjectTemplate.values();
        for (int i = 0; i < templates.length; i++) {
            RubyProjectTemplate template = templates[i];
            group.add(template.getName(), template.getIcon(), i, new RubyNewModuleBuilderProcessor(template));
        }
    }
}
