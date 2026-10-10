import com.sarf.qasioun.prayer.SolarThemeMath;
import java.time.*;

public class SolarThemeMathTest {
    private static void check(boolean ok,String name) {
        if(!ok)throw new AssertionError(name);
        System.out.println("PASS "+name);
    }
    public static void main(String[] args) {
        ZoneId tz=ZoneId.of("Asia/Riyadh");
        LocalDate date=LocalDate.of(2026,10,10);
        double latitude=21.5433,longitude=39.1728;
        long rise=SolarThemeMath.eventMillis(date,latitude,longitude,tz,true);
        long set=SolarThemeMath.eventMillis(date,latitude,longitude,tz,false);
        check(rise>0 && set>rise,"sunrise before sunset");
        ZonedDateTime riseLocal=Instant.ofEpochMilli(rise).atZone(tz);
        ZonedDateTime setLocal=Instant.ofEpochMilli(set).atZone(tz);
        check(riseLocal.getHour()==6,"Jeddah sunrise ~06:17");
        check(setLocal.getHour()==18,"Jeddah sunset ~18:04");
        check(!SolarThemeMath.daylight(rise-1,rise,set),"night before sunrise");
        check(SolarThemeMath.daylight(rise,rise,set),"light at sunrise");
        check(SolarThemeMath.daylight(set-1,rise,set),"light before sunset");
        check(!SolarThemeMath.daylight(set,rise,set),"night at sunset");
        check(!SolarThemeMath.daylight(set+1,rise,set),"night after sunset");
        check(SolarThemeMath.daylightNow(date.atTime(13,0).atZone(tz).toInstant().toEpochMilli(),latitude,longitude,tz),"offline day");
        check(!SolarThemeMath.daylightNow(date.atTime(20,0).atZone(tz).toInstant().toEpochMilli(),latitude,longitude,tz),"offline night");
        long atNoon=date.atTime(12,0).atZone(tz).toInstant().toEpochMilli();
        long upcoming=SolarThemeMath.nextBoundary(atNoon,latitude,longitude,tz);
        check(upcoming==set,"offline next boundary sunset");
        check(SolarThemeMath.eventMillis(date,Double.NaN,longitude,tz,true)==0L,"invalid coordinates rejected");
        System.out.println("JEDDAH 2026-10-10 sunrise="+riseLocal+" sunset="+setLocal);
    }
}
