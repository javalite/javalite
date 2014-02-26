<@content for="title">Restful Posts</@content>


<@link_to controller="posts">All Posts</@link_to> | <@link_to controller="rposts" action="new_form">New Post</@link_to>   
<span class="message"><@flash name="post_deleted"/></span>
<span class="message"><@flash name="post_saved"/></span>

<p/>
Posts:

<#if posts?size == 0>
    no posts found
    <#else>
    <@render partial="post" collection=posts spacer="spacer"/>
</#if>


