$(document).on('click', 'button#rescanAll', function (e) {
    e.preventDefault();
    var button = $(this);
    $.ajax({
        url: rescanAllUrl,
        success: function (result) {
            console.log(result);
        },
        error: function (result) {
            console.log(result);
            alert('Error submitting Scan All Systems job.');
        }
    });
});

function updateRunningJobs() {
    $.ajax({
        url: runningJobsDataUrl,
        success: function (result) {
            var output = "";
            var id;
            var message;
            for (id in result) {
                message = result[id];
                if (output) {
                    output += "<br/>";
                }
                output += message;
            }
            if (output) {
                console.log(output);
                runningJobsDiv.html(output);
            }
            else {
                runningJobsDiv.html("No running jobs.");
            }
        },
        error: function (result) {
            console.log(result);
            console.log('Error running updateRunningJobs.');

        }
    });
}

function updateSystems() {
    $.ajax({
        url: systemToRomCountDataUrl,
        success: function (result) {
            var output = "";
            var id;
            var message;
            for (id in result.systemToCount) {
                message = result.systemToCount[id];
                if (output) {
                    output += "<br/>";
                }
                output += message;
            }
            if (output) {
                output += '<br/>';
            }
            output += "<strong>Total ROM count: </strong>";
            output += result.totalCount;

            systemsAndCountsDiv.html(output);
        },
        error: function (result) {
            console.log('Error running updateSystems. Result is ...');
            console.log(result);

        }
    });
}

$(document).ready(function() {
    runningJobsDiv = $("div#runningJobs");
    runningJobsDiv.html('Waiting for update...');

    systemsAndCountsDiv = $("div#systemsAndCounts");
    systemsAndCountsDiv.html('Waiting for update...');

    setInterval("updateRunningJobs()", 2 * 1000);
    setInterval("updateSystems()", 2 * 1000);
});
