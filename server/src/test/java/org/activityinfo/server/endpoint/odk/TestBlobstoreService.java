package org.activityinfo.server.endpoint.odk;

import com.google.inject.Provider;
import com.sun.jersey.api.core.InjectParam;
import org.activityinfo.legacy.shared.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.DeploymentConfiguration;
import org.activityinfo.server.blob.GcsBlobFieldStorageService;
import org.activityinfo.store.spi.BlobAuthorizer;
import org.activityinfo.store.spi.BlobId;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

public class TestBlobstoreService extends GcsBlobFieldStorageService implements BlobAuthorizer {

    public TestBlobstoreService(DeploymentConfiguration config, final EntityManager em) {
        super(config, new Provider<EntityManager>() {
            @Override
            public EntityManager get() {
                return em;
            }
        });
    }

    @Override
    public Response getBlobUrl(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    public void put(AuthenticatedUser user, String contentDisposition, String mimeType,
                    BlobId blobId, ResourceId formId, InputStream inputStream) throws IOException {

    }

    @Override
    public Response getImage(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getImageUrl(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getThumbnail(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response exists(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response getUploadCredentials(@InjectParam AuthenticatedUser user, BlobId blobId, ResourceId resourceId, String fileName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOwner(int userId, String blobId) {
        return false;
    }
}
