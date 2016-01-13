package org.activityinfo.ui.client.component.form;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.BuiltinFields;
import org.activityinfo.model.lock.LockEvaluator;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.model.type.attachment.AttachmentValue;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.form.subform.SubFormTabsManipulator;
import org.activityinfo.ui.client.component.form.field.NullFieldWidget;
import org.activityinfo.ui.client.widget.DisplayWidget;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Displays a simple view of the form, where users can edit instances
 */
public class SimpleFormPanel implements DisplayWidget<FormInstance>, FormWidgetCreator.FieldUpdated {

    private final FlowPanel panel;
    private final ScrollPanel scrollPanel;
    private final boolean withScroll;

    private final FormModel model;
    private final ResourceLocator locator;
    private final RelevanceHandler relevanceHandler;
    private final FormWidgetCreator widgetCreator;
    private final FormActions formActions;

    /**
     * The original, unmodified instance
     */
    private Resource instance;

    /**
     * A new version of the instance, being updated by the user
     */
    private FormInstance workingInstance;

    private FormClass formClass;
    private ResourceLocator locator;
    private final RelevanceHandler relevanceHandler;

    // validation form class is used to refer to "top-level" form class.
    // For example "Properties panel" renders current type-formClass but in order to validate expression we need
    // reference to formClass that is currently editing on FormDesigner.
    // it can be null.
    private FormClass validationFormClass = null;

    public SimpleFormPanel(ResourceLocator locator, FieldContainerFactory containerFactory,
                           FormFieldWidgetFactory widgetFactory) {
        this(locator, containerFactory, widgetFactory, true);
    }

    public SimpleFormPanel(ResourceLocator locator, FieldContainerFactory containerFactory,
                           FormFieldWidgetFactory widgetFactory, boolean withScroll) {
        FormPanelStyles.INSTANCE.ensureInjected();

        Preconditions.checkNotNull(locator);
        Preconditions.checkNotNull(containerFactory);
        Preconditions.checkNotNull(widgetFactory);

        this.locator = locator;
        this.model = new FormModel(locator);
        this.withScroll = withScroll;
        this.relevanceHandler = new RelevanceHandler(this);
        this.widgetCreator = new FormWidgetCreator(model, containerFactory, widgetFactory);

        this.panel = new FlowPanel();
        this.panel.setStyleName(FormPanelStyles.INSTANCE.formPanel());
        this.scrollPanel = new ScrollPanel(panel);
        this.formActions = new FormActions(locator, this);
    }

    public FormModel getModel() {
        return model;
    }

    public Promise<Void> show(final Resource instance) {
        return show(FormInstance.fromResource(instance));
    }

