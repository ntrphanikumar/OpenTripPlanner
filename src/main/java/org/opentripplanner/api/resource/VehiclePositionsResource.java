package org.opentripplanner.api.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.vehiclepositions.GtfsRealtimeVehiclePostionsUpdater;
import org.opentripplanner.updater.vehiclepositions.VehiclePositionsUpdateHandler.VehiclePositionDTO;

@Path("/routers/{routerId}/vehiclepositions")
@XmlRootElement
public class VehiclePositionsResource {

    @Context OTPServer otpServer;
    
    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public Collection<VehiclePositionDTO> getVehiclePositions(@PathParam("routerId") String routerId, @QueryParam("route") String routeId, @QueryParam("trip") String tripId) {
    	final Graph graph = otpServer.getRouter(routerId).graph;
		GraphUpdaterManager updaterManager = graph.updaterManager;
        for(int i=0;i<updaterManager.size();i++) {
        	GraphUpdater updater = updaterManager.getUpdater(i);
        	if(updater instanceof GtfsRealtimeVehiclePostionsUpdater) {
				AgencyAndId route = routeId != null ? AgencyAndId.convertFromString(routeId, ':') : null;
				AgencyAndId trip = tripId != null ? AgencyAndId.convertFromString(tripId, ':') : null;
        		return ((GtfsRealtimeVehiclePostionsUpdater) updater).getVehiclePositions().stream()
        				.filter(vp -> route == null || route.equals(vp.getRoute()))
        				.filter(vp -> trip == null || trip.equals(vp.getTrip()))
        				.collect(Collectors.toList());
        	}
        }
        return Collections.emptyList();
    }
}
