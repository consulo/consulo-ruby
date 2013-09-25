package org.consulo.jruby.rails.module.extension;

import javax.swing.JComponent;

import org.consulo.module.extension.ModuleExtension;
import org.consulo.module.extension.MutableModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 25.09.13.
 */
public class JRubyOnRailsMutableModuleExtension extends JRubyOnRailsModuleExtension
		implements MutableModuleExtension<JRubyOnRailsModuleExtension>, ModuleExtension<JRubyOnRailsModuleExtension>
{
	private JRubyOnRailsModuleExtension myOriginalExtension;

	public JRubyOnRailsMutableModuleExtension(@NotNull String id, @NotNull Module module, @NotNull JRubyOnRailsModuleExtension originalExtension)
	{
		super(id, module);
		myOriginalExtension = originalExtension;
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull ModifiableRootModel modifiableRootModel, @Nullable Runnable runnable)
	{
		return null;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified()
	{
		return myOriginalExtension.isEnabled() != isEnabled();
	}

	@Override
	public void commit()
	{
		myOriginalExtension.commit(this);
	}
}