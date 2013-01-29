package org.springframework.data.rest.tck;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

/**
 * @author Jon Brisbin
 */
public abstract class ResourceMatchers {

  private ResourceMatchers() {
  }

  public static <T> Matcher<Resources<? super T>> contains(Matcher<T> matcher) {
    return new ResourcesMatcher<>(matcher);
  }

  public static <T> Matcher<Resource<? super T>> hasContent(Matcher<T> matcher) {
    return new ResourceMatcher<>(matcher);
  }

  static class ResourceMatcher<T> extends BaseMatcher<Resource<? super T>> {
    private final Matcher<T> matcher;

    ResourceMatcher(Matcher<T> matcher) {
      this.matcher = matcher;
    }

    @SuppressWarnings({"unchecked"})
    @Override public boolean matches(Object item) {
      return item instanceof Resource && matcher.matches(((Resource<T>)item).getContent());
    }

    @Override public void describeTo(Description description) {
      description.appendText("a Resource that has content ")
                 .appendDescriptionOf(matcher);
    }
  }

  static class ResourcesMatcher<T> extends BaseMatcher<Resources<? super T>> {

    private final Matcher<T> matcher;

    ResourcesMatcher(Matcher<T> matcher) {
      this.matcher = matcher;
    }

    @SuppressWarnings({"unchecked"})
    @Override public boolean matches(Object item) {
      if(!(item instanceof Resources)) {
        return false;
      }

      try {
        for(Object o : ((Resources<T>)item).getContent()) {
          if(!matcher.matches(o)) {
            return false;
          }
        }
      } catch(ClassCastException e) {
        return false;
      }

      return true;
    }

    @Override public void describeTo(Description description) {
      description.appendText("a Resources<T> that matches ")
                 .appendDescriptionOf(matcher);
    }
  }

}
