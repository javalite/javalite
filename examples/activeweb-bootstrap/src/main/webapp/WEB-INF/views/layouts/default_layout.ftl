<#setting url_escaping_charset='ISO-8859-1'>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css" rel="stylesheet">
    <LINK href="${context_path}/css/main.css" rel="stylesheet" type="text/css"/>
    <script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
    <script src="//netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
    <script src="${context_path}/js/aw.js" type="text/javascript"></script>
    <title>ActiveWeb - <@yield to="title"/></title>
</head>
<body>

<#include "header.ftl" >

<div class="container">

${page_content}

</div><!-- /.container -->


</body>

</html>
