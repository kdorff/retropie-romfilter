<%@ page import="retropie.romfilter.indexed.Game" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Systems and Jobs</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="jobs/index.js" asset-defer="true" />
    <asset:stylesheet src="jobs/index.css"/>
    <g:javascript>
       var jobsAndRomsData = '<g:createLink action="jobsAndRomsData"/>';
       var rescanAllUrl = '<g:createLink action="rescanAll"/>';
       var systemsAndCountsDiv;
       var jobsDiv;
    </g:javascript>
</head>
<body>
    <p>Navigation:
        <g:link controller="games" action="browse">Browse ROMs</g:link> | <g:link controller="jobs" action="index">Systems and Jobs</g:link>
    </p>
    <p>&nbsp;</p>
    <strong>ROM count per system</strong>
    <div id="systemsAndCounts">
    </div>
    <p>&nbsp;</p>
    <button id='rescanAll' type="button">Rescan all systems</button>
    <p>&nbsp;</p>
    <p><strong>Running and recent jobs</strong></p>
    <div id="jobs">
    </div>
</body>
</html>