    @Override
    public Promise<Void> show(final FormInstance instance) {
        model.setWorkingRootInstance(instance);
        return model.loadFormClassWithDependentSubForms(instance.getClassId()).then(new Function<Void, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable Void input) {
                return buildForm(model.getRootFormClass());
            }
        }).join(new Function<Promise<Void>, Promise<Void>>() {
            @Nullable
            @Override
            public Promise<Void> apply(@Nullable Promise<Void> input) {
                return setValue(instance);
            }
        });
    }

    private Promise<Void> buildForm(final FormClass formClass) {
        this.relevanceHandler.formClassChanged();

        try {
            return widgetCreator.createWidgets(formClass, this).then(new Function<Void, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable Void input) {
                    addFormElements(formClass, 0);
                    return null;
                }
            });

        } catch (Throwable caught) {
            return Promise.rejected(caught);
        }
    }

    public Promise<Void> setValue(FormInstance instance) {
        model.setWorkingRootInstance(instance);

        List<Promise<Void>> tasks = Lists.newArrayList();

        for (FieldContainer container : widgetCreator.getContainers().values()) {
            FormField field = container.getField();
            FieldValue value = model.getWorkingRootInstance().get(field.getId(), field.getType());

            if (value != null && value.getTypeClass() == field.getType().getTypeClass()) {
                tasks.add(container.getFieldWidget().setValue(value));
            } else {
                container.getFieldWidget().clearValue();
            }
            container.setValid();
        }

        return Promise.waitAll(tasks).then(new Function<Void, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable Void input) {
                relevanceHandler.onValueChange(); // invoke relevance handler once values are set
                return null;
            }
        });
    }

    private void addFormElements(FormElementContainer container, int depth) {
        for (FormElement element : container.getElements()) {
            if (element instanceof FormSection) {
                panel.add(createHeader(depth, element.getLabel()));
                addFormElements((FormElementContainer) element, depth + 1);
            } else if (element instanceof FormField) {
                FormField formField = (FormField) element;
                if (formField.isVisible()) {
                    if (formField.getType() instanceof SubFormType) {
                        FormClass subForm = getModel().getSubFormByOwnerFieldId(formField.getId());
                        final SubFormTabsManipulator subFormTabsManipulator = new SubFormTabsManipulator(locator);

                        panel.add(createHeader(depth, subForm.getLabel()));
                        panel.add(subFormTabsManipulator.getPresenter().getView());

                        subFormTabsManipulator.show(subForm, model);
                        addFormElements(subForm, depth + 1);
                    } else {
                        panel.add(widgetCreator.get(formField.getId()));
                    }
                }
            }
        }
    }

    public void onFieldUpdated(FormField field, FieldValue newValue) {
        FormInstance workingInstance = model.getWorkingInstance(field.getId());
        if (!Objects.equals(workingInstance.get(field.getId()), newValue)) {
            workingInstance.set(field.getId(), newValue);
            relevanceHandler.onValueChange(); // skip handler must be applied after workingInstance is updated
        }
        validateField(widgetCreator.get(field.getId()));
    }

    private boolean validateField(FieldContainer container) {
        FormField field = container.getField();
        FieldValue value = getCurrentValue(field);
        if (value != null && value.getTypeClass() != field.getType().getTypeClass()) {
            value = null;
        }

        Optional<Boolean> validatedBuiltInDates = validateBuiltinDates(container, field);
        if (validatedBuiltInDates.isPresent()) {
            return validatedBuiltInDates.get();
        }

        if (field.isRequired() && isEmpty(value) && field.isVisible() && !container.getFieldWidget().isReadOnly()) { // if field is not visible user doesn't have chance to fix it
            container.setInvalid(I18N.CONSTANTS.requiredFieldMessage());
            return false;
        } else {
            container.setValid();
            return true;
        }
    }

    private Optional<Boolean> validateBuiltinDates(FieldContainer container, FormField field) {
        if (BuiltinFields.isBuiltInDate(field.getId())) {
            DateRange dateRange = BuiltinFields.getDateRange(workingInstance, formClass);

            if (!formClass.getLocks().isEmpty()) {
                if (new LockEvaluator(formClass).isLockedSilently(workingInstance)) {
                    getFieldContainer(BuiltinFields.getStartDateField(formClass).getId()).setInvalid(I18N.CONSTANTS.siteIsLocked());
                    getFieldContainer(BuiltinFields.getEndDateField(formClass).getId()).setInvalid(I18N.CONSTANTS.siteIsLocked());
                    return Optional.of(false);
                }
            }

            if (!dateRange.isValidWithNull()) {
                container.setInvalid(I18N.CONSTANTS.inconsistentDateRangeWarning());
                return Optional.of(false);
            } else {
                if (dateRange.isValid()) {
                    getFieldContainer(BuiltinFields.getStartDateField(formClass).getId()).setValid();
                    getFieldContainer(BuiltinFields.getEndDateField(formClass).getId()).setValid();
                    return Optional.of(true);
                }
            }
        }
        return Optional.absent();
    }

    private boolean isEmpty(FieldValue value) {
        return value == null ||
                (value instanceof EnumValue && ((EnumValue) value).getResourceIds().isEmpty()) ||
                (value instanceof ReferenceValue && ((ReferenceValue) value).getResourceIds().isEmpty()) ||
                (value instanceof AttachmentValue && ((AttachmentValue) value).getValues().isEmpty());
    }

    public boolean validate() {
        boolean valid = true;
        for (FieldContainer container : this.widgetCreator.getContainers().values()) {
            if (!validateField(container)) {
                valid = false;
            }
        }
        return valid;
    }

    private FieldValue getCurrentValue(FormField field) {
        return model.getWorkingInstance(field.getId()).get(field.getId());
    }

    private static Widget createHeader(int depth, String header) {
        String hn = "h" + (3 + depth);
        return new HTML("<" + hn + ">" + SafeHtmlUtils.htmlEscape(header) + "</" + hn + ">");
    }

    @Override
    public Widget asWidget() {
        return withScroll ? scrollPanel : panel;
    }

    public FormWidgetCreator getWidgetCreator() {
        return widgetCreator;
    }

    public ResourceLocator getLocator() {
        return locator;
    }

    public FormActions getFormActions() {
        return formActions;
    }

    public void setValidationFormClass(FormClass validationFormClass) {
        this.validationFormClass = validationFormClass;
    }

    public FormClass getValidationFormClass() {
        return validationFormClass;
    }

    public RelevanceHandler getRelevanceHandler() {
        return relevanceHandler;
    }
}
