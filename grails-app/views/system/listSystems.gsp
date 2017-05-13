<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of Systems</title>

    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
</head>
<body>
    <p>Navigation:
        <g:link mapping="listSystems">Systems</g:link>

    <table id="systemTable">
        <thead>
        <tr>
            <th>System name</th>
        </tr>
        </thead>
        <tbody>
            <g:each var="system" in="${systems}">
                <g:if test="${system.romCount == null || system.romCount > 0}">
                    <tr>
                        <td>
                            <g:link mapping="listRomsForSystem" params="[system: system.name]">
                                ${system.name}
                                ~${system.romCount} ROMs
                            </g:link>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </tbody>
    </table>
</body>
</html>
