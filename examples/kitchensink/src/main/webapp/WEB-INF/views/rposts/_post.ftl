<div class="post">

    Title: <@link_to controller="rposts"  id=post.id>${post.title}</@link_to> |
    Author: ${post.author}
    | Created: ${post.created_at}

    <div class="post_content">${post.content}</div>
</div>


<@form controller="rposts" id=post.id  method="post">
    
    <input type="hidden" name="_method" value="DELETE">
    <input type="submit" value="Delete">
</@form>


