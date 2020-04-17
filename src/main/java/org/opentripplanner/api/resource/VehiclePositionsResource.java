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

import org.apache.commons.lang3.StringUtils;
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
        		return ((GtfsRealtimeVehiclePostionsUpdater) updater).getVehiclePositions().stream()
        				.filter(vp -> StringUtils.isBlank(routeId) || StringUtils.equals(routeId, vp.getRoute()))
        				.filter(vp -> StringUtils.isBlank(tripId) ||StringUtils.equals(tripId, vp.getTrip()))
        				.collect(Collectors.toList());
        	}
        }
        return Collections.emptyList();
    }
}
