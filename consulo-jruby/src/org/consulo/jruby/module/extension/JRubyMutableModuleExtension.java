package org.consulo.jruby.module.extension;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.consulo.module.extension.MutableModuleExtensionWithSdk;
import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.consulo.module.extension.ui.ModuleExtensionWithSdkPanel;
import org.consulo.ruby.module.extension.RubyModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 25.09.13.
 */
public class JRubyMutableModuleExtension extends JRubyModuleExtension implements RubyModuleExtension<JRubyModuleExtension>, MutableModuleExtensionWithSdk<JRubyModuleExtension>
{
	private JRubyModuleExtension myOriginalExtension;

	public JRubyMutableModuleExtension(@NotNull String id, @NotNull Module module, @NotNull JRubyModuleExtension originalExtension)
	{
		super(id, module);
		myOriginalExtension = originalExtension;
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull ModifiableRootModel modifiableRootModel, @Nullable Runnable runnable)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new ModuleExtensionWithSdkPanel(this, runnable), BorderLayout.NORTH);
		return panel;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified()
	{
		return isModifiedImpl(myOriginalExtension);
	}

	@Override
	public void commit()
	{
		myOriginalExtension.commit(this);
	}
}
