<#list students as student>
<#if student.enrollment_date??>
Student: ${student.first_name}  ${student.last_name}, enrollment date: ${student.enrollment_date}
<#else>
Student: ${student.first_name}  ${student.last_name}, enrollment date:
</#if>
</#list>