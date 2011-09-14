<@content for="title">All Posts</@content>


<@link_to controller="posts">All Posts</@link_to> | <@link_to controller="posts" action="new_post">New Post</@link_to>   
<span class="message"><@flash name="post_deleted"/></span>
<span class="message"><@flash name="post_saved"/></span>
<span class="message"><@flash name="message"/></span>

<p/>
Posts:

<#if posts?size == 0>
    no posts found
    <#else>
    <@render partial="post" collection=posts spacer="spacer"/>
</#if>


