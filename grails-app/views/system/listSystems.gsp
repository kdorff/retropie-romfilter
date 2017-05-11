<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of Systems</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
    <p>Available systems
    <ul>
        <g:each var="system" in="${systems}">
            <li>
                <g:link mapping="listRomsForSystem" params="[system: system]">
                    ${system}
                </g:link>
            </li>
        </g:each>
    </ul>
</body>
</html>
