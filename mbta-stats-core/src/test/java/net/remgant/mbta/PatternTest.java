package net.remgant.mbta;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jdr on 1/28/18.
 */
public class PatternTest {
    @Test
    public void test1() {
       String s = "CR-Saturday-Fall-17-1752";
        Pattern p = Pattern.compile("CR-(Sunday|Saturday|Weekday)-((?:Fall|Spring)-\\d\\d)-(\\d+)");
        Matcher m = p.matcher(s);
        assertTrue(m.matches());
        assertEquals("Saturday",m.group(1));
        assertEquals("Fall-17",m.group(2));
        assertEquals("1752",m.group(3));
    }
}
