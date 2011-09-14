<div class="post">
    Title: <@link_to controller="posts" action="view" id="${post.id}">${post.title}</@link_to> |
    Author: ${post.author} |
    Created: ${post.created_at}  |
    <@link_to controller="posts" action="edit_post" id=post.id>Edit</@link_to> |
    <@confirm text="Are you sure you want to delete post: \\\'${post.title}\\\'?" form=post.id>Delete</@confirm>

    <div class="post_content">${post.content}</div>
</div>
<@form controller="posts"  id=post.id action="delete" method="delete" html_id=post.id>
    <input type="hidden" name="id" value="${post.id}">
</@form>


    