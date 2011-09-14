<@content for="title">JavaLite - Unobtrusive</@content>
    


<h1>Simple links</h1>
<form id="form1" action="blah" method="get">
    First name: <input type="text" name="first_name"><br>
    Last name: <input type="text" name="last_name">    
</form>

<h2>Include form data:</h2>
<@link_to controller="unobtrusive" action="do-get" form="form1" destination="result1">Ajax Get</@>
<br>
<@link_to controller="unobtrusive" action="do-post" form="form1" destination="result1" method="post">Ajax Post</@>
<br>
<@link_to controller="unobtrusive" action="do-put" form="form1" destination="result1" method="put">Ajax Put</@>
<br>
<@link_to controller="unobtrusive" action="do-delete" form="form1" destination="result1" method="delete">Ajax Delete</@>

<div id="result1" style="margin:20px;font-size:large;background-color:#f5f5f5;">result will be here</div>

<h1>Links with JS callbacks</h1>

<@link_to controller="unobtrusive" action="do-get" form="form1"  before="doBeforeWithArg" before_arg="callbacks_result"
                                after="doAfterWithArg" after_arg="callbacks_result">Callbacks Get</@>
<br>
<@link_to controller="unobtrusive" action="do-post" form="form1"  method="post" before="doBefore" after="doAfter" >Callbacks Post</@>
<br>
<@link_to controller="unobtrusive" action="do-put" form="form1"  method="put" before="doBefore" after="doAfter">Callbacks Put</@>
<br>
<@link_to controller="unobtrusive" action="do-delete" form="form1"  method="delete" before="doBefore" after="doAfter" confirm="are you really sure???">Callbacks Delete</@> - uses confirmation 


<br>
<@link_to controller="unobtrusive" action="doesnotexist" error="onError" destination="callbacks_result">Will cause error</@>




<#--TODO: write onError handler!!!-->




<div id="callbacks_result" style="margin:20px;font-size:large;background-color:#f5f5f5;">Result  Here</div>

<script type="text/javascript">

    function onError(status, responseText){
        alert("Got error, status: " + status + ", Response: " + responseText);

    }
    
    function doBeforeWithArg(elm){
        $("#" + elm).html("wait...");
    }

    function doAfterWithArg(elm, data){
        $("#" + elm).html(data);
    }

    function doBefore(arg){
        $('#callbacks_result').html("wait...");
    }
    function doAfter(arg, data){
        $('#callbacks_result').html(data);
    }
</script>

