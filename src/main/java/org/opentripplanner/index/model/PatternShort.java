package org.opentripplanner.index.model;

import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.ServiceCalendar;
import org.opentripplanner.model.calendar.CalendarServiceData;
import org.opentripplanner.routing.edgetype.TripPattern;

import com.beust.jcommander.internal.Lists;

public class PatternShort {

    public String id;
    public String name;
    public String desc;
    
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
        StringBuilder descStr = new StringBuilder("Route: ").append(route);
        if (from != null) {
            descStr.append("\n").append("From: ").append(from);
        }
        if (to != null) {
            descStr.append("\n").append("To: ").append(to);
        }
        desc = descStr.toString();
        name = desc;
    }
    
    public PatternShort (TripPattern pattern, String serviceDays) {
        this(pattern);
        if(serviceDays != null && serviceDays.trim().length()>0) {
            name += "\nDays of operation: " + serviceDays;
            desc = name;
        }
    }
    
    public static List<PatternShort> list (Collection<TripPattern> in) {
        List<PatternShort> out = Lists.newArrayList();
        for (TripPattern pattern : in) out.add(new PatternShort(pattern));
        return out;
    }    
    
    public static List<PatternShort> list (Collection<TripPattern> patterns, Map<FeedScopedId, Integer> services, CalendarServiceData csd) {
        Map<Integer, String> serviceCalendars = services.entrySet().stream().collect(toMap(e -> e.getValue(), e -> servicedays(csd.getServiceCalendar(e.getKey()))));
        return patterns.stream().map(pattern -> pattern.getServices().stream().mapToObj(i->new PatternShort(pattern,serviceCalendars.get(i)))).flatMap(s->s).collect(toList());
    }
    
    public enum Days {
        Mon(ServiceCalendar::getMonday),
        Tue(ServiceCalendar::getTuesday),
        Wed(ServiceCalendar::getWednesday),
        Thu(ServiceCalendar::getThursday),
        Fri(ServiceCalendar::getFriday),
        Sat(ServiceCalendar::getSaturday),
        Sun(ServiceCalendar::getSunday);
        
        private Function<ServiceCalendar, Integer> isRunningOnDayFunc;
        
        private Days(Function<ServiceCalendar, Integer> isRunningOnDayFunc) {
            this.isRunningOnDayFunc = isRunningOnDayFunc;
        }
        
        public Boolean isRunning(ServiceCalendar calendar) {
            return calendar != null && isRunningOnDayFunc.apply(calendar) == 1;
        }
    }
    
    private static String servicedays(ServiceCalendar calendar) {
        return allOf(Days.class).stream().filter(day -> day.isRunning(calendar)).map(Days::toString).collect(joining(", "));
//        Integer runningDaysInWeek = allOf(Days.class).stream().map(day -> day.isRunning(calendar) ? 1 : 0).collect(reducing(0, (t,u) -> t+u));
//        Map<Days, Boolean> runningDaysMap = allOf(Days.class).stream().collect(toMap(day -> day, day -> day.isRunning(calendar)));
//        String daysStr = allOf(Days.class).stream().filter(runningDaysMap::get).map(Days::toString).collect(joining(", "));
//        // Daily
//        if(runningDaysInWeek == 7) {
//            return " "+daysStr;
//        }
//        //On weekends or weekdays
//        if((runningDaysInWeek == 2 && of(Sat, Sun).stream().map(runningDaysMap::get).reduce(true, (a,b)->a&&b)) 
//                || (runningDaysInWeek == 5 && complementOf(of(Sat, Sun)).stream().map(runningDaysMap::get).reduce(true, (a,b)->a&&b))) {
//            return " on " + daysStr;
//        }
//        return runningDaysInWeek > 0 ? (" every " + daysStr) : "";
    }
}
