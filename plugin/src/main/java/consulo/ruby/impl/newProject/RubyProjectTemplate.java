package consulo.ruby.impl.newProject;

import consulo.localize.LocalizeValue;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public enum RubyProjectTemplate {
    APPLICATION(RubyApiLocalize.newProjectApplication(), RubyApiIconGroup.rubyRuby()),
    GEM(RubyApiLocalize.newProjectGem(), RubyApiIconGroup.rubyRuby()),
    RAILS(RubyApiLocalize.newProjectRails(), RubyApiIconGroup.railsRails());

    private final LocalizeValue myName;
    private final Image myIcon;

    RubyProjectTemplate(LocalizeValue name, Image icon) {
        myName = name;
        myIcon = icon;
    }

    public LocalizeValue getName() {
        return myName;
    }

    public Image getIcon() {
        return myIcon;
    }
}
