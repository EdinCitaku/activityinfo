package org.activityinfo.store.mysql.mapping;

import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.NarrativeValue;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.store.mysql.Join;
import org.activityinfo.store.mysql.collections.Activity;
import org.activityinfo.store.mysql.collections.ActivityField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.activityinfo.model.legacy.CuidAdapter.*;


public class ActivityTableMappingBuilder {

    public static final int REPORT_ONCE = 0;
    public static final int REPORT_MONTHLY = 1;

    
    private static final Join PERIOD_JOIN = new Join("period", 
            "LEFT JOIN reportingperiod period ON (period.SiteId=base.siteId)");

    private Activity activity;
    private ResourceId classId;
    
    private String baseFromClause;
    private String baseFilter;
    private String baseTable;
    private FormClass formClass;
    private List<FieldMapping> mappings = Lists.newArrayList();
    private PrimaryKeyMapping primaryKeyMapping;


    public ActivityTableMappingBuilder() {
    }

    public static ActivityTableMappingBuilder site(Activity activity) {
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.activity = activity;
        mapping.baseTable = "site";
        mapping.baseFromClause = "site base";
        mapping.baseFilter = "base.dateDeleted is NULL AND base.activityId=" + activity.getId();
        mapping.classId = CuidAdapter.activityFormClass(activity.getId());
        mapping.formClass = new FormClass(mapping.classId);
        mapping.formClass.setLabel(activity.getName());
        mapping.formClass.setOwnerId(CuidAdapter.databaseId(activity.getDatabaseId()));
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.SITE_DOMAIN, "siteId");

        if(activity.getReportingFrequency() == Activity.REPORT_ONCE) {
            mapping.addDateFields();
        }

        mapping.addPartnerField();
    //    mapping.addProjectField();

        if(activity.hasLocationType()) {
            mapping.addLocationField();
        }

        for(ActivityField field : activity.getAttributeAndIndicatorFields()) {
            mapping.addIndicatorOrAttributeField(field);
        }
        
        mapping.addComments();

        return mapping;
    }

    public static ActivityTableMappingBuilder reportingPeriod(Activity activity) {
        ActivityTableMappingBuilder mapping = new ActivityTableMappingBuilder();
        mapping.activity = activity;
        mapping.baseTable = "reportingperiod";
        mapping.baseFromClause = "reportingperiod base LEFT JOIN site on (site.siteId=base.siteId)";
        mapping.baseFilter = "site.dateDeleted IS NULL AND site.activityId=" + activity.getId();
        mapping.classId = CuidAdapter.reportingPeriodFormClass(activity.getId());
        mapping.formClass = new FormClass(mapping.classId);
        mapping.formClass.setLabel(activity.getName() + " Monthly Reports");
        mapping.formClass.setOwnerId(CuidAdapter.activityFormClass(activity.getId()));
        mapping.primaryKeyMapping = new PrimaryKeyMapping(CuidAdapter.MONTHLY_REPORT, "reportingPeriodId");
        
        mapping.addSiteField();
        mapping.addDateFields();

        for (ActivityField indicatorField : activity.getIndicatorFields()) {
            mapping.addIndicatorOrAttributeField(indicatorField);
        }
        
        return mapping;
    }


    public void addDateFields() {
        FormField date1 = new FormField(field(classId, START_DATE_FIELD))
                .setLabel("Start Date")
                .setCode("date1")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(date1);
        mappings.add(new FieldMapping(date1, "date1", Mapping.DATE));

        FormField date2 = new FormField(field(classId, END_DATE_FIELD))
                .setLabel("End Date")
                .setCode("date2")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);
        formClass.addElement(date2);
        mappings.add(new FieldMapping(date2, "date2", Mapping.DATE));
    }
    
    public void addSiteField() {
        FormField siteField = new FormField(field(classId, SITE_FIELD));
        siteField.setLabel("Site");
        siteField.setCode("site");
        siteField.setType(ReferenceType.single(CuidAdapter.activityFormClass(activity.getId())));
        siteField.setRequired(true);
        
        formClass.addElement(siteField);
        mappings.add(new FieldMapping(siteField, "siteId", new ForeignKeyMapping(SITE_DOMAIN)));
    }
    
    public void addLocationField() {
        FormField locationField = new FormField(field(classId, LOCATION_FIELD));
        locationField.setLabel(activity.getLocationTypeName());
        locationField.setCode("location");
        locationField.setType(ReferenceType.single(activity.getLocationFormClassId()));
        locationField.setRequired(true);
        
        formClass.addElement(locationField);
        mappings.add(new FieldMapping(locationField, "locationId", new ForeignKeyMapping(LOCATION_DOMAIN)));
    }

    public void addPartnerField() {

        FormField partnerField = new FormField(field(classId, PARTNER_FIELD))
                .setLabel("Partner")
                .setCode("partner")
                .setType(ReferenceType.single(CuidAdapter.partnerFormClass(activity.getDatabaseId())))
                .setRequired(true);
        formClass.addElement(partnerField);
        mappings.add(new FieldMapping(partnerField, "partnerId", new ForeignKeyMapping(PARTNER_DOMAIN)));
    }

    public void addProjectField() {
        FormField partnerField = new FormField(field(classId, PARTNER_FIELD))
                .setLabel("Project")
                .setCode("project")
                .setType(ReferenceType.single(activity.getProjectFormClassId()))
                .setRequired(false);
        formClass.addElement(partnerField);
        mappings.add(new FieldMapping(partnerField, "projectId", new ForeignKeyMapping(PROJECT_DOMAIN)));
    }
    
    public void addComments(){
        FormField commentsField = new FormField(field(classId, COMMENT_FIELD))
                .setLabel("Comments")
                .setCode("comments")
                .setType(NarrativeType.INSTANCE)
                .setRequired(false);
        
        formClass.addElement(commentsField);
        mappings.add(new FieldMapping(commentsField, "comments", new FieldValueMapping() {
            @Override
            public FieldValue extract(ResultSet rs, int index) throws SQLException {
                return NarrativeValue.valueOf(rs.getString(index));
            }

            @Override
            public Collection<?> toParameters(FieldValue value) {
                if(value instanceof NarrativeValue) {
                    return Arrays.asList(((NarrativeValue) value).asString());
                } else {
                    return Arrays.asList(null);
                }
            }
        }));
    }

    public TableMapping build() {
        return new TableMapping("site", baseFromClause, baseFilter, primaryKeyMapping, mappings, formClass,
                DeleteMethod.SOFT_BY_DATE, Collections.<String, Object>emptyMap());
    }

    public void addIndicatorOrAttributeField(ActivityField field) {
        formClass.addElement(field.getFormField());
    }

}
