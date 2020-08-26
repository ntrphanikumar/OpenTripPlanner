package org.opentripplanner.index.model;

import static java.util.EnumSet.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.ServiceCalendar;
import org.opentripplanner.model.calendar.CalendarServiceData;
import org.opentripplanner.routing.edgetype.TripPattern;

import com.beust.jcommander.internal.Lists;

public class PatternShort {

    public String id;
    public String name;
    public String desc;
    public String color, textColor;
    
    public PatternShort (TripPattern pattern, boolean legacyDisplay) {
        this(pattern);
        desc = desc.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br/>", "\n");
        name = desc;
    }
    
    public PatternShort (TripPattern pattern) {
        id = pattern.code;
        desc = pattern.name.replaceAll("\\(" + pattern.getFeedId() + ":.*?\\)","").replaceAll("( )+", " ").trim();
        String[] split = desc.split(" to ", 2);
        String route, from=null, to=null;
        if(split.length == 2) {
            route = split[0];
            split = split[1].split(" from ", 2);
            to = split[0];
            if(split.length == 2) {
                from = split[1];
            }
        } else {
            split = split[0].split(" from ", 2);
            route = split[0];
            if(split.length == 2) {
                from = split[1];
            }
        }
        StringBuilder descStr = new StringBuilder("<b>Route:</b> ").append(route);
        if (from != null) {
            descStr.append("<br/>").append("<b>From:</b> ").append(from);
        }
        if (to != null) {
            descStr.append("<br/>").append("<b>To:</b> ").append(to);
        }
        desc = descStr.toString();
        name = desc;
        if(!pattern.getTrips().isEmpty()) {
            color = pattern.getTrip(0).getRoute().getColor();
            textColor = pattern.getTrip(0).getRoute().getTextColor();
        }
    }
    
    public PatternShort (TripPattern pattern, String serviceDays) {
        this(pattern);
        if(serviceDays != null && serviceDays.trim().length()>0) {
            name += "<br/><b>Days of operation:</b> " + serviceDays;
            desc = name;
        }
    }
    
    public static List<PatternShort> list (Collection<TripPattern> in) {
        List<PatternShort> out = Lists.newArrayList();
        for (TripPattern pattern : in) out.add(new PatternShort(pattern));
        return out;
    }    
    
    public static Collection<PatternShort> list (Collection<TripPattern> patterns, Map<FeedScopedId, Integer> services, CalendarServiceData csd) {
        Map<Integer, String> serviceCalendars = services.entrySet().stream().collect(toMap(e -> e.getValue(), e -> servicedays(csd.getServiceCalendar(e.getKey()))));
        Map<Integer, Days> serviceStartDay = services.entrySet().stream().collect(toMap(e -> e.getValue(), e -> servicedaysStream(csd.getServiceCalendar(e.getKey())).findFirst().orElse(Days.NONE)));
        return patterns.stream()
                .map(pattern -> pattern.getServices().stream().mapToObj(i -> i).sorted((s1, s2) -> serviceStartDay.get(s1).ordinal() - serviceStartDay.get(s2).ordinal())
                        .map(i -> new PatternShort(pattern, serviceCalendars.get(i))))
                .flatMap(s -> s).distinct().collect(toList());
    }
    
    public enum Days {
        Mon(ServiceCalendar::getMonday),
        Tue(ServiceCalendar::getTuesday),
        Wed(ServiceCalendar::getWednesday),
        Thu(ServiceCalendar::getThursday),
        Fri(ServiceCalendar::getFriday),
        Sat(ServiceCalendar::getSaturday),
        Sun(ServiceCalendar::getSunday),
        NONE(s -> 1);
        
        private Function<ServiceCalendar, Integer> isRunningOnDayFunc;
        
        private Days(Function<ServiceCalendar, Integer> isRunningOnDayFunc) {
            this.isRunningOnDayFunc = isRunningOnDayFunc;
        }
        
        public Boolean isRunning(ServiceCalendar calendar) {
            return calendar != null && isRunningOnDayFunc.apply(calendar) == 1;
        }
    }
    
    private static Stream<Days> servicedaysStream(ServiceCalendar calendar) {
        return complementOf(of(Days.NONE)).stream().filter(day -> day.isRunning(calendar));
    }
    
    private static String servicedays(ServiceCalendar calendar) {
        return servicedaysStream(calendar).map(Days::toString).collect(joining(", "));
    }
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
