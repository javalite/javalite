<@content for="title">Add new book</@content>

<span class="error_message"><@flash name="message"/></span>
<h2>Adding new book</h2>


<@form action="create" method="post">
    <table style="margin:30px">
        <tr>
            <td>Book author</td>
            <td><input type="text" name="author" value="${(flasher.params.author)!}"> *
                            <span class="error">${(flasher.errors.author)!}</span>
            </td>
        </tr>
        <tr>
            <td>Title:</td>
            <td><input type="text" name="title" value="${(flasher.params.title)!}"> *
                            <span class="error">${(flasher.errors.title)!}</span>
            </td>
        </tr>
        <tr>
            <td>ISBN:</td>
            <td><input type="text" name="isbn" value="${(flasher.params.isbn)!}"> *
                <span class="error">${(flasher.errors.isbn)!}</span>
            </td>
        </tr>
        <tr>
            <td></td>
            <td><@link_to>Cancel</@link_to> | <input type="submit" value="Add new book"></td>

        </tr>
    </table>
</@form>



