<@content for="title">Edit Post</@content>

<h2>Edit Post:</h2>

<@form controller="posts" action="save" method="post">
<input type="hidden" name="id" value="${(post.id)!""}" />
<table>
    <tr>
        <td>Author:</td>
        <td>
            <input type="text" name="author" size="40" value="${(post.author)!""}"/>
            <span class="message">${(errors.author)!""}</span>
        </td>
    </tr>
    <tr>
        <td>Title:</td>
        <td>
            <input type="text" name="title" size="40" value="${(post.title)!""}"/>
            <span class="message">${(errors.title)!""}</span>
        </td>
    </tr>
    <tr>
        <td>Content:</td>
        <td>
            <textarea type="text" name="content" cols="50">${(post.content)!""}</textarea>
            <span class="message">${(errors.content)!""}</span>
        </td>
    </tr>

    <tr>
        <td></td>
        <td><input type="submit" value="Save" /></td>
    </tr>
</table>
</@form>

