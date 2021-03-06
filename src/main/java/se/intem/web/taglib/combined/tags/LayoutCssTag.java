package se.intem.web.taglib.combined.tags;

import com.google.common.base.Strings;

import java.util.List;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.intem.web.taglib.combined.RequestPath;
import se.intem.web.taglib.combined.ResourceType;
import se.intem.web.taglib.combined.configuration.ConfigurationItemsCollection;
import se.intem.web.taglib.combined.configuration.InlineContent;
import se.intem.web.taglib.combined.node.ConfigurationItem;

public class LayoutCssTag extends LayoutTagSupport {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(LayoutCssTag.class);

    private String media = null; // TODO support media

    public static final String CSS_PASS_COMPLETE = "combine_css_pass_complete";

    public List<RequestPath> getResources(final ConfigurationItem configuration) {
        return configuration.getCss();
    }

    @Override
    protected String format(final RequestPath path, final String elementId) {
        String mediaAttribute = "";
        if (!Strings.isNullOrEmpty(media)) {
            mediaAttribute = "media=\"" + media + "\" ";
        }

        String idAttribute = "";
        if (!Strings.isNullOrEmpty(elementId)) {
            idAttribute = "id=\"" + elementId + "\" ";
        }

        return String.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\" %s%s/>", path, mediaAttribute,
                idAttribute);
    }

    public void setMedia(final String media) {
        this.media = media;
    }

    public String getMedia() {
        return media;
    }

    @Override
    protected ResourceType getType() {
        return ResourceType.css;
    }

    @Override
    protected void outputInlineResources(final ConfigurationItemsCollection configurationItems) throws JspException {
        List<InlineContent> inlineStyles = configurationItems.getInlineStyles();

        if (inlineStyles.isEmpty()) {
            return;
        }

        addInline(inlineStyles);
    }

    public void addInline(final List<InlineContent> inlineStyles) throws JspException {
        for (InlineContent inline : inlineStyles) {

            writeConditionalStart(inline);

            String output = String.format("<style>%s</style>", inline.getContents());
            println(output);

            writeConditionalEnd(inline);
        }
    }

    @Override
    protected void outputInlineResourcesBefore(final ConfigurationItemsCollection cic) throws JspException {
        List<InlineContent> styles = cic.getInlineStyleEarlies();
        if (styles.isEmpty()) {
            return;
        }

        addInline(styles);
    }

    @Override
    protected boolean beforeResolve(final ConfigurationItemsCollection configurationItems) {
        Boolean complete = (Boolean) pageContext.getRequest().getAttribute(CSS_PASS_COMPLETE);

        if (Boolean.TRUE.equals(complete)) {
            log.warn("CSS has already been laid out for this request. Will not run again.");
            return false;
        }

        return true;
    }

    @Override
    protected void afterResolve() {
        pageContext.getRequest().setAttribute(CSS_PASS_COMPLETE, Boolean.TRUE);
    }

}
