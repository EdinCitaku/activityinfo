package org.activityinfo.geoadmin.model;

import com.bedatadriven.geojson.GeoJsonModule;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.vividsolutions.jts.geom.Point;
import org.activityinfo.geoadmin.source.FeatureSourceCatalog;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.formTree.FormClassProvider;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.activityinfo.store.query.impl.views.GeoColumnView;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ActivityInfo REST Client
 */
public class ActivityInfoClient implements FormClassProvider {

    public static final Logger LOGGER = Logger.getLogger(ActivityInfoClient.class.getName());

    private Client client;
    private URI root;
    
    private FeatureSourceCatalog localCatalog = new FeatureSourceCatalog();

    @Override
    public FormClass getFormClass(ResourceId resourceId) {
        Preconditions.checkArgument(resourceId.getDomain() == CuidAdapter.ADMIN_LEVEL_DOMAIN);
        return ActivityInfoClient.this.getFormClass(CuidAdapter.getLegacyIdFromCuid(resourceId));
    }

    public static class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> type) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new GeoJsonModule());
            return mapper;
        }

    }

    /**
     * Creates a new instance using the given endpoint, ActivityInfo username
     * and password.
     * 
     * @param endpoint
     *            Rest endpoint (for example:
     *            https://www.activityinfo.org/resources)
     * @param username
     *            Email address of user (for example: akbertram@gmail.com)
     * @param password
     *            User's plaintext password
     */
    public ActivityInfoClient(String endpoint, String username, String password) {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        clientConfig.getClasses().add(ObjectMapperProvider.class);
//        clientConfig.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
//                new HTTPSProperties(new ActivityInfoHostnameVerifier(), getSSLContext()));


        client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(username, password));

        root = UriBuilder.fromUri(endpoint).build();
    }



    /**
     * @return the list of Countries in ActivityInfo's geographic reference
     *         database
     */
    public List<Country> getCountries() {
        URI uri = UriBuilder.fromUri(root).path("countries").build();
        return Arrays.asList(
            client.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Country[].class));
    }

    /**
     * @return the list of Administrative levels for a given country in
     *         ActivityInfo's geographic reference database
     */
    public List<AdminLevel> getAdminLevels(Country country) {
        return getAdminLevelsByCountryCode(country.getCode());
    }

	public List<AdminLevel> getAdminLevelsByCountryCode(String countryCode) {
		URI uri = UriBuilder.fromUri(root)
            .path("country")
            .path(countryCode)
            .path("adminLevels").build();
        return Arrays.asList(
                client.resource(uri)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(AdminLevel[].class));
	}
	
	public List<LocationType> getLocationTypesByCountryCode(String countryCode) {
		URI uri = UriBuilder.fromUri(root)
	            .path("country")
	            .path(countryCode)
	            .path("locationTypes").build();
	        return Arrays.asList(
	            client.resource(uri)
	                .accept(MediaType.APPLICATION_JSON_TYPE)
	                .get(LocationType[].class));	
	}
	
	public List<Location> getLocations(int locationType) {
		URI uri = UriBuilder.fromUri(root)
	            .path("locations")
	            .queryParam("type", locationType)
	            .build();
        return Arrays.asList(
            client.resource(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Location[].class));	
	}
	
	public void postNewLocations(int locationType, List<NewLocation> locations) {
		URI uri = UriBuilder.fromUri(root)
	            .path("locations")
	            .path(Integer.toString(locationType))
	            .build();
	
        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(locations);	
		
	}
	
    public void updateAdminLevel(AdminLevel level) {
        URI uri = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(level.getId()))
            .build();

        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .put(level);
    }
    
    public void deleteLevel(AdminLevel level) {
        URI uri = UriBuilder.fromUri(root)
                .path("adminLevel")
                .path(Integer.toString(level.getId()))
                .build();

        client.resource(uri)
        	.delete();
    }

    public AdminLevel getAdminLevel(int id) {
        URI build = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(id))
            .build();
        return client.resource(build)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .get(AdminLevel.class);
    }

    public List<AdminEntity> getAdminEntities(AdminLevel level) {
        return getAdminEntities(level.getId());
    }

    public List<AdminEntity> getAdminEntities(int levelId) {

        LOGGER.info("Fetching admin entities for level " + levelId);

        URI uri = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(levelId))
            .path("entities")
            .build();

        return Arrays.asList(
                client.resource(uri)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(AdminEntity[].class));
    }

    /**
     * Creates a new root administrative level for a given country
     */
    public void postRootLevel(Country country, AdminLevel newLevel) {
        URI uri = UriBuilder.fromUri(root)
            .path("country")
            .path(country.getCode())
            .path("adminLevels")
            .build();
        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .post(newLevel);
    }

    /**
     * Creates a new root administrative level for a given country
     */
    public void postChildLevel(AdminLevel parentLevel, AdminLevel newLevel) {
        URI uri = UriBuilder.fromUri(root)
            .path("adminLevel")
            .path(Integer.toString(parentLevel.getId()))
            .path("childLevels")
            .build();
        client.resource(uri)
            .accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .post(newLevel);
    }
    
    public List<AdminEntity> geocode(double latitude, double longitude) {
	   URI uri = UriBuilder.fromUri(root)
	            .path("geocode")
	            .queryParam("lat", latitude)
	            .queryParam("lng", longitude)
	            .build();

       return Arrays.asList(
           client.resource(uri)
               .accept(MediaType.APPLICATION_JSON_TYPE)
               .get(AdminEntity[].class));    
    }
    
    public List<List<AdminEntity>> geocode(List<Point> points) {
    	List<LatLng> latLngs = Lists.newArrayList();
    	for(Point point : points) {
    		latLngs.add(new LatLng(point));
    	}
    	return geocodePoints(latLngs);
    }
    
    public List<List<AdminEntity>> geocodePoints(List<LatLng> points) {
    	 URI uri = UriBuilder.fromUri(root)
 	            .path("geocode")
 	            .build();
    	 
        return client.resource(uri)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .post(new GenericType<List<List<AdminEntity>>>() {
            }, points);
    }

    public ObjectMapper getObjectMapper() {
        return new ObjectMapperProvider().getContext(ObjectMapper.class);
    }
    
    
    public FormClass getFormClass(int adminLevelId) {
        
        String json = formResource(CuidAdapter.adminLevelFormClass(adminLevelId)).path("class").get(String.class);


        return FormClass.fromResource(Resources.fromJson(json));
    }

    public FormTree getFormTree(ResourceId resourceId) {
        
        if(localCatalog.isLocalResource(resourceId)) {
            FormTreeBuilder treeBuilder = new FormTreeBuilder(localCatalog);
            return treeBuilder.queryTree(resourceId);
        
        } else {

            String json = formResource(resourceId).path("tree").get(String.class);
            JsonObject object = new Gson().fromJson(json, JsonObject.class);
            return JsonFormTreeBuilder.fromJson(object);
        }
    }

    public FormTree getFormTree(int adminLevelId) {
        return getFormTree(CuidAdapter.adminLevelFormClass(adminLevelId));
    }
    
    public ColumnSet queryColumns(QueryModel queryModel) {
        
        if(localCatalog.isLocalQuery(queryModel)) {
            ColumnSetBuilder builder = new ColumnSetBuilder(localCatalog, ColumnCache.NULL);
            return builder.build(queryModel);

        } else {

            return queryColumnsRemotely(queryModel);
        }
    }
    
    private ColumnSet queryColumnsRemotely(QueryModel queryModel) {
        String json = client.resource(root)
                .path("query")
                .path("columns")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(String.class, queryModel);

        JsonObject object = new Gson().fromJson(json, JsonObject.class);
        int numRows = object.getAsJsonPrimitive("rows").getAsInt();

        Map<String, ColumnView> columnMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> column : object.getAsJsonObject("columns").entrySet()) {
            JsonObject columnValue = column.getValue().getAsJsonObject();
            String storage = columnValue.getAsJsonPrimitive("storage").getAsString();
            switch (storage) {
                case "array":
                    columnMap.put(column.getKey(), new ColumnViewWrapper(numRows, columnValue.getAsJsonArray("values")));
                    break;
                case "coordinates":
                    columnMap.put(column.getKey(), parseCoordinates(columnValue.getAsJsonArray("coordinates")));
                    break;
                default:
                    throw new UnsupportedOperationException(storage);
            }
        }

        return new ColumnSet(numRows, columnMap);
    }

    public void executeTransaction(TransactionBuilder builder) {
        ClientResponse response = client.resource(root)
                .path("update")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(builder.build().toString(), MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class);
        
        if(response.getStatus() != 200) {
            throw new RuntimeException("Transaction failed with status code: " + response.getStatus() + " "  +
            response.getEntity(String.class));
        }
    }
    
    private ColumnView parseCoordinates(JsonArray coordinateArray) {
        double[] coordinates = new double[coordinateArray.size()];
        for (int i = 0; i < coordinateArray.size(); i++) {
            JsonElement coord = coordinateArray.get(i);
            if(coord.isJsonNull()) {
                coordinates[i] = Double.NaN;
            } else {
                coordinates[i] = coord.getAsDouble();
            }
        }
        return new GeoColumnView(coordinates);
    }

    private WebResource formResource(ResourceId resourceId) {
        return client.resource(root)
                .path("form")
                .path(resourceId.asString());
    }
}
