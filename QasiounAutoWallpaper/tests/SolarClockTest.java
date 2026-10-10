import java.time.*;
import com.sarf.qasioun.wallpaper.SolarClock;
public class SolarClockTest {
  static void check(boolean cond,String label){
    if(!cond)throw new AssertionError(label);
    System.out.println("PASS "+label);
  }
  public static void main(String[] args){
    ZoneId zone=ZoneId.of("Asia/Riyadh");
    LocalDate day=LocalDate.of(2026,10,10);
    double lat=21.5433,lon=39.1728;
    SolarClock.Events e=SolarClock.forDate(day,lat,lon,zone);
    check(e.sunrise.toLocalDate().equals(day),"sunrise local date");
    check(e.sunset.toLocalDate().equals(day),"sunset local date");
    check(e.sunrise.getHour()>=5 && e.sunrise.getHour()<=7,"Jeddah sunrise approx");
    check(e.sunset.getHour()>=17 && e.sunset.getHour()<=19,"Jeddah sunset approx");
    check(!SolarClock.isNight(day.atTime(12,0).atZone(zone),lat,lon),"midday light");
    check(SolarClock.isNight(day.atTime(23,0).atZone(zone),lat,lon),"night dark");
    check(SolarClock.isNight(e.sunrise.minusMinutes(1),lat,lon),"minute before sunrise");
    check(!SolarClock.isNight(e.sunrise.plusMinutes(1),lat,lon),"minute after sunrise");
    check(!SolarClock.isNight(e.sunset.minusMinutes(1),lat,lon),"minute before sunset");
    check(SolarClock.isNight(e.sunset.plusMinutes(1),lat,lon),"minute after sunset");
    check(!SolarClock.isNight(day.atTime(16,0).atZone(zone),lat,lon),"afternoon light");
    System.out.println("SUN TIMES Jeddah "+e.sunrise+" / "+e.sunset);
  }
}
