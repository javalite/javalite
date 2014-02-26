<@content for="title">Create New Post</@content>

<h2>Create New Post:</h2>



<span class="message"><@flash name="values_missing"/></span>
<@form controller="rposts" method="post">
<table>
    <tr>
        <td>Author:</td>
        <td>
            <input type="text" name="author" size="40" value="${(flasher.params.author)!""}"/>
            <span class="message">${(flasher.errors.author)!""}</span>
        </td>
    </tr>
    <tr>
        <td>Title:</td>
        <td>
            <input type="text" name="title" size="40" value="${(flasher.params.title)!""}"/>
            <span class="message">${(flasher.errors.title)!""}</span>
        </td>
    </tr>
    <tr>
        <td>Content:</td>
        <td>
            <textarea type="text" name="content"cols="50">${(flasher.params.content)!""}</textarea>
            <span class="message">${(flasher.errors.content)!""}</span>
        </td>
    </tr>
    <tr>
        <td></td>
        <td>

            <img class="captcha" src="${context_path}/captcha" id="captcha_img">
            <br/>
            <a href="#" onclick="$('#captcha_img').attr('src', '${context_path}/captcha?t=' + new Date().getTime());">Reset captcha</a>

            <br/>Verify that you are human, enter text above: <br/>
            <input type="text" name="captcha">
            <span class="message"><@flash name="bad_captcha"/></span>
        </td>

    </tr>
    <tr>
        <td></td>
        <td><input type="submit" value="Create"></td>
    </tr>
</table>



</@form>

