package consulo.ruby.impl.bundler.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.ui.AbstractExternalSystemToolWindowFactory;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerToolWindowFactory extends AbstractExternalSystemToolWindowFactory {
    public BundlerToolWindowFactory() {
        super(BundlerConstants.SYSTEM_ID);
    }

    @Nonnull
    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return RubyApiIconGroup.rubyRubygems();
    }
}
