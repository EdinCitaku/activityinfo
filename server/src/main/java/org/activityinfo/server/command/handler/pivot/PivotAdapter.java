package org.activityinfo.server.command.handler.pivot;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.PivotSites;
import org.activityinfo.legacy.shared.command.result.Bucket;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.reports.content.DimensionCategory;
import org.activityinfo.legacy.shared.reports.content.TargetCategory;
import org.activityinfo.legacy.shared.reports.model.AdminDimension;
import org.activityinfo.legacy.shared.reports.model.AttributeGroupDimension;
import org.activityinfo.legacy.shared.reports.model.DateDimension;
import org.activityinfo.legacy.shared.reports.model.Dimension;
import org.activityinfo.model.expr.*;
import org.activityinfo.model.expr.functions.AndFunction;
import org.activityinfo.model.expr.functions.GreaterOrEqualFunction;
import org.activityinfo.model.expr.functions.LessOrEqualFunction;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.service.store.BatchingFormTreeBuilder;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnSetBuilder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.*;
import static org.activityinfo.model.expr.Exprs.*;

/**
 * Executes a legacy PivotSites query against the new API
 */
public class PivotAdapter {

    private static final Logger LOGGER = Logger.getLogger(PivotAdapter.class.getName());
    public static final String SITE_ID_KEY = "__site_id";

    private final IndicatorOracle indicatorOracle;
    private final CollectionCatalog catalog;
    private final PivotSites command;
    private final Filter filter;

    /**
     * Maps indicator ids to form class ids
     */
    private final List<ActivityMetadata> activities;
    private final Multimap<Integer, ActivityMetadata> databases = HashMultimap.create();

    private Map<ResourceId, FormTree> formTrees;

    private List<DimBinding> groupBy;

    private Optional<IndicatorDimBinding> indicatorDimension = Optional.absent();

    private Multimap<String, String> attributeFilters = HashMultimap.create();

    private final Map<Object, Accumulator> buckets = Maps.newHashMap();

    private final Stopwatch metadataTime = Stopwatch.createUnstarted();
    private final Stopwatch treeTime = Stopwatch.createUnstarted();
    private final Stopwatch queryTime = Stopwatch.createUnstarted();
    private final Stopwatch aggregateTime = Stopwatch.createUnstarted();


    public PivotAdapter(IndicatorOracle indicatorOracle, CollectionCatalog catalog, PivotSites command) throws InterruptedException {
        this.indicatorOracle = indicatorOracle;
        this.catalog = catalog;
        this.command = command;
        this.filter = command.getFilter();


        // Mapping from indicator id -> activityId
        metadataTime.start();
        activities = indicatorOracle.fetch(filter);
        for (ActivityMetadata activity : activities) {
            databases.put(activity.getDatabaseId(), activity);
        }
        metadataTime.stop();

        // Query form trees: needed to determine attribute mapping
        formTrees = queryFormTrees();

        findAttributeFilterNames();

        // Define the dimensions we're pivoting by
        groupBy = new ArrayList<>();
        for (Dimension dimension : command.getDimensions()) {
            if(dimension.getType() == DimensionType.Indicator) {
                indicatorDimension = Optional.of(new IndicatorDimBinding());
            } else {
                groupBy.add(bindingFor(dimension));
            }
        }
    }

    private Map<ResourceId, FormTree> queryFormTrees() {

        treeTime.start();

        Set<ResourceId> formIds = new HashSet<>();
        for(ActivityMetadata activity : activities) {
            formIds.add(activity.getFormClassId());
            for (ActivityMetadata linkedActivity : activity.getLinkedActivities()) {
                formIds.add(linkedActivity.getFormClassId());
            }
        }

        BatchingFormTreeBuilder builder = new BatchingFormTreeBuilder(catalog);
        Map<ResourceId, FormTree> trees = builder.queryTrees(formIds);

        treeTime.stop();

        return trees;

    }

