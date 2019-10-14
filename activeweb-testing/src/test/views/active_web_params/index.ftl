<#list activeweb?keys as key>
${key} = ${activeweb[key]?string}
</#list>
Context path: ${context_path}
AppContext: ${app_context}
AppContext Value: ${app_context.name}

