<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>List of ROMS for ${system}</title>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico" />
    <asset:javascript src="listRomsForSystem.js" asset-defer="true" />
    <asset:stylesheet src="listRomsForSystem.css"/>
</head>
<body>
    <p>Navigation:
        <g:link mapping="listSystems">Systems</g:link> |
        <g:link mapping="listRomsForSystem" params="[system: system]">${system}</g:link>

    <table id="romTable">
        <thead>
            <tr>
                <th>Scrape Name<br/>Filename</th>
                <th>Name</th>
                <th>Genre</th>
                <th>Desc</th>
                <th>Image</th>
            </tr>
        </thead>
        <tbody>
            <g:each var="rom" in="${roms}">
                <tr>
                    <% def gamelistEntry = rom.gamelistEntry %>
                    <td>
                        ${rom.filename}
                        <p>
                            <input type="button" value="Delete / Move Trash" class="deleteRom"
                                  data-delete-url='<g:createLink mapping="deleteRomForSystem" params="[system: system, id: rom.id]"/>' />
                        </p>
                    </td>
                    <td>
                        <g:if test="${gamelistEntry}">
                            <g:link mapping="romForSystem" params="[system: gamelistEntry.system, id: gamelistEntry.id]">
                                ${gamelistEntry?.name}
                            </g:link>
                        </g:if>
                    </td>
                    <td>${gamelistEntry?.genre}</td>
                    <td>${gamelistEntry?.desc}</td>
                    <td>
                        <g:if test="${gamelistEntry && gamelistEntry.image && gamelistEntry.system && gamelistEntry.id}">
                            <img width='200px' src='<g:createLink mapping="showRomImageForSystem" params="[system: gamelistEntry.system, id: gamelistEntry.id]"/>' />
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</body>
</html>
