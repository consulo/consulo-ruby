package consulo.ruby.impl.refactoring.introduceVariable;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.CheckBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.TextBoxWithHistory;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.DialogDescriptor;
import consulo.ui.ex.dialog.DialogValue;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.LabeledBuilder;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.function.Function;

/**
 * Asks for the name of the new variable and whether all the occurrences are replaced.
 */
public class RubyIntroduceVariableDialogDescriptor extends DialogDescriptor {
    private final int myOccurrencesCount;
    private final List<String> mySuggestedNames;
    private final Function<String, LocalizeValue> myNameChecker;

    private TextBoxWithHistory myNameBox;
    private CheckBox myReplaceAllBox;
    private Label myErrorLabel;

    /**
     * @param nameChecker the error for a name, empty if the name may be used
     */
    public RubyIntroduceVariableDialogDescriptor(int occurrencesCount, @Nonnull List<String> suggestedNames, @Nonnull Function<String, LocalizeValue> nameChecker) {
        super(RubyApiLocalize.refactoringIntroduceVariableDialogTitle());
        myOccurrencesCount = occurrencesCount;
        mySuggestedNames = suggestedNames;
        myNameChecker = nameChecker;
    }

    @Nonnull
    @Override
    @RequiredUIAccess
    public Component createCenterComponent(@Nonnull Disposable uiDisposable) {
        myNameBox = TextBoxWithHistory.create(mySuggestedNames.isEmpty() ? "" : mySuggestedNames.get(0));
        myNameBox.setHistory(mySuggestedNames);
        myNameBox.addValueListener(event -> {
            updateError();
            updateOkButtonState();
        });

        myReplaceAllBox = CheckBox.create(RubyApiLocalize.refactoringIntroduceVariableReplaceAll(myOccurrencesCount));
        myReplaceAllBox.setEnabled(myOccurrencesCount > 1);

        myErrorLabel = Label.create(LocalizeValue.empty());

        VerticalLayout layout = VerticalLayout.create();
        layout.add(LabeledBuilder.filled(RubyApiLocalize.refactoringIntroduceVariableName(), myNameBox));
        layout.add(myReplaceAllBox);
        layout.add(myErrorLabel);
        updateError();
        return layout;
    }

    @Override
    @RequiredUIAccess
    public Component getPreferredFocusedComponent() {
        return myNameBox;
    }

    @Override
    public boolean doUpdateOkButtonState() {
        return myNameBox != null && myNameChecker.apply(getName()).isEmpty();
    }

    @Override
    public DialogValue getOkValue() {
        return new RubyIntroduceVariableResult(getName(), Boolean.TRUE.equals(myReplaceAllBox.getValue()));
    }

    @RequiredUIAccess
    private void updateError() {
        myErrorLabel.setText(myNameChecker.apply(getName()));
    }

    private String getName() {
        String value = myNameBox.getValue();
        return value == null ? "" : value.trim();
    }
}
