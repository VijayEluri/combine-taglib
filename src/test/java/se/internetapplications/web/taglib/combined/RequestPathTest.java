package se.internetapplications.web.taglib.combined;

import static org.junit.Assert.*;

import org.junit.Test;

public class RequestPathTest {

    @Test
    public void toString_should_return_path() {
        RequestPath path = new RequestPath("//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js");
        assertEquals("//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js", path.getPath());
    }

}
