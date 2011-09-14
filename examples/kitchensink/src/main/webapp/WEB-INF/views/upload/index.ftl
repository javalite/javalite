<@content for="title">Upload</@content>


<h2>Upload page</h2>
<div style="background-color:lightblue;margin:20px"><@flash name="file_accepted" /></div>


<#if (flasher.file_content)??>
    File Content:
    <div style="background-color:#ffffe0;margin:20px">
        <code>
            <pre>
            <@flash name="file_content" />
            </pre>
        </code>

    </div>
</#if>
<@form controller="upload" action="save" method="post" enctype="multipart/form-data">
    Select a file to upload:<input type="file" name="file">
    <button>Upload File</button>
</@>
