package org.opentripplanner.api.resource;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.vehiclepositions.GtfsRealtimeVehiclePostionsUpdater;
import org.opentripplanner.updater.vehiclepositions.VehiclePositionsUpdateHandler.VehiclePositionDTO;

@Path("/routers/{routerId}/vehiclepositions")
@XmlRootElement
public class VehiclePositionsResource {

    @Context OTPServer otpServer;
    
    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public Collection<VehiclePositionDTO> getVehiclePositions(@PathParam("routerId") String routerId, @QueryParam("trip") String tripId) {
        final Graph graph = otpServer.getRouter(routerId).graph;
        GraphUpdaterManager updaterManager = graph.updaterManager;
        boolean allTrips = StringUtils.isBlank(tripId);
        return IntStream.range(0, updaterManager.size()).boxed().map(updaterManager::getUpdater)
                .filter(updater -> (updater instanceof GtfsRealtimeVehiclePostionsUpdater))
                .map(u -> (GtfsRealtimeVehiclePostionsUpdater) u)
                .map(u -> u.getVehiclePositions().stream()
                        .filter(vp -> allTrips || StringUtils.equals(tripId, vp.getTrip())))
                .flatMap(listStream -> listStream).collect(Collectors.toList());
    }
}