    /**
     * Maps attribute filter ids to their attribute group id and name.
     *
     * Attribute filters are _SERIALIZED_ as only the integer ids of the required attributes,
     * but they are actually applied by _NAME_ to all forms in the query.
     *
     */
    private void findAttributeFilterNames() {

        Set<Integer> attributeIds = filter.getRestrictions(DimensionType.Attribute);
        if(attributeIds.isEmpty()) {
            return;
        }

        for (FormTree formTree : formTrees.values()) {
            for (FormTree.Node node : formTree.getLeaves()) {
                if(node.isEnum()) {
                    EnumType type = (EnumType) node.getType();
                    for (EnumItem enumItem : type.getValues()) {
                        int attributeId = CuidAdapter.getLegacyIdFromCuid(enumItem.getId());
                        if(attributeIds.contains(attributeId)) {
                            attributeFilters.put(node.getField().getLabel(), enumItem.getLabel());
                            attributeIds.remove(attributeId);
                            if(attributeIds.isEmpty()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }


    private DimBinding bindingFor(Dimension dimension) {
        switch (dimension.getType()) {
            case Partner:
                return new PartnerDimBinding();
            case Project:
                return new ProjectDimBinding();

            case Activity:
                return new ActivityDimBinding();
            case ActivityCategory:
                return new ActivityCategoryDimBinding();

            case Date:
                return new DateDimBinding((DateDimension) dimension);

            case AttributeGroup:
                return new AttributeDimBinding((AttributeGroupDimension) dimension, formTrees.values());

            case AdminLevel:
                return new AdminDimBinding((AdminDimension) dimension);

            case Location:
                return new LocationDimBinding();

            case Site:
                return new SiteDimBinding();

            case Database:
                return new DatabaseDimBinding();


            case Target:
                return new TargetDimBinding();

            case Status:
            case Attribute:
                break;
        }
        throw new UnsupportedOperationException("Unsupported dimension " + dimension);
    }

    public PivotSites.PivotResult execute() {

        for (ActivityMetadata activity : activities) {
            switch (command.getValueType()) {
                case INDICATOR:
                    executeIndicatorValuesQuery(activity);
                    for (ActivityMetadata linkedActivity : activity.getLinkedActivities()) {
                        executeIndicatorValuesQuery(linkedActivity);
                    }
                    break;
                case TOTAL_SITES:
                    executeSiteCountQuery(activity);
                    for (ActivityMetadata linkedActivity : activity.getLinkedActivities()) {
                        executeSiteCountQuery(linkedActivity);
                    }
                    break;
            }
        }

        if(command.isPivotedBy(DimensionType.Target) &&
           command.getValueType() == PivotSites.ValueType.INDICATOR) {
           
            for (Integer databaseId : databases.keySet()) {
                executeTargetValuesQuery(databaseId);
            }
        }

        LOGGER.info(String.format("Pivot timings: metadata %s, trees: %s, query: %s, aggregate: %s",
                metadataTime, treeTime, queryTime, aggregateTime));


        return new PivotSites.PivotResult(createBuckets());
    }


    private void executeIndicatorValuesQuery(ActivityMetadata activity) {
        ResourceId formClassId = activity.getFormClassId();
        FormTree formTree = formTrees.get(activity.getFormClassId());
        QueryModel queryModel = new QueryModel(formClassId);

        // Add Indicators
        for (IndicatorMetadata indicator : activity.getIndicators()) {
            queryModel.selectExpr(indicator.getFieldExpression()).as(indicator.getAlias());
        }

        // Add dimensions columns as needed
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        // if any of the indicators are "site count" indicators, then we need
        // to query the site id as well
        addSiteIdToQuery(activity, queryModel);

        // declare the filter
        queryModel.setFilter(composeFilter(activity));

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        // Now add the results to the buckets
        aggregateTime.start();
        int[] siteIds = extractSiteIds(columnSet);
        DimensionCategory[][] categories = extractCategories(activity, formTree, columnSet);


        for (IndicatorMetadata indicator : activity.getIndicators()) {
            ColumnView measureView = columnSet.getColumnView(indicator.getAlias());
            DimensionCategory indicatorCategory = null;

            if (indicatorDimension.isPresent()) {
                indicatorCategory = indicatorDimension.get().category(indicator);
            }
            for (int i = 0; i < columnSet.getNumRows(); i++) {

                if(indicator.getAggregation() == IndicatorDTO.AGGREGATE_SITE_COUNT) {

                    Map<Dimension, DimensionCategory> key = bucketKey(categories, indicatorCategory, i);
                    Accumulator bucket = bucketForKey(key, indicator.aggregation);
                    bucket.addSite(siteIds[i]);

                } else {
                    double value = measureView.getDouble(i);
                    if (!Double.isNaN(value)) {
                        Map<Dimension, DimensionCategory> key = bucketKey(categories, indicatorCategory, i);
                        
                        if(command.getValueType() == PivotSites.ValueType.INDICATOR) {
                            Accumulator bucket = bucketForKey(key, indicator.aggregation);
                            bucket.addValue(value);
                        } else {
                            Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);
                            bucket.addSite(siteIds[i]);
                        }
                    }
                }

            }
        }
        aggregateTime.stop();
    }

    private void executeTargetValuesQuery(Integer databaseId) {

        ResourceId targetFormClassId = CuidAdapter.cuid(CuidAdapter.TARGET_FORM_CLASS_DOMAIN, databaseId);

        QueryModel queryModel = new QueryModel(targetFormClassId);

        Collection<ActivityMetadata> activities = databases.get(databaseId);

        // Add all indicators we're querying for
        for (ActivityMetadata activity : activities) {
            for (IndicatorMetadata indicator : activity.getIndicators()) {
                queryModel.selectField(indicator.getTargetFieldId()).as(indicator.getAlias());
            }
        }

        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getTargetColumnQuery(targetFormClassId)) {
                queryModel.addColumn(columnModel);
            }
        }

        ColumnSet columnSet = executeQuery(queryModel);

        // Now add the results to the buckets
        aggregateTime.start();

        Dimension targetDim = new Dimension(DimensionType.Target);
        
        for (ActivityMetadata activity : activities) {
            for (IndicatorMetadata indicator : activity.getIndicators()) {

                ColumnView measureView = columnSet.getColumnView(indicator.getAlias());
                DimensionCategory indicatorCategory = null;

                if (indicatorDimension.isPresent()) {
                    indicatorCategory = indicatorDimension.get().category(indicator);
                }
                for (int i = 0; i < columnSet.getNumRows(); i++) {
                    double value = measureView.getDouble(i);
                    if (!Double.isNaN(value)) {
                        Map<Dimension, DimensionCategory> key = new HashMap<>();
                        key.put(targetDim, TargetCategory.TARGETED);
                        if(indicatorCategory != null) {
                            key.put(indicatorDimension.get().getModel(), indicatorCategory);
                        }
                        Accumulator bucket = bucketForKey(key, indicator.aggregation);
                        bucket.addValue(value);
                    }
                }
            }
        }
        aggregateTime.stop();
    }


    private Map<Dimension, DimensionCategory> bucketKey(
            DimensionCategory[][] categories, @Nullable DimensionCategory indicatorCategory, int rowIndex) {

        Map<Dimension, DimensionCategory> key = new HashMap<>();

        // Only include indicator as dimension if we are pivoting on dimension
        if (indicatorCategory != null) {
            key.put(indicatorDimension.get().getModel(), indicatorCategory);
        }

        for (int j = 0; j < groupBy.size(); j++) {
            Dimension dimension = groupBy.get(j).getModel();
            DimensionCategory category = categories[j][rowIndex];
            if(category != null) {
                key.put(dimension, category);
            }
        }
        return key;
    }

    private Accumulator bucketForKey(Map<Dimension, DimensionCategory> key, int aggregation) {
        Accumulator bucket = buckets.get(key);
        if(bucket == null) {
            bucket = new Accumulator(key, aggregation);
            buckets.put(key, bucket);
        }
        return bucket;
    }

    /**
     * Queries the count of distinct sites (not monthly reports) that match the filter
     */
    private void executeSiteCountQuery(ActivityMetadata activity) {
        
        if(command.isPivotedBy(DimensionType.Indicator) ||
           command.getFilter().isRestricted(DimensionType.Indicator)) {
            
            // only count sites which have non-empty values for the given
            // indicators
            executeIndicatorValuesQuery(activity);
        
        } else if(activity.isMonthly() && 
                (command.isPivotedBy(DimensionType.Date) ||
                 command.getFilter().isDateRestricted())) {
            
            // if we are pivoting or filtering by date, then we need
            // to query the actual reporting periods and count distinct sites
                
            executeSiteCountQueryOnReportingPeriod(activity);
        
        } else {
            
            // Otherwise, we only need to query the sites add up the total rows
            executeSiteCountQueryOnSite(activity);
        }
    }

    private void executeSiteCountQueryOnSite(ActivityMetadata activity) {
        Preconditions.checkState(!indicatorDimension.isPresent());

        FormTree formTree = formTrees.get(activity.getSiteFormClassId());
        QueryModel queryModel = new QueryModel(activity.getSiteFormClassId());
        queryModel.setFilter(composeFilter(activity));

        // Add dimensions columns as needed
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        aggregateTime.start();

        // Now add the counts to the buckets
        DimensionCategory[][] categories = extractCategories(activity, formTree, columnSet);

        for (int i = 0; i < columnSet.getNumRows(); i++) {

            Map<Dimension, DimensionCategory> key = bucketKey(categories, null, i);
            Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);

            bucket.addCount(1);
        }
        aggregateTime.stop();
    }

    private void executeSiteCountQueryOnReportingPeriod(ActivityMetadata activity) {
        Preconditions.checkArgument(activity.isMonthly());

        FormTree formTree = formTrees.get(activity.getFormClassId());
        QueryModel queryModel = new QueryModel(activity.getFormClassId());
        queryModel.setFilter(composeFilter(activity));

        // Add dimensions columns as needed
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        addSiteIdToQuery(activity, queryModel);

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        aggregateTime.start();

        // Now add the counts to the buckets
        DimensionCategory[][] categories = extractCategories(activity, formTree, columnSet);
        int siteId[] = extractSiteIds(columnSet);

        for (int i = 0; i < columnSet.getNumRows(); i++) {

            Map<Dimension, DimensionCategory> key = bucketKey(categories, null, i);
            Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);

            bucket.addSite(siteId[i]);
        }
        aggregateTime.stop();
    }


    private void executeSiteCountWithIndicatorFilter(ActivityMetadata activity) {
        Preconditions.checkArgument(activity.isMonthly());

        FormTree formTree = formTrees.get(activity.getFormClassId());
        QueryModel queryModel = new QueryModel(activity.getFormClassId());
        queryModel.setFilter(composeFilter(activity));

        // Add dimensions columns as needed
        for (DimBinding dimension : groupBy) {
            for (ColumnModel columnModel : dimension.getColumnQuery(formTree)) {
                queryModel.addColumn(columnModel);
            }
        }

        addSiteIdToQuery(activity, queryModel);

        // Query the table 
        ColumnSet columnSet = executeQuery(queryModel);

        aggregateTime.start();

        // Now add the counts to the buckets
        DimensionCategory[][] categories = extractCategories(activity, formTree, columnSet);
        int siteId[] = extractSiteIds(columnSet);

        for (int i = 0; i < columnSet.getNumRows(); i++) {

            Map<Dimension, DimensionCategory> key = bucketKey(categories, null, i);
            Accumulator bucket = bucketForKey(key, IndicatorDTO.AGGREGATE_SITE_COUNT);

            bucket.addSite(siteId[i]);
        }
        aggregateTime.stop();
    }




    private ColumnSet executeQuery(QueryModel queryModel) {
        queryTime.start();
        ColumnSetBuilder builder = new ColumnSetBuilder(catalog);
        ColumnSet columnSet = builder.build(queryModel);
        queryTime.stop();
        return columnSet;
    }

    private void addSiteIdToQuery(ActivityMetadata activity, QueryModel queryModel) {
        if(activity.getReportingFrequency() == ActivityFormDTO.REPORT_ONCE) {
            queryModel.selectResourceId().as(SITE_ID_KEY);
        } else {
            queryModel.selectField(CuidAdapter.field(activity.getFormClassId(), CuidAdapter.SITE_FIELD)).as(SITE_ID_KEY);
        }
    }

    private int[] extractSiteIds(ColumnSet columnSet) {
        ColumnView columnView = columnSet.getColumnView(SITE_ID_KEY);
        int[] ids = new int[columnView.numRows()];

        for (int i = 0; i < columnView.numRows(); i++) {
            String resourceId = columnView.getString(i);
            if(resourceId != null) {
                ids[i] = CuidAdapter.getLegacyIdFromCuid(resourceId);
            }
        }
        return ids;
    }

    private List<Bucket> createBuckets() {
        List<Bucket> list = Lists.newArrayList();
        for (Accumulator accumulator : buckets.values()) {
            list.add(accumulator.createBucket());
        }
        return list;
    }

    private ExprNode composeFilter(ActivityMetadata activity) {
        List<ExprNode> conditions = Lists.newArrayList();
        conditions.addAll(filterExpr(ColumnModel.ID_SYMBOL, CuidAdapter.SITE_DOMAIN, DimensionType.Site));
        conditions.addAll(filterExpr("partner", CuidAdapter.PARTNER_DOMAIN, DimensionType.Partner));
        conditions.addAll(filterExpr("project", CuidAdapter.PROJECT_DOMAIN, DimensionType.Project));
        conditions.addAll(filterExpr("location", CuidAdapter.LOCATION_DOMAIN, DimensionType.Location));
        conditions.addAll(adminFilter(activity));
        conditions.addAll(attributeFilters());
        conditions.addAll(dateFilter());

        if(conditions.size() > 0) {
            ExprNode filterExpr = Exprs.allTrue(conditions);
            LOGGER.info("Filter: " + filterExpr);

            return filterExpr;

        } else {
            return null;
        }
    }


    private Set<ExprNode> adminFilter(ActivityMetadata activity) {
        if (this.filter.isRestricted(DimensionType.AdminLevel)) {

            List<ExprNode> conditions = Lists.newArrayList();

            // we don't know which adminlevel this belongs to so we have construct a giant OR statement
            List<ExprNode> adminIdExprs = findAdminIdExprs(formTrees.get(activity.getFormClassId()));

            for(ExprNode adminIdExpr : adminIdExprs) {
                for (Integer adminEntityId : this.filter.getRestrictions(DimensionType.AdminLevel)) {
                    conditions.add(Exprs.equals(adminIdExpr, idConstant(CuidAdapter.entity(adminEntityId))));
                }
            }

            return singleton(anyTrue(conditions));

        } else {
            return emptySet();
        }
    }

    private List<ExprNode> findAdminIdExprs(FormTree formTree) {
        List<ExprNode> expressions = Lists.newArrayList();
        for (FormClass formClass : formTree.getFormClasses()) {
            if(formClass.getId().getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN) {
                expressions.add(new CompoundExpr(formClass.getId(), ColumnModel.ID_SYMBOL));
            }
        }
        return expressions;
    }

    private List<ExprNode> attributeFilters() {

        List<ExprNode> conditions = Lists.newArrayList();

        for (String field : attributeFilters.keySet()) {
            List<ExprNode> valueConditions = Lists.newArrayList();
            for (String value : attributeFilters.get(field)) {
                valueConditions.add(Exprs.equals(new SymbolExpr(field), new ConstantExpr(value)));
            }
            conditions.add(Exprs.anyTrue(valueConditions));
        }
        return conditions;
    }

    private List<ExprNode> filterExpr(String fieldName, char domain, DimensionType type) {
        Set<Integer> ids = this.filter.getRestrictions(type);
        if(ids.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<ExprNode> conditions = Lists.newArrayList();

            for (Integer id : ids) {
                conditions.add(Exprs.equals(symbol(fieldName), idConstant(CuidAdapter.cuid(domain, id))));
            }

            return singletonList(Exprs.anyTrue(conditions));
        }
    }


    private Collection<FunctionCallNode> dateFilter() {
        if(filter.isDateRestricted()) {

            SymbolExpr dateExpr = new SymbolExpr("date2");
            ConstantExpr minDate = new ConstantExpr(new LocalDate(filter.getMinDate()));
            ConstantExpr maxDate = new ConstantExpr(new LocalDate(filter.getMaxDate()));

            return singleton(
                    new FunctionCallNode(AndFunction.INSTANCE,
                            new FunctionCallNode(GreaterOrEqualFunction.INSTANCE, dateExpr, minDate),
                            new FunctionCallNode(LessOrEqualFunction.INSTANCE, dateExpr, maxDate)));

        } else {
            return Collections.emptyList();
        }
    }

    private DimensionCategory[][] extractCategories(ActivityMetadata activity, FormTree formTree, ColumnSet columnSet) {
        DimensionCategory[][] array = new DimensionCategory[groupBy.size()][];

        for (int i = 0; i < groupBy.size(); i++) {
            array[i] = groupBy.get(i).extractCategories(activity, formTree, columnSet);
        }
        return array;
    }
}
