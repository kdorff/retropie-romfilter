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


function updateJobsAndRomsData() {
    $.ajax({
        url: jobsAndRomsData,
        success: function (result) {
            updateSystemsAndCounts(result.systemToCount, result.totalCount, systemsAndCountsDiv);
            updateJobsDiv(result.runningJobs, runningJobsDiv);
            updateJobsDiv(result.recentJobs, recentJobsDiv);
        },
        error: function (result) {
            console.log('Error running updateJobsAndRomsData. Result is ...');
            console.log(result);

        }
    });
}

function updateSystemsAndCounts(systemToCount, totalCount, div) {
    var output = "";
    var id;
    var message;
    for (id in systemToCount) {
        message = systemToCount[id];
        if (output) {
            output += "<br/>";
        }
        output += message;
    }
    if (output) {
        output += '<br/>';
    }
    output += "<strong>Total ROM count: </strong>";
    output += totalCount;

    div.html(output);
}

function updateJobsDiv(result, div) {
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
        div.html(output);
    }
    else {
        div.html("No jobs.");
    }
}

$(document).ready(function() {
    runningJobsDiv = $("div#runningJobs");
    runningJobsDiv.html('Waiting for update...');

    recentJobsDiv = $("div#recentJobs");
    recentJobsDiv.html('Waiting for update...');

    systemsAndCountsDiv = $("div#systemsAndCounts");
    systemsAndCountsDiv.html('Waiting for update...');

    setInterval("updateJobsAndRomsData()", 2 * 1000);
});
