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
            <g:each var="filename" in="${gamelist}">
                <tr>
                    <% def gameDetails = filenameToDetails[filename] %>
                    <td>
                        ${filename}
                        <p>
                            <input type="button" value="Delete / Move Trash" class="deleteRom"
                                  data-delete-url='<g:createLink mapping="deleteRomForSystem" params="[system: system, hash: romfilterDataService.hash(filename)]"/>' />
                        </p>
                    </td>
                    <td>
                        <g:if test="${gameDetails}">
                            <g:link mapping="romForSystem" params="[system: gameDetails.system, id: gameDetails.id]">
                                ${gameDetails?.name}
                            </g:link>
                        </g:if>
                    </td>
                    <td>${gameDetails?.genre}</td>
                    <td>${gameDetails?.desc}</td>
                    <td>
                        <g:if test="${gameDetails && gameDetails.image && gameDetails.system && gameDetails.id}">
                            <img width='200px' src='<g:createLink mapping="showRomImageForSystem" params="[system: gameDetails.system, id: gameDetails.id]"/>' />
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</body>
</html>
