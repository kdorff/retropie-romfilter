<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of Systems</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
    <p>Available systems
    <table id="systemTable">
        <thead>
        <tr>
            <th>System name</th>
        </tr>
        </thead>
        <tbody>
        <g:each var="system" in="${systems}">
            <tr>
                <td>
                    <g:link mapping="listRomsForSystem" params="[system: system]">
                        ${system}
                    </g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</body>
</html>
