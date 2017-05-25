<%@ page import="retropie.romfilter.indexed.Game" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Browse ROMs</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="jobs/index.js" asset-defer="true" />
    <asset:stylesheet src="jobs/index.css"/>
    <g:javascript>
       var runningJobsDataUrl = '<g:createLink action="runningJobsData"/>';
       var rescanAllUrl = '<g:createLink action="rescanAll"/>';
       var systemToRomCountDataUrl = '<g:createLink action="systemToRomCountData"/>';
       var runningJobsDiv;
       var systemsAndCountsDiv;
    </g:javascript>
</head>
<body>
    <p>Navigation:
        <g:link controller="games" action="browse">Browse ROMs</g:link> | <g:link controller="jobs" action="index">Systems and Scan Jobs</g:link>
    </p>
    <p>&nbsp;</p>
    <strong>ROM count per system</strong>
    <div id="systemsAndCounts">
    </div>
    <p>&nbsp;</p>
    <button id='rescanAll' type="button">Rescan All Systems</button>
    <p>&nbsp;</p>
    <p><strong>Running jobs</strong></p>
    <div id="runningJobs">
    </div>
</body>
</html>
