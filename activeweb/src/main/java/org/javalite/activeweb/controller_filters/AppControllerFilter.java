package org.javalite.activeweb.controller_filters;

/**
 * Controller filters are similar to that of Servlet filters, but designed to wrap execution of controllers.
 * They can be used for many tasks that need to trigger before and after execution of a controller, such as login in, logging,
 * opening a DB connection, timing, etc.
 *
 *  <p/><p/>
 * Instances of filters are <font color="red"><em>not thread safe</em></font>.
 * The same object will be reused across many threads at the same time. Create instance variables at your own peril.
 *
 * <p/><p/>
 * Many use cases for  controller filters:
 * <ul>
 *     <li>catch all exceptions and respond with a canned error (page)</li>
 *     <li>check if uses logged in, and redirect to a login page is not </li>
 * </ul>
 * , another is an authentication controller
 * that will redirect to a login page if user not logged in.
 *
 * @author igor on 6/26/17.
 */
public class AppControllerFilter extends HttpSupportFilter {
}
