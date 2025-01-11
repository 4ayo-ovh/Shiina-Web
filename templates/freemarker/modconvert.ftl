<span class="fw-bold"><#if passedMods?size == 0>-<#else><#list passedMods as mod><#if mod?index == 0>+ ${mod}<#else>, ${mod}</#if></#list></#if></span>
