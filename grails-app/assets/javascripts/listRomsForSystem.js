$(document).ready(function() {
    $('#romTable').DataTable({
        aLengthMenu: [
            [-1],
            ["All"]
        ],
        iDisplayLength: -1,
        aoColumnDefs: [
            {
                // Custom rendering for the first column
                // which is filename over scrape name
                "mRender": function ( data, type, row ) {
                    if (row[1]) {
                        return row[1] + '<br/>' + data;
                    }
                    else {
                        return data;
                }
                },
                "aTargets": [ 0 ]
            },
            { "bVisible": false,  "aTargets": [ 1 ] }
        ],
         "columns": [
            { "width": "55%" },
            { "width": "0%" },
            { "width": "5%" },
            { "width": "45%" }
        ],
        "paging":   false
    });
});
