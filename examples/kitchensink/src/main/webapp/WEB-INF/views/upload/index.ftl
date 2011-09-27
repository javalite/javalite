<@content for="title">Upload</@content>


<h2>Upload page</h2>

<#if (flasher.file_name)??>
File: "<@flash name="file_name"/>":
<div style="background-color:#ffffe0;margin:20px">
    <code>
            <pre>
            <@flash name="file_content" />
            </pre>
    </code>
</div>
</#if>

<#if (flasher.field_name)??>
Field: "<@flash name="field_name"/>":
<div style="background-color:#ffffe0;margin:20px">
    <code>
            <pre>
            <@flash name="field_content" />
            </pre>
    </code>
</div>
</#if>
<@form controller="upload" action="save" method="post" enctype="multipart/form-data">
<table>
    <tr>
        <td>Enter some value:</td>
        <td><input name="a_field" type="text"></td>
    </tr>
    <tr>
        <td>Select a file to upload:</td>
        <td><input type="file" name="a_file"></td>
    </tr>
    <tr>
        <td><button>Submit</button></td>
        <td></td>
    </tr>

</table>
</@>


