package org.opentripplanner.netex.loader.mapping;

import org.junit.Test;
import org.opentripplanner.model.Route;
import org.opentripplanner.model.Trip;
import org.opentripplanner.model.impl.OtpTransitServiceBuilder;
import org.opentripplanner.netex.loader.util.HierarchicalMapById;
import org.rutebanken.netex.model.JourneyPattern;
import org.rutebanken.netex.model.JourneyPatternRefStructure;
import org.rutebanken.netex.model.LineRefStructure;
import org.rutebanken.netex.model.RouteRefStructure;
import org.rutebanken.netex.model.ServiceJourney;

import javax.xml.bind.JAXBElement;

import static org.junit.Assert.assertEquals;
import static org.opentripplanner.netex.loader.mapping.FeedScopedIdFactory.createFeedScopedId;

public class TripMapperTest {

    private static final String ROUTE_ID = "RUT:Route:1";
    private static final String SERVICE_JOURNEY_ID = "RUT:ServiceJourney:1";
    private static final String JOURNEY_PATTERN_ID = "RUT:JourneyPattern:1";

    private static final JAXBElement<LineRefStructure> LINE_REF = MappingSupport.createWrappedRef(
            ROUTE_ID, LineRefStructure.class
    );

    @Test
    public void mapTrip() {
        OtpTransitServiceBuilder transitBuilder = new OtpTransitServiceBuilder();
        Route route = new Route();
        route.setId(createFeedScopedId(ROUTE_ID));
        transitBuilder.getRoutes().add(route);

        TripMapper tripMapper = new TripMapper(transitBuilder.getRoutes(),
                new HierarchicalMapById<>(),
                new HierarchicalMapById<>()
        );

        ServiceJourney serviceJourney = createExampleServiceJourney();

        serviceJourney.setLineRef(LINE_REF);

        Trip trip = tripMapper.mapServiceJourney(serviceJourney);

        assertEquals(trip.getId(), createFeedScopedId(SERVICE_JOURNEY_ID));
    }

    @Test
    public void mapTripWithRouteRefViaJourneyPattern() {
        OtpTransitServiceBuilder transitBuilder = new OtpTransitServiceBuilder();
        Route route = new Route();
        route.setId(createFeedScopedId(ROUTE_ID));
        transitBuilder.getRoutes().add(route);

        JourneyPattern journeyPattern = new JourneyPattern().withId(JOURNEY_PATTERN_ID);
        journeyPattern.setRouteRef(new RouteRefStructure().withRef(ROUTE_ID));

        ServiceJourney serviceJourney = createExampleServiceJourney();
        serviceJourney.setJourneyPatternRef(
                MappingSupport.createWrappedRef(JOURNEY_PATTERN_ID, JourneyPatternRefStructure.class)
        );

        org.rutebanken.netex.model.Route netexRoute = new org.rutebanken.netex.model.Route();
        netexRoute.setLineRef(LINE_REF);
        netexRoute.setId(ROUTE_ID);

        HierarchicalMapById<org.rutebanken.netex.model.Route> routeById = new HierarchicalMapById<>();
        routeById.add(netexRoute);
        HierarchicalMapById<JourneyPattern> journeyPatternById = new HierarchicalMapById<>();
        journeyPatternById.add(journeyPattern);

        TripMapper tripMapper = new TripMapper(
                transitBuilder.getRoutes(),
                routeById,
                journeyPatternById
        );

        Trip trip = tripMapper.mapServiceJourney(serviceJourney);

        assertEquals(trip.getId(), createFeedScopedId("RUT:ServiceJourney:1"));
    }

    private ServiceJourney createExampleServiceJourney() {
        ServiceJourney serviceJourney = new ServiceJourney();
        serviceJourney.setId("RUT:ServiceJourney:1");
        serviceJourney.setDayTypes(NetexTestDataSample.createEveryDayRefs());
        return serviceJourney;
    }
}