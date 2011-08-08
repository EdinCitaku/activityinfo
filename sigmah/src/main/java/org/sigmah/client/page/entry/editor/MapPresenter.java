/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.entry.editor;

import org.sigmah.client.dispatch.AsyncMonitor;
import org.sigmah.client.page.entry.editor.MapPresenter.EditView.CoordinatesChangedEvent;
import org.sigmah.client.page.entry.editor.MapPresenter.EditView.CoordinatesChangedHandler;
import org.sigmah.client.page.entry.editor.MapPresenter.EditView.MarkerMovedEvent;
import org.sigmah.client.page.entry.editor.MapPresenter.EditView.MarkerMovedHandler;
import org.sigmah.shared.dto.SiteDTO;
import org.sigmah.shared.report.content.AiLatLng;
import org.sigmah.shared.util.mapping.BoundingBoxDTO;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/*
 * UI logic for managing a MapView where the user is allowed to change a location
 * by dragging a marker over a map
 */
public class MapPresenter {
	public interface MapView  {
        public void setMapView(BoundingBoxDTO bounds);
		public Widget asWidget();	
		public void initialize();
		
		/*
		 * The UI displaying loading, network status (retry/error/complete)
		 * Usually some standard Async monitor UI view, such as NullAsyncMonitor,
		 * MaskingAsyncMonitor etc
		 * 
		 */
		public AsyncMonitor getAsyncMonitor();
		  void setValue(AiLatLng value);

		  /**
		   * Returns the current value.
		   * 
		   * @return the value as an object of type V
		   * @see #setValue
		   */
		  AiLatLng getValue();
	}
	
    public interface EditView extends MapView {
        public Double getX();
        public Double getY();
        public void setMarkerPosition(AiLatLng latLng);
        public BoundingBoxDTO getBounds();
        public void setBounds(String name, BoundingBoxDTO bounds);
        public void panTo(AiLatLng latLng);
        
        public HandlerRegistration addMarkerMovedHandler(MarkerMovedHandler handler);
        public HandlerRegistration addCoordinatesChangedHandler(CoordinatesChangedHandler handler);
        public HandlerRegistration addMapViewChangedHandler(MapViewChangedHandler handler);
        
        public interface MarkerMovedHandler extends EventHandler {
        	public void onMarkedMoved(MarkerMovedEvent event);
        }
        public interface CoordinatesChangedHandler extends EventHandler {
        	public void onCoordinatesChanged(CoordinatesChangedEvent event);
        }
        public interface MapViewChangedHandler extends EventHandler {
        	public void onMapViewChanged(MapViewChangedEvent event);
        }
        
        public class MarkerMovedEvent extends GwtEvent<MarkerMovedHandler> {
			public static Type<MarkerMovedHandler> TYPE = new Type<MarkerMovedHandler>();
        	double my;
        	double mx;
			
        	public MarkerMovedEvent(double my, double mx) {
				this.my = my;
				this.mx = mx;
			}

			public double getMy() {
				return my;
			}

			public double getMx() {
				return mx;
			}

			@Override
			public Type<MarkerMovedHandler> getAssociatedType() {
				return TYPE;
			}

			@Override
			protected void dispatch(MarkerMovedHandler handler) {
				handler.onMarkedMoved(this);
			}
        }
        
        public class CoordinatesChangedEvent extends GwtEvent<CoordinatesChangedHandler> {
        	public static Type<CoordinatesChangedHandler> TYPE = new Type<CoordinatesChangedHandler>();
        	double x;
        	double y;
        	
        	public CoordinatesChangedEvent(double x, double y) {
				this.x = x;
				this.y = y;
			}

			public double getX() {
				return x;
			}

			public double getY() {
				return y;
			}

			@Override
			public Type<CoordinatesChangedHandler> getAssociatedType() {
				return TYPE;
			}

			@Override
			protected void dispatch(CoordinatesChangedHandler handler) {
				handler.onCoordinatesChanged(this);
			}
        }
        
        public class MapViewChangedEvent extends GwtEvent<MapViewChangedHandler> {
        	public static Type<MapViewChangedHandler> TYPE = new Type<MapViewChangedHandler>();
        	private BoundingBoxDTO boundingBox;
        	
        	public MapViewChangedEvent(BoundingBoxDTO boundingBox) {
				this.boundingBox = boundingBox;
			}

			public BoundingBoxDTO getBoundingBox() {
				return boundingBox;
			}

			@Override
			public Type<MapViewChangedHandler> getAssociatedType() {
				return TYPE;
			}

			@Override
			protected void dispatch(MapViewChangedHandler handler) {
				handler.onMapViewChanged(this);
			}
        }
    }

    private BoundingBoxDTO bounds;
    private EditView view;

    public MapPresenter(EditView view) {
        this.view = view;
        
        addHandlers();
        
        this.bounds = new BoundingBoxDTO(-180, -90, 180, 90);
    }

    private void addHandlers() {
    	view.addCoordinatesChangedHandler(new CoordinatesChangedHandler() {
			@Override
			public void onCoordinatesChanged(CoordinatesChangedEvent event) {
				double x = event.getX();
				double y = event.getY();
				
		        if(haveValidCoords()) {
		            view.setMarkerPosition(new AiLatLng(y, x));

		            if(!view.getBounds().contains(x, y)) {
		                view.panTo(new AiLatLng(y, x));
		            }
		        }
			}
		});
    	
    	view.addMarkerMovedHandler(new MarkerMovedHandler() {
			@Override
			public void onMarkedMoved(MarkerMovedEvent event) {
				double my = event.getMy();
				double mx = event.getMx();
				
		        if(!bounds.contains(mx, my)) {
		            double clampedY = bounds.clampY(my);
		            double clampedX = bounds.clampX(mx);
		            view.setMarkerPosition(new AiLatLng(clampedY, clampedX));
		            view.setValue(new AiLatLng(clampedY, clampedX));
		        } else {
		            view.setValue(new AiLatLng(my, mx));
		        }
			}
		});
	}

	public void setSite(SiteDTO site, String name, BoundingBoxDTO bounds) {
        setBounds(name, bounds);
    }

    public void setBounds(String name, BoundingBoxDTO bounds) {
        this.bounds = bounds;
        view.setBounds(name, bounds);
        view.setMapView(bounds);

        if(!haveValidCoords()) {
            view.setMarkerPosition(new AiLatLng(bounds.getCenterY(), bounds.getCenterX()));
        }
    }

    private boolean haveValidCoords() {
        Double x = view.getX();
        Double y = view.getY();

        return x!=null && y!=null && bounds.contains(x, y);
    }
}
