package consulo.ruby.impl.rails.paramDef;

import consulo.annotation.component.ExtensionImpl;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.HashParamDef;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ListParamDef;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDef;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefManager;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefProvider;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ResolvingParamDependency;

/**
 * What the arguments of the Rails controller and view methods refer to: actions, controllers, templates, partials,
 * layouts, helpers, associated models and assets. Methods are registered under their Rails 7+ module and their older one.
 */
@ExtensionImpl
public class RailsParamDefProvider implements ParamDefProvider {
    @Override
    public void registerParamDefs(@Nonnull ParamDefManager manager) {
        ParamDef render = hash("action", actionRef(false), "template", new ViewRefParamDef(), "partial", new PartialRefParamDef(), "layout", new LayoutRefParamDef());
        register(manager, render, "AbstractController::Rendering::render", "ActionController::Rendering::render", "ActionView::Rendering::render", "ActionController::Base::render");
        register(manager, render, "AbstractController::Rendering::render_to_string", "ActionController::Base::render_to_string");
        register(manager, render, "ActionView::Helpers::RenderingHelper::render");

        ParamDef urlOptions = hash("controller", new ControllerRefParamDef(), "action", actionRef(true));
        register(manager, urlOptions, "ActionController::Redirecting::redirect_to", "ActionController::Base::redirect_to");
        register(manager, urlOptions, "ActionDispatch::Routing::UrlFor::url_for", "ActionView::RoutingUrlFor::url_for", "ActionController::Base::url_for");
        register(manager, urlOptions, "ActionView::Helpers::FormTagHelper::form_tag");
        manager.registerParamDef("ActionView::Helpers::UrlHelper::link_to", null, urlOptions);

        ParamDef callbackOptions = hash("only", actionRef(false), "except", actionRef(false));
        for (String callback : new String[]{"before_action", "after_action", "around_action", "skip_before_action", "append_before_action", "prepend_before_action"}) {
            manager.registerParamDef("AbstractController::Callbacks::ClassMethods::" + callback, new MethodRefParamDef(false, LookupItemType.Symbol, null), callbackOptions);
        }
        manager.registerParamDef("ActionController::Filters::ClassMethods::append_before_filter", new MethodRefParamDef(false, LookupItemType.Symbol, null), callbackOptions);

        register(manager, new LayoutRefParamDef(), "ActionView::Layouts::ClassMethods::layout", "ActionController::Layout::ClassMethods::layout");
        register(manager, new ListParamDef(new HelperRefParamDef()), "AbstractController::Helpers::ClassMethods::helper", "ActionController::Helpers::ClassMethods::helper");

        manager.registerParamDef("ActionView::Helpers::AssetTagHelper::stylesheet_link_tag", new ListParamDef(new AssetRefParamDef("css", "app/assets/stylesheets", "app/assets/builds", "public/stylesheets")));
        manager.registerParamDef("ActionView::Helpers::AssetTagHelper::javascript_include_tag", new ListParamDef(new AssetRefParamDef("js", "app/javascript", "app/assets/javascripts", "app/assets/builds", "public/javascripts")));
        manager.registerParamDef("ActionView::Helpers::AssetTagHelper::image_tag", new AssetRefParamDef(null, "app/assets/images", "public/images"));

        String associations = "ActiveRecord::Associations::ClassMethods::";
        manager.registerParamDef(associations + "has_many", new ModelRefParamDef(true));
        manager.registerParamDef(associations + "has_and_belongs_to_many", new ModelRefParamDef(true));
        manager.registerParamDef(associations + "belongs_to", new ModelRefParamDef(false));
        manager.registerParamDef(associations + "has_one", new ModelRefParamDef(false));
    }

    private static ParamDef actionRef(boolean controllerDependent) {
        return new ControllerMethodRefParamDef(controllerDependent ? new ResolvingParamDependency("controller") : null);
    }

    private static HashParamDef hash(Object... keysAndValues) {
        HashParamDef hash = new HashParamDef();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            hash.add((String) keysAndValues[i], (ParamDef) keysAndValues[i + 1]);
        }
        return hash;
    }

    private static void register(@Nonnull ParamDefManager manager, @Nonnull ParamDef paramDef, @Nonnull String... methods) {
        for (String method : methods) {
            manager.registerParamDef(method, paramDef);
        }
    }
}
