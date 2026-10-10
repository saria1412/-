import com.sarf.qasioun.prayer.WeatherCodes;

public final class WeatherCodesTest {
    private static void ok(boolean truth, String description) {
        if (!truth) throw new IllegalStateException(description);
    }
    public static void main(String[] args) {
        ok("صحو".equals(WeatherCodes.label(0)), "clear");
        ok("غائم جزئيًا".equals(WeatherCodes.label(2)), "partly cloudy");
        ok("ضباب".equals(WeatherCodes.label(45)), "fog");
        ok("أمطار".equals(WeatherCodes.label(63)), "rain");
        ok("ثلوج".equals(WeatherCodes.label(71)), "snow");
        ok("عواصف رعدية".equals(WeatherCodes.label(95)), "thunderstorm");
        ok("☾".equals(WeatherCodes.glyph(0, false)), "clear night");
        ok("☀".equals(WeatherCodes.glyph(0, true)), "clear day");
        ok("حالة الطقس".equals(WeatherCodes.label(-1)), "unknown");
        System.out.println("PASS 9 WMO weather code and day/night tests");
    }
}
