package se.intem.web.taglib.combined.node;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ConfigurationItemTest {

    @Test
    public void should_not_add_requires_for_empty_string() {

        ConfigurationItem ci = new ConfigurationItem();
        assertEquals(0, ci.getRequiresList().size());
        ci.addRequires("");
        assertEquals(0, ci.getRequiresList().size());
    }

    @Test
    public void should_split_strings_even_when_setting_collection() {
        ConfigurationItem ci = new ConfigurationItem();

        ci.setRequires(Arrays.asList("angular, jquery-ui"));
        assertEquals(2, ci.getRequiresList().size());
    }

    @Test
    public void should_split_on_whitespace_and_comma() {
        ConfigurationItem ci = new ConfigurationItem();

        ci.setRequires(Arrays.asList("angular, jquery-ui extjs angular-ui"));
        assertEquals(4, ci.getRequiresList().size());
    }

    @Test
    public void should_remove_duplicate_requires_and_keep_insertion_order() {
        ConfigurationItem ci = new ConfigurationItem();
        ci.addRequires("jquery, angular-ui,jquery");

        assertEquals("Should remove duplicates", 2, ci.getRequiresList().size());
        assertThat("Should keep insertion order", ci.getRequiresList(), is(Arrays.asList("jquery", "angular-ui")));
    }

    @Test
    public void configuration_item_without_resources_is_empty() {
        ConfigurationItem ci = new ConfigurationItem();
        ci.addRequires("requires does not count as empty");
        assertTrue(ci.isEmpty());
    }

}
