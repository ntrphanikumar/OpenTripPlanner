package org.opentripplanner.api.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.opentripplanner.routing.alertpatch.AlertPatch;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.GraphIndex;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.alerts.GtfsRealtimeAlertsUpdater;

@Path("/routers/{routerId}/alerts")
@XmlRootElement
public class AlertsResource {

    @Context OTPServer otpServer;

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public Collection<AlertDTO> getAllAlerts(@PathParam("routerId") String routerId) {
    	final Graph graph = otpServer.getRouter(routerId).graph;
		GraphUpdaterManager updaterManager = graph.updaterManager;
    	Collection<AlertPatch> alertPatches = Collections.emptyList();
        for(int i=0;i<updaterManager.size();i++) {
        	GraphUpdater updater = updaterManager.getUpdater(i);
        	if(updater instanceof GtfsRealtimeAlertsUpdater) {
        		GtfsRealtimeAlertsUpdater alertsUpdater = (GtfsRealtimeAlertsUpdater)updater;
        		alertPatches = alertsUpdater.getAlertPatchService().getAllAlertPatches();
        		break;
        	}
        }
		return alertPatches.stream().filter(t -> t.getRoute()!=null).map(t -> AlertDTO.from(t, graph.index)).collect(Collectors.groupingBy(AlertDTO::getHeader)).values().stream()
				.map(a -> a.stream().reduce((t, u) -> t.merge(u)).get()).collect(Collectors.toList());
    }
    
    public static final class AlertDTO {
    	private String header, description;
    	private Date start, end;
//    	private List<InformedEntityDTO> informedEntities = new ArrayList<>();
    	private Set<RouteDTO> routes = new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()));
    	private AlertDTO(AlertPatch patch, GraphIndex index) {
    		header = patch.getAlert().alertHeaderText.toString();
    		description = patch.getAlert().alertDescriptionText.toString();
    		start = patch.getAlert().effectiveStartDate;
    		end = patch.getAlert().effectiveEndDate;
//    		getInformedEntities().add(new InformedEntityDTO(patch, index));
    		getRoutes().add(new RouteDTO(patch.getRoute(), index));
    	}
    	
    	AlertDTO merge(AlertDTO alert) {
//    		getInformedEntities().addAll(alert.getInformedEntities());
    		getRoutes().addAll(alert.getRoutes());
    		return this;
    	}
    	
    	static AlertDTO from(AlertPatch alert, GraphIndex index) {
    		return new AlertDTO(alert, index);
    	}
    	
    	public String getHeader() {
			return header;
		}
    	
    	public String getDescription() {
			return description;
		}
    	
    	public Date getStart() {
			return start;
		}
    	
    	public Date getEnd() {
			return end;
		}
    	
//    	public List<InformedEntityDTO> getInformedEntities() {
//			return informedEntities;
//		}
    	
    	public Set<RouteDTO> getRoutes() {
			return routes;
		}
    	
		private static String strVal(AgencyAndId aid) {
    		if(aid == null) {
    			return null;
    		}
    		return aid.getAgencyId()+":"+aid.getId();
    	}

    	
    	public static final class RouteDTO {
    		private String id, shortName, longName, color, textColor;
    		
    		public RouteDTO(AgencyAndId route, GraphIndex index) {
    			id = strVal(route);
    			Route r = index.routeForId.get(route);
    			shortName = r.getShortName();
    			longName = r.getLongName();
    			color = r.getColor();
    			textColor = r.getTextColor();
			}

			public String getId() {
				return id;
			}

			public String getShortName() {
				return shortName;
			}

			public String getLongName() {
				return longName;
			}

			public String getColor() {
				return color;
			}

			public String getTextColor() {
				return textColor;
			}
			
			@Override
			public int hashCode() {
				return id.hashCode();
			}
			
			@Override
			public boolean equals(Object obj) {
				if(!(obj instanceof RouteDTO)) {
					return false;
				}
				return id.equalsIgnoreCase(((RouteDTO) obj).id);
			}
    	}
    	
    	public static final class InformedEntityDTO {
    		private String route,routeId, stop, stopId, trip, tripId;
    		public InformedEntityDTO(AlertPatch alert, GraphIndex index) {
				route = index.routeForId.containsKey(alert.getRoute()) ? index.routeForId.get(alert.getRoute()).getShortName() : null;
				stop = index.stopForId.containsKey(alert.getStop()) ? index.stopForId.get(alert.getStop()).getName() : null;
				trip = index.tripForId.containsKey(alert.getTrip()) ? index.tripForId.get(alert.getTrip()).getTripShortName() : null;
				
				routeId = strVal(alert.getRoute());
				stopId = strVal(alert.getStop());
				tripId = strVal(alert.getTrip());
			}
    		
    		
    		public String getRoute() {
				return route;
			}
    		
    		public String getStop() {
				return stop;
			}
    		
    		public String getTrip() {
				return trip;
			}
    		
    		public String getRouteId() {
				return routeId;
			}
    		
    		public String getStopId() {
				return stopId;
			}
    		
    		public String getTripId() {
				return tripId;
			}
    	}
    }
}
