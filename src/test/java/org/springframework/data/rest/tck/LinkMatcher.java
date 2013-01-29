package org.springframework.data.rest.tck;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

/**
 * @author Jon Brisbin
 */
public class LinkMatcher extends BaseMatcher<Resource<?>> {

  private String href, rel;

  private LinkMatcher(String href, String rel) {
    this.href = href;
    this.rel = rel;
  }

  public static LinkMatcher hasLink(String href, String rel) {
    return new LinkMatcher(href, rel);
  }

  @Override public boolean matches(Object item) {
    if(!(item instanceof Link)) {
      return false;
    }
    Link l = (Link)item;

    String s1 = String.format("%s,%s", href, rel);
    String s2 = String.format("%s,%s", l.getHref(), l.getRel());

    return s1.equals(s2);
  }

  @Override public void describeTo(Description description) {
    description.appendText(String.format("a Link  with rel=%s, href=%s}", rel, href));
  }

}
