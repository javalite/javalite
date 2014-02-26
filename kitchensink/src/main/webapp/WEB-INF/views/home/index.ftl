<@content for="title">TOC</@content>

<h2>Various examples</h2>


<ul>
    <li><@link_to controller="posts">Posts</@link_to></li>
    <li><@link_to controller="rposts">Restful Posts</@link_to></li>
    <li><@link_to controller="unobtrusive">Unobtrusive JS</@link_to></li>
    <li><@link_to controller="upload">Upload</@link_to></li>
    <li><@link_to controller="download">Download</@link_to></li>
    <li><@link_to action="wrapped">Layout that is itself wrapped</@link_to></li>
    <li><@link_to action="wrapped_too">Template wrapper with default layout</@link_to></li>
    <li><@link_to controller="injection">Dependency Injection</@link_to></li>
    <li><@link_to action="gwt">GWT demo</@link_to></li>
    <li><@link_to action="meaning_of_life">Message Tag Example</@link_to></li>
</ul>


<h2>Custom routing examples</h2>

<h3>Segments: static</h3>

Link: <a href="${context_path}/myposts">See posts</a>
<ul>
    <li>
        Configuration: <code>route("/myposts").to(PostsController.class);</code>
    </li>
    <li>
        Anchor code: <code>&lt;a href=&quot;${context_path}/myposts&quot;&gt;See posts&lt;/a&gt;</code>
    </li>
</ul>


<h3>Segments: static, built-in and user</h3>

Link: <a href="${context_path}/greeting/show/ActiveWeb">What is the name?</a>
<ul>
    <li>
        Configuration: <code>route("/greeting/{action}/{name}").to(HelloController.class);</code>
    </li>
    <li>
        Anchor code: <code>&lt;a href=&quot;${context_path}/greeting/show/ActiveWeb&quot;&gt;What is the name?&lt;/a&gt;</code>
    </li>
</ul>


<h3>Non-restful action of restful controller</h3>

Link: <a href="/rposts_internal/hello">Non-restful action of restful controller</a>
<ul>
    <li>
        Configuration: <code>route("/rposts_internal/{action}").to(RpostsController.class);</code>
    </li>
    <li>
        Anchor code: <code>&lt;a href=&quot;/rposts_internal/hello&quot;&gt;Non-restful action of restful controller&lt;/a&gt;</code>
    </li>
</ul>





